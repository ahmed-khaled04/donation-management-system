package impacthub.observer;

import impacthub.donation.Donation;
import impacthub.donation.DonationService;
import impacthub.donation.receipt.Receipt;

import java.util.List;

public class ReceiptObserver implements Observer {
    @Override
    public void update(Subject source, ChangeEvent change) {
        if (!"DONATION_RECORDED".equals(change.getType()) || !(source instanceof DonationService service)) {
            return;
        }
        Donation donation = (Donation) change.getPayload();
        Receipt receipt = service.buildReceipt(donation, List.of());
        System.out.println(receipt.render());
    }
}
