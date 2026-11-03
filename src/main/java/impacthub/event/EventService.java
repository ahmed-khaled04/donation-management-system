package impacthub.event;

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
        Event event = EventFactory.create(type, name, date, venue);
        events.insert(event);
        return event;
    }

    public void changeDate(Event event, LocalDateTime newDate) {
        event.updateDate(newDate);
        events.update(event);
        notifyObservers(new ChangeEvent("EVENT_DATE_CHANGED", event));
    }

    public void changeVenue(Event event, String newVenue) {
        event.updateVenue(newVenue);
        events.update(event);
        notifyObservers(new ChangeEvent("EVENT_VENUE_CHANGED", event));
    }

    public void registerAttendee(Event event, Person person) {
        event.register(person);
        events.update(event);
    }

    public void markAttendance(Event event, Person person) {
        event.markAttendance(person);
        events.update(event);
    }

    /** Extras: "VIP" adds VIP seating, "MEAL" adds dinner. The holder is registered for the event. */
    public Ticket sellTicket(Event event, Person person, List<String> extras) {
        registerAttendee(event, person);
        Ticket ticket = new BasicTicket(BASE_TICKET_PRICE, event, person);
        if (extras.contains("VIP")) {
            ticket = new VipDecorator(ticket);
        }
        if (extras.contains("MEAL")) {
            ticket = new MealDecorator(ticket);
        }
        tickets.insert(ticket);
        return ticket;
    }

    public List<Ticket> getTicketsByEvent(Event event) {
        return tickets.findAll().stream()
                .filter(t -> t.getEvent().getId() == event.getId())
                .toList();
    }

    public void sendReminders(Event event) {
        notifyObservers(new ChangeEvent("EVENT_REMINDER", event));
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
