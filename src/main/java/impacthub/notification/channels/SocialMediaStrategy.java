package impacthub.notification.channels;

import impacthub.notification.Message;
import impacthub.notification.NotificationStrategy;
import impacthub.people.Person;

/** Sends a message on social media (simulated: printed to the console). */
public class SocialMediaStrategy implements NotificationStrategy {
    @Override
    public boolean send(Person to, Message msg) {
        String handle = to.getSocialHandle().isEmpty() ? to.getName() : to.getSocialHandle();
        System.out.println("[SOCIAL to @" + handle + "] " + msg.getSubject() + " - " + msg.getBody());
        return true;
    }
}
