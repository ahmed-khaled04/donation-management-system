package impacthub.beneficiary;

import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;
import impacthub.people.Beneficiary;
import impacthub.persistence.GenericDAO;

import java.util.ArrayList;
import java.util.List;

/** Hands resources to beneficiaries and evaluates the impact of each program. */
public class DistributionService implements Subject {
    private final List<Observer> observers = new ArrayList<>();
    private final GenericDAO<Distribution> distributions = new GenericDAO<>(Distribution.class);
    private final GenericDAO<Resource> resources = new GenericDAO<>(Resource.class);
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

    /** programType is a resource category name: "Food" uses food-aid scoring, anything else education scoring. */
    public ImpactReport evaluateImpact(String programType) {
        evaluator.setStrategy(programType.equalsIgnoreCase("Food")
                ? new FoodAidImpactStrategy() : new EducationImpactStrategy());
        List<Distribution> inProgram = distributions.findAll().stream()
                .filter(d -> d.getResource().getCategory().getName().equalsIgnoreCase(programType))
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
