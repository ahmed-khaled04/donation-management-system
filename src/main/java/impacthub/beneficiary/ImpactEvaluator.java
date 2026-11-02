package impacthub.beneficiary;

import java.util.List;

/** Strategy context: evaluates distributions with whichever strategy is set. */
public class ImpactEvaluator {
    private ImpactEvaluationStrategy strategy;

    public void setStrategy(ImpactEvaluationStrategy s) {
        this.strategy = s;
    }

    public ImpactReport evaluate(List<Distribution> distributions) {
        return strategy.evaluate(distributions);
    }
}
