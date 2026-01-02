package strategy;

import java.util.ArrayList;
import java.util.List;

import models.Split;

public class EqualSplit implements SplitStrategy {
    @Override
    public List<Split> calculateSplit(double totalAmount, List<String> userIds, List<Double> amounts) {
        List<Split> splits = new ArrayList<>();
        double amountPerUser = totalAmount / userIds.size();

        // validation

        for (String userId : userIds) {
            splits.add(new Split(userId, amountPerUser));
        }

        return splits;
    }
}
