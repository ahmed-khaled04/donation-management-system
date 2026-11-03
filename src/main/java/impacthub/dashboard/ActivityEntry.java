package impacthub.dashboard;

import java.io.Serializable;
import java.time.LocalDateTime;

/** One tracked action: when it happened, who did it and what they did. */
public class ActivityEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private final LocalDateTime time;
    private final String actor;
    private final String description;

    public ActivityEntry(LocalDateTime time, String actor, String description) {
        this.time = time;
        this.actor = actor;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getActor() {
        return actor;
    }

    public String getDescription() {
        return description;
    }
}
