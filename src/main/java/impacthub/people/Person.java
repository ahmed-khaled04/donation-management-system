package impacthub.people;

import impacthub.notification.Channel;

import java.io.Serializable;

/** Base class for everyone the organization works with. */
public abstract class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String phone;
    private String socialHandle = "";
    private Channel preferredChannel;
    private String passwordHash;

    protected Person(String name, String email, String phone, Channel preferredChannel) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.preferredChannel = preferredChannel;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSocialHandle() {
        return socialHandle;
    }

    public void setSocialHandle(String socialHandle) {
        this.socialHandle = socialHandle;
    }

    public Channel getPreferredChannel() {
        return preferredChannel;
    }

    public void setPreferredChannel(Channel preferredChannel) {
        this.preferredChannel = preferredChannel;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
