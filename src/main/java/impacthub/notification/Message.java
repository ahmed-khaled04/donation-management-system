package impacthub.notification;

import java.time.LocalDateTime;

public class Message {
    private final String subject;
    private final String body;
    private final LocalDateTime createdAt;

    public Message(String subject, String body) {
        this.subject = subject;
        this.body = body;
        this.createdAt = LocalDateTime.now();
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
