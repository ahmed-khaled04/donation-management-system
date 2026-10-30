package impacthub.donation.receipt;

/** Decorator component: anything that can be printed as a receipt. */
public interface Receipt {
    String render();
}
