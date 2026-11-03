package impacthub.observer;

import java.time.LocalDateTime;

public class ChangeEvent {
    private final Object payload;
    private final LocalDateTime timestamp;
    private final String type;
    private final String description;

    public ChangeEvent(String type, Object payload) {
        this(type, payload, "");
    }

    /** description is what the activity log shows; changes without one are not tracked. */
    public ChangeEvent(String type, Object payload, String description) {
        this.type = type;
        this.description = description;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
