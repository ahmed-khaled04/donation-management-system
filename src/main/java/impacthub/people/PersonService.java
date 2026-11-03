package impacthub.people;

import impacthub.Validation;
import impacthub.event.Event;
import impacthub.notification.Channel;
import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;
import impacthub.persistence.GenericDAO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** Sign-up, login, search, update and delete for all people. */
public class PersonService implements Subject {
    private static final String EMAIL_FORMAT = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";
    private static final String PHONE_FORMAT = "\\+?[0-9]{7,15}";
    private static final int MIN_PASSWORD_LENGTH = 4;

    private final List<Observer> observers = new ArrayList<>();
    private final GenericDAO<Person> people = new GenericDAO<>(Person.class);
    private final GenericDAO<Event> events = new GenericDAO<>(Event.class);

    public Person register(String type, String name, String email, String phone, String password) {
        String cleanName = Validation.requireText(name, "The name");
        String cleanEmail = Validation.requireText(email, "The e-mail");
        if (!cleanEmail.matches(EMAIL_FORMAT)) {
            throw new IllegalArgumentException("Please enter a valid e-mail, for example name@mail.com.");
        }
        String cleanPhone = phone == null ? "" : phone.trim();
        if (!cleanPhone.isEmpty() && !cleanPhone.matches(PHONE_FORMAT)) {
            throw new IllegalArgumentException("The phone must be 7 to 15 digits, or left empty.");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("The password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        boolean emailTaken = people.findAll().stream().anyMatch(existing -> existing.getEmail().equalsIgnoreCase(cleanEmail));
        if (emailTaken) {
            throw new IllegalArgumentException("The e-mail " + cleanEmail + " is already registered.");
        }
        Person p = switch (type) {
            case "Donor" -> new Donor(cleanName, cleanEmail, cleanPhone, Channel.EMAIL);
            case "Volunteer" -> new Volunteer(cleanName, cleanEmail, cleanPhone, Channel.EMAIL);
            case "Beneficiary" -> new Beneficiary(cleanName, cleanEmail, cleanPhone, Channel.EMAIL);
            default -> throw new IllegalArgumentException("Unknown person type: " + type);
        };
        p.setPasswordHash(hash(password));
        people.insert(p);
        notifyObservers(new ChangeEvent("PERSON_REGISTERED", p, "Signed up as " + type));
        return p;
    }

    public Person authenticate(String email, String password) {
        String hash = hash(password);
        for (Person p : people.findAll()) {
            if (p.getEmail().equalsIgnoreCase(email) && p.getPasswordHash().equals(hash)) {
                return p;
            }
        }
        return null;
    }

    public Person findById(int id) {
        return people.findById(id);
    }

    /** An empty name returns everyone. */
    public List<Person> searchByName(String name) {
        String text = name.toLowerCase();
        return people.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(text))
                .toList();
    }

    /** Saves the person and refreshes the copy each event keeps in its attendee list. */
    public void update(Person p) {
        people.update(p);
        for (Event event : events.findAll()) {
            List<Person> attendees = event.getAttendees();
            for (int i = 0; i < attendees.size(); i++) {
                if (attendees.get(i).getId() == p.getId()) {
                    attendees.set(i, p);
                    events.update(event);
                    break;
                }
            }
        }
    }

    /** Deletes the person and takes them off every event, so they are no longer notified. */
    public boolean delete(int id) {
        Person person = people.findById(id);
        boolean deleted = people.delete(id);
        if (deleted) {
            for (Event event : events.findAll()) {
                if (event.getAttendees().removeIf(attendee -> attendee.getId() == id)) {
                    events.update(event);
                }
            }
            notifyObservers(new ChangeEvent("PERSON_DELETED", person, "Deleted person #" + id + " (" + person.getName() + ")"));
        }
        return deleted;
    }

    private static String hash(String password) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
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
