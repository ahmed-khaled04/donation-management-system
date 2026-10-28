package impacthub.people;

import impacthub.notification.Channel;

/** A person or company that gives donations. */
public class Donor extends Person {
    private String donorType = "Individual";
    private String taxId = "";
    private double totalDonated;

    public Donor(String name, String email, String phone, Channel preferredChannel) {
        super(name, email, phone, preferredChannel);
    }

    public String getDonorType() {
        return donorType;
    }

    public void setDonorType(String donorType) {
        this.donorType = donorType;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public double getTotalDonated() {
        return totalDonated;
    }

    public void setTotalDonated(double totalDonated) {
        this.totalDonated = totalDonated;
    }
}
