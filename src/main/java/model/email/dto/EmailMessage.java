package model.email.dto;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable email message DTO with attachment support.
 */
public record EmailMessage(
        String to,
        String cc,
        String bcc,
        String subject,
        String body,
        boolean isHtml,
        List<Path> attachments) {

    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    /**
     * @param to          primary recipient email(s), comma-separated, each must be valid
     * @param cc          carbon-copy recipient(s), may be null or blank
     * @param bcc         blind carbon-copy recipient(s), may be null or blank
     * @param subject     email subject line, non-blank
     * @param body        email body, non-null
     * @param isHtml      whether body contains HTML markup
     * @param attachments list of file paths to attach, may be empty
     */
    public EmailMessage {
        Objects.requireNonNull(to, "to cannot be null");
        for (String address : to.split(",")) {
            if (address.isBlank() || !address.trim().matches(EMAIL_REGEX)) {
                throw new IllegalArgumentException("Invalid email address: " + to);
            }
        }
        Objects.requireNonNull(subject, "subject cannot be null");
        Objects.requireNonNull(body, "body cannot be null");
        if (attachments == null) {
            attachments = List.of();
        } else {
            attachments = Collections.unmodifiableList(new ArrayList<>(attachments));
        }
    }
}
