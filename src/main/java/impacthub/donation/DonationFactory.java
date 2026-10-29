package impacthub.donation;

import impacthub.people.Donor;

/** Factory: creates the right kind of donation from the type the user picked. */
public class DonationFactory {
    public static Donation create(String type, Donor donor, double amount) {
        return switch (type) {
            case "Online" -> new OnlineDonation(donor, amount);
            case "Check" -> new CheckDonation(donor, amount);
            case "InKind" -> new InKindDonation(donor, amount);
            default -> throw new IllegalArgumentException("Unknown donation type: " + type);
        };
    }
}
