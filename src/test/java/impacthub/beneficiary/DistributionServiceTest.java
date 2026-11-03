package impacthub.beneficiary;

import impacthub.TestDb;
import impacthub.lookup.ResourceCategory;
import impacthub.observer.StockObserver;
import impacthub.people.Beneficiary;
import impacthub.people.PersonService;
import impacthub.persistence.GenericDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistributionServiceTest {
    private PersonService people;
    private DistributionService service;
    private GenericDAO<Resource> resources;
    private GenericDAO<ResourceCategory> categories;
    private ResourceCategory food;
    private ResourceCategory education;
    private Resource rice;
    private Resource kit;
    private Beneficiary fatma;
    private Beneficiary hassan;

    @BeforeEach
    void setUp() {
        TestDb.clear();
        people = new PersonService();
        service = new DistributionService();
        service.addObserver(new StockObserver());
        resources = new GenericDAO<>(Resource.class);
        categories = new GenericDAO<>(ResourceCategory.class);
        food = new ResourceCategory("Food");
        education = new ResourceCategory("Education");
        categories.insert(food);
        categories.insert(education);
        rice = new Resource("Rice", "bags", 200, food);
        kit = new Resource("Kit", "kits", 100, education);
        resources.insert(rice);
        resources.insert(kit);
        fatma = beneficiary("Fatma", "fatma@mail.com", 5);
        hassan = beneficiary("Hassan", "hassan@mail.com", 3);
    }

    private Beneficiary beneficiary(String name, String email, int familySize) {
        Beneficiary b = (Beneficiary) people.register("Beneficiary", name, email, "0100", "pw");
        b.setFamilySize(familySize);
        people.update(b);
        return b;
    }

    @Test
    void reducesAndSavesTheStock() {
        service.recordDistribution(fatma, rice, 10);
        assertEquals(190, resources.findById(rice.getId()).getStockQuantity());
    }

    @Test
    void refusesQuantitiesOutsideTheStock() {
        assertThrows(IllegalArgumentException.class, () -> service.recordDistribution(fatma, rice, 0));
        assertThrows(IllegalArgumentException.class, () -> service.recordDistribution(fatma, rice, -1));
        assertThrows(IllegalArgumentException.class, () -> service.recordDistribution(fatma, rice, 201));
        assertEquals(200, resources.findById(rice.getId()).getStockQuantity());
        assertTrue(service.getDistributionsByBeneficiary(fatma).isEmpty());
    }

    @Test
    void canGiveAwayTheWholeStock() {
        service.recordDistribution(fatma, rice, 200);
        assertEquals(0, resources.findById(rice.getId()).getStockQuantity());
    }

    @Test
    void foodImpactCountsEachFamilyOnce() {
        service.recordDistribution(fatma, rice, 1);
        service.recordDistribution(fatma, rice, 1);
        service.recordDistribution(hassan, rice, 1);
        assertEquals(8, service.evaluateImpact("Food").getScore());
    }

    @Test
    void educationImpactIsTheShareThatMetARecordedNeed() {
        hassan.getNeeds().add(new Need(education, "Supplies", 1));
        people.update(hassan);
        service.recordDistribution(hassan, kit, 1);
        service.recordDistribution(fatma, kit, 1);
        assertEquals(50, service.evaluateImpact("Education").getScore());
    }

    @Test
    void aProgramWithNoDistributionsScoresZero() {
        assertEquals(0, service.evaluateImpact("Food").getScore());
        assertEquals(0, service.evaluateImpact("Education").getScore());
    }

    private void renameEducation(String name) {
        education.setName(name);
        categories.update(education);
    }

    @Test
    void programsWithNoImpactMeasureAreRefusedNotScoredWithTheWrongOne() {
        categories.insert(new ResourceCategory("Health"));
        assertThrows(IllegalArgumentException.class, () -> service.evaluateImpact("Health"));
    }

    @Test
    void aRenamedProgramIsRefusedNotScoredAsZero() {
        service.recordDistribution(fatma, kit, 1);
        renameEducation("Schooling");
        assertThrows(IllegalArgumentException.class, () -> service.evaluateImpact("Education"));
        assertThrows(IllegalArgumentException.class, () -> service.evaluateImpact("Schooling"));
    }

    @Test
    void pastDistributionsCountAgainWhenTheNameIsRestored() {
        service.recordDistribution(fatma, kit, 1);
        renameEducation("Schooling");
        renameEducation("Education");
        assertTrue(service.evaluateImpact("Education").getSummary().startsWith("0 of 1"));
    }

    @Test
    void savedCopiesWithOldCategoryNamesStillMatchById() {
        hassan.getNeeds().add(new Need(education, "Supplies", 1));
        people.update(hassan); // the saved need keeps the name "Education"
        renameEducation("Schooling");
        service.recordDistribution(hassan, kit, 1); // the saved distribution keeps the name "Schooling"
        renameEducation("Education");

        assertEquals(100, service.evaluateImpact("Education").getScore());
    }
}
