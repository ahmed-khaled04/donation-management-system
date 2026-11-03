package impacthub.event;

import impacthub.Validation;
import impacthub.event.ticket.BasicTicket;
import impacthub.event.ticket.MealDecorator;
import impacthub.event.ticket.Ticket;
import impacthub.event.ticket.VipDecorator;
import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;
import impacthub.people.Person;
import impacthub.persistence.GenericDAO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Creates and manages events and tells its observers when an event changes. */
public class EventService implements Subject {
    private static final double BASE_TICKET_PRICE = 100;

    private final List<Observer> observers = new ArrayList<>();
    private final GenericDAO<Event> events = new GenericDAO<>(Event.class);
    private final GenericDAO<Ticket> tickets = new GenericDAO<>(Ticket.class);

    public Event createEvent(String type, String name, LocalDateTime date, String venue) {
        String cleanName = Validation.requireText(name, "The event name");
        String cleanVenue = Validation.requireText(venue, "The venue");
        Validation.requireFutureDate(date);
        Event event = EventFactory.create(type, cleanName, date, cleanVenue);
        events.insert(event);
        notifyObservers(new ChangeEvent("EVENT_CREATED", event,
                "Created " + event.getType() + " #" + event.getId() + " \"" + cleanName + "\""));
        return event;
    }

    public void changeDate(Event event, LocalDateTime newDate) {
        Validation.requireFutureDate(newDate);
        event.updateDate(newDate);
        events.update(event);
        notifyObservers(new ChangeEvent("EVENT_DATE_CHANGED", event, "Moved " + event.getName() + " to " + newDate.toString().replace('T', ' ')));
    }

    public void changeVenue(Event event, String newVenue) {
        String venue = Validation.requireText(newVenue, "The venue");
        event.updateVenue(venue);
        events.update(event);
        notifyObservers(new ChangeEvent("EVENT_VENUE_CHANGED", event, "Moved " + event.getName() + " to venue " + venue));
    }

    public void registerAttendee(Event event, Person person) {
        requireNotOver(event);
        int before = event.getAttendees().size();
        event.register(person);
        if (event.getAttendees().size() == before) {
            throw new IllegalArgumentException(person.getName() + " is already registered for " + event.getName() + ".");
        }
        events.update(event);
        notifyObservers(new ChangeEvent("ATTENDEE_REGISTERED", event,
                "Registered " + person.getName() + " for " + event.getName()));
    }

    public void markAttendance(Event event, Person person) {
        int before = event.getAttended().size();
        event.markAttendance(person);
        if (event.getAttended().size() == before) {
            throw new IllegalArgumentException(person.getName() + " is not registered for " + event.getName()
                    + " or was already marked as attended.");
        }
        events.update(event);
        notifyObservers(new ChangeEvent("ATTENDANCE_MARKED", event,
                "Marked " + person.getName() + " as attended at " + event.getName()));
    }

    /** Extras: "VIP" adds VIP seating, "MEAL" adds dinner. The holder is registered for the event. */
    public Ticket sellTicket(Event event, Person person, List<String> extras) {
        requireNotOver(event);
        event.register(person); // does nothing if they are already registered
        events.update(event);
        Ticket ticket = new BasicTicket(BASE_TICKET_PRICE, event, person);
        if (extras.contains("VIP")) {
            ticket = new VipDecorator(ticket);
        }
        if (extras.contains("MEAL")) {
            ticket = new MealDecorator(ticket);
        }
        tickets.insert(ticket);
        notifyObservers(new ChangeEvent("TICKET_SOLD", ticket, "Sold ticket #" + ticket.getId() + ": "
                + ticket.getDescription() + " for " + ticket.getPrice() + " EGP"));
        return ticket;
    }

    public List<Ticket> getTicketsByEvent(Event event) {
        return tickets.findAll().stream()
                .filter(t -> t.getEvent().getId() == event.getId())
                .toList();
    }

    public void sendReminders(Event event) {
        notifyObservers(new ChangeEvent("EVENT_REMINDER", event, "Sent reminders for " + event.getName()));
    }

    private static void requireNotOver(Event event) {
        if (!event.getDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(event.getName() + " has already taken place.");
        }
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(ChangeEvent change) {
        for (Observer o : observers) {
            o.update(this, change);
        }
    }
}
