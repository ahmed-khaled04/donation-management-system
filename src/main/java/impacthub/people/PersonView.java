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

/** People window: sign up, log in, search and delete. */
public class PersonView implements View {
    private final PersonController controller;
    private final VBox form = new VBox(6);
    private final TextArea output = new TextArea();

    public PersonView(PersonService personService) {
        controller = new PersonController(this, personService);
    }

    @Override
    public void show() {
        Button login = new Button("Log in form");
        login.setOnAction(e -> showLoginForm());
        Button signup = new Button("Sign up form");
        signup.setOnAction(e -> showSignupForm());

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

        output.setEditable(false);
        output.setPrefRowCount(14);
        VBox root = new VBox(8, new HBox(8, login, signup), form,
                new Label("Search"), new HBox(8, searchName, search),
                new Label("Delete"), new HBox(8, deleteId, delete), output);
        root.setStyle("-fx-padding: 10;");
        showLoginForm();

        Stage stage = new Stage();
        stage.setTitle("People");
        stage.setScene(new Scene(root, 520, 620));
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
        go.setOnAction(e -> controller.login(email.getText().trim(), password.getText()));
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
        phone.setPromptText("phone");
        PasswordField password = new PasswordField();
        password.setPromptText("password");
        Button go = new Button("Sign up");
        go.setOnAction(e -> controller.signup(type.getValue(), name.getText().trim(),
                email.getText().trim(), phone.getText().trim(), password.getText()));
        form.getChildren().setAll(new Label("Sign up"), type, name, email, phone, password, go);
    }

    public void showProfile(Person p) {
        showMessage("#" + p.getId() + " " + p.getName() + " (" + p.getClass().getSimpleName() + ")"
                + " | " + p.getEmail() + " | " + p.getPhone() + " | contact by " + p.getPreferredChannel());
    }
}
