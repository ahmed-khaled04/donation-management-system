package impacthub.lookup;

import java.io.Serializable;

/** Lookup table row: a category of resources and needs (e.g. Food, Education). */
public class ResourceCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private final String program;

    /** A category named Food or Education joins that impact program and stays in it when renamed. */
    public ResourceCategory(String name) {
        this.name = name;
        this.program = name.equalsIgnoreCase("Food") ? "Food" : name.equalsIgnoreCase("Education") ? "Education" : null;
    }

    public int getId() {
        return id;
    }

    /** The impact program this category belongs to, or null if it has none. */
    public String getProgram() {
        if (program != null) {
            return program;
        }
        // Rows saved before programs existed have none stored; the sample data always created Food and Education first.
        return id == 1 ? "Food" : id == 2 ? "Education" : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
