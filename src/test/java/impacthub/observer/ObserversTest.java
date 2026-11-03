package impacthub.observer;

import impacthub.beneficiary.Distribution;
import impacthub.beneficiary.Resource;
import impacthub.donation.Donation;
import impacthub.donation.DonationService;
import impacthub.event.Event;
import impacthub.notification.Channel;
import impacthub.notification.Message;
import impacthub.notification.NotificationService;
import impacthub.person.Donor;
import impacthub.person.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObserversTest {
    private final List<Person> sentTo = new ArrayList<>();
    private final List<Message> sentMsgs = new ArrayList<>();
    private NotificationService notifications;
    private DonationService donationService;
    private Donor donor;

    @BeforeEach
    void setUp() {
        notifications = new NotificationService();
        notifications.setStrategy((to, m) -> {
            sentTo.add(to);
            sentMsgs.add(m);
            return true;
        });
        donationService = new DonationService();
        donor = new Donor("Sara", Channel.EMAIL);
    }

    private ChangeEvent donation(double amount) {
        return new ChangeEvent("DONATION_RECORDED", new Donation(donor, amount));
    }

    @Test
    void changeEventExposesData() {
        Object payload = new Object();
        ChangeEvent e = new ChangeEvent("T", payload);
        assertEquals("T", e.getType());
        assertSame(payload, e.getPayload());
        assertNotNull(e.getTimestamp());
    }

    @Test
    void subjectNotifiesRegisteredObserversUntilRemoved() {
        StatsObserver stats = new StatsObserver();
        donationService.addObserver(stats);
        donationService.notifyObservers(donation(10));
        donationService.removeObserver(stats);
        donationService.notifyObservers(donation(10));
        assertEquals(10, stats.getTotalRaised());
    }

    @Test
    void statsObserverAccumulatesTotalsAndDonorTotal() {
        StatsObserver stats = new StatsObserver();
        stats.update(donationService, donation(50));
        stats.update(donationService, donation(25));
        assertEquals(75, stats.getTotalRaised());
        assertEquals(75, donor.getTotalDonated());
    }

    @Test
    void statsObserverIgnoresOtherEvents() {
        StatsObserver stats = new StatsObserver();
        stats.update(donationService, new ChangeEvent("OTHER", null));
        assertEquals(0, stats.getTotalRaised());
    }

    @Test
    void thankYouObserverMessagesDonor() {
        new ThankYouObserver(notifications).update(donationService, donation(40));
        assertEquals(List.of(donor), sentTo);
        assertTrue(sentMsgs.get(0).getBody().contains("Sara"));
        assertTrue(sentMsgs.get(0).getBody().contains("40"));
    }

    @Test
    void thankYouObserverIgnoresOtherEvents() {
        new ThankYouObserver(notifications).update(donationService, new ChangeEvent("OTHER", null));
        assertTrue(sentTo.isEmpty());
    }

    @Test
    void receiptObserverRendersReceipt() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new ReceiptObserver().update(donationService, donation(30));
        } finally {
            System.setOut(original);
        }
        assertTrue(out.toString().contains("Receipt: 30.0 from Sara"));
    }

    @Test
    void attendeeObserverNotifiesAllAttendeesOnDateAndVenueChange() {
        Event event = new Event("Gala", LocalDateTime.of(2026, 12, 1, 18, 0), "Hall A");
        Person p1 = new Donor("A", Channel.EMAIL);
        Person p2 = new Donor("B", Channel.SMS);
        event.register(p1);
        event.register(p2);
        AttendeeNotificationObserver obs = new AttendeeNotificationObserver(notifications);

        obs.update(null, new ChangeEvent("EVENT_DATE_CHANGED", event));
        obs.update(null, new ChangeEvent("EVENT_VENUE_CHANGED", event));
        obs.update(null, new ChangeEvent("OTHER", event));

        assertEquals(List.of(p1, p2, p1, p2), sentTo);
        assertTrue(sentMsgs.get(0).getBody().contains("date"));
        assertTrue(sentMsgs.get(2).getBody().contains("Hall A"));
    }

    @Test
    void stockObserverReducesStock() {
        Resource rice = new Resource(100);
        new StockObserver().update(null, new ChangeEvent("DISTRIBUTION_RECORDED", new Distribution(rice, 30)));
        assertEquals(70, rice.getStockQuantity());
    }

    @Test
    void stockObserverIgnoresOtherEvents() {
        Resource rice = new Resource(100);
        new StockObserver().update(null, new ChangeEvent("OTHER", new Distribution(rice, 30)));
        assertEquals(100, rice.getStockQuantity());
    }
}
