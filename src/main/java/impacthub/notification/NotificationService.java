package impacthub.notification;

import impacthub.people.Person;

import java.util.EnumMap;
import java.util.Map;

public class NotificationService {
    private final Map<Channel, NotificationStrategy> strategies = new EnumMap<>(Channel.class);
    private NotificationStrategy strategy;

    public void registerStrategy(Channel c, NotificationStrategy s) {
        strategies.put(c, s);
    }

    public void setStrategy(NotificationStrategy s) {
        this.strategy = s;
    }

    public boolean sendNotification(Person to, Message msg) {
        NotificationStrategy chosen = strategy;
        Channel preferred = to.getPreferredChannel();
        if (preferred != null && strategies.containsKey(preferred)) {
            chosen = strategies.get(preferred);
        }
        return chosen != null && chosen.send(to, msg);
    }
}
