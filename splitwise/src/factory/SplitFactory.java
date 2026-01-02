package factory;

import enums.SplitType;
import strategy.EqualSplit;
import strategy.ExactSplit;
import strategy.PercentageSplit;
import strategy.SplitStrategy;

public class SplitFactory {

    public static SplitStrategy getInstance(SplitType type) {
        switch (type) {
            case EQUAL -> {
                return new EqualSplit();
            }
            case EXACT -> {
                return new ExactSplit();
            }
            case PERCENT -> {
                return new PercentageSplit();
            }
            default -> throw new IllegalArgumentException("Invalid split type");
        }

    }
}
