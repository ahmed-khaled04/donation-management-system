package impacthub.observer;

import java.time.LocalDateTime;

public class ChangeEvent {
    private final Object payload;
    private final LocalDateTime timestamp;
    private final String type;

    public ChangeEvent(String type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
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
