package impacthub.volunteer;

import impacthub.View;
import impacthub.people.PersonService;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/** Volunteers window: list volunteers, assign them, log hours and issue certificates. */
public class VolunteerView implements View {
    private final VolunteerController controller;
    private final TextArea output = new TextArea();

    public VolunteerView(VolunteerService volunteerService, PersonService personService) {
        controller = new VolunteerController(this, volunteerService, personService);
    }

    @Override
    public void show() {
        Button load = new Button("List volunteers");
        load.setOnAction(e -> controller.loadVolunteers());

        TextField volunteerId = new TextField();
        volunteerId.setPromptText("volunteer id");
        TextField eventId = new TextField();
        eventId.setPromptText("event id");
        TextField role = new TextField();
        role.setPromptText("role");
        Button assign = new Button("Assign");
        assign.setOnAction(e -> {
            try {
                controller.assign(Integer.parseInt(volunteerId.getText().trim()),
                        Integer.parseInt(eventId.getText().trim()), role.getText().trim());
            } catch (NumberFormatException ex) {
                showMessage("Please enter numbers for the volunteer id and event id.");
            }
        });

        TextField assignmentId = new TextField();
        assignmentId.setPromptText("assignment id");
        TextField hours = new TextField();
        hours.setPromptText("hours");
        Button log = new Button("Log hours");
        log.setOnAction(e -> {
            try {
                controller.logHours(Integer.parseInt(assignmentId.getText().trim()),
                        Double.parseDouble(hours.getText().trim()));
            } catch (NumberFormatException ex) {
                showMessage("Please enter numbers for the assignment id and hours.");
            }
        });

        Button certificate = new Button("Issue certificate (uses volunteer id)");
        certificate.setOnAction(e -> {
            try {
                controller.issueCertificate(Integer.parseInt(volunteerId.getText().trim()));
            } catch (NumberFormatException ex) {
                showMessage("Please enter a number for the volunteer id.");
            }
        });

        output.setEditable(false);
        output.setPrefRowCount(14);
        VBox root = new VBox(8, load,
                new Label("Assign to an event"), new HBox(8, volunteerId, eventId, role, assign),
                new Label("Hours"), new HBox(8, assignmentId, hours, log),
                certificate, output);
        root.setStyle("-fx-padding: 10;");

        Stage stage = new Stage();
        stage.setTitle("Volunteers");
        stage.setScene(new Scene(root, 620, 520));
        stage.show();
    }

    @Override
    public void showMessage(String msg) {
        output.appendText(msg + "\n");
    }

    public void showAssignments(List<Assignment> list) {
        showMessage("--- " + list.size() + " assignment(s) at this event ---");
        for (Assignment a : list) {
            showMessage("#" + a.getId() + " " + a.getVolunteer().getName() + " | " + a.getRole() + " | "
                    + a.getHoursWorked() + " h");
        }
    }

    public void showCertificate(String text) {
        showMessage(text);
    }
}
