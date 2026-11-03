package impacthub.event;

import java.time.LocalDateTime;

/** A training session led by a trainer, with limited seats. */
public class Workshop extends Event {
    private static final long serialVersionUID = 1L;

    private final int capacity;
    private final String trainer;

    public Workshop(String name, LocalDateTime date, String venue) {
        super(name, date, venue);
        this.capacity = 30;
        this.trainer = "To be announced";
    }

    @Override
    public String getType() {
        return "Workshop";
    }

    public int getCapacity() {
        return capacity;
    }

    public String getTrainer() {
        return trainer;
    }
}
