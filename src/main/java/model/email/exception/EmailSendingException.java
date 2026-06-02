package model.email.exception;

/**
 * Unchecked exception thrown when an email cannot be sent.
 * Contains context about the failed delivery without exposing credentials.
 */
public class EmailSendingException extends RuntimeException {

    private final String to;
    private final String subject;

    public EmailSendingException(String to, String subject, Throwable cause) {
        super(buildMessage(to, subject, cause), cause);
        this.to = to;
        this.subject = subject;
    }

    private static String buildMessage(String to, String subject, Throwable cause) {
        String msg = "Failed to send email to '" + to + "'";
        if (subject != null && !subject.isBlank()) {
            msg += " with subject '" + subject + "'";
        }
        if (cause != null) {
            msg += ": " + cause.getMessage();
        }
        return msg;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }
}
