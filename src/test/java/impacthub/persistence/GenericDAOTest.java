package impacthub.persistence;

import impacthub.TestDb;
import impacthub.people.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenericDAOTest {
    private GenericDAO<Skill> dao;

    @BeforeEach
    void setUp() {
        TestDb.clear();
        dao = new GenericDAO<>(Skill.class);
    }

    @Test
    void databaseManagerIsASingleton() {
        assertSame(DatabaseManager.getInstance(), DatabaseManager.getInstance());
    }

    @Test
    void insertSetsTheIdAndFindReadsItBack() {
        Skill s = new Skill("Cooking");
        int id = dao.insert(s);
        assertEquals(id, s.getId());
        assertEquals("Cooking", dao.findById(id).getName());
        assertNull(dao.findById(id + 1));
    }

    @Test
    void findAllReturnsRowsInIdOrder() {
        dao.insert(new Skill("A"));
        dao.insert(new Skill("B"));
        assertEquals("A", dao.findAll().get(0).getName());
        assertEquals("B", dao.findAll().get(1).getName());
    }

    @Test
    void updateAndDeleteChangeTheSavedRow() {
        Skill s = new Skill("Cooking");
        int id = dao.insert(s);
        s.setName("Baking");
        dao.update(s);
        assertEquals("Baking", dao.findById(id).getName());
        assertTrue(dao.delete(id));
        assertFalse(dao.delete(id));
    }
}
