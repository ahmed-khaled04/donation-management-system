package impacthub.donation;

import impacthub.people.Donor;

import java.time.LocalDateTime;

/** A donation paid by check; it clears a few days after it is received. */
public class CheckDonation extends Donation {
    private final String checkNumber;
    private final String bankName;
    private final LocalDateTime clearingDate;

    public CheckDonation(Donor donor, double amount) {
        super(donor, amount);
        this.checkNumber = "CHK-" + System.currentTimeMillis();
        this.bankName = "Donor's bank";
        this.clearingDate = getDate().plusDays(3);
    }

    @Override
    public String getType() {
        return "Check";
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public LocalDateTime getClearingDate() {
        return clearingDate;
    }
}
