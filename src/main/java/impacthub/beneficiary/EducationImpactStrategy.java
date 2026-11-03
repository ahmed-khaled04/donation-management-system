package impacthub.beneficiary;

import java.util.List;

/** Education is measured by the share of distributions that met a need the beneficiary recorded. */
public class EducationImpactStrategy implements ImpactEvaluationStrategy {
    @Override
    public ImpactReport evaluate(List<Distribution> distributions) {
        long matched = distributions.stream().filter(this::meetsANeed).count();
        double score = distributions.isEmpty() ? 0 : 100.0 * matched / distributions.size();
        return new ImpactReport("Education", score,
                matched + " of " + distributions.size() + " education distribution(s) met a recorded need ("
                        + Math.round(score) + "%).");
    }

    private boolean meetsANeed(Distribution d) {
        int category = d.getResource().getCategory().getId();
        return d.getBeneficiary().getNeeds().stream()
                .anyMatch(n -> n.getCategory().getId() == category);
    }
}
