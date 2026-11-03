package impacthub.beneficiary;

import impacthub.lookup.ResourceCategory;

import java.io.Serializable;

/** Something a beneficiary needs; priority 1 is the most urgent. */
public class Need implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private final ResourceCategory category;
    private final String description;
    private final int priority;

    public Need(ResourceCategory category, String description, int priority) {
        this.category = category;
        this.description = description;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public ResourceCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }
}
