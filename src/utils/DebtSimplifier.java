package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtSimplifier {

    public static Map<String, Map<String, Double>> simplifyDebt(Map<String, Map<String, Double>> deptMap) {
        Map<String, Map<String, Double>> simplifiedDebtMap = new HashMap<>();
        Map<String, Double> netBalanceMap = new HashMap<>();

        // 1. Calculate net balance for every involved user
        // netBalance = (Amount others owe me) - (Amount I owe others)
        // If netBalance > 0, they are a Creditor.
        // If netBalance < 0, they are a Debtor.
        for (Map.Entry<String, Map<String, Double>> entry : deptMap.entrySet()) {
            String debtor = entry.getKey();
            for (Map.Entry<String, Double> innerEntry : entry.getValue().entrySet()) {
                String creditor = innerEntry.getKey();
                double amount = innerEntry.getValue();

                netBalanceMap.put(debtor, netBalanceMap.getOrDefault(debtor, 0.0) - amount);
                netBalanceMap.put(creditor, netBalanceMap.getOrDefault(creditor, 0.0) + amount);
            }
        }

        // 2. Separate into Creditors (positive balance) and Debtors (negative balance)
        // We use absolute values in lists to make math easier
        List<BalanceNode> creditors = new ArrayList<>();
        List<BalanceNode> debtors = new ArrayList<>();

        for (String user : netBalanceMap.keySet()) {
            double bal = netBalanceMap.get(user);
            if (bal > 0.001) {
                creditors.add(new BalanceNode(user, bal));
            } else if (bal < -0.001) {
                debtors.add(new BalanceNode(user, Math.abs(bal)));
            }
        }

        // 3. Greedy algorithm to settle debts
        int i = 0, j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            BalanceNode creditorNode = creditors.get(i);
            BalanceNode debtorNode = debtors.get(j);

            double settleAmount = Math.min(creditorNode.amount, debtorNode.amount);

            simplifiedDebtMap.computeIfAbsent(debtorNode.userId, k -> new HashMap<>()).put(creditorNode.userId,
                    settleAmount);

            creditorNode.amount -= settleAmount;
            debtorNode.amount -= settleAmount;

            if (creditorNode.amount < 0.001)
                i++;
            if (debtorNode.amount < 0.001)
                j++;
        }

        return simplifiedDebtMap;
    }

    private static class BalanceNode {
        String userId;
        double amount;

        BalanceNode(String u, double a) {
            userId = u;
            amount = a;
        }
    }

}
