package impacthub.people;

import java.io.Serializable;

/** Lookup table row: a skill a volunteer can have. */
public class Skill implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;

    public Skill(String name) {
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
