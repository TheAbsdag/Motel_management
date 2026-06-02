package model.email.service;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import model.email.config.AuthMode;
import model.email.config.EmailConfig;
import model.email.dto.EmailMessage;
import model.email.exception.EmailSendingException;

/**
 * Sends emails using Jakarta Mail with the provided {@link EmailConfig}.
 * <p>
 * {@link Transport#send(Message)} opens and closes a connection per call.
 * For high-volume scenarios, consider using a pooled transport.
 */
public class EmailSender {

    private static final Logger LOG = System.getLogger(EmailSender.class.getName());

    private final EmailConfig config;

    /**
     * @param config the SMTP configuration (host, port, TLS, auth mode, etc.)
     */
    public EmailSender(EmailConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }

    /**
     * Sends an email synchronously. Blocks until the SMTP handshake completes.
     *
     * @param msg the email message to send
     * @throws EmailSendingException on any delivery failure
     */
    public void send(EmailMessage msg) {
        Objects.requireNonNull(msg, "msg cannot be null");
        try {
            Session session = createSession();
            MimeMessage mimeMsg = buildMimeMessage(session, msg);
            Transport.send(mimeMsg);
            LOG.log(Level.INFO, "Email sent to " + msg.to());
        } catch (MessagingException e) {
            throw new EmailSendingException(msg.to(), msg.subject(), e);
        }
    }

    private Session createSession() {
        Properties props = config.toProperties();
        Authenticator authenticator = null;

        if (config.authMode() == AuthMode.PASSWORD) {
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.username(),
                            config.credential() != null ? config.credential() : "");
                }
            };
        }

        return Session.getInstance(props, authenticator);
    }

    private MimeMessage buildMimeMessage(Session session, EmailMessage msg) throws MessagingException {
        MimeMessage mimeMsg = new MimeMessage(session);
        mimeMsg.setFrom(new InternetAddress(config.username()));
        mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(msg.to(), false));

        if (msg.cc() != null && !msg.cc().isBlank()) {
            mimeMsg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(msg.cc(), false));
        }

        mimeMsg.setSubject(msg.subject(), StandardCharsets.UTF_8.name());

        if (msg.attachments().isEmpty()) {
            String subtype = msg.isHtml() ? "html" : "plain";
            mimeMsg.setContent(msg.body(), "text/" + subtype + "; charset=UTF-8");
        } else {
            Multipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            String subtype = msg.isHtml() ? "html" : "plain";
            textPart.setContent(msg.body(), "text/" + subtype + "; charset=UTF-8");
            multipart.addBodyPart(textPart);

            for (Path attachment : msg.attachments()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                FileDataSource source = new FileDataSource(attachment.toFile());
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(attachment.getFileName().toString());
                multipart.addBodyPart(attachmentPart);
            }

            mimeMsg.setContent(multipart);
        }

        mimeMsg.saveChanges();
        return mimeMsg;
    }
}
