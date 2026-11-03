package impacthub.beneficiary;

import impacthub.lookup.ResourceCategory;
import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;
import impacthub.people.Beneficiary;
import impacthub.persistence.GenericDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Hands resources to beneficiaries and evaluates the impact of each program. */
public class DistributionService implements Subject {
    private final List<Observer> observers = new ArrayList<>();
    private final GenericDAO<Distribution> distributions = new GenericDAO<>(Distribution.class);
    private final GenericDAO<Resource> resources = new GenericDAO<>(Resource.class);
    private final GenericDAO<ResourceCategory> categories = new GenericDAO<>(ResourceCategory.class);
    private final ImpactEvaluator evaluator = new ImpactEvaluator();

    public Distribution recordDistribution(Beneficiary b, Resource r, int quantity) {
        if (quantity <= 0 || quantity > r.getStockQuantity()) {
            throw new IllegalArgumentException("Quantity must be between 1 and the stock (" + r.getStockQuantity() + ").");
        }
        Distribution d = new Distribution(b, r, quantity);
        distributions.insert(d);
        notifyObservers(new ChangeEvent("DISTRIBUTION_RECORDED", d));
        resources.update(d.getResource()); // save the stock reduced by StockObserver
        return d;
    }

    public List<Distribution> getDistributionsByBeneficiary(Beneficiary b) {
        return distributions.findAll().stream()
                .filter(d -> d.getBeneficiary().getId() == b.getId())
                .toList();
    }

    /** programType is "Food" (people fed) or "Education" (needs met), and must match a resource category name. */
    public ImpactReport evaluateImpact(String programType) {
        if (programType.equalsIgnoreCase("Food")) {
            evaluator.setStrategy(new FoodAidImpactStrategy());
        } else if (programType.equalsIgnoreCase("Education")) {
            evaluator.setStrategy(new EducationImpactStrategy());
        } else {
            throw new IllegalArgumentException("There is no impact measure for " + programType + ".");
        }
        // saved distributions keep the category name they had, so match on the id, which never changes
        Set<Integer> categoryIds = categories.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(programType))
                .map(ResourceCategory::getId)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            throw new IllegalArgumentException("No resource category is named " + programType + ", so its impact cannot be evaluated.");
        }
        List<Distribution> inProgram = distributions.findAll().stream()
                .filter(d -> categoryIds.contains(d.getResource().getCategory().getId()))
                .toList();
        return evaluator.evaluate(inProgram);
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(ChangeEvent change) {
        for (Observer o : observers) {
            o.update(this, change);
        }
    }
}
