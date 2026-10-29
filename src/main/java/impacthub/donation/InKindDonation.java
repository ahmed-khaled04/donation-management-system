package impacthub.donation;

import impacthub.people.Donor;

/** A donation of goods; the amount is their estimated value. */
public class InKindDonation extends Donation {
    private final String itemDescription;
    private final int quantity;
    private final double estimatedValue;

    public InKindDonation(Donor donor, double amount) {
        super(donor, amount);
        this.itemDescription = "Donated goods";
        this.quantity = 1;
        this.estimatedValue = amount;
    }

    @Override
    public String getType() {
        return "InKind";
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getEstimatedValue() {
        return estimatedValue;
    }
}
