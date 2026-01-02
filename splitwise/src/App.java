import java.util.Arrays;
import java.util.List;

import enums.SplitType;
import models.*;

public class App {
    public static void main(String[] args) throws Exception {
           Splitwise manager = Splitwise.getInstance();
        
        System.out.println("\n=========== Creating Users ====================");
        User user1 = manager.createUser("Aditya", "aditya@gmail.com");
        User user2 = manager.createUser("Rohit", "rohit@gmail.com");
        User user3 = manager.createUser("Manish", "manish@gmail.com");
        User user4 = manager.createUser("Saurav", "saurav@gmail.com");
        
        System.out.println("\n=========== Creating Group and Adding Members ====================");
        Group hostelGroup = manager.createGroup("Hostel Expenses");
        manager.addGroupUser(hostelGroup.getId(), user1.getId());
        manager.addGroupUser(hostelGroup.getId(), user2.getId());
        manager.addGroupUser(hostelGroup.getId(), user3.getId());
        manager.addGroupUser(hostelGroup.getId(), user4.getId());

        System.out.println("\n=========== Adding Expenses in group ====================");    
        List<String> groupMembers = Arrays.asList(user1.getId(), user2.getId(), user3.getId(), user4.getId());
        manager.addExpenseToGroup(hostelGroup.getId(), "Lunch", 800.0, user1.getId(), groupMembers, SplitType.EQUAL);
        
        List<String> dinnerMembers = Arrays.asList(user1.getId(), user3.getId(), user4.getId());
        List<Double> dinnerAmounts = Arrays.asList(200.0, 300.0, 200.0);
        manager.addExpenseToGroup(hostelGroup.getId(), "Dinner", 700.0, user3.getId(), dinnerMembers, 
                                 SplitType.EXACT, dinnerAmounts);

        System.out.println("\n=========== printing Group-Specific Balances ===================="); 
        manager.showGroupBalances(hostelGroup.getId());

        System.out.println("\n=========== Debt Simplification ===================="); 
        manager.simplifyGroupDept(hostelGroup.getId());

        System.out.println("\n=========== printing Group-Specific Balances ===================="); 
        manager.showGroupBalances(hostelGroup.getId());

        System.out.println("\n=========== Adding Individual Expense ===================="); 
        manager.addIndividualPayment("Coffee", 40.0, user2.getId(), user4.getId(), SplitType.EQUAL);
        
        System.out.println("\n=========== printing User Balances ===================="); 
        manager.showUserBalance(user1.getId());
        manager.showUserBalance(user2.getId());
        manager.showUserBalance(user3.getId());
        manager.showUserBalance(user4.getId());

        System.out.println("\n==========Attempting to remove Rohit from group==========");
        manager.removeGroupUser(hostelGroup.getId(), user2.getId());

        System.out.println("\n======== Making Settlement to Clear Rohit's Debt =========="); 
        manager.settlePaymentInGroup(hostelGroup.getId(), user2.getId(), user3.getId(), 200.0);
        
        System.out.println("\n======== Attempting to Remove Rohit Again ==========");
        manager.removeGroupUser(hostelGroup.getId(), user2.getId());
        
        System.out.println("\n=========== Updated Group Balances ===================="); 
        manager.showGroupBalances(hostelGroup.getId());
    }
}
