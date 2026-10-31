package impacthub.event.ticket;

/** Adds VIP seating to a ticket. */
public class VipDecorator extends TicketDecorator {
    public VipDecorator(Ticket wrapped) {
        super(wrapped);
    }

    @Override
    public double getPrice() {
        return super.getPrice() + 150;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + VIP seating";
    }
}
