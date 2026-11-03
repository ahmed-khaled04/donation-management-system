package impacthub.lookup;

import impacthub.Validation;
import impacthub.beneficiary.Need;
import impacthub.beneficiary.Resource;
import impacthub.observer.ChangeEvent;
import impacthub.observer.Observer;
import impacthub.observer.Subject;
import impacthub.people.Beneficiary;
import impacthub.people.Person;
import impacthub.people.Skill;
import impacthub.people.Volunteer;
import impacthub.persistence.GenericDAO;

import java.util.ArrayList;
import java.util.List;

/** CRUD on the lookup tables: "Skill" and "ResourceCategory". */
public class LookupController implements Subject {
    private final List<Observer> observers = new ArrayList<>();
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
        String clean = checkName(table, name, 0);
        if (clean == null) {
            return;
        }
        int id = table.equals("Skill") ? skills.insert(new Skill(clean)) : categories.insert(new ResourceCategory(clean));
        notifyObservers(new ChangeEvent("LOOKUP_CREATED", clean, "Created " + table + " \"" + clean + "\""));
        view.showMessage("Created " + table + " #" + id + ".");
        loadTable(table);
    }

    public void updateItem(String table, int id, String name) {
        String clean = checkName(table, name, id);
        if (clean == null) {
            return;
        }
        if (table.equals("Skill")) {
            Skill s = skills.findById(id);
            if (s == null) {
                view.showMessage("No Skill with id " + id + ".");
                return;
            }
            s.setName(clean);
            skills.update(s);
            renameSkillOnVolunteers(id, clean);
        } else {
            ResourceCategory c = categories.findById(id);
            if (c == null) {
                view.showMessage("No ResourceCategory with id " + id + ".");
                return;
            }
            c.setName(clean);
            categories.update(c);
            renameCategoryOnRecords(id, clean);
        }
        notifyObservers(new ChangeEvent("LOOKUP_RENAMED", clean, "Renamed " + table + " #" + id + " to \"" + clean + "\""));
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
        if (deleted) {
            notifyObservers(new ChangeEvent("LOOKUP_DELETED", id, "Deleted " + table + " #" + id));
        }
        view.showMessage(deleted ? "Deleted " + table + " #" + id + "." : "No " + table + " with id " + id + ".");
        loadTable(table);
    }

    // Returns the cleaned name, or null after telling the user what is wrong. Names are unique in their table;
    // ownId lets a row keep its own name in a different case.
    private String checkName(String table, String name, int ownId) {
        if (name.isBlank()) {
            view.showMessage("Please enter a name.");
            return null;
        }
        String clean;
        try {
            clean = Validation.requireText(name, "The name");
        } catch (IllegalArgumentException e) {
            view.showMessage(e.getMessage());
            return null;
        }
        boolean taken = table.equals("Skill")
                ? skills.findAll().stream().anyMatch(s -> s.getId() != ownId && s.getName().equalsIgnoreCase(clean))
                : categories.findAll().stream().anyMatch(c -> c.getId() != ownId && c.getName().equalsIgnoreCase(clean));
        if (taken) {
            view.showMessage(table + " \"" + clean + "\" already exists.");
            return null;
        }
        return clean;
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

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(ChangeEvent change) {
        for (Observer o : observers) {
            o.update(this, change);
        }
    }
}
