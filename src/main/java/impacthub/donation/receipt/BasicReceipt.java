package impacthub.donation.receipt;

import impacthub.donation.Donation;

/** The plain receipt for one donation. */
public class BasicReceipt implements Receipt {
    private final Donation donation;

    public BasicReceipt(Donation donation) {
        this.donation = donation;
    }

    @Override
    public String render() {
        return "Receipt: " + donation.getAmount() + " from " + donation.getDonor().getName()
                + " (" + donation.getType() + " donation #" + donation.getId() + ", " + donation.getDate().toLocalDate()
                + ")";
    }
}
