package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtSimplifier {

    public static Map<String, Map<String, Double>> simplifyDebt(Map<String, Map<String, Double>> deptMap) {
        Map<String, Map<String, Double>> simplifiedDebtMap = new HashMap<>();

        Map<String, Double> netDebtMap = new HashMap<>();

        for (String user : deptMap.keySet()) {
            netDebtMap.put(user, 0.0);
        }

        for (String user : deptMap.keySet()) {
            for (String otherUser : deptMap.get(user).keySet()) {

                double amount = deptMap.get(user).get(otherUser);

                netDebtMap.put(user, netDebtMap.get(user) + amount);
                netDebtMap.put(otherUser, netDebtMap.get(otherUser) - amount);
            }
        }

        List<String> creditors = new ArrayList<>();
        List<String> debtors = new ArrayList<>();

        for (String user : netDebtMap.keySet()) {
            if (netDebtMap.get(user) > 0) {
                creditors.add(user);
            } else if (netDebtMap.get(user) < 0) {
                debtors.add(user);
            }
        }

        int i = 0;
        int j = 0;

        while (i < creditors.size() && j < debtors.size()) {

            String creditor = creditors.get(i);
            String debtor = debtors.get(j);

            double amount = Math.min(Math.abs(netDebtMap.get(creditor)), Math.abs(netDebtMap.get(debtor)));

            simplifiedDebtMap.computeIfAbsent(debtor, k -> new HashMap<>()).put(creditor, amount);

            netDebtMap.put(creditor, netDebtMap.get(creditor) - amount);
            netDebtMap.put(debtor, netDebtMap.get(debtor) + amount);

            if (netDebtMap.get(creditor) == 0.0)
                i++;
            if (netDebtMap.get(debtor) == 0.0)
                j++;

        }

        return simplifiedDebtMap;
    }

}
