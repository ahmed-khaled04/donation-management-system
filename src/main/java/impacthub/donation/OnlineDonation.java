package impacthub.donation;

import impacthub.people.Donor;

/** A donation paid online through a payment gateway. */
public class OnlineDonation extends Donation {
    private final String transactionId;
    private final String gateway;

    public OnlineDonation(Donor donor, double amount) {
        super(donor, amount);
        this.transactionId = "TXN-" + System.currentTimeMillis();
        this.gateway = "Card gateway";
    }

    @Override
    public String getType() {
        return "Online";
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getGateway() {
        return gateway;
    }
}
