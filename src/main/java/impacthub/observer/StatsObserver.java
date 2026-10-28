package impacthub.observer;

import impacthub.donation.Donation;
import impacthub.people.Donor;

public class StatsObserver implements Observer {
    private double totalRaised;

    @Override
    public void update(Subject source, ChangeEvent change) {
        if (!"DONATION_RECORDED".equals(change.getType())) {
            return;
        }
        Donation donation = (Donation) change.getPayload();
        totalRaised += donation.getAmount();
        Donor donor = donation.getDonor();
        donor.setTotalDonated(donor.getTotalDonated() + donation.getAmount());
    }

    public double getTotalRaised() {
        return totalRaised;
    }
}
