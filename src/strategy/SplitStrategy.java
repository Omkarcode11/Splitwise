package strategy;

import java.util.List;

import models.Split;

public interface SplitStrategy {
    List<Split> calculateSplit(double totalAmount, List<String> userIds, List<Double> amounts);
}
