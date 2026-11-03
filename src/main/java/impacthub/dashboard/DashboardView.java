package impacthub.dashboard;

import impacthub.View;
import impacthub.people.Person;
import impacthub.people.PersonView;
import impacthub.people.Session;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** The window the app opens on: account buttons, one button per service and the last 10 actions. */
public class DashboardView implements View {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String HEADING = "-fx-font-weight: bold;";

    private final Stage stage;
    private final DashboardController controller;
    private final Supplier<PersonView> accountWindows;
    private final Map<String, Supplier<View>> services;

    private final Label account = new Label();
    private final Label message = new Label();
    private final Button login = new Button("Log in");
    private final Button signup = new Button("Sign up");
    private final Button logout = new Button("Log out");
    private final TilePane serviceButtons = new TilePane(8, 8);
    private final ListView<String> activity = new ListView<>();

    /** services maps each button's label to the window it opens, in display order. */
    public DashboardView(Stage stage, Session session, ActivityLogService activityLog,
                         Supplier<PersonView> accountWindows, Map<String, Supplier<View>> services) {
        this.stage = stage;
        this.accountWindows = accountWindows;
        this.services = services;
        controller = new DashboardController(this, session, activityLog);
    }

    @Override
    public void show() {
        login.setOnAction(e -> controller.openAuth(() -> accountWindows.get().show()));
        signup.setOnAction(e -> controller.openAuth(() -> {
            PersonView window = accountWindows.get();
            window.show();
            window.showSignupForm();
        }));
        logout.setOnAction(e -> controller.logout());

        services.forEach((label, window) -> {
            Button button = new Button(label);
            button.setPrefWidth(180);
            button.setOnAction(e -> controller.openService(window));
            serviceButtons.getChildren().add(button);
        });
        serviceButtons.setPrefColumns(3);

        Label title = new Label("Impact Hub - NGO Management System");
        title.setStyle("-fx-font-size: 18px;" + HEADING);
        message.setStyle("-fx-text-fill: #b00020;");
        Label activityHeading = heading("Last 10 actions");
        activity.setPrefHeight(270);

        VBox root = new VBox(10, title,
                heading("Account"), account, new HBox(8, login, signup, logout), message,
                heading("Services"), serviceButtons,
                activityHeading, activity,
                new Label("Notifications and receipts are printed in the terminal."));
        root.setPadding(new Insets(16));

        controller.refresh();
        stage.setTitle("Impact Hub - Dashboard");
        stage.setScene(new Scene(root, 800, 640));
        // closing the dashboard closes the whole app, including any other open window
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }

    @Override
    public void showMessage(String msg) {
        message.setText(msg);
    }

    /** user is null when nobody is logged in. */
    public void showAccount(Person user) {
        boolean loggedIn = user != null;
        account.setText(loggedIn
                ? "Logged in as " + user.getName() + " (" + user.getClass().getSimpleName() + ")"
                : "Not logged in. Log in or sign up to use the services.");
        login.setDisable(loggedIn);
        signup.setDisable(loggedIn);
        logout.setDisable(!loggedIn);
        serviceButtons.getChildren().forEach(button -> button.setDisable(!loggedIn));
        activity.setPlaceholder(new Label(loggedIn ? "No actions yet." : "Log in to see the recent actions."));
    }

    public void showActivity(List<ActivityEntry> entries) {
        activity.getItems().setAll(entries.stream()
                .map(e -> e.getTime().format(TIME) + "  |  " + e.getActor() + "  |  " + e.getDescription())
                .toList());
    }

    /** Closes everything except the dashboard. */
    public void closeOtherWindows() {
        for (Window window : new ArrayList<>(Window.getWindows())) {
            if (window != stage) {
                window.hide();
            }
        }
    }

    private static Label heading(String text) {
        Label label = new Label(text);
        label.setStyle(HEADING);
        return label;
    }
}
