package impacthub.lookup;

import impacthub.people.Skill;
import impacthub.persistence.GenericDAO;

/** CRUD on the lookup tables: "Skill" and "ResourceCategory". */
public class LookupController {
    private final LookupView view;
    private final GenericDAO<Skill> skills = new GenericDAO<>(Skill.class);
    private final GenericDAO<ResourceCategory> categories = new GenericDAO<>(ResourceCategory.class);

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
        if (table.equals("Skill")) {
            Skill s = skills.findById(id);
            if (s == null) {
                view.showMessage("No Skill with id " + id + ".");
                return;
            }
            s.setName(name);
            skills.update(s);
        } else {
            ResourceCategory c = categories.findById(id);
            if (c == null) {
                view.showMessage("No ResourceCategory with id " + id + ".");
                return;
            }
            c.setName(name);
            categories.update(c);
        }
        view.showMessage("Updated " + table + " #" + id + ".");
        loadTable(table);
    }

    public void deleteItem(String table, int id) {
        boolean deleted = table.equals("Skill") ? skills.delete(id) : categories.delete(id);
        view.showMessage(deleted ? "Deleted " + table + " #" + id + "." : "No " + table + " with id " + id + ".");
        loadTable(table);
    }
}
