package impacthub.people;

/** Handles the People window's actions. */
public class PersonController {
    private final PersonView view;
    private final PersonService personService;
    private final Session session;

    public PersonController(PersonView view, PersonService personService, Session session) {
        this.view = view;
        this.personService = personService;
        this.session = session;
    }

    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

    /** Signing up also logs the new person in. */
    public void signup(String type, String name, String email, String phone, String password) {
        try {
            session.login(personService.register(type, name, email, phone, password));
        } catch (IllegalArgumentException e) {
            view.showMessage(e.getMessage());
        }
    }

    public void login(String email, String password) {
        if (email.isBlank() || password.isEmpty()) {
            view.showMessage("Please enter your e-mail and password.");
            return;
        }
        Person p = personService.authenticate(email.trim(), password);
        if (p == null) {
            view.showMessage("Wrong email or password.");
            return;
        }
        session.login(p);
    }

    public void search(String name) {
        var found = personService.searchByName(name);
        view.showMessage(found.size() + " person(s) found:");
        found.forEach(view::showProfile);
    }

    public void deletePerson(int id) {
        if (session.isLoggedIn() && session.getCurrentUser().getId() == id) {
            view.showMessage("You cannot delete your own account while you are logged in.");
            return;
        }
        view.showMessage(personService.delete(id) ? "Deleted person #" + id + "." : "No person with id " + id + ".");
    }
}
