package impacthub.event;

import impacthub.person.Person;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** STUB - replace with teammate's real class. */
public class Event {
    private final String name;
    private LocalDateTime date;
    private String venue;
    private final List<Person> attendees = new ArrayList<>();

    public Event(String name, LocalDateTime date, String venue) {
        this.name = name;
        this.date = date;
        this.venue = venue;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getVenue() {
        return venue;
    }

    public List<Person> getAttendees() {
        return attendees;
    }

    public void register(Person p) {
        attendees.add(p);
    }
}
