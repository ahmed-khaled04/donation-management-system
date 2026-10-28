package impacthub.people;

import impacthub.notification.Channel;
import impacthub.persistence.GenericDAO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/** Sign-up, login, search, update and delete for all people. */
public class PersonService {
    private final GenericDAO<Person> people = new GenericDAO<>(Person.class);

    public Person register(String type, String name, String email, String phone, String password) {
        Person p = switch (type) {
            case "Donor" -> new Donor(name, email, phone, Channel.EMAIL);
            case "Volunteer" -> new Volunteer(name, email, phone, Channel.EMAIL);
            case "Beneficiary" -> new Beneficiary(name, email, phone, Channel.EMAIL);
            default -> throw new IllegalArgumentException("Unknown person type: " + type);
        };
        p.setPasswordHash(hash(password));
        people.insert(p);
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

    public void update(Person p) {
        people.update(p);
    }

    public boolean delete(int id) {
        return people.delete(id);
    }

    private static String hash(String password) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
