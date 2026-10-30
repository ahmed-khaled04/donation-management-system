package impacthub.donation.receipt;

/** Adds a thank-you note to a receipt. */
public class ThankYouNoteDecorator extends ReceiptDecorator {
    public ThankYouNoteDecorator(Receipt wrapped) {
        super(wrapped);
    }

    @Override
    public String render() {
        return super.render() + "\nThank you for supporting our cause!";
    }
}
