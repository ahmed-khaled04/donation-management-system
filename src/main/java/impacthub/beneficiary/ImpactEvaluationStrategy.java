package impacthub.beneficiary;

import java.util.List;

/** Strategy: one way of measuring a program's impact. */
public interface ImpactEvaluationStrategy {
    ImpactReport evaluate(List<Distribution> distributions);
}
