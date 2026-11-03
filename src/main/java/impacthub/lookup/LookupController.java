package impacthub.lookup;

import impacthub.beneficiary.Need;
import impacthub.beneficiary.Resource;
import impacthub.people.Beneficiary;
import impacthub.people.Person;
import impacthub.people.Skill;
import impacthub.people.Volunteer;
import impacthub.persistence.GenericDAO;

/** CRUD on the lookup tables: "Skill" and "ResourceCategory". */
public class LookupController {
    private final LookupView view;
    private final GenericDAO<Skill> skills = new GenericDAO<>(Skill.class);
    private final GenericDAO<ResourceCategory> categories = new GenericDAO<>(ResourceCategory.class);
    private final GenericDAO<Person> people = new GenericDAO<>(Person.class);
    private final GenericDAO<Resource> resources = new GenericDAO<>(Resource.class);

    public LookupController(LookupView view) {
        this.view = view;
    }

    public void loadTable(String table) {
        view.showTable(table.equals("Skill") ? skills.findAll() : categories.findAll());
    }

    public void createItem(String table, String name) {
        if (name.isEmpty()) {
            view.showMessage("Please enter a name.");
            return;
        }
        int id = table.equals("Skill") ? skills.insert(new Skill(name)) : categories.insert(new ResourceCategory(name));
        view.showMessage("Created " + table + " #" + id + ".");
        loadTable(table);
    }

    public void updateItem(String table, int id, String name) {
        if (name.isEmpty()) {
            view.showMessage("Please enter a name.");
            return;
        }
        if (table.equals("Skill")) {
            Skill s = skills.findById(id);
            if (s == null) {
                view.showMessage("No Skill with id " + id + ".");
                return;
            }
            s.setName(name);
            skills.update(s);
            renameSkillOnVolunteers(id, name);
        } else {
            ResourceCategory c = categories.findById(id);
            if (c == null) {
                view.showMessage("No ResourceCategory with id " + id + ".");
                return;
            }
            c.setName(name);
            categories.update(c);
            renameCategoryOnRecords(id, name);
        }
        view.showMessage("Updated " + table + " #" + id + ".");
        loadTable(table);
    }

    public void deleteItem(String table, int id) {
        long uses = table.equals("Skill") ? countSkillUses(id) : countCategoryUses(id);
        if (uses > 0) {
            view.showMessage("Cannot delete " + table + " #" + id + ": it is still used by " + uses + " record(s).");
            return;
        }
        boolean deleted = table.equals("Skill") ? skills.delete(id) : categories.delete(id);
        view.showMessage(deleted ? "Deleted " + table + " #" + id + "." : "No " + table + " with id " + id + ".");
        loadTable(table);
    }

    private void renameSkillOnVolunteers(int id, String name) {
        for (Person p : people.findAll()) {
            if (p instanceof Volunteer v && v.getSkills().stream().anyMatch(s -> s.getId() == id)) {
                v.getSkills().stream().filter(s -> s.getId() == id).forEach(s -> s.setName(name));
                people.update(v);
            }
        }
    }

    private void renameCategoryOnRecords(int id, String name) {
        for (Resource r : resources.findAll()) {
            if (r.getCategory().getId() == id) {
                r.getCategory().setName(name);
                resources.update(r);
            }
        }
        for (Person p : people.findAll()) {
            if (p instanceof Beneficiary b && b.getNeeds().stream().anyMatch(n -> n.getCategory().getId() == id)) {
                b.getNeeds().stream().map(Need::getCategory).filter(c -> c.getId() == id).forEach(c -> c.setName(name));
                people.update(b);
            }
        }
    }

    private long countSkillUses(int id) {
        return people.findAll().stream()
                .filter(p -> p instanceof Volunteer v && v.getSkills().stream().anyMatch(s -> s.getId() == id))
                .count();
    }

    private long countCategoryUses(int id) {
        long usedByResources = resources.findAll().stream().filter(r -> r.getCategory().getId() == id).count();
        long usedByBeneficiaries = people.findAll().stream()
                .filter(p -> p instanceof Beneficiary b && b.getNeeds().stream().anyMatch(n -> n.getCategory().getId() == id))
                .count();
        return usedByResources + usedByBeneficiaries;
    }
}
