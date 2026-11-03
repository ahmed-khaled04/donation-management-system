package impacthub.donation;

import impacthub.person.Donor;

/** STUB - replace with teammate's real class. */
public class Donation {
    private final Donor donor;
    private final double amount;

    public Donation(Donor donor, double amount) {
        this.donor = donor;
        this.amount = amount;
    }

    public Donor getDonor() {
        return donor;
    }

    public double getAmount() {
        return amount;
    }
}
