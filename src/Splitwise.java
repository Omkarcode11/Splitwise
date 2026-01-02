import java.util.*;

import enums.SplitType;
import factory.SplitFactory;
import models.*;
import strategy.SplitStrategy;

public class Splitwise {
    Map<String, Group> groups;
    Map<String, User> users;
    Map<String, Expense> expenses;

    private static Splitwise instance;

    public Splitwise() {
        this.groups = new HashMap<>();
        this.users = new HashMap<>();
        this.expenses = new HashMap<>();
    }

    public synchronized static Splitwise getInstance() {
        if (instance == null) {
            instance = new Splitwise();
        }

        return instance;
    }

    public synchronized static void resetInstance() {
        instance = null;
    }

    public User createUser(String name, String email) {
        User user = new User(name, email);
        this.users.put(user.getId(), user);
        System.out.println("User created name : " + user.getName() + " and email : " + user.getEmail());
        return user;
    }

    public Group createGroup(String name) {
        Group group = new Group(name);
        this.groups.put(group.getId(), group);
        System.out.println("Group created name : " + group.getName());
        return group;
    }

    public Group getGroupById(String id) {
        return this.groups.get(id);
    }

    public void addGroupUser(String groupId, String userId) {

        User user = users.get(userId);
        Group group = groups.get(groupId);

        if (user == null || group == null) {
            System.out.println("User or Group not found");
            return;
        }

        group.addUser(user);
    }

    public boolean removeGroupUser(String groupId, String userId) {
        User user = users.get(userId);

        if (user == null) {
            System.out.println("User not found");
            return false;
        }
        Group group = groups.get(groupId);

        if (group == null) {
            System.out.println("Group not found");
            return false;
        }

        boolean removedUser = group.removeUser(userId);

        if (!removedUser) {
            System.out.println("User not removed");
            return false;
        }

        System.out.println("User removed name : " + user.getName() + " and email : " + user.getEmail());

        return true;
    }

    public void addExpenseToGroup(String groupId, String description, double totalAmount, String paidBy,
            List<String> users, SplitType splitType, List<Double> splits) {
        Group group = getGroupById(groupId);

        if (group == null) {
            System.out.println("Group not found");
            return;
        }

        group.addExpense(description, totalAmount, paidBy, splits, users, splitType);
    }

    public void addExpenseToGroup(String groupId, String description, double amount,
            String paidByUserId, List<String> involvedUsers,
            SplitType splitType) {
        addExpenseToGroup(groupId, description, amount, paidByUserId, involvedUsers, splitType, new ArrayList<>());
    }

    public void settlePaymentInGroup(String groupId, String fromUserId, String toUserId, double amount) {
        Group group = getGroupById(groupId);

        if (group == null) {
            System.out.println("Group not found");
            return;
        }

        group.settleExpense(fromUserId, toUserId, amount);
    }

    public User getUserById(String id) {
        return this.users.get(id);
    }

    public void settleIndividualPayment(String fromUserId, String toUserId, double amount) {
        User fromUser = getUserById(fromUserId);
        User toUser = getUserById(toUserId);

        if (fromUser == null || toUser == null) {
            System.out.println("User not found");
            return;
        }

        fromUser.updateBalance(toUserId, amount);
        toUser.updateBalance(fromUserId, -amount);

        System.out.println("Individual payment settled from " + fromUser.getName() + " to " + toUser.getName() + " (Rs "
                + amount + ")");
    }

    public Expense getExpenseById(String id) {
        return this.expenses.get(id);
    }

    public void settleIndividualPayment(String description, double amount, String fromUserId, String toUserId,
            SplitType splitType, List<Double> splits) {

        SplitStrategy splitStrategy = SplitFactory.getInstance(splitType);
        List<Split> newSplits = splitStrategy.calculateSplit(amount, Arrays.asList(fromUserId, toUserId), splits);

        Expense expense = new Expense(description, amount, fromUserId, newSplits);
        this.expenses.put(expense.getId(), expense);

        User fromUser = getUserById(fromUserId);
        User toUser = getUserById(toUserId);

        fromUser.updateBalance(toUserId, amount);
        toUser.updateBalance(fromUserId, -amount);

        System.out.println("Individual payment settled from " + fromUser.getName() + " to " + toUser.getName() + " (Rs "
                + amount + ")");

    }

    public void addIndividualPayment(String description, double amount,
            String paidByUserId, String otherUserId, SplitType splitType) {

        User paidByUser = getUserById(paidByUserId);
        User otherUser = getUserById(otherUserId);

        if (paidByUser == null || otherUser == null) {
            System.out.println("User not found");
            return;
        }

        List<String> involvedUsers = Arrays.asList(paidByUserId, otherUserId);

        List<Split> newSplits = SplitFactory.getInstance(splitType).calculateSplit(amount, involvedUsers,
                new ArrayList<>());

        Expense expense = new Expense(description, amount, paidByUserId, newSplits);
        this.expenses.put(expense.getId(), expense);

        for (Split split : newSplits) {
            if (!split.getUserId().equals(paidByUserId)) {
                User user = getUserById(split.getUserId());

                // user owes paidByUser
                user.updateBalance(paidByUserId, split.getAmount());
                paidByUser.updateBalance(split.getUserId(), -split.getAmount());
            }
        }

        System.out.println(
                "Individual Expense added : " + description + " (Rs " + amount + ") paid by " + paidByUser.getName());
        for (Split split : newSplits) {
            System.out.println(getUserById(split.getUserId()).getName() + " : " + split.getAmount());
        }
    }

    public void showUserBalance(String userId) {
        User user = getUserById(userId);
        if (user == null) {
            System.out.println("User not found");
            return;
        }
        System.out.println("User balance for " + user.getName() + " is " + user.getBalance());

        for (Map.Entry<String, Double> otherUser : user.getBalance().entrySet()) {
            System.out.println("User balance for " + otherUser.getKey() + " is " + otherUser.getValue());
            User othUser = getUserById(otherUser.getKey());

            if (othUser == null) {
                System.out.println("User not found");
                return;
            } else {
                if (otherUser.getValue() < 0) {
                    System.out.println(othUser.getName() + " owes money to " + user.getName() + ": Rs "
                            + Math.abs(otherUser.getValue()));
                } else if (otherUser.getValue() > 0) {
                    System.out.println(
                            user.getName() + " owes money to " + othUser.getName() + ": Rs " + otherUser.getValue());
                }
            }
        }
    }

    public void showGroupBalances(String groupId) {
        Group group = getGroupById(groupId);
        if (group == null) {
            System.out.println("Group not found");
            return;
        }
        group.showGroupBalance();
    }

    public void simplifyGroupDept(String groupId) {
        Group group = getGroupById(groupId);
        if (group == null) {
            System.out.println("Group not found");
            return;
        }
        group.simplifyBalanceSheet();
    }

}
