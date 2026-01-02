package strategy;

import java.util.ArrayList;
import java.util.List;

import models.Split;

public class PercentageSplit implements SplitStrategy {
    @Override
    public List<Split> calculateSplit(double totalAmount, List<String> userIds, List<Double> amounts) {
        List<Split> splits = new ArrayList<>();

        // validation 
        if(userIds.size() != amounts.size()){
            throw new IllegalArgumentException("UserIds and amounts must have same size");
        }

        for(int i = 0 ; i < userIds.size(); i++){
            double amount = (totalAmount * amounts.get(i)) / 100.0;
            splits.add(new Split(userIds.get(i), amount));
        }

        return splits;
    }
}
