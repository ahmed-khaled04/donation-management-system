package impacthub.donation;

import impacthub.View;
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

import java.util.ArrayList;
import java.util.List;

/** Donations window: record a donation, list donations and print receipts. */
public class DonationView implements View {
    private final DonationController controller;
    private final TextArea output = new TextArea();

    public DonationView(DonationService donationService, PersonService personService) {
        controller = new DonationController(this, donationService, personService);
    }

    @Override
    public void show() {
        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("Online", "Check", "InKind");
        type.setValue("Online");
        TextField donorId = new TextField();
        donorId.setPromptText("donor id");
        TextField amount = new TextField();
        amount.setPromptText("amount");
        Button submit = new Button("Record donation");
        submit.setOnAction(e -> {
            try {
                controller.submitDonation(type.getValue(), Integer.parseInt(donorId.getText().trim()),
                        Double.parseDouble(amount.getText().trim()));
            } catch (NumberFormatException ex) {
                showMessage("Please enter numbers for the donor id and amount.");
            }
        });

        Button load = new Button("List all donations");
        load.setOnAction(e -> controller.loadDonations());

        TextField donationId = new TextField();
        donationId.setPromptText("donation id");
        CheckBox tax = new CheckBox("Tax information");
        CheckBox thanks = new CheckBox("Thank-you note");
        Button receipt = new Button("Generate receipt");
        receipt.setOnAction(e -> {
            List<String> options = new ArrayList<>();
            if (tax.isSelected()) {
                options.add("TAX");
            }
            if (thanks.isSelected()) {
                options.add("THANK_YOU");
            }
            try {
                controller.generateReceipt(Integer.parseInt(donationId.getText().trim()), options);
            } catch (NumberFormatException ex) {
                showMessage("Please enter a number for the donation id.");
            }
        });

        output.setEditable(false);
        output.setPrefRowCount(14);
        VBox root = new VBox(8,
                new Label("New donation"), new HBox(8, type, donorId, amount, submit),
                load,
                new Label("Receipt"), new HBox(8, donationId, tax, thanks), receipt,
                output);
        root.setStyle("-fx-padding: 10;");

        Stage stage = new Stage();
        stage.setTitle("Donations");
        stage.setScene(new Scene(root, 620, 560));
        stage.show();
    }

    @Override
    public void showMessage(String msg) {
        output.appendText(msg + "\n");
    }

    public void showDonationList(List<Donation> list) {
        showMessage("--- " + list.size() + " donation(s) ---");
        for (Donation d : list) {
            showMessage("#" + d.getId() + " " + d.getType() + " " + d.getAmount() + " from " + d.getDonor().getName()
                    + " on " + d.getDate().toLocalDate());
        }
    }

    public void showReceipt(String text) {
        showMessage(text);
    }
}
