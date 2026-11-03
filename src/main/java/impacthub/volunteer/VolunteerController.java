package impacthub.volunteer;

import impacthub.event.Event;
import impacthub.people.Person;
import impacthub.people.PersonService;
import impacthub.people.Skill;
import impacthub.people.Volunteer;
import impacthub.persistence.GenericDAO;

import java.util.stream.Collectors;

/** Handles the Volunteers window's actions. */
public class VolunteerController {
    private final VolunteerView view;
    private final VolunteerService volunteerService;
    private final PersonService personService;
    private final GenericDAO<Event> events = new GenericDAO<>(Event.class);
    private final GenericDAO<Assignment> assignments = new GenericDAO<>(Assignment.class);

    public VolunteerController(VolunteerView view, VolunteerService volunteerService, PersonService personService) {
        this.view = view;
        this.volunteerService = volunteerService;
        this.personService = personService;
    }

    public void loadVolunteers() {
        view.showMessage("--- volunteers ---");
        for (Person p : personService.searchByName("")) {
            if (p instanceof Volunteer v) {
                String skills = v.getSkills().stream().map(Skill::getName).collect(Collectors.joining(", "));
                view.showMessage("#" + v.getId() + " " + v.getName() + " | skills: " + skills
                        + " | available: " + String.join(", ", v.getAvailability())
                        + " | prefers: " + String.join(", ", v.getPreferences())
                        + " | " + v.getTotalHours() + " h");
            }
        }
    }

    public void assign(int volunteerId, int eventId, String role) {
        Event e = events.findById(eventId);
        if (!(personService.findById(volunteerId) instanceof Volunteer v) || e == null) {
            view.showMessage("Check the volunteer id and event id.");
            return;
        }
        try {
            Assignment a = volunteerService.assign(v, e, role);
            view.showMessage("Assignment #" + a.getId() + ": " + v.getName() + " is " + role + " at " + e.getName() + ".");
            view.showAssignments(volunteerService.getAssignmentsByEvent(e));
        } catch (IllegalArgumentException ex) {
            view.showMessage(ex.getMessage());
        }
    }

    public void logHours(int assignmentId, double hours) {
        Assignment a = assignments.findById(assignmentId);
        if (a == null) {
            view.showMessage("No assignment with id " + assignmentId + ".");
            return;
        }
        try {
            volunteerService.logHours(a, hours);
            view.showMessage("Logged " + hours + " h for " + a.getVolunteer().getName() + ".");
        } catch (IllegalArgumentException ex) {
            view.showMessage(ex.getMessage());
        }
    }

    public void issueCertificate(int volunteerId) {
        if (!(personService.findById(volunteerId) instanceof Volunteer v)) {
            view.showMessage("No volunteer with id " + volunteerId + ".");
            return;
        }
        view.showCertificate(volunteerService.generateCertificate(v).render());
    }
}
