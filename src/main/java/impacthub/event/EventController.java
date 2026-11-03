package impacthub.event;

import impacthub.people.Person;
import impacthub.people.PersonService;
import impacthub.persistence.GenericDAO;

import java.time.LocalDateTime;
import java.util.List;

/** Handles the Events window's actions. */
public class EventController {
    private final EventView view;
    private final EventService eventService;
    private final PersonService personService;
    private final GenericDAO<Event> events = new GenericDAO<>(Event.class);

    public EventController(EventView view, EventService eventService, PersonService personService) {
        this.view = view;
        this.eventService = eventService;
        this.personService = personService;
    }

    public void loadEvents() {
        view.showEventList(events.findAll());
    }

    public void createEvent(String type, String name, LocalDateTime date, String venue) {
        try {
            Event e = eventService.createEvent(type, name, date, venue);
            view.showMessage("Created " + e.getType() + " #" + e.getId() + ": " + e.getName() + ".");
        } catch (IllegalArgumentException ex) {
            view.showMessage(ex.getMessage());
        }
    }

    public void changeDate(int eventId, LocalDateTime newDate) {
        Event e = events.findById(eventId);
        if (e == null) {
            view.showMessage("No event with id " + eventId + ".");
            return;
        }
        try {
            eventService.changeDate(e, newDate);
            view.showMessage(e.getName() + " moved to " + newDate + ". " + e.getAttendees().size() + " attendee(s) notified.");
        } catch (IllegalArgumentException ex) {
            view.showMessage(ex.getMessage());
        }
    }

    public void registerAttendee(int eventId, int personId) {
        Event e = events.findById(eventId);
        Person p = personService.findById(personId);
        if (e == null || p == null) {
            view.showMessage("Check the event id and person id.");
            return;
        }
        try {
            eventService.registerAttendee(e, p);
            view.showMessage(p.getName() + " is registered for " + e.getName() + ".");
        } catch (IllegalArgumentException ex) {
            view.showMessage(ex.getMessage());
        }
    }

    public void sellTicket(int eventId, int personId, List<String> extras) {
        Event e = events.findById(eventId);
        Person p = personService.findById(personId);
        if (e == null || p == null) {
            view.showMessage("Check the event id and person id.");
            return;
        }
        try {
            view.showTicket(eventService.sellTicket(e, p, extras));
        } catch (IllegalArgumentException ex) {
            view.showMessage(ex.getMessage());
        }
    }

    public void loadTickets(int eventId) {
        Event e = events.findById(eventId);
        if (e == null) {
            view.showMessage("No event with id " + eventId + ".");
            return;
        }
        view.showTicketList(eventService.getTicketsByEvent(e));
    }
}
