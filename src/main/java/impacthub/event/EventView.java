package impacthub.event;

import impacthub.View;
import impacthub.event.ticket.Ticket;
import impacthub.people.PersonService;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Events window: create events, list them, change dates, register people and sell tickets. */
public class EventView implements View {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EventController controller;
    private final TextArea output = new TextArea();

    public EventView(EventService eventService, PersonService personService) {
        controller = new EventController(this, eventService, personService);
    }

    @Override
    public void show() {
        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("Fundraiser", "Workshop", "Outreach");
        type.setValue("Fundraiser");
        TextField name = new TextField();
        name.setPromptText("name");
        TextField date = new TextField();
        date.setPromptText("yyyy-MM-dd HH:mm");
        TextField venue = new TextField();
        venue.setPromptText("venue");
        Button create = new Button("Create event");
        create.setOnAction(e -> {
            try {
                controller.createEvent(type.getValue(), name.getText().trim(),
                        LocalDateTime.parse(date.getText().trim(), DATE), venue.getText().trim());
            } catch (DateTimeParseException ex) {
                showMessage("Please write the date as yyyy-MM-dd HH:mm, e.g. 2026-12-01 18:00.");
            }
        });

        Button load = new Button("List all events");
        load.setOnAction(e -> controller.loadEvents());

        TextField eventId = new TextField();
        eventId.setPromptText("event id");
        TextField newDate = new TextField();
        newDate.setPromptText("new date yyyy-MM-dd HH:mm");
        Button change = new Button("Change date");
        change.setOnAction(e -> {
            try {
                controller.changeDate(Integer.parseInt(eventId.getText().trim()),
                        LocalDateTime.parse(newDate.getText().trim(), DATE));
            } catch (NumberFormatException | DateTimeParseException ex) {
                showMessage("Please enter an event id and a date as yyyy-MM-dd HH:mm.");
            }
        });

        TextField personId = new TextField();
        personId.setPromptText("person id");
        Button register = new Button("Register");
        register.setOnAction(e -> {
            try {
                controller.registerAttendee(Integer.parseInt(eventId.getText().trim()),
                        Integer.parseInt(personId.getText().trim()));
            } catch (NumberFormatException ex) {
                showMessage("Please enter numbers for the event id and person id.");
            }
        });
        CheckBox vip = new CheckBox("VIP seating");
        CheckBox meal = new CheckBox("Dinner");
        Button sell = new Button("Sell ticket");
        sell.setOnAction(e -> {
            List<String> extras = new ArrayList<>();
            if (vip.isSelected()) {
                extras.add("VIP");
            }
            if (meal.isSelected()) {
                extras.add("MEAL");
            }
            try {
                controller.sellTicket(Integer.parseInt(eventId.getText().trim()),
                        Integer.parseInt(personId.getText().trim()), extras);
            } catch (NumberFormatException ex) {
                showMessage("Please enter numbers for the event id and person id.");
            }
        });

        Button listTickets = new Button("List tickets");
        listTickets.setOnAction(e -> {
            try {
                controller.loadTickets(Integer.parseInt(eventId.getText().trim()));
            } catch (NumberFormatException ex) {
                showMessage("Please enter a number for the event id.");
            }
        });

        output.setEditable(false);
        output.setPrefRowCount(12);
        VBox root = new VBox(8,
                new Label("New event"), new HBox(8, type, name), new HBox(8, date, venue, create),
                load,
                new Label("Existing event (use the event id)"), new HBox(8, eventId, newDate, change),
                new HBox(8, personId, register), new HBox(8, vip, meal, sell, listTickets),
                output);
        root.setStyle("-fx-padding: 10;");

        Stage stage = new Stage();
        stage.setTitle("Events");
        stage.setScene(new Scene(root, 620, 600));
        stage.show();
    }

    @Override
    public void showMessage(String msg) {
        output.appendText(msg + "\n");
    }

    public void showEventList(List<Event> list) {
        showMessage("--- " + list.size() + " event(s) ---");
        for (Event e : list) {
            showMessage("#" + e.getId() + " " + e.getType() + " " + e.getName() + " | " + e.getDate().format(DATE)
                    + " | " + e.getVenue() + " | " + e.getStatus() + " | " + e.getAttendees().size() + " registered");
        }
    }

    public void showTicket(Ticket ticket) {
        showMessage("Ticket #" + ticket.getId() + ": " + ticket.getDescription() + " | price " + ticket.getPrice());
    }

    public void showTicketList(List<Ticket> list) {
        showMessage("--- " + list.size() + " ticket(s) for this event ---");
        list.forEach(this::showTicket);
    }
}
