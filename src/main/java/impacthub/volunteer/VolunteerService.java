package impacthub.volunteer;

import impacthub.event.Event;
import impacthub.people.Person;
import impacthub.people.Skill;
import impacthub.people.Volunteer;
import impacthub.persistence.GenericDAO;

import java.util.List;

/** Assigns volunteers to events, tracks their hours and issues certificates. */
public class VolunteerService {
    private final GenericDAO<Assignment> assignments = new GenericDAO<>(Assignment.class);
    private final GenericDAO<Person> people = new GenericDAO<>(Person.class);

    public Assignment assign(Volunteer v, Event e, String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Please enter a role.");
        }
        boolean alreadyAssigned = getAssignmentsByEvent(e).stream()
                .anyMatch(existing -> existing.getVolunteer().getId() == v.getId());
        if (alreadyAssigned) {
            throw new IllegalArgumentException(v.getName() + " is already assigned to " + e.getName() + ".");
        }
        Assignment a = new Assignment(v, e, role);
        assignments.insert(a);
        return a;
    }

    public void logHours(Assignment a, double hours) {
        if (!Double.isFinite(hours) || hours <= 0) {
            throw new IllegalArgumentException("Hours must be more than 0.");
        }
        // check the volunteer first, so nothing is half saved if they were deleted
        if (!(people.findById(a.getVolunteer().getId()) instanceof Volunteer v)) {
            throw new IllegalArgumentException(a.getVolunteer().getName() + " no longer exists, so hours cannot be logged.");
        }
        a.setHoursWorked(a.getHoursWorked() + hours);
        assignments.update(a);
        // add the hours to the saved volunteer too
        v.setTotalHours(v.getTotalHours() + hours);
        people.update(v);
    }

    public List<Volunteer> findBySkill(Skill skill) {
        return people.findAll().stream()
                .filter(p -> p instanceof Volunteer)
                .map(p -> (Volunteer) p)
                .filter(v -> v.getSkills().stream().anyMatch(s -> s.getId() == skill.getId()))
                .toList();
    }

    public List<Assignment> getAssignmentsByEvent(Event e) {
        return assignments.findAll().stream()
                .filter(a -> a.getEvent().getId() == e.getId())
                .toList();
    }

    public VolunteerCertificate generateCertificate(Volunteer v) {
        return new VolunteerCertificate(v);
    }
}
