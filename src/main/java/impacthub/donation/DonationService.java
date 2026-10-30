package impacthub.donation;

import impacthub.donation.receipt.BasicReceipt;
import impacthub.donation.receipt.Receipt;
import impacthub.donation.receipt.TaxInfoDecorator;
import impacthub.donation.receipt.ThankYouNoteDecorator;
import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;
import impacthub.people.Donor;
import impacthub.people.Person;
import impacthub.persistence.GenericDAO;

import java.util.ArrayList;
import java.util.List;

/** Records donations and tells its observers about each new one. */
public class DonationService implements Subject {
    private final List<Observer> observers = new ArrayList<>();
    private final GenericDAO<Donation> donations = new GenericDAO<>(Donation.class);
    private final GenericDAO<Person> people = new GenericDAO<>(Person.class);

    public Donation recordDonation(String type, Donor donor, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("The amount must be more than 0.");
        }
        Donation donation = DonationFactory.create(type, donor, amount);
        donations.insert(donation);
        notifyObservers(new ChangeEvent("DONATION_RECORDED", donation));
        people.update(donation.getDonor()); // save the new total set by StatsObserver
        return donation;
    }

    public List<Donation> getDonationsByDonor(Donor donor) {
        return donations.findAll().stream()
                .filter(d -> d.getDonor().getId() == donor.getId())
                .toList();
    }

    public String generateTaxStatement(Donor donor, int year) {
        double total = getDonationsByDonor(donor).stream()
                .filter(d -> d.getDate().getYear() == year)
                .mapToDouble(Donation::getAmount)
                .sum();
        return "Tax statement " + year + " for " + donor.getName() + " (tax id " + donor.getTaxId() + "): total "
                + total;
    }

    /** Options: "TAX" adds tax information, "THANK_YOU" adds a thank-you note. */
    public Receipt buildReceipt(Donation donation, List<String> options) {
        Receipt receipt = new BasicReceipt(donation);
        if (options.contains("TAX")) {
            receipt = new TaxInfoDecorator(receipt);
        }
        if (options.contains("THANK_YOU")) {
            receipt = new ThankYouNoteDecorator(receipt);
        }
        return receipt;
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
