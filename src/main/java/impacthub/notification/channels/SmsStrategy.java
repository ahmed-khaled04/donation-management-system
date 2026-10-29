package impacthub.notification.channels;

import impacthub.notification.Message;
import impacthub.notification.NotificationStrategy;
import impacthub.people.Person;

/** Sends a message by SMS (simulated: printed to the console). */
public class SmsStrategy implements NotificationStrategy {
    @Override
    public boolean send(Person to, Message msg) {
        System.out.println("[SMS to " + to.getPhone() + "] " + msg.getSubject() + " - " + msg.getBody());
        return true;
    }
}
