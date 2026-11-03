package impacthub.people;

import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;

import java.util.ArrayList;
import java.util.List;

/** Who is logged in right now. Nobody is logged in when the app starts. */
public class Session implements Subject {
    private final List<Observer> observers = new ArrayList<>();
    private Person currentUser;

    public void login(Person person) {
        currentUser = person;
        notifyObservers(new ChangeEvent("USER_LOGGED_IN", person, "Logged in"));
    }

    public void logout() {
        if (currentUser == null) {
            return;
        }
        Person person = currentUser;
        currentUser = null;
        notifyObservers(new ChangeEvent("USER_LOGGED_OUT", person, "Logged out"));
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public Person getCurrentUser() {
        return currentUser;
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
