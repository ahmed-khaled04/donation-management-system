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
        assertInstanceOf(Donor.class, service.register("Donor", "A", "a@mail.com", "01000000000", "pass"));
        assertInstanceOf(Volunteer.class, service.register("Volunteer", "B", "b@mail.com", "01000000000", "pass"));
        Person c = service.register("Beneficiary", "C", "c@mail.com", "01000000000", "pass");
        assertInstanceOf(Beneficiary.class, c);
        assertEquals(Channel.EMAIL, c.getPreferredChannel());
    }

    @Test
    void rejectsUnknownType() {
        assertThrows(IllegalArgumentException.class, () -> service.register("Alien", "A", "a@mail.com", "01000000000", "pass"));
    }

    @Test
    void storesOnlyAHashAndAuthenticates() {
        Person p = service.register("Donor", "A", "a@mail.com", "01000000000", "secret");
        assertNotEquals("secret", p.getPasswordHash());
        assertEquals(64, p.getPasswordHash().length());
        assertEquals(p.getId(), service.authenticate("A@MAIL.COM", "secret").getId());
        assertNull(service.authenticate("a@mail.com", "wrong"));
    }

    @Test
    void rejectsInvalidSignUpDetails() {
        assertThrows(IllegalArgumentException.class, () -> service.register("Donor", " ", "a@mail.com", "", "pass"));
        assertThrows(IllegalArgumentException.class, () -> service.register("Donor", "A", " ", "", "pass"));
        for (String email : new String[] {"a", "a@b", "@mail.com", "a b@mail.com", "a@@mail.com"}) {
            assertThrows(IllegalArgumentException.class, () -> service.register("Donor", "A", email, "", "pass"), email);
        }
        for (String phone : new String[] {"123", "abc1234567", "1234567890123456"}) {
            assertThrows(IllegalArgumentException.class, () -> service.register("Donor", "A", "a@mail.com", phone, "pass"), phone);
        }
        assertThrows(IllegalArgumentException.class, () -> service.register("Donor", "A", "a@mail.com", "", "123"));
        assertTrue(service.searchByName("").isEmpty());
    }

    @Test
    void trimsDetailsAndAllowsAnEmptyPhone() {
        Person p = service.register("Donor", "  Sara ", " sara@mail.com ", "", "pass");
        assertEquals("Sara", p.getName());
        assertEquals("sara@mail.com", p.getEmail());
        assertEquals(p.getId(), service.authenticate("sara@mail.com", "pass").getId());
        assertDoesNotThrow(() -> service.register("Donor", "Omar", "omar@mail.com", "+201000000000", "pass"));
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        service.register("Donor", "A", "a@mail.com", "01000000000", "pass");
        assertThrows(IllegalArgumentException.class, () -> service.register("Volunteer", "B", "A@Mail.com", "01000000000", "pass"));
        assertEquals(1, service.searchByName("").size());
    }

    @Test
    void emailIsFreeAgainAfterTheOwnerIsDeleted() {
        Person p = service.register("Donor", "A", "a@mail.com", "01000000000", "pass");
        service.delete(p.getId());
        assertDoesNotThrow(() -> service.register("Donor", "B", "a@mail.com", "01000000000", "pass"));
    }

    @Test
    void searchIgnoresCaseAndEmptyNameFindsEveryone() {
        service.register("Donor", "Ali Hassan", "a@mail.com", "01000000000", "pass");
        service.register("Donor", "Hassan Omar", "b@mail.com", "01000000000", "pass");
        service.register("Donor", "Sara", "c@mail.com", "01000000000", "pass");
        assertEquals(2, service.searchByName("hassan").size());
        assertEquals(3, service.searchByName("").size());
    }

    @Test
    void deleteReportsWhetherAPersonWasRemoved() {
        Person p = service.register("Donor", "A", "a@mail.com", "01000000000", "pass");
        assertTrue(service.delete(p.getId()));
        assertFalse(service.delete(p.getId()));
        assertNull(service.findById(p.getId()));
    }

    @Test
    void deletingAPersonRemovesThemFromEventAttendees() {
        Person stays = service.register("Donor", "A", "a@mail.com", "01000000000", "pass");
        Person leaves = service.register("Donor", "B", "b@mail.com", "01000000000", "pass");
        Event event = registerBoth(stays, leaves);

        service.delete(leaves.getId());

        Event saved = new GenericDAO<>(Event.class).findById(event.getId());
        assertEquals(1, saved.getAttendees().size());
        assertEquals(stays.getId(), saved.getAttendees().get(0).getId());
    }

    @Test
    void updatingAPersonRefreshesTheirCopyOnEvents() {
        Person a = service.register("Donor", "A", "a@mail.com", "01000000000", "pass");
        Person b = service.register("Donor", "B", "b@mail.com", "01000000000", "pass");
        Event event = registerBoth(a, b);

        a.setPreferredChannel(Channel.SMS);
        service.update(a);

        Event saved = new GenericDAO<>(Event.class).findById(event.getId());
        assertEquals(Channel.SMS, saved.getAttendees().get(0).getPreferredChannel());
        assertEquals(Channel.EMAIL, saved.getAttendees().get(1).getPreferredChannel());
    }

    private Event registerBoth(Person a, Person b) {
        EventService events = new EventService();
        Event event = events.createEvent("Fundraiser", "Gala", LocalDateTime.now().plusDays(30), "Hall");
        events.registerAttendee(event, a);
        events.registerAttendee(event, b);
        return event;
    }
}
