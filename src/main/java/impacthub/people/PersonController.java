package impacthub.people;

/** Handles the People window's actions. */
public class PersonController {
    private final PersonView view;
    private final PersonService personService;

    public PersonController(PersonView view, PersonService personService) {
        this.view = view;
        this.personService = personService;
    }

    public void signup(String type, String name, String email, String phone, String password) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            view.showMessage("Name, email and password are required.");
            return;
        }
        Person p = personService.register(type, name, email, phone, password);
        view.showMessage("Signed up:");
        view.showProfile(p);
    }

    public void login(String email, String password) {
        Person p = personService.authenticate(email, password);
        if (p == null) {
            view.showMessage("Wrong email or password.");
            return;
        }
        view.showMessage("Welcome, " + p.getName() + "!");
        view.showProfile(p);
    }

    public void search(String name) {
        var found = personService.searchByName(name);
        view.showMessage(found.size() + " person(s) found:");
        found.forEach(view::showProfile);
    }

    public void deletePerson(int id) {
        view.showMessage(personService.delete(id) ? "Deleted person #" + id + "." : "No person with id " + id + ".");
    }
}
