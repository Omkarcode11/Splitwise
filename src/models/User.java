package models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import observer.Observer;

public class User implements Observer {

    private String nextUserId;
    private String id;
    private String name;
    private String email;
    Map<String, Double> balance; // if positive then user owes money to other user if negative then other user
                                 // owes money to user

    public User(String name, String email) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.balance = new HashMap<>();
    }

    public void update(String message) {
        System.out.println("User: " + this.name + " received message: " + message);
    }

    public void updateBalance(String otherUserId, double balance) {
        if (this.balance.containsKey(otherUserId)) {
            this.balance.put(otherUserId, this.balance.get(otherUserId) + balance);
        } else {
            this.balance.put(otherUserId, balance);
        }

        if (Math.abs(this.balance.get(otherUserId)) < 0.001) {
            this.balance.remove(otherUserId);
        }
    }

    public double getOwedAmount() {
        double totalAmount = 0;
        for (double balance : this.balance.values()) {
            if (balance < 0) {
                totalAmount += Math.abs(balance);
            }
        }
        return totalAmount;
    }

    public double getOwingAmount() {
        double totalAmount = 0;
        for (double balance : this.balance.values()) {
            if (balance > 0) {
                totalAmount += balance;
            }
        }
        return totalAmount;
    }

    public String getNextUserId() {
        return nextUserId;
    }

    public void setNextUserId(String nextUserId) {
        this.nextUserId = nextUserId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Double> getBalance() {
        return balance;
    }

    public void setBalance(Map<String, Double> balance) {
        this.balance = balance;
    }

}
