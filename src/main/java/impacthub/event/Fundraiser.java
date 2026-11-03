package impacthub.event;

import java.time.LocalDateTime;

/** An event held to raise money towards a target. */
public class Fundraiser extends Event {
    private static final long serialVersionUID = 1L;

    private final double targetAmount;

    public Fundraiser(String name, LocalDateTime date, String venue) {
        super(name, date, venue);
        this.targetAmount = 10000;
    }

    @Override
    public String getType() {
        return "Fundraiser";
    }

    public double getTargetAmount() {
        return targetAmount;
    }
}
