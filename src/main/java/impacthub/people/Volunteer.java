package impacthub.people;

import impacthub.notification.Channel;

import java.util.ArrayList;
import java.util.List;

/** A person who gives their time, with skills, availability and preferences. */
public class Volunteer extends Person {
    private final List<Skill> skills = new ArrayList<>();
    private final List<String> availability = new ArrayList<>();
    private double totalHours;
    private final List<String> preferences = new ArrayList<>();

    public Volunteer(String name, String email, String phone, Channel preferredChannel) {
        super(name, email, phone, preferredChannel);
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public List<String> getAvailability() {
        return availability;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public List<String> getPreferences() {
        return preferences;
    }
}
