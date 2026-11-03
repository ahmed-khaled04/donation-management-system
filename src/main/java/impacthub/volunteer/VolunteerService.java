package impacthub.volunteer;

import impacthub.Validation;
import impacthub.event.Event;
import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;
import impacthub.people.Person;
import impacthub.people.Skill;
import impacthub.people.Volunteer;
import impacthub.persistence.GenericDAO;

import java.util.ArrayList;
import java.util.List;

/** Assigns volunteers to events, tracks their hours and issues certificates. */
public class VolunteerService implements Subject {
    private static final double MAX_HOURS_PER_ENTRY = 24;

    private final List<Observer> observers = new ArrayList<>();
    private final GenericDAO<Assignment> assignments = new GenericDAO<>(Assignment.class);
    private final GenericDAO<Person> people = new GenericDAO<>(Person.class);

    public Assignment assign(Volunteer v, Event e, String role) {
        String cleanRole = Validation.requireText(role, "The role");
        boolean alreadyAssigned = getAssignmentsByEvent(e).stream()
                .anyMatch(existing -> existing.getVolunteer().getId() == v.getId());
        if (alreadyAssigned) {
            throw new IllegalArgumentException(v.getName() + " is already assigned to " + e.getName() + ".");
        }
        Assignment a = new Assignment(v, e, cleanRole);
        assignments.insert(a);
        notifyObservers(new ChangeEvent("VOLUNTEER_ASSIGNED", a,
                "Assigned " + v.getName() + " as " + cleanRole + " at " + e.getName()));
        return a;
    }

    public void logHours(Assignment a, double hours) {
        Validation.requirePositive(hours, MAX_HOURS_PER_ENTRY, "Hours");
        // check the volunteer first, so nothing is half saved if they were deleted
        if (!(people.findById(a.getVolunteer().getId()) instanceof Volunteer v)) {
            throw new IllegalArgumentException(a.getVolunteer().getName() + " no longer exists, so hours cannot be logged.");
        }
        a.setHoursWorked(a.getHoursWorked() + hours);
        assignments.update(a);
        // add the hours to the saved volunteer too
        v.setTotalHours(v.getTotalHours() + hours);
        people.update(v);
        notifyObservers(new ChangeEvent("HOURS_LOGGED", a, "Logged " + hours + " h for " + v.getName()));
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
        notifyObservers(new ChangeEvent("CERTIFICATE_ISSUED", v, "Issued a certificate to " + v.getName()));
        return new VolunteerCertificate(v);
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(ChangeEvent change) {
        for (Observer o : observers) {
            o.update(this, change);
        }
    }
}
