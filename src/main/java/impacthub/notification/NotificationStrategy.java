package impacthub.notification;

import impacthub.person.Person;

public interface NotificationStrategy {
    boolean send(Person to, Message msg);
}
