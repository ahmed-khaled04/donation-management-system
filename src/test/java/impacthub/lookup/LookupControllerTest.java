package impacthub.lookup;

import impacthub.TestDb;
import impacthub.beneficiary.Need;
import impacthub.beneficiary.Resource;
import impacthub.people.Beneficiary;
import impacthub.people.PersonService;
import impacthub.people.Skill;
import impacthub.people.Volunteer;
import impacthub.persistence.GenericDAO;
import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LookupControllerTest {
    /** The real view, with its messages captured instead of shown. */
    private static class CapturingView extends LookupView {
        final List<String> messages = new ArrayList<>();

        @Override
        public void showMessage(String msg) {
            messages.add(msg);
        }
    }

    private final GenericDAO<Skill> skills = new GenericDAO<>(Skill.class);
    private final GenericDAO<ResourceCategory> categories = new GenericDAO<>(ResourceCategory.class);
    private final GenericDAO<Resource> resources = new GenericDAO<>(Resource.class);
    private PersonService people;
    private CapturingView view;
    private LookupController controller;

    /** The view is a JavaFX class; these tests are skipped on a machine with no display. */
    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyStarted) {
            // fine: another test started it
        } catch (RuntimeException | LinkageError noDisplay) {
            Assumptions.abort("JavaFX cannot start here: " + noDisplay);
        }
    }

    @BeforeEach
    void setUp() {
        TestDb.clear();
        people = new PersonService();
        view = new CapturingView();
        controller = new LookupController(view);
    }

    private Skill skill(String name) {
        Skill s = new Skill(name);
        skills.insert(s);
        return s;
    }

    private ResourceCategory category(String name) {
        ResourceCategory c = new ResourceCategory(name);
        categories.insert(c);
        return c;
    }

    private Volunteer volunteerWith(Skill s) {
        Volunteer v = (Volunteer) people.register("Volunteer", "Ali", "ali@mail.com", "0100", "pw");
        v.getSkills().add(s);
        people.update(v);
        return v;
    }

    private Beneficiary beneficiaryNeeding(ResourceCategory c) {
        Beneficiary b = (Beneficiary) people.register("Beneficiary", "Fatma", "fatma@mail.com", "0100", "pw");
        b.getNeeds().add(new Need(c, "Basket", 1));
        people.update(b);
        return b;
    }

    @Test
    void createsRenamesAndDeletesUnusedRows() {
        controller.createItem("Skill", "Driving");
        int id = skills.findAll().get(0).getId();
        controller.updateItem("Skill", id, "Driving B");
        assertEquals("Driving B", skills.findById(id).getName());
        controller.deleteItem("Skill", id);
        assertNull(skills.findById(id));
    }

    @Test
    void refusesEmptyNames() {
        Skill s = skill("Cooking");
        controller.createItem("Skill", "");
        controller.updateItem("Skill", s.getId(), "");
        assertEquals("Cooking", skills.findById(s.getId()).getName());
        assertEquals(1, skills.findAll().size());
        assertEquals(List.of("Please enter a name.", "Please enter a name."), view.messages);
    }

    @Test
    void renamingASkillUpdatesTheVolunteersWhoHaveIt() {
        Skill s = skill("First aid");
        Volunteer v = volunteerWith(s);
        controller.updateItem("Skill", s.getId(), "Paramedic");
        Volunteer saved = (Volunteer) people.findById(v.getId());
        assertEquals("Paramedic", saved.getSkills().get(0).getName());
    }

    @Test
    void renamingACategoryUpdatesResourcesAndNeeds() {
        ResourceCategory food = category("Food");
        Resource rice = new Resource("Rice", "bags", 10, food);
        resources.insert(rice);
        Beneficiary b = beneficiaryNeeding(food);

        controller.updateItem("ResourceCategory", food.getId(), "Nutrition");

        assertEquals("Nutrition", resources.findById(rice.getId()).getCategory().getName());
        Beneficiary saved = (Beneficiary) people.findById(b.getId());
        assertEquals("Nutrition", saved.getNeeds().get(0).getCategory().getName());
    }

    @Test
    void renamingOneRowLeavesOtherRowsAlone() {
        Skill renamed = skill("First aid");
        Skill other = skill("Cooking");
        Volunteer v = volunteerWith(other);
        controller.updateItem("Skill", renamed.getId(), "Paramedic");
        assertEquals("Cooking", ((Volunteer) people.findById(v.getId())).getSkills().get(0).getName());
    }

    @Test
    void refusesToDeleteASkillInUse() {
        Skill s = skill("First aid");
        volunteerWith(s);
        controller.deleteItem("Skill", s.getId());
        assertNotNull(skills.findById(s.getId()));
        assertTrue(view.messages.get(0).startsWith("Cannot delete Skill"));
    }

    @Test
    void refusesToDeleteACategoryUsedByAResource() {
        ResourceCategory food = category("Food");
        resources.insert(new Resource("Rice", "bags", 10, food));
        controller.deleteItem("ResourceCategory", food.getId());
        assertNotNull(categories.findById(food.getId()));
    }

    @Test
    void refusesToDeleteACategoryUsedByANeed() {
        ResourceCategory food = category("Food");
        beneficiaryNeeding(food);
        controller.deleteItem("ResourceCategory", food.getId());
        assertNotNull(categories.findById(food.getId()));
    }

    @Test
    void reportsRowsThatDoNotExist() {
        controller.updateItem("Skill", 999, "x");
        controller.deleteItem("Skill", 999);
        assertEquals("No Skill with id 999.", view.messages.get(0));
        assertEquals("No Skill with id 999.", view.messages.get(1));
    }
}
