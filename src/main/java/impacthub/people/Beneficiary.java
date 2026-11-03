package impacthub.people;

import impacthub.beneficiary.Need;
import impacthub.notification.Channel;

import java.util.ArrayList;
import java.util.List;

/** A person or family that receives help, with their recorded needs. */
public class Beneficiary extends Person {
    private static final long serialVersionUID = 1L;

    private String address = "";
    private int familySize = 1;
    private final List<Need> needs = new ArrayList<>();

    public Beneficiary(String name, String email, String phone, Channel preferredChannel) {
        super(name, email, phone, preferredChannel);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getFamilySize() {
        return familySize;
    }

    public void setFamilySize(int familySize) {
        this.familySize = familySize;
    }

    public List<Need> getNeeds() {
        return needs;
    }
}
