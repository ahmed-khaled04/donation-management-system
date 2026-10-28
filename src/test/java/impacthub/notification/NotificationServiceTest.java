package impacthub.notification;

import impacthub.people.Donor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {
    private final Message msg = new Message("s", "b");

    @Test
    void usesStrategyRegisteredForPreferredChannel() {
        List<String> log = new ArrayList<>();
        NotificationService service = new NotificationService();
        service.registerStrategy(Channel.EMAIL, (to, m) -> log.add("email") );
        service.registerStrategy(Channel.SMS, (to, m) -> log.add("sms"));

        assertTrue(service.sendNotification(new Donor("a", "a@mail.com", "0100", Channel.SMS), msg));
        assertEquals(List.of("sms"), log);
    }

    @Test
    void fallsBackToDefaultStrategy() {
        List<String> log = new ArrayList<>();
        NotificationService service = new NotificationService();
        service.setStrategy((to, m) -> log.add("default"));

        assertTrue(service.sendNotification(new Donor("a", "a@mail.com", "0100", Channel.SOCIAL), msg));
        assertEquals(List.of("default"), log);
    }

    @Test
    void returnsFalseWhenNoStrategyAvailable() {
        assertFalse(new NotificationService().sendNotification(new Donor("a", "a@mail.com", "0100", Channel.EMAIL), msg));
    }

    @Test
    void returnsStrategyResult() {
        NotificationService service = new NotificationService();
        service.registerStrategy(Channel.EMAIL, (to, m) -> false);
        assertFalse(service.sendNotification(new Donor("a", "a@mail.com", "0100", Channel.EMAIL), msg));
    }

    @Test
    void messageKeepsFieldsAndSetsCreationTime() {
        assertEquals("s", msg.getSubject());
        assertEquals("b", msg.getBody());
        assertNotNull(msg.getCreatedAt());
    }
}
