package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtSimplifier {

    public static Map<String, Map<String, Double>> simplifyDebt(Map<String, Map<String, Double>> deptMap) {
        Map<String, Map<String, Double>> simplifiedDebtMap = new HashMap<>();
        Map<String, Double> netDebtMap = new HashMap<>();

        // 1. Calculate net balance for every involved user
        for (Map.Entry<String, Map<String, Double>> entry : deptMap.entrySet()) {
            String user = entry.getKey();
            netDebtMap.putIfAbsent(user, 0.0);

            for (Map.Entry<String, Double> innerEntry : entry.getValue().entrySet()) {
                String otherUser = innerEntry.getKey();
                double amount = innerEntry.getValue();

                netDebtMap.put(user, netDebtMap.get(user) + amount);
                netDebtMap.putIfAbsent(otherUser, 0.0);
                netDebtMap.put(otherUser, netDebtMap.get(otherUser) - amount);
            }
        }

        // 2. Separate into Creditors (positive) and Debtors (negative)
        List<String> creditors = new ArrayList<>();
        List<String> debtors = new ArrayList<>();

        for (String user : netDebtMap.keySet()) {
            double balance = netDebtMap.get(user);
            if (balance > 0.001) {
                creditors.add(user);
            } else if (balance < -0.001) {
                debtors.add(user);
            }
        }

        // 3. Greedy algorithm to settle debts
        int i = 0, j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            String creditor = creditors.get(i);
            String debtor = debtors.get(j);

            double creditAmount = netDebtMap.get(creditor);
            double debitAmount = Math.abs(netDebtMap.get(debtor));
            double settleAmount = Math.min(creditAmount, debitAmount);

            if (settleAmount > 0.001) {
                simplifiedDebtMap.computeIfAbsent(debtor, k -> new HashMap<>()).put(creditor, settleAmount);
            }

            netDebtMap.put(creditor, creditAmount - settleAmount);
            netDebtMap.put(debtor, netDebtMap.get(debtor) + settleAmount);

            if (Math.abs(netDebtMap.get(creditor)) < 0.001)
                i++;
            if (Math.abs(netDebtMap.get(debtor)) < 0.001)
                j++;
        }

        return simplifiedDebtMap;
    }

}
