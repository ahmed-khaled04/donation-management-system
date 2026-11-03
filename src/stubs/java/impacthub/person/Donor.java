package impacthub.person;

import impacthub.notification.Channel;

/** STUB - replace with teammate's real class. */
public class Donor extends Person {
    private double totalDonated;

    public Donor(String name, Channel preferredChannel) {
        super(name, preferredChannel);
    }

    public double getTotalDonated() {
        return totalDonated;
    }

    public void setTotalDonated(double totalDonated) {
        this.totalDonated = totalDonated;
    }
}
