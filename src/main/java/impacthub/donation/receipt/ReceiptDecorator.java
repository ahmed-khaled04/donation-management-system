package impacthub.donation.receipt;

/** Decorator: wraps a receipt and adds a section to it. */
public abstract class ReceiptDecorator implements Receipt {
    private final Receipt wrapped;

    protected ReceiptDecorator(Receipt wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String render() {
        return wrapped.render();
    }
}
