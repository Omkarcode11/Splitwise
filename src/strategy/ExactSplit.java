package strategy;

import java.util.ArrayList;
import java.util.List;

import models.Split;

public class ExactSplit implements SplitStrategy {
    @Override
    public List<Split> calculateSplit(double totalAmount, List<String> userIds, List<Double> amounts) {
        List<Split> splits = new ArrayList<>();

        // validation
        if(userIds.size() != amounts.size()){
            throw new IllegalArgumentException("UserIds and amounts must have same size");
        }

        for(int i = 0; i < userIds.size(); i++){
            splits.add(new Split(userIds.get(i), amounts.get(i)));
        }

        return splits;
    }    
}
