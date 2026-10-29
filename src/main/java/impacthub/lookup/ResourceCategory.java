package impacthub.lookup;

import java.io.Serializable;

/** Lookup table row: a category of resources and needs (e.g. Food, Education). */
public class ResourceCategory implements Serializable {
    private int id;
    private String name;

    public ResourceCategory(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
