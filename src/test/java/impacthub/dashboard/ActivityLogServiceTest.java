package impacthub.dashboard;

import impacthub.TestDb;
import impacthub.beneficiary.DistributionService;
import impacthub.beneficiary.Resource;
import impacthub.donation.DonationService;
import impacthub.event.Event;
import impacthub.event.EventService;
import impacthub.lookup.ResourceCategory;
import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.people.Beneficiary;
import impacthub.people.Donor;
import impacthub.people.Person;
import impacthub.people.PersonService;
import impacthub.people.Session;
import impacthub.people.Volunteer;
import impacthub.persistence.GenericDAO;
import impacthub.volunteer.Assignment;
import impacthub.volunteer.VolunteerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityLogServiceTest {
    private PersonService people;
    private Session session;
    private ActivityLogService log;

    @BeforeEach
    void setUp() {
        TestDb.clear();
        people = new PersonService();
        session = new Session();
        log = new ActivityLogService(session);
        people.addObserver(log);
        session.addObserver(log);
    }

    private List<String> descriptions() {
        return log.getRecent().stream().map(ActivityEntry::getDescription).toList();
    }

    @Test
    void tracksAnActionFromEveryService() {
        Person sara = people.register("Donor", "Sara", "sara@mail.com", "01000000000", "pass");
        Volunteer ali = (Volunteer) people.register("Volunteer", "Ali", "ali@mail.com", "01000000000", "pass");
        Beneficiary fatma = (Beneficiary) people.register("Beneficiary", "Fatma", "fatma@mail.com", "01000000000", "pass");

        DonationService donations = new DonationService();
        EventService events = new EventService();
        VolunteerService volunteers = new VolunteerService();
        DistributionService distributions = new DistributionService();
        donations.addObserver(log);
        events.addObserver(log);
        volunteers.addObserver(log);
        distributions.addObserver(log);

        donations.recordDonation("Online", (Donor) sara, 500);
        Event gala = events.createEvent("Fundraiser", "Gala", LocalDateTime.now().plusDays(1), "Hall");
        volunteers.assign(ali, gala, "Helper");
        ResourceCategory food = new ResourceCategory("Food");
        new GenericDAO<>(ResourceCategory.class).insert(food);
        Resource rice = new Resource("Rice", "bags", 10, food);
        new GenericDAO<>(Resource.class).insert(rice);
        distributions.recordDistribution(fatma, rice, 1);

        List<String> recent = descriptions();
        assertEquals("Gave 1 bags of Rice to Fatma", recent.get(0));
        assertEquals("Assigned Ali as Helper at Gala", recent.get(1));
        assertEquals("Created Fundraiser #" + gala.getId() + " \"Gala\"", recent.get(2));
        assertTrue(recent.get(3).startsWith("Recorded Online donation"));
        assertEquals("Signed up as Beneficiary", recent.get(4));
    }

    @Test
    void keepsTheLast10NewestFirst() {
        for (int i = 1; i <= 12; i++) {
            people.register("Donor", "P" + i, "p" + i + "@mail.com", "", "pass");
        }
        List<ActivityEntry> recent = log.getRecent();
        assertEquals(10, recent.size());
        assertEquals("P12", recent.get(0).getActor());
        assertEquals("P3", recent.get(9).getActor());
    }

    @Test
    void survivesARestart() {
        people.register("Donor", "Sara", "sara@mail.com", "", "pass");
        assertEquals(List.of("Signed up as Donor"), new ActivityLogService(new Session()).getRecent().stream()
                .map(ActivityEntry::getDescription).toList());
    }

    @Test
    void recordsWhoActed() {
        Person sara = people.register("Donor", "Sara", "sara@mail.com", "", "pass");
        Person omar = people.register("Donor", "Omar", "omar@mail.com", "", "pass");
        session.login(sara);
        people.delete(omar.getId());
        session.logout();

        List<ActivityEntry> recent = log.getRecent();
        assertEquals("Logged out", recent.get(0).getDescription());
        assertEquals("Sara", recent.get(0).getActor());
        assertEquals("Deleted person #" + omar.getId() + " (Omar)", recent.get(1).getDescription());
        assertEquals("Sara", recent.get(1).getActor());
        assertEquals("Logged in", recent.get(2).getDescription());
    }

    @Test
    void refusedActionsAreNotTracked() {
        people.register("Donor", "Sara", "sara@mail.com", "", "pass");
        int before = log.getRecent().size();
        assertThrows(IllegalArgumentException.class, () -> people.register("Donor", "Dup", "sara@mail.com", "", "pass"));
        assertEquals(before, log.getRecent().size());
    }

    @Test
    void changesWithoutADescriptionAreNotTracked() {
        log.update(session, new ChangeEvent("INTERNAL", "x"));
        assertTrue(log.getRecent().isEmpty());
    }

    @Test
    void tellsItsObserversAboutEachAction() {
        List<String> seen = new ArrayList<>();
        Observer dashboard = (source, change) -> seen.add(change.getType());
        log.addObserver(dashboard);
        people.register("Donor", "Sara", "sara@mail.com", "", "pass");
        assertEquals(List.of("ACTIVITY_RECORDED"), seen);
    }

    @Test
    void hoursAndCertificatesAreTracked() {
        Volunteer ali = (Volunteer) people.register("Volunteer", "Ali", "ali@mail.com", "", "pass");
        Event gala = new EventService().createEvent("Fundraiser", "Gala", LocalDateTime.now().plusDays(1), "Hall");
        VolunteerService volunteers = new VolunteerService();
        volunteers.addObserver(log);
        Assignment a = volunteers.assign(ali, gala, "Helper");
        volunteers.logHours(a, 4);
        volunteers.generateCertificate(ali);
        assertEquals("Issued a certificate to Ali", descriptions().get(0));
        assertEquals("Logged 4.0 h for Ali", descriptions().get(1));
    }
}
