import java.util.Arrays;
import java.util.List;
import enums.SplitType;
import models.*;

public class SplitwiseTest {
    public static void main(String[] args) {
        try {
            Splitwise.resetInstance();
            Splitwise manager = Splitwise.getInstance();

            System.out.println("\n--- Test Case 1: Equal Split in Group ---");
            User u1 = manager.createUser("Alice", "alice@test.com");
            User u2 = manager.createUser("Bob", "bob@test.com");
            User u3 = manager.createUser("Charlie", "charlie@test.com");

            Group g1 = manager.createGroup("Trip to Goa");
            manager.addGroupUser(g1.getId(), u1.getId());
            manager.addGroupUser(g1.getId(), u2.getId());
            manager.addGroupUser(g1.getId(), u3.getId());

            manager.addExpenseToGroup(g1.getId(), "Dinner", 300.0, u1.getId(),
                    Arrays.asList(u1.getId(), u2.getId(), u3.getId()), SplitType.EQUAL);

            manager.showGroupBalances(g1.getId());

            System.out.println("\n--- Test Case 2: Exact Split in Group ---");
            manager.addExpenseToGroup(g1.getId(), "Taxis", 100.0, u2.getId(),
                    Arrays.asList(u1.getId(), u3.getId()), SplitType.EXACT, Arrays.asList(40.0, 60.0));

            manager.showGroupBalances(g1.getId());

            System.out.println("\n--- Test Case 3: Debt Simplification ---");
            manager.simplifyGroupDept(g1.getId());
            manager.showGroupBalances(g1.getId());

            System.out.println("\n--- Test Case 4: Individual (P2P) Equal Payment ---");
            manager.addIndividualPayment("Movie Ticket", 40.0, u1.getId(), u2.getId(), SplitType.EQUAL);
            manager.showUserBalance(u1.getId());
            manager.showUserBalance(u2.getId());

            System.out.println("\n--- Test Case 5: Settle Payment ---");
            manager.settlePaymentInGroup(g1.getId(), u2.getId(), u1.getId(), 50.0);
            manager.showGroupBalances(g1.getId());

            System.out.println("\n--- Test Case 6: Leaving Group Validation ---");
            boolean removed = manager.removeGroupUser(g1.getId(), u1.getId());
            System.out.println("Can Alice leave with active debt? " + removed);

            System.out.println("\n--- ALL TESTS COMPLETED SUCCESSFULLY ---");

        } catch (Exception e) {
            System.err.println("Test Failed with Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
