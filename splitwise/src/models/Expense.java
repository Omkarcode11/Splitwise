package models;

import java.util.List;
import java.util.UUID;

import enums.SplitType;

public class Expense {
    private String id;
    private String description;
    private double totalAmount;
    private String paidBy;
    private List<Split> splits;
    private String groupId;

    public Expense(String description, double totalAmount, String paidBy, List<Split> splits, String groupId) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidBy = paidBy;
        this.splits = splits;
        this.groupId = groupId;
    }
    public Expense(String description, double totalAmount, String paidBy, List<Split> splits) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidBy = paidBy;
        this.splits = splits;
        this.groupId = null;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public List<Split> getSplits() {
        return splits;
    }

    public String getGroupId() {
        return groupId;
    }
}
