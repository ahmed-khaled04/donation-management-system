package impacthub.event.ticket;

/** Decorator component: an event ticket with a price and a description. */
public interface Ticket {
    double getPrice();

    String getDescription();
}
