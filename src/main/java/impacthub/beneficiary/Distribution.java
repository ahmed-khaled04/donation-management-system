package impacthub.beneficiary;

import impacthub.people.Beneficiary;

import java.io.Serializable;
import java.time.LocalDateTime;

/** A quantity of a resource handed to a beneficiary. */
public class Distribution implements Serializable {
    private int id;
    private final LocalDateTime date;
    private final Beneficiary beneficiary;
    private final Resource resource;
    private final int quantity;

    public Distribution(Beneficiary beneficiary, Resource resource, int quantity) {
        this.beneficiary = beneficiary;
        this.resource = resource;
        this.quantity = quantity;
        this.date = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public Resource getResource() {
        return resource;
    }

    public int getQuantity() {
        return quantity;
    }
}
