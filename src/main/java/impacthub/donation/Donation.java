package impacthub.donation;

import impacthub.people.Donor;

import java.io.Serializable;
import java.time.LocalDateTime;

/** A donation made by a donor. Subclasses are the donation methods. */
public abstract class Donation implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private final double amount;
    private final LocalDateTime date;
    private final Donor donor;

    protected Donation(Donor donor, double amount) {
        this.donor = donor;
        this.amount = amount;
        this.date = LocalDateTime.now();
    }

    public abstract String getType();

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Donor getDonor() {
        return donor;
    }
}
