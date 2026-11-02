package impacthub.beneficiary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Food aid is measured by how many people were fed (the family sizes of everyone helped). */
public class FoodAidImpactStrategy implements ImpactEvaluationStrategy {
    @Override
    public ImpactReport evaluate(List<Distribution> distributions) {
        Map<Integer, Integer> familySizes = new HashMap<>();
        for (Distribution d : distributions) {
            familySizes.put(d.getBeneficiary().getId(), d.getBeneficiary().getFamilySize());
        }
        int peopleFed = familySizes.values().stream().mapToInt(Integer::intValue).sum();
        return new ImpactReport("Food aid", peopleFed,
                distributions.size() + " food distribution(s) fed " + peopleFed + " people in "
                        + familySizes.size() + " famil(ies).");
    }
}
