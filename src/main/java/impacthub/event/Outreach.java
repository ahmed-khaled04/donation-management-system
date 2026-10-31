package impacthub.event;

import java.time.LocalDateTime;

/** A field program where a team goes out to a community. */
public class Outreach extends Event {
    private final String location;
    private final int teamSize;

    public Outreach(String name, LocalDateTime date, String venue) {
        super(name, date, venue);
        this.location = venue;
        this.teamSize = 5;
    }

    @Override
    public String getType() {
        return "Outreach";
    }

    public String getLocation() {
        return location;
    }

    public int getTeamSize() {
        return teamSize;
    }
}
