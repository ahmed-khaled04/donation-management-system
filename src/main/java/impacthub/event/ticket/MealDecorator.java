package impacthub.event.ticket;

/** Adds dinner to a ticket. */
public class MealDecorator extends TicketDecorator {
    private static final long serialVersionUID = 1L;

    public MealDecorator(Ticket wrapped) {
        super(wrapped);
    }

    @Override
    public double getPrice() {
        return super.getPrice() + 40;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + dinner";
    }
}
