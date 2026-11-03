package impacthub.event.ticket;

import impacthub.event.Event;

import java.io.Serializable;

/** Decorator component: an event ticket with a price and a description. */
public interface Ticket extends Serializable {
    int getId();

    Event getEvent();

    double getPrice();

    String getDescription();
}
