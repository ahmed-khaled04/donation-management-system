package impacthub.donation.receipt;

/** Adds tax information to a receipt. */
public class TaxInfoDecorator extends ReceiptDecorator {
    public TaxInfoDecorator(Receipt wrapped) {
        super(wrapped);
    }

    @Override
    public String render() {
        return super.render() + "\nTax info: this donation is tax-deductible. Keep this receipt for your tax records.";
    }
}
