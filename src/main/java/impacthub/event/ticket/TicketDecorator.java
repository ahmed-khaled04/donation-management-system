package impacthub.event.ticket;

/** Decorator: wraps a ticket and adds an extra to it. */
public abstract class TicketDecorator implements Ticket {
    private final Ticket wrapped;

    protected TicketDecorator(Ticket wrapped) {
        this.wrapped = wrapped;
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
