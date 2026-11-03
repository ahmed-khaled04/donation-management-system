package impacthub.event.ticket;

import impacthub.event.Event;
import impacthub.people.Person;

/** A standard ticket for one person to one event. */
public class BasicTicket implements Ticket {
    private static final long serialVersionUID = 1L;

    private int id;
    private final double basePrice;
    private final Event event;
    private final Person holder;

    public BasicTicket(double basePrice, Event event, Person holder) {
        this.basePrice = basePrice;
        this.event = event;
        this.holder = holder;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Event getEvent() {
        return event;
    }

    @Override
    public double getPrice() {
        return basePrice;
    }

    @Override
    public String getDescription() {
        return event.getName() + " ticket for " + holder.getName();
    }
}
