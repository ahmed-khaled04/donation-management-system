package impacthub.event;

import impacthub.people.Person;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** An event the organization runs, with its registered and attending people. */
public abstract class Event implements Serializable {
    private int id;
    private final String name;
    private LocalDateTime date;
    private String venue;
    private EventStatus status = EventStatus.PLANNED;
    private final List<Person> attendees = new ArrayList<>();
    private final List<Person> attended = new ArrayList<>();

    protected Event(String name, LocalDateTime date, String venue) {
        this.name = name;
        this.date = date;
        this.venue = venue;
    }

    public abstract String getType();

    public void register(Person p) {
        if (!contains(attendees, p)) {
            attendees.add(p);
        }
    }

    /** Only registered people can be marked as attended. */
    public void markAttendance(Person p) {
        if (contains(attendees, p) && !contains(attended, p)) {
            attended.add(p);
        }
    }

    public void updateDate(LocalDateTime newDate) {
        this.date = newDate;
    }

    public void updateVenue(String newVenue) {
        this.venue = newVenue;
    }

    public void setStatus(EventStatus s) {
        this.status = s;
    }

    public int getId() {
        return id;
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

    public EventStatus getStatus() {
        return status;
    }

    public List<Person> getAttendees() {
        return attendees;
    }

    public List<Person> getAttended() {
        return attended;
    }

    // Saved people are compared by id; people not saved yet (id 0) by identity.
    private static boolean contains(List<Person> list, Person p) {
        return list.stream().anyMatch(x -> x == p || (p.getId() != 0 && x.getId() == p.getId()));
    }
}
