package model.email.service;

import java.util.List;
import model.email.service.EmailPendingQueue.PendingEmail;
import model.email.dto.EmailMessage;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EmailPendingQueueTest {

    private static EmailMessage message(String to) {
        return new EmailMessage(to, null, null, "Sujeto", "Cuerpo", false, List.of());
    }

    @Test
    void addAndSnapshot_shouldKeepInsertionOrder() {
        EmailPendingQueue queue = new EmailPendingQueue();
        queue.add(message("a@test.com"), "fallo 1");
        queue.add(message("b@test.com"), "fallo 2");

        List<PendingEmail> snapshot = queue.snapshot();

        assertThat(snapshot).hasSize(2);
        assertThat(snapshot).extracting(p -> p.message().to())
                .containsExactly("a@test.com", "b@test.com");
        assertThat(snapshot).extracting(PendingEmail::failureReason)
                .containsExactly("fallo 1", "fallo 2");
    }

    @Test
    void remove_shouldDropMatchingMessage() {
        EmailPendingQueue queue = new EmailPendingQueue();
        EmailMessage msg = message("a@test.com");
        queue.add(msg, "fallo");

        boolean removed = queue.remove(msg);

        assertThat(removed).isTrue();
        assertThat(queue.size()).isZero();
    }

    @Test
    void remove_unknownMessage_shouldReturnFalse() {
        EmailPendingQueue queue = new EmailPendingQueue();
        queue.add(message("a@test.com"), "fallo");

        assertThat(queue.remove(message("b@test.com"))).isFalse();
        assertThat(queue.size()).isEqualTo(1);
    }

    @Test
    void clear_shouldEmptyQueue() {
        EmailPendingQueue queue = new EmailPendingQueue();
        queue.add(message("a@test.com"), "fallo");

        queue.clear();

        assertThat(queue.size()).isZero();
    }
}
