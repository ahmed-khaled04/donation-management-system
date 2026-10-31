package impacthub.event;

import java.time.LocalDateTime;

/** Factory: creates the right kind of event from the type the user picked. */
public class EventFactory {
    public static Event create(String type, String name, LocalDateTime date, String venue) {
        return switch (type) {
            case "Fundraiser" -> new Fundraiser(name, date, venue);
            case "Workshop" -> new Workshop(name, date, venue);
            case "Outreach" -> new Outreach(name, date, venue);
            default -> throw new IllegalArgumentException("Unknown event type: " + type);
        };
    }
}
