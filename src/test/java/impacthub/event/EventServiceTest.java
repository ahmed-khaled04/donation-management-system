package impacthub.event;

import impacthub.TestDb;
import impacthub.event.ticket.Ticket;
import impacthub.notification.NotificationService;
import impacthub.observer.AttendeeNotificationObserver;
import impacthub.people.Person;
import impacthub.people.PersonService;
import impacthub.persistence.GenericDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {
    private static final LocalDateTime DATE = LocalDateTime.of(2026, 12, 1, 18, 0);

    private PersonService people;
    private EventService service;
    private GenericDAO<Event> events;
    private Person sara;
    private Person omar;
    private Event gala;

    @BeforeEach
    void setUp() {
        TestDb.clear();
        people = new PersonService();
        service = new EventService();
        events = new GenericDAO<>(Event.class);
        sara = people.register("Donor", "Sara", "sara@mail.com", "0100", "pw");
        omar = people.register("Donor", "Omar", "omar@mail.com", "0101", "pw");
        gala = service.createEvent("Fundraiser", "Gala", DATE, "Hall");
    }

    @Test
    void factoryCreatesEachType() {
        assertInstanceOf(Fundraiser.class, EventFactory.create("Fundraiser", "n", DATE, "v"));
        assertInstanceOf(Workshop.class, EventFactory.create("Workshop", "n", DATE, "v"));
        assertInstanceOf(Outreach.class, EventFactory.create("Outreach", "n", DATE, "v"));
        assertThrows(IllegalArgumentException.class, () -> EventFactory.create("Party", "n", DATE, "v"));
    }

    @Test
    void registeringTwiceCountsOnce() {
        service.registerAttendee(gala, sara);
        service.registerAttendee(events.findById(gala.getId()), people.findById(sara.getId()));
        assertEquals(1, events.findById(gala.getId()).getAttendees().size());
    }

    @Test
    void onlyRegisteredPeopleCanBeMarkedAsAttended() {
        service.registerAttendee(gala, sara);
        service.markAttendance(gala, sara);
        service.markAttendance(gala, omar);
        List<Person> attended = events.findById(gala.getId()).getAttended();
        assertEquals(1, attended.size());
        assertEquals(sara.getId(), attended.get(0).getId());
    }

    @Test
    void ticketPricesAddUpForEveryCombination() {
        assertEquals(100, service.sellTicket(gala, sara, List.of()).getPrice());
        assertEquals(250, service.sellTicket(gala, sara, List.of("VIP")).getPrice());
        assertEquals(140, service.sellTicket(gala, sara, List.of("MEAL")).getPrice());
        assertEquals(290, service.sellTicket(gala, sara, List.of("VIP", "MEAL")).getPrice());
    }

    @Test
    void sellingATicketRegistersTheHolder() {
        service.sellTicket(gala, sara, List.of());
        assertEquals(1, events.findById(gala.getId()).getAttendees().size());
    }

    @Test
    void soldTicketsAreSavedWithTheirExtrasAndListedByEvent() {
        Event workshop = service.createEvent("Workshop", "Java", DATE, "Lab");
        Ticket sold = service.sellTicket(gala, omar, List.of("VIP", "MEAL"));
        service.sellTicket(workshop, sara, List.of());

        assertTrue(sold.getId() > 0);
        List<Ticket> saved = service.getTicketsByEvent(gala);
        assertEquals(1, saved.size());
        assertEquals(sold.getId(), saved.get(0).getId());
        assertEquals(290, saved.get(0).getPrice());
        assertEquals("Gala ticket for Omar + VIP seating + dinner", saved.get(0).getDescription());
    }

    @Test
    void savedTicketsSurviveTheHolderBeingDeleted() {
        service.sellTicket(gala, omar, List.of("VIP"));
        people.delete(omar.getId());
        assertEquals(250, service.getTicketsByEvent(gala).get(0).getPrice());
    }

    @Test
    void changesAreSavedAndNotifyOnlyPeopleWhoStillExist() {
        List<Integer> notified = new ArrayList<>();
        NotificationService notifications = new NotificationService();
        notifications.setStrategy((to, msg) -> notified.add(to.getId()));
        service.addObserver(new AttendeeNotificationObserver(notifications));
        service.registerAttendee(gala, sara);
        service.registerAttendee(gala, omar);
        people.delete(omar.getId());

        Event current = events.findById(gala.getId());
        service.changeDate(current, DATE.plusDays(1));
        service.changeVenue(current, "New Hall");
        service.sendReminders(current);

        assertEquals(List.of(sara.getId(), sara.getId(), sara.getId()), notified);
        Event saved = events.findById(gala.getId());
        assertEquals(DATE.plusDays(1), saved.getDate());
        assertEquals("New Hall", saved.getVenue());
    }
}
