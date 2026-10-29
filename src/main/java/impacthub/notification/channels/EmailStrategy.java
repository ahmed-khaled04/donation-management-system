package impacthub.notification.channels;

import impacthub.notification.Message;
import impacthub.notification.NotificationStrategy;
import impacthub.people.Person;

/** Sends a message by e-mail (simulated: printed to the console). */
public class EmailStrategy implements NotificationStrategy {
    @Override
    public boolean send(Person to, Message msg) {
        System.out.println("[EMAIL to " + to.getEmail() + "] " + msg.getSubject() + " - " + msg.getBody());
        return true;
    }
}
