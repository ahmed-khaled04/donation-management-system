package impacthub.beneficiary;

import impacthub.people.Beneficiary;
import impacthub.people.Person;
import impacthub.people.PersonService;
import impacthub.persistence.GenericDAO;

import java.util.stream.Collectors;

/** Handles the Beneficiaries window's actions. */
public class BeneficiaryController {
    private final BeneficiaryView view;
    private final DistributionService distributionService;
    private final PersonService personService;
    private final GenericDAO<Resource> resources = new GenericDAO<>(Resource.class);

    public BeneficiaryController(BeneficiaryView view, DistributionService distributionService,
                                 PersonService personService) {
        this.view = view;
        this.distributionService = distributionService;
        this.personService = personService;
    }

    /** Lists beneficiaries with their needs, and the resources in stock. */
    public void loadBeneficiaries() {
        view.showMessage("--- beneficiaries ---");
        for (Person p : personService.searchByName("")) {
            if (p instanceof Beneficiary b) {
                String needs = b.getNeeds().stream()
                        .map(n -> n.getCategory().getName() + ": " + n.getDescription() + " (priority " + n.getPriority() + ")")
                        .collect(Collectors.joining("; "));
                view.showMessage("#" + b.getId() + " " + b.getName() + " | family of " + b.getFamilySize()
                        + " | " + b.getAddress() + " | needs: " + needs);
            }
        }
        view.showMessage("--- resources ---");
        for (Resource r : resources.findAll()) {
            view.showMessage("#" + r.getId() + " " + r.getName() + " (" + r.getCategory().getName() + ") | "
                    + r.getStockQuantity() + " " + r.getUnit() + " in stock");
        }
    }

    public void recordDistribution(int beneficiaryId, int resourceId, int quantity) {
        Resource r = resources.findById(resourceId);
        if (!(personService.findById(beneficiaryId) instanceof Beneficiary b) || r == null) {
            view.showMessage("Check the beneficiary id and resource id.");
            return;
        }
        try {
            distributionService.recordDistribution(b, r, quantity);
            view.showMessage("Gave " + quantity + " " + r.getUnit() + " of " + r.getName() + " to " + b.getName()
                    + ". " + r.getStockQuantity() + " left in stock.");
            view.showDistributions(distributionService.getDistributionsByBeneficiary(b));
        } catch (IllegalArgumentException e) {
            view.showMessage(e.getMessage());
        }
    }

    public void showImpact(String programType) {
        view.showImpact(distributionService.evaluateImpact(programType));
    }
}
