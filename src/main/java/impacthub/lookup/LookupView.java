package impacthub.lookup;

import impacthub.View;
import impacthub.people.Skill;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/** Lookup tables window: list, create, rename and delete skills and resource categories. */
public class LookupView implements View {
    private final LookupController controller = new LookupController(this);
    private final VBox form = new VBox(6);
    private final TextArea output = new TextArea();

    @Override
    public void show() {
        output.setEditable(false);
        output.setPrefRowCount(14);
        showItemForm();
        VBox root = new VBox(8, form, output);
        root.setStyle("-fx-padding: 10;");

        Stage stage = new Stage();
        stage.setTitle("Lookup Tables");
        stage.setScene(new Scene(root, 520, 520));
        stage.show();
    }

    @Override
    public void showMessage(String msg) {
        output.appendText(msg + "\n");
    }

    public void showTable(List<?> items) {
        showMessage("--- " + items.size() + " row(s) ---");
        for (Object item : items) {
            if (item instanceof Skill s) {
                showMessage("#" + s.getId() + " " + s.getName());
            } else if (item instanceof ResourceCategory c) {
                showMessage("#" + c.getId() + " " + c.getName());
            }
        }
    }

    public void showItemForm() {
        ComboBox<String> table = new ComboBox<>();
        table.getItems().addAll("Skill", "ResourceCategory");
        table.setValue("Skill");
        TextField id = new TextField();
        id.setPromptText("id (for update / delete)");
        TextField name = new TextField();
        name.setPromptText("name (for create / update)");

        Button load = new Button("Load");
        load.setOnAction(e -> controller.loadTable(table.getValue()));
        Button create = new Button("Create");
        create.setOnAction(e -> controller.createItem(table.getValue(), name.getText().trim()));
        Button update = new Button("Update");
        update.setOnAction(e -> {
            try {
                controller.updateItem(table.getValue(), Integer.parseInt(id.getText().trim()), name.getText().trim());
            } catch (NumberFormatException ex) {
                showMessage("Please enter a number for the id.");
            }
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            try {
                controller.deleteItem(table.getValue(), Integer.parseInt(id.getText().trim()));
            } catch (NumberFormatException ex) {
                showMessage("Please enter a number for the id.");
            }
        });
        form.getChildren().setAll(new Label("Table"), table, id, name, new HBox(8, load, create, update, delete));
    }
}
