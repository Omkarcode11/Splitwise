package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import enums.SplitType;
import factory.SplitFactory;
import utils.DebtSimplifier;

public class Group {
    private String id;
    private String name;
    private List<User> users;
    private Map<String, Expense> expenses;
    private Map<String, Map<String, Double>> balanceSheet;

    public Group(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.users = new ArrayList<>();
        this.expenses = new HashMap<>();
        this.balanceSheet = new HashMap<>();
    }

    private User getUserByUserId(String id) {
        for (User user : this.users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    public void addUser(User user) {
        this.users.add(user);
        this.balanceSheet.put(user.getId(), new HashMap<>());
    }

    public boolean removeUser(String userId) {

        if (!canLeaveGroup(userId)) {
            return false;
        }

        this.users.remove(getUserByUserId(userId));
        this.balanceSheet.remove(userId);

        for (Map<String, Double> balanceSheet : this.balanceSheet.values()) {
            balanceSheet.remove(userId);
        }

        return true;
    }

    public boolean canLeaveGroup(String userId) {
        // Check if this user owes anything
        Map<String, Double> owesothers = this.balanceSheet.getOrDefault(userId, new HashMap<>());
        for (double amount : owesothers.values()) {
            if (Math.abs(amount) > 0.001)
                return false;
        }

        // Check if anyone owes this user
        for (Map<String, Double> othersBalances : this.balanceSheet.values()) {
            if (Math.abs(othersBalances.getOrDefault(userId, 0.0)) > 0.001)
                return false;
        }

        return true;
    }

    public void notifyUsers(String message) {
        for (User user : this.users) {
            user.update(message);
        }
    }

    public boolean isMember(String userId) {
        return this.users.stream().anyMatch(user -> user.getId().equals(userId));
    }

    public void updateGroupBalance(String debtorId, String creditorId, Double amount) {
        if (debtorId.equals(creditorId))
            return;

        // Ensure both maps exist
        this.balanceSheet.computeIfAbsent(debtorId, k -> new HashMap<>());
        this.balanceSheet.computeIfAbsent(creditorId, k -> new HashMap<>());

        // If creditorId already owes debtorId, reduce that debt first
        double creditorOwesDebtor = this.balanceSheet.get(creditorId).getOrDefault(debtorId, 0.0);

        if (creditorOwesDebtor > 0.001) {
            if (creditorOwesDebtor >= amount) {
                this.balanceSheet.get(creditorId).put(debtorId, creditorOwesDebtor - amount);
            } else {
                this.balanceSheet.get(creditorId).put(debtorId, 0.0);
                double remaining = amount - creditorOwesDebtor;
                this.balanceSheet.get(debtorId).put(creditorId,
                        this.balanceSheet.get(debtorId).getOrDefault(creditorId, 0.0) + remaining);
            }
        } else {
            this.balanceSheet.get(debtorId).put(creditorId,
                    this.balanceSheet.get(debtorId).getOrDefault(creditorId, 0.0) + amount);
        }

        // Clean up zero balances
        if (this.balanceSheet.get(debtorId).getOrDefault(creditorId, 0.0) < 0.001) {
            this.balanceSheet.get(debtorId).remove(creditorId);
        }
        if (this.balanceSheet.get(creditorId).getOrDefault(debtorId, 0.0) < 0.001) {
            this.balanceSheet.get(creditorId).remove(debtorId);
        }
    }

    public boolean addExpense(String description, double totalAmount, String paidBy, List<Double> splits,
            List<String> users, SplitType splitType) {
        if (!isMember(paidBy)) {
            throw new IllegalArgumentException("User is not a member of the group");
        }

        for (String user : users) {
            if (!isMember(user)) {
                throw new IllegalArgumentException("User is not a member of the group");
            }
        }

        // SplitFactory splitFactory = SplitFactory.getInstance(splitType);
        List<Split> newSplits = SplitFactory.getInstance(splitType).calculateSplit(totalAmount, users, splits);

        Expense expense = new Expense(description, totalAmount, paidBy, newSplits, this.id);
        this.expenses.put(expense.getId(), expense);

        for (Split split : newSplits) {
            if (!split.getUserId().equals(paidBy)) {
                // split.getUserId() now owes paidBy
                this.updateGroupBalance(split.getUserId(), paidBy, split.getAmount());
            }
        }

        // Notify users
        System.out.println("\n=========== Sending Notifications ====================");
        String paidByName = getUserByUserId(paidBy).getName();
        this.notifyUsers("New expense added: " + description + " (Rs " + totalAmount + ")");

        // Printing console message-------
        System.out.println("\n=========== Expense Message ====================");
        System.out.println("Expense added to " + name + ": " + description + " (Rs " + totalAmount
                + ") paid by " + paidByName + " and involved people are : ");

        for (Split split : newSplits) {
            System.out.println(getUserByUserId(split.getUserId()).getName() + " : " + split.getAmount());
        }

        return true;

    }

    public boolean settleExpense(String fromUserId, String toUserId, double amount) {

        if (!isMember(fromUserId) || !isMember(toUserId)) {
            System.out.println("user is not part or group");
            return false;
        }

        // When fromUserId gives money to toUserId, it REDUCES what fromUserId owes
        // toUserId
        // OR it increases what toUserId owes fromUserId.
        // Both are handled by updateGroupBalance(toUserId, fromUserId, amount)
        // Wait, if I (from) pay you (to), I am the creditor of this settlement
        // transaction.
        this.updateGroupBalance(toUserId, fromUserId, amount);

        String fromUserName = getUserByUserId(fromUserId).getName();
        String toUserName = getUserByUserId(toUserId).getName();

        System.out.println("\n=========== Settling Expense ====================");
        System.out.println("Settled expense from " + fromUserName + " to " + toUserName + " (Rs " + amount + ")");

        this.notifyUsers("Settled expense from " + fromUserName + " to " + toUserName + " (Rs " + amount + ")");

        return true;
    }

    public void showGroupBalance() {
        System.out.println("balances sheet of " + name + " : ");
        boolean hasBalance = false;

        for (Map.Entry<String, Map<String, Double>> entry : this.balanceSheet.entrySet()) {
            String userName = getUserByUserId(entry.getKey()).getName();

            for (Map.Entry<String, Double> balanceEntry : entry.getValue().entrySet()) {
                double amount = balanceEntry.getValue();
                if (amount > 0.001) {
                    String balanceUserName = getUserByUserId(balanceEntry.getKey()).getName();
                    System.out.println(userName + " owes " + balanceUserName + " Rs " + amount);
                    hasBalance = true;
                }
            }
        }

        if (!hasBalance) {
            System.out.println("All settled up!");
        }
    }

    public void simplifyBalanceSheet() {
        Map<String, Map<String, Double>> simplifiedBalanceSheet = DebtSimplifier.simplifyDebt(this.balanceSheet);
        this.balanceSheet = simplifiedBalanceSheet;
        return;
    }

    public void removeExpense(String expenseId) {
        this.expenses.remove(expenseId);
    }

    public void addBalanceSheet(String userId, Map<String, Double> balanceSheet) {
        this.balanceSheet.put(userId, balanceSheet);
    }

    public void removeBalanceSheet(String userId) {
        this.balanceSheet.remove(userId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Map<String, Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Map<String, Expense> expenses) {
        this.expenses = expenses;
    }

    public Map<String, Map<String, Double>> getBalanceSheet() {
        return balanceSheet;
    }

    public void setBalanceSheet(Map<String, Map<String, Double>> balanceSheet) {
        this.balanceSheet = balanceSheet;
    }

}
