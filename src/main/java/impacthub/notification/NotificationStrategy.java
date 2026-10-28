package impacthub.notification;

import impacthub.people.Person;

public interface NotificationStrategy {
    boolean send(Person to, Message msg);
}
