package impacthub.people;

import impacthub.View;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * People window. Logged out it only offers logging in and signing up; logged in it offers search and delete.
 * The dashboard closes it whenever someone logs in or out, so it never shows the wrong one.
 */
public class PersonView implements View {
    private final PersonController controller;
    private final VBox form = new VBox(6);
    private final TextArea output = new TextArea();

    public PersonView(PersonService personService, Session session) {
        controller = new PersonController(this, personService, session);
    }

    @Override
    public void show() {
        output.setEditable(false);
        output.setPrefRowCount(6);
        boolean loggedIn = controller.isLoggedIn();
        VBox root = loggedIn ? peopleScreen() : accountScreen();
        root.setStyle("-fx-padding: 10;");

        Stage stage = new Stage();
        stage.setTitle(loggedIn ? "People" : "Log in / Sign up");
        stage.setScene(new Scene(root, 520, 460));
        stage.show();
    }

    @Override
    public void showMessage(String msg) {
        output.appendText(msg + "\n");
    }

    public void showLoginForm() {
        TextField email = new TextField();
        email.setPromptText("email");
        PasswordField password = new PasswordField();
        password.setPromptText("password");
        Button go = new Button("Log in");
        go.setOnAction(e -> controller.login(email.getText(), password.getText()));
        form.getChildren().setAll(new Label("Log in"), email, password, go);
    }

    public void showSignupForm() {
        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("Donor", "Volunteer", "Beneficiary");
        type.setValue("Donor");
        TextField name = new TextField();
        name.setPromptText("name");
        TextField email = new TextField();
        email.setPromptText("email");
        TextField phone = new TextField();
        phone.setPromptText("phone (optional)");
        PasswordField password = new PasswordField();
        password.setPromptText("password (at least 4 characters)");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("confirm password");
        Button go = new Button("Sign up");
        go.setOnAction(e -> {
            if (password.getText().equals(confirm.getText())) {
                controller.signup(type.getValue(), name.getText(), email.getText(), phone.getText(), password.getText());
            } else {
                showMessage("The passwords do not match.");
            }
        });
        form.getChildren().setAll(new Label("Sign up"), type, name, email, phone, password, confirm, go);
    }

    public void showProfile(Person p) {
        showMessage("#" + p.getId() + " " + p.getName() + " (" + p.getClass().getSimpleName() + ")"
                + " | " + p.getEmail() + " | " + p.getPhone() + " | contact by " + p.getPreferredChannel());
    }

    private VBox accountScreen() {
        Button login = new Button("Log in form");
        login.setOnAction(e -> showLoginForm());
        Button signup = new Button("Sign up form");
        signup.setOnAction(e -> showSignupForm());
        showLoginForm();
        return new VBox(8, new HBox(8, login, signup), form, output);
    }

    private VBox peopleScreen() {
        TextField searchName = new TextField();
        searchName.setPromptText("name (empty = everyone)");
        Button search = new Button("Search");
        search.setOnAction(e -> controller.search(searchName.getText()));

        TextField deleteId = new TextField();
        deleteId.setPromptText("person id");
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            try {
                controller.deletePerson(Integer.parseInt(deleteId.getText().trim()));
            } catch (NumberFormatException ex) {
                showMessage("Please enter a number for the id.");
            }
        });
        return new VBox(8, new Label("Search"), new HBox(8, searchName, search),
                new Label("Delete"), new HBox(8, deleteId, delete), output);
    }
}
