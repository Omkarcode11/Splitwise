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

        Map<String, Double> balanceSheet = this.balanceSheet.get(userId);
        for (Map.Entry<String, Double> entry : balanceSheet.entrySet()) {
            if (Math.abs(entry.getValue()) > 0) {
                return false;
            }
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

    public void updateGroupBalance(String fromId, String toId, Double amount) {
        this.balanceSheet.computeIfAbsent(fromId, k -> new HashMap<>());
        this.balanceSheet.computeIfAbsent(toId, k -> new HashMap<>());

        double fromBalance = this.balanceSheet.get(fromId).getOrDefault(toId, 0.0);
        this.balanceSheet.get(fromId).put(toId, fromBalance - amount);

        double toBalance = this.balanceSheet.get(toId).getOrDefault(fromId, 0.0);
        this.balanceSheet.get(toId).put(fromId, toBalance + amount);

        if (Math.abs(this.balanceSheet.get(fromId).get(toId)) == 0) {
            this.balanceSheet.get(fromId).remove(toId);
        }

        if (Math.abs(this.balanceSheet.get(toId).get(fromId)) == 0) {
            this.balanceSheet.get(toId).remove(fromId);
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

        this.updateGroupBalance(fromUserId, toUserId, amount);

        String fromUserName = getUserByUserId(fromUserId).getName();
        String toUserName = getUserByUserId(toUserId).getName();

        System.out.println("\n=========== Settling Expense ====================");
        System.out.println("Settled expense from " + fromUserName + " to " + toUserName + " (Rs " + amount + ")");

        this.notifyUsers("Settled expense from " + fromUserName + " to " + toUserName + " (Rs " + amount + ")");

        return true;
    }

    public void showGroupBalance() {
        System.out.println("balances sheet of " + name + " : ");

        for (Map.Entry<String, Map<String, Double>> entry : this.balanceSheet.entrySet()) {
            String userId = entry.getKey();
            String userName = getUserByUserId(userId).getName();
            // System.out.println(userName + " : ");

            Map<String, Double> userBalance = entry.getValue();

            if (userBalance.size() == 0) {
                System.out.println("user " + userName + " has no balance");
                continue;
            }

            for (Map.Entry<String, Double> balanceEntry : userBalance.entrySet()) {
                String balanceUserId = balanceEntry.getKey();
                String balanceUserName = getUserByUserId(balanceUserId).getName();

                double balanceAmount = balanceEntry.getValue();
                if (balanceAmount > 0) {
                    System.out.println(userName + " owes " + balanceUserName + " Rs " + balanceAmount);
                } else {
                    System.out.println(balanceUserName + " owes " + userName + " Rs " + Math.abs(balanceAmount));
                }
            }

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
