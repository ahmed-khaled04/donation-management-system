package impacthub.volunteer;

import impacthub.people.Volunteer;

import java.time.LocalDateTime;

/** A certificate of appreciation showing a volunteer's total hours. */
public class VolunteerCertificate {
    private final Volunteer volunteer;
    private final double totalHours;
    private final LocalDateTime issuedDate;

    public VolunteerCertificate(Volunteer volunteer) {
        this.volunteer = volunteer;
        this.totalHours = volunteer.getTotalHours();
        this.issuedDate = LocalDateTime.now();
    }

    public String render() {
        return "CERTIFICATE OF APPRECIATION\n"
                + "This certifies that " + volunteer.getName() + " volunteered " + totalHours + " hours.\n"
                + "Issued on " + issuedDate.toLocalDate() + ".";
    }
}
