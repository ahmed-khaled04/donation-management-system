package impacthub.event.ticket;

import impacthub.event.Event;

/** Decorator: wraps a ticket and adds an extra to it. */
public abstract class TicketDecorator implements Ticket {
    private static final long serialVersionUID = 1L;

    private int id;
    private final Ticket wrapped;

    protected TicketDecorator(Ticket wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Event getEvent() {
        return wrapped.getEvent();
    }

    @Override
    public double getPrice() {
        return wrapped.getPrice();
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }
}
