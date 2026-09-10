package model.email.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.email.dto.EmailMessage;

/**
 * In-memory queue of emails whose SMTP delivery failed.
 * Entries survive only while the app is running; a manual "retry" re-sends them
 * with the current SMTP config/credentials once the user fixes the cause.
 */
public final class EmailPendingQueue {

    /**
     * @param message       the fully rendered email that failed to send
     * @param createdAt     when the failure was recorded
     * @param failureReason short human-readable reason
     */
    public record PendingEmail(EmailMessage message, Instant createdAt, String failureReason) {
    }

    private final List<PendingEmail> items = new ArrayList<>();

    public synchronized void add(EmailMessage message, String failureReason) {
        items.add(new PendingEmail(message, Instant.now(), failureReason));
    }

    public synchronized boolean remove(EmailMessage message) {
        return items.removeIf(p -> p.message().equals(message));
    }

    public synchronized List<PendingEmail> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public synchronized void clear() {
        items.clear();
    }

    public synchronized int size() {
        return items.size();
    }
}
