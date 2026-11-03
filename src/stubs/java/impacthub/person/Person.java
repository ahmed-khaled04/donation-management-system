package impacthub.person;

import impacthub.notification.Channel;

/** STUB - replace with teammate's real class. */
public abstract class Person {
    private final String name;
    private final Channel preferredChannel;

    protected Person(String name, Channel preferredChannel) {
        this.name = name;
        this.preferredChannel = preferredChannel;
    }

    public String getName() {
        return name;
    }

    public Channel getPreferredChannel() {
        return preferredChannel;
    }
}
