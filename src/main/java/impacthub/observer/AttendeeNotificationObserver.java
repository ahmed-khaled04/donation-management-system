package impacthub.observer;

import impacthub.event.Event;
import impacthub.notification.Message;
import impacthub.notification.NotificationService;
import impacthub.person.Person;

public class AttendeeNotificationObserver implements Observer {
    private final NotificationService notificationService;

    public AttendeeNotificationObserver(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void update(Subject source, ChangeEvent change) {
        String body;
        switch (change.getType()) {
            case "EVENT_DATE_CHANGED" -> body = "has moved to a new date: ";
            case "EVENT_VENUE_CHANGED" -> body = "has moved to a new venue: ";
            default -> {
                return;
            }
        }
        Event event = (Event) change.getPayload();
        String detail = change.getType().equals("EVENT_DATE_CHANGED")
                ? String.valueOf(event.getDate()) : event.getVenue();
        Message msg = new Message("Update: " + event.getName(),
                "The event " + event.getName() + " " + body + detail + ".");
        for (Person attendee : event.getAttendees()) {
            notificationService.sendNotification(attendee, msg);
        }
    }
}
