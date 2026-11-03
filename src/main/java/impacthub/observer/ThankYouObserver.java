package impacthub.observer;

import impacthub.donation.Donation;
import impacthub.notification.Message;
import impacthub.notification.NotificationService;

public class ThankYouObserver implements Observer {
    private final NotificationService notificationService;

    public ThankYouObserver(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void update(Subject source, ChangeEvent change) {
        if (!"DONATION_RECORDED".equals(change.getType())) {
            return;
        }
        Donation donation = (Donation) change.getPayload();
        Message msg = new Message(
                "Thank you for your donation",
                "Dear " + donation.getDonor().getName() + ", thank you for your donation of "
                        + donation.getAmount() + ".");
        notificationService.sendNotification(donation.getDonor(), msg);
    }
}
