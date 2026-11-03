package impacthub.dashboard;

import impacthub.View;
import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;
import impacthub.people.Person;
import impacthub.people.Session;

import java.util.List;
import java.util.function.Supplier;

/** Decides what the dashboard shows and guards the services: nothing opens until someone is logged in. */
public class DashboardController implements Observer {
    private final DashboardView view;
    private final Session session;
    private final ActivityLogService activityLog;
    private boolean wasLoggedIn;

    public DashboardController(DashboardView view, Session session, ActivityLogService activityLog) {
        this.view = view;
        this.session = session;
        this.activityLog = activityLog;
        // logging in and out are tracked actions too, so this one subscription covers every change
        activityLog.addObserver(this);
    }

    @Override
    public void update(Subject source, ChangeEvent change) {
        refresh();
    }

    public void refresh() {
        boolean loggedIn = session.isLoggedIn();
        if (loggedIn != wasLoggedIn) {
            // Windows opened by the previous state must not stay usable (for example after logging out).
            view.closeOtherWindows();
            wasLoggedIn = loggedIn;
        }
        Person user = session.getCurrentUser();
        view.showAccount(user);
        view.showActivity(loggedIn ? activityLog.getRecent() : List.of());
    }

    public void openService(Supplier<View> service) {
        if (!session.isLoggedIn()) {
            view.showMessage("Please log in or sign up first.");
            return;
        }
        view.showMessage("");
        service.get().show();
    }

    public void openAuth(Runnable openWindow) {
        if (session.isLoggedIn()) {
            view.showMessage("You are already logged in. Log out first to switch account.");
            return;
        }
        view.showMessage("");
        openWindow.run();
    }

    public void logout() {
        session.logout();
        view.showMessage("You have been logged out.");
    }
}
