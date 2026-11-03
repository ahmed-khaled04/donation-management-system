package impacthub.volunteer;

import impacthub.TestDb;
import impacthub.event.Event;
import impacthub.event.EventService;
import impacthub.people.PersonService;
import impacthub.people.Skill;
import impacthub.people.Volunteer;
import impacthub.persistence.GenericDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VolunteerServiceTest {
    private PersonService people;
    private VolunteerService service;
    private GenericDAO<Assignment> assignments;
    private Volunteer ali;
    private Event event;

    @BeforeEach
    void setUp() {
        TestDb.clear();
        people = new PersonService();
        service = new VolunteerService();
        assignments = new GenericDAO<>(Assignment.class);
        ali = (Volunteer) people.register("Volunteer", "Ali", "ali@mail.com", "01000000000", "pass");
        event = new EventService().createEvent("Workshop", "Training", LocalDateTime.now().plusDays(30), "Hall");
    }

    private double savedHours(Volunteer v) {
        return ((Volunteer) people.findById(v.getId())).getTotalHours();
    }

    @Test
    void hoursAddUpOnTheAssignmentAndTheVolunteer() {
        Assignment a = service.assign(ali, event, "Helper");
        service.logHours(a, 4);
        service.logHours(a, 1.5);
        assertEquals(5.5, assignments.findById(a.getId()).getHoursWorked());
        assertEquals(5.5, savedHours(ali));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsHoursThatAreNotPositiveFiniteNumbers(double hours) {
        Assignment a = service.assign(ali, event, "Helper");
        assertThrows(IllegalArgumentException.class, () -> service.logHours(a, hours));
        assertEquals(0, assignments.findById(a.getId()).getHoursWorked());
        assertEquals(0, savedHours(ali));
    }

    @Test
    void oneEntryCannotBeMoreThan24Hours() {
        Assignment a = service.assign(ali, event, "Helper");
        assertDoesNotThrow(() -> service.logHours(a, 24));
        assertThrows(IllegalArgumentException.class, () -> service.logHours(a, 24.5));
    }

    @Test
    void loggingHoursForADeletedVolunteerIsRefusedAndSavesNothing() {
        Assignment a = service.assign(ali, event, "Helper");
        people.delete(ali.getId());

        IllegalArgumentException refused = assertThrows(IllegalArgumentException.class, () -> service.logHours(a, 3));
        assertTrue(refused.getMessage().contains("Ali"));
        assertEquals(0, assignments.findById(a.getId()).getHoursWorked());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsABlankRole(String role) {
        assertThrows(IllegalArgumentException.class, () -> service.assign(ali, event, role));
        assertTrue(service.getAssignmentsByEvent(event).isEmpty());
    }

    @Test
    void rejectsAssigningTheSameVolunteerToTheSameEventTwice() {
        service.assign(ali, event, "Helper");
        assertThrows(IllegalArgumentException.class, () -> service.assign(ali, event, "Driver"));
        assertEquals(1, service.getAssignmentsByEvent(event).size());
    }

    @Test
    void sameVolunteerCanWorkAtADifferentEvent() {
        Event other = new EventService().createEvent("Outreach", "Visit", LocalDateTime.now().plusDays(30), "Giza");
        service.assign(ali, event, "Helper");
        assertDoesNotThrow(() -> service.assign(ali, other, "Helper"));
    }

    @Test
    void findsVolunteersBySkill() {
        GenericDAO<Skill> skills = new GenericDAO<>(Skill.class);
        Skill firstAid = new Skill("First aid");
        Skill cooking = new Skill("Cooking");
        skills.insert(firstAid);
        skills.insert(cooking);
        ali.getSkills().add(firstAid);
        people.update(ali);
        people.register("Volunteer", "Mona", "mona@mail.com", "01000000000", "pass");

        assertEquals(1, service.findBySkill(firstAid).size());
        assertTrue(service.findBySkill(cooking).isEmpty());
    }

    @Test
    void certificateShowsTheTotalHours() {
        service.logHours(service.assign(ali, event, "Helper"), 4);
        Volunteer current = (Volunteer) people.findById(ali.getId());
        assertTrue(service.generateCertificate(current).render().contains("Ali volunteered 4.0 hours"));
    }
}
