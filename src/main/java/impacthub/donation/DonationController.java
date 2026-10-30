package impacthub.donation;

import impacthub.people.Donor;
import impacthub.people.Person;
import impacthub.people.PersonService;

import java.util.ArrayList;
import java.util.List;

/** Handles the Donations window's actions. */
public class DonationController {
    private final DonationView view;
    private final DonationService donationService;
    private final PersonService personService;

    public DonationController(DonationView view, DonationService donationService, PersonService personService) {
        this.view = view;
        this.donationService = donationService;
        this.personService = personService;
    }

    public void loadDonations() {
        view.showDonationList(allDonations());
    }

    public void submitDonation(String type, int donorId, double amount) {
        if (!(personService.findById(donorId) instanceof Donor donor)) {
            view.showMessage("No donor with id " + donorId + ".");
            return;
        }
        try {
            Donation d = donationService.recordDonation(type, donor, amount);
            view.showMessage("Recorded " + d.getType() + " donation #" + d.getId() + " of " + d.getAmount()
                    + " from " + donor.getName() + ". Their total is now " + donor.getTotalDonated() + ".");
        } catch (IllegalArgumentException e) {
            view.showMessage(e.getMessage());
        }
    }

    public void generateReceipt(int donationId, List<String> options) {
        for (Donation d : allDonations()) {
            if (d.getId() == donationId) {
                view.showReceipt(donationService.buildReceipt(d, options).render());
                return;
            }
        }
        view.showMessage("No donation with id " + donationId + ".");
    }

    private List<Donation> allDonations() {
        List<Donation> all = new ArrayList<>();
        for (Person p : personService.searchByName("")) {
            if (p instanceof Donor donor) {
                all.addAll(donationService.getDonationsByDonor(donor));
            }
        }
        return all;
    }
}
