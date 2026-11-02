package impacthub.beneficiary;

import impacthub.View;
import impacthub.people.PersonService;
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

/** Beneficiaries window: list beneficiaries and stock, record distributions and evaluate impact. */
public class BeneficiaryView implements View {
    private final BeneficiaryController controller;
    private final TextArea output = new TextArea();

    public BeneficiaryView(DistributionService distributionService, PersonService personService) {
        controller = new BeneficiaryController(this, distributionService, personService);
    }

    @Override
    public void show() {
        Button load = new Button("List beneficiaries and resources");
        load.setOnAction(e -> controller.loadBeneficiaries());

        TextField beneficiaryId = new TextField();
        beneficiaryId.setPromptText("beneficiary id");
        TextField resourceId = new TextField();
        resourceId.setPromptText("resource id");
        TextField quantity = new TextField();
        quantity.setPromptText("quantity");
        Button give = new Button("Record distribution");
        give.setOnAction(e -> {
            try {
                controller.recordDistribution(Integer.parseInt(beneficiaryId.getText().trim()),
                        Integer.parseInt(resourceId.getText().trim()), Integer.parseInt(quantity.getText().trim()));
            } catch (NumberFormatException ex) {
                showMessage("Please enter numbers for the ids and quantity.");
            }
        });

        ComboBox<String> program = new ComboBox<>();
        program.getItems().addAll("Food", "Education");
        program.setValue("Food");
        Button impact = new Button("Evaluate impact");
        impact.setOnAction(e -> controller.showImpact(program.getValue()));

        output.setEditable(false);
        output.setPrefRowCount(14);
        VBox root = new VBox(8, load,
                new Label("Distribution"), new HBox(8, beneficiaryId, resourceId, quantity, give),
                new Label("Program impact"), new HBox(8, program, impact),
                output);
        root.setStyle("-fx-padding: 10;");

        Stage stage = new Stage();
        stage.setTitle("Beneficiaries");
        stage.setScene(new Scene(root, 640, 520));
        stage.show();
    }

    @Override
    public void showMessage(String msg) {
        output.appendText(msg + "\n");
    }

    public void showDistributions(List<Distribution> list) {
        showMessage("--- " + list.size() + " distribution(s) to this beneficiary ---");
        for (Distribution d : list) {
            showMessage("#" + d.getId() + " " + d.getQuantity() + " " + d.getResource().getUnit() + " of "
                    + d.getResource().getName() + " on " + d.getDate().toLocalDate());
        }
    }

    public void showImpact(ImpactReport report) {
        showMessage(report.getProgramName() + " impact score: " + Math.round(report.getScore() * 10) / 10.0
                + " - " + report.getSummary());
    }
}
