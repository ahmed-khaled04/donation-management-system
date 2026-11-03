package impacthub.people;

import impacthub.TestDb;
import impacthub.event.Event;
import impacthub.event.EventService;
import impacthub.notification.Channel;
import impacthub.persistence.GenericDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PersonServiceTest {
    private PersonService service;

    @BeforeEach
    void setUp() {
        TestDb.clear();
        service = new PersonService();
    }

    @Test
    void registersEachTypeWithEmailAsDefaultChannel() {
        assertInstanceOf(Donor.class, service.register("Donor", "A", "a@mail.com", "1", "pw"));
        assertInstanceOf(Volunteer.class, service.register("Volunteer", "B", "b@mail.com", "2", "pw"));
        Person c = service.register("Beneficiary", "C", "c@mail.com", "3", "pw");
        assertInstanceOf(Beneficiary.class, c);
        assertEquals(Channel.EMAIL, c.getPreferredChannel());
    }

    @Test
    void rejectsUnknownType() {
        assertThrows(IllegalArgumentException.class, () -> service.register("Alien", "A", "a@mail.com", "1", "pw"));
    }

    @Test
    void storesOnlyAHashAndAuthenticates() {
        Person p = service.register("Donor", "A", "a@mail.com", "1", "secret");
        assertNotEquals("secret", p.getPasswordHash());
        assertEquals(64, p.getPasswordHash().length());
        assertEquals(p.getId(), service.authenticate("A@MAIL.COM", "secret").getId());
        assertNull(service.authenticate("a@mail.com", "wrong"));
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        service.register("Donor", "A", "a@mail.com", "1", "pw");
        assertThrows(IllegalArgumentException.class, () -> service.register("Volunteer", "B", "A@Mail.com", "2", "pw"));
        assertEquals(1, service.searchByName("").size());
    }

    @Test
    void emailIsFreeAgainAfterTheOwnerIsDeleted() {
        Person p = service.register("Donor", "A", "a@mail.com", "1", "pw");
        service.delete(p.getId());
        assertDoesNotThrow(() -> service.register("Donor", "B", "a@mail.com", "2", "pw"));
    }

    @Test
    void searchIgnoresCaseAndEmptyNameFindsEveryone() {
        service.register("Donor", "Ali Hassan", "a@mail.com", "1", "pw");
        service.register("Donor", "Hassan Omar", "b@mail.com", "2", "pw");
        service.register("Donor", "Sara", "c@mail.com", "3", "pw");
        assertEquals(2, service.searchByName("hassan").size());
        assertEquals(3, service.searchByName("").size());
    }

    @Test
    void deleteReportsWhetherAPersonWasRemoved() {
        Person p = service.register("Donor", "A", "a@mail.com", "1", "pw");
        assertTrue(service.delete(p.getId()));
        assertFalse(service.delete(p.getId()));
        assertNull(service.findById(p.getId()));
    }

    @Test
    void deletingAPersonRemovesThemFromEventAttendees() {
        Person stays = service.register("Donor", "A", "a@mail.com", "1", "pw");
        Person leaves = service.register("Donor", "B", "b@mail.com", "2", "pw");
        Event event = registerBoth(stays, leaves);

        service.delete(leaves.getId());

        Event saved = new GenericDAO<>(Event.class).findById(event.getId());
        assertEquals(1, saved.getAttendees().size());
        assertEquals(stays.getId(), saved.getAttendees().get(0).getId());
    }

    @Test
    void updatingAPersonRefreshesTheirCopyOnEvents() {
        Person a = service.register("Donor", "A", "a@mail.com", "1", "pw");
        Person b = service.register("Donor", "B", "b@mail.com", "2", "pw");
        Event event = registerBoth(a, b);

        a.setPreferredChannel(Channel.SMS);
        service.update(a);

        Event saved = new GenericDAO<>(Event.class).findById(event.getId());
        assertEquals(Channel.SMS, saved.getAttendees().get(0).getPreferredChannel());
        assertEquals(Channel.EMAIL, saved.getAttendees().get(1).getPreferredChannel());
    }

    private Event registerBoth(Person a, Person b) {
        EventService events = new EventService();
        Event event = events.createEvent("Fundraiser", "Gala", LocalDateTime.of(2026, 12, 1, 18, 0), "Hall");
        events.registerAttendee(event, a);
        events.registerAttendee(event, b);
        return event;
    }
}
