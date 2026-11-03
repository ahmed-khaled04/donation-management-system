package impacthub.dashboard;

import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;
import impacthub.people.Person;
import impacthub.people.Session;
import impacthub.persistence.GenericDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Observes every service and saves each action they announce, then tells the dashboard.
 * Changes with no description (nothing a user did) are not tracked.
 */
public class ActivityLogService implements Observer, Subject {
    private static final int RECENT_LIMIT = 10;

    private final List<Observer> observers = new ArrayList<>();
    private final GenericDAO<ActivityEntry> entries = new GenericDAO<>(ActivityEntry.class);
    private final Session session;

    public ActivityLogService(Session session) {
        this.session = session;
    }

    @Override
    public void update(Subject source, ChangeEvent change) {
        if (change.getDescription().isEmpty()) {
            return;
        }
        entries.insert(new ActivityEntry(change.getTimestamp(), actorOf(change), change.getDescription()));
        notifyObservers(new ChangeEvent("ACTIVITY_RECORDED", change));
    }

    /** The last 10 actions, newest first. */
    public List<ActivityEntry> getRecent() {
        List<ActivityEntry> all = entries.findAll();
        return all.subList(Math.max(0, all.size() - RECENT_LIMIT), all.size()).reversed();
    }

    // Signing up and logging out happen with nobody logged in, so the person is taken from the change itself.
    private String actorOf(ChangeEvent change) {
        if (session.isLoggedIn()) {
            return session.getCurrentUser().getName();
        }
        return change.getPayload() instanceof Person person ? person.getName() : "Guest";
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
