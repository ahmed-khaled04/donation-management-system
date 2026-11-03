package impacthub.donation;

import impacthub.TestDb;
import impacthub.observer.StatsObserver;
import impacthub.people.Donor;
import impacthub.people.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DonationServiceTest {
    private PersonService people;
    private DonationService service;
    private Donor donor;

    @BeforeEach
    void setUp() {
        TestDb.clear();
        people = new PersonService();
        service = new DonationService();
        donor = (Donor) people.register("Donor", "Sara", "sara@mail.com", "01000000000", "pass");
    }

    @Test
    void factoryCreatesEachType() {
        assertInstanceOf(OnlineDonation.class, DonationFactory.create("Online", donor, 1));
        assertInstanceOf(CheckDonation.class, DonationFactory.create("Check", donor, 1));
        assertInstanceOf(InKindDonation.class, DonationFactory.create("InKind", donor, 1));
        assertThrows(IllegalArgumentException.class, () -> DonationFactory.create("Bitcoin", donor, 1));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -5, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsAmountsThatAreNotPositiveFiniteNumbers(double amount) {
        assertThrows(IllegalArgumentException.class, () -> service.recordDonation("Online", donor, amount));
        assertTrue(service.getAllDonations().isEmpty());
    }

    @Test
    void rejectsAmountsAboveTheLimitSoTotalsCannotOverflow() {
        assertDoesNotThrow(() -> service.recordDonation("Online", donor, 1_000_000_000));
        assertThrows(IllegalArgumentException.class, () -> service.recordDonation("Online", donor, 1_000_000_001));
        assertThrows(IllegalArgumentException.class, () -> service.recordDonation("Online", donor, 1e308));
    }

    @Test
    void savesTheDonorsNewTotal() {
        service.addObserver(new StatsObserver());
        service.recordDonation("Online", donor, 500);
        service.recordDonation("Check", donor, 50);
        assertEquals(550, ((Donor) people.findById(donor.getId())).getTotalDonated());
    }

    @Test
    void deletingADonorKeepsTheirDonationsAndReceipts() {
        Donation donation = service.recordDonation("Online", donor, 500);
        people.delete(donor.getId());

        List<Donation> all = service.getAllDonations();
        assertEquals(1, all.size());
        assertEquals(donation.getId(), all.get(0).getId());
        assertTrue(service.buildReceipt(all.get(0), List.of()).render().contains("from Sara"));
    }

    @Test
    void listsOnlyTheDonationsOfTheGivenDonor() {
        Donor other = (Donor) people.register("Donor", "Omar", "omar@mail.com", "01000000000", "pass");
        service.recordDonation("Online", donor, 10);
        service.recordDonation("Online", other, 20);
        assertEquals(1, service.getDonationsByDonor(other).size());
        assertEquals(2, service.getAllDonations().size());
    }

    @Test
    void receiptAddsOnlyTheChosenSections() {
        Donation d = service.recordDonation("Online", donor, 500);
        String plain = service.buildReceipt(d, List.of()).render();
        String tax = service.buildReceipt(d, List.of("TAX")).render();
        String both = service.buildReceipt(d, List.of("TAX", "THANK_YOU")).render();

        assertFalse(plain.contains("Tax info") || plain.contains("Thank you"));
        assertTrue(tax.contains("Tax info") && !tax.contains("Thank you"));
        assertTrue(both.startsWith(plain) && both.contains("Tax info") && both.contains("Thank you"));
    }

    @Test
    void taxStatementTotalsTheChosenYearOnly() {
        service.recordDonation("Online", donor, 100);
        service.recordDonation("Check", donor, 50);
        int year = LocalDate.now().getYear();
        assertTrue(service.generateTaxStatement(donor, year).endsWith("total 150.0"));
        assertTrue(service.generateTaxStatement(donor, year - 1).endsWith("total 0.0"));
    }
}
