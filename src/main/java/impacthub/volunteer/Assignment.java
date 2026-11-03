package impacthub.volunteer;

import impacthub.event.Event;
import impacthub.people.Volunteer;

import java.io.Serializable;
import java.time.LocalDateTime;

/** A volunteer's role at an event and the hours they worked there. */
public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private final Volunteer volunteer;
    private final Event event;
    private final String role;
    private double hoursWorked;
    private final LocalDateTime date;

    public Assignment(Volunteer volunteer, Event event, String role) {
        this.volunteer = volunteer;
        this.event = event;
        this.role = role;
        this.date = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public Event getEvent() {
        return event;
    }

    public String getRole() {
        return role;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
