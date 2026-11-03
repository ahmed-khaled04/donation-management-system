package impacthub.dashboard;

import impacthub.TestDb;
import impacthub.View;
import impacthub.people.Person;
import impacthub.people.PersonController;
import impacthub.people.PersonService;
import impacthub.people.PersonView;
import impacthub.people.Session;
import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** The login rules, through the real controllers. Skipped on a machine with no display. */
class DashboardControllerTest {
    private static class CapturingDashboard extends DashboardView {
        String message = "";
        Person account;
        List<ActivityEntry> activity = List.of();
        int closedWindows;

        CapturingDashboard(Session session, ActivityLogService log) {
            super(null, session, log, () -> null, Map.of());
        }

        @Override
        public void showMessage(String msg) {
            message = msg;
        }

        @Override
        public void showAccount(Person user) {
            account = user;
        }

        @Override
        public void showActivity(List<ActivityEntry> entries) {
            activity = entries;
        }

        @Override
        public void closeOtherWindows() {
            closedWindows++;
        }
    }

    private static class CapturingPeople extends PersonView {
        final List<String> messages = new ArrayList<>();

        CapturingPeople(PersonService people, Session session) {
            super(people, session);
        }

        @Override
        public void showMessage(String msg) {
            messages.add(msg);
        }
    }

    private PersonService people;
    private Session session;
    private CapturingDashboard dashboard;
    private DashboardController controller;
    private CapturingPeople authView;
    private PersonController auth;
    private int opened;
    private final View service = new View() {
        @Override
        public void show() {
            opened++;
        }

        @Override
        public void showMessage(String msg) {
        }
    };

    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyStarted) {
            // fine: another test started it
        } catch (RuntimeException | LinkageError noDisplay) {
            Assumptions.abort("JavaFX cannot start here: " + noDisplay);
        }
    }

    @BeforeEach
    void setUp() {
        TestDb.clear();
        opened = 0;
        people = new PersonService();
        session = new Session();
        ActivityLogService log = new ActivityLogService(session);
        session.addObserver(log);
        people.addObserver(log);
        dashboard = new CapturingDashboard(session, log);
        controller = new DashboardController(dashboard, session, log);
        authView = new CapturingPeople(people, session);
        auth = new PersonController(authView, people, session);
        people.register("Donor", "Sara", "sara@mail.com", "01000000000", "1234");
    }

    @Test
    void servicesDoNotOpenUntilLoggedIn() {
        controller.openService(() -> service);
        assertEquals(0, opened);
        assertEquals("Please log in or sign up first.", dashboard.message);

        auth.login("sara@mail.com", "1234");
        controller.openService(() -> service);
        assertEquals(1, opened);
    }

    @Test
    void servicesDoNotOpenAfterLoggingOut() {
        auth.login("sara@mail.com", "1234");
        controller.logout();
        controller.openService(() -> service);
        assertEquals(0, opened);
        assertNull(dashboard.account);
    }

    @Test
    void activityIsHiddenUntilLoggedIn() {
        controller.refresh();
        assertTrue(dashboard.activity.isEmpty());
        auth.login("sara@mail.com", "1234");
        assertEquals("Logged in", dashboard.activity.get(0).getDescription());
        controller.logout();
        assertTrue(dashboard.activity.isEmpty());
    }

    @Test
    void otherWindowsCloseWhenTheLoginStateChanges() {
        auth.login("sara@mail.com", "1234");
        int afterLogin = dashboard.closedWindows;
        assertTrue(afterLogin > 0);
        controller.logout();
        assertTrue(dashboard.closedWindows > afterLogin);
    }

    @Test
    void loginFormCannotOpenWhileLoggedIn() {
        auth.login("sara@mail.com", "1234");
        boolean[] ran = {false};
        controller.openAuth(() -> ran[0] = true);
        assertFalse(ran[0]);
    }

    @Test
    void wrongOrMissingCredentialsDoNotLogIn() {
        auth.login("sara@mail.com", "wrong");
        auth.login("", "");
        auth.login("sara@mail.com", "");
        assertFalse(session.isLoggedIn());
        assertEquals(List.of("Wrong email or password.", "Please enter your e-mail and password.",
                "Please enter your e-mail and password."), authView.messages);
    }

    @Test
    void signingUpLogsTheNewPersonIn() {
        auth.signup("Volunteer", "Ali", "ali@mail.com", "", "1234");
        assertTrue(session.isLoggedIn());
        assertEquals("Ali", session.getCurrentUser().getName());
    }

    @Test
    void signupProblemsAreShownAndDoNotLogIn() {
        auth.signup("Donor", "", "ali@mail.com", "", "1234");
        auth.signup("Donor", "Ali", "not-an-email", "", "1234");
        auth.signup("Donor", "Ali", "ali@mail.com", "12", "1234");
        auth.signup("Donor", "Ali", "ali@mail.com", "", "123");
        auth.signup("Donor", "Ali", "SARA@mail.com", "", "1234");
        assertFalse(session.isLoggedIn());
        assertEquals(5, authView.messages.size());
        assertEquals("The name is required.", authView.messages.get(0));
    }

    @Test
    void youCannotDeleteYourOwnAccountWhileLoggedIn() {
        auth.login("sara@mail.com", "1234");
        CapturingPeople view = new CapturingPeople(people, session);
        int ownId = session.getCurrentUser().getId();
        new PersonController(view, people, session).deletePerson(ownId);
        assertNotNull(people.findById(ownId));
        assertEquals("You cannot delete your own account while you are logged in.", view.messages.get(0));
    }
}
