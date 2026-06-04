package model.modelManagers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import model.email.config.AuthMode;
import model.email.config.CredentialStore;
import model.email.config.EmailCaseConfig;
import model.email.config.EmailConfig;
import model.email.config.EmailSmtpConfig;
import model.email.config.EmailSecureData;
import model.email.dto.EmailMessage;
import model.email.exception.EmailSendingException;
import model.email.service.EmailSender;
import model.email.service.MarkdownConverter;

public class EmailConfigurationService {

    private static final String EMAIL_CONFIG_FILE = "emailConfig";
    private static final String ENCRYPTED_FILE = "email-secure.dat";

    private final FileManager fileManager;
    private final ObjectMapper objectMapper;
    private final Path secureDataPath;

    // In-memory cache
    private boolean emailEnabled;
    private String senderName;
    private EmailSmtpConfig smtpConfig;
    private List<EmailCaseConfig> caseConfigs;
    private EmailSecureData secureData;

    public EmailConfigurationService(FileManager fileManager, ObjectMapper objectMapper) {
        this.fileManager = Objects.requireNonNull(fileManager);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.secureDataPath = Path.of(FileManager.PATH, "data", ENCRYPTED_FILE);
        this.caseConfigs = new ArrayList<>();
        loadAll();
    }

    // ========== Public Save Methods ==========

    public void saveEmailConfig(String senderName, EmailSmtpConfig smtp, List<EmailCaseConfig> cases) {
        this.senderName = senderName;
        this.smtpConfig = smtp;
        this.caseConfigs = cases != null ? cases : List.of();
        writeEmailConfigJson();
    }

    public void saveSecureData(EmailSecureData data) {
        this.secureData = data;
        writeEncryptedData();
    }

    public void saveEmailEnabled(boolean enabled) {
        this.emailEnabled = enabled;
        writeEmailConfigJson();
    }

    // ========== Public Load Methods ==========

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public Optional<String> loadSenderName() {
        return Optional.ofNullable(senderName);
    }

    public Optional<EmailSmtpConfig> loadSmtpConfig() {
        return Optional.ofNullable(smtpConfig);
    }

    public Optional<List<EmailCaseConfig>> loadCaseConfigs() {
        return caseConfigs.isEmpty() ? Optional.empty() : Optional.of(Collections.unmodifiableList(caseConfigs));
    }

    public Optional<EmailSecureData> loadSecureData() {
        return Optional.ofNullable(secureData);
    }

    // ========== Verify Connection ==========

    public boolean verifyConnection(EmailSmtpConfig smtp, String username, String credential) {
        Objects.requireNonNull(smtp);
        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.host", smtp.smtpHost());
        props.put("mail.smtp.port", String.valueOf(smtp.smtpPort()));
        props.put("mail.smtp.auth", smtp.authMode() != AuthMode.NONE ? "true" : "false");
        props.put("mail.smtp.starttls.enable", String.valueOf(smtp.useStartTls()));
        if (smtp.useImplicitSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", String.valueOf(smtp.connectionTimeoutMs()));
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        jakarta.mail.Authenticator authenticator = null;
        if (smtp.authMode() == AuthMode.PASSWORD && username != null && !username.isBlank()) {
            String finalUsername = username;
            String finalCredential = credential;
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(finalUsername,
                            finalCredential != null ? finalCredential : "");
                }
            };
        }

        Session session = Session.getInstance(props, authenticator);
        try (Transport transport = session.getTransport("smtp")) {
            transport.connect();
            return true;
        } catch (Exception e) {
            System.err.println("Email connection verification failed: " + e.getMessage());
            return false;
        }
    }

    // ========== Validation & Send (Phase 2 stubs) ==========

    public boolean validateCaseConfig(int caseIndex) {
        if (!emailEnabled) return false;
        if (smtpConfig == null) return false;
        if (caseConfigs == null || caseIndex >= caseConfigs.size()) return false;
        EmailCaseConfig caseCfg = caseConfigs.get(caseIndex);
        if (caseCfg == null || !caseCfg.enabled()) return false;
        if (caseCfg.useGlobalReceivers()) {
            if (secureData == null || secureData.receivers().isEmpty()) return false;
        } else {
            if (caseCfg.specificReceivers().isEmpty()) return false;
        }
        return true;
    }

    public boolean sendCaseEmail(int caseIndex, Map<String, String> placeholders, List<Path> attachments) {
        if (!validateCaseConfig(caseIndex)) {
            System.err.println("Email: case " + caseIndex + " not configured for sending");
            return false;
        }
        if (secureData == null || secureData.credential() == null || secureData.credential().isBlank()) {
            System.err.println("Email: no credentials available");
            return false;
        }
        if (smtpConfig == null) {
            System.err.println("Email: no SMTP config");
            return false;
        }

        EmailCaseConfig caseCfg = caseConfigs.get(caseIndex);

        String to = caseCfg.useGlobalReceivers()
                ? String.join(",", secureData.receivers())
                : String.join(",", caseCfg.specificReceivers());
        if (to.isBlank()) {
            System.err.println("Email: no receivers for case " + caseIndex);
            return false;
        }

        String cc = null;
        if (caseCfg.useGlobalReceivers()
                && secureData.cc() != null && !secureData.cc().isEmpty()) {
            cc = String.join(",", secureData.cc());
        }

        String subject = resolvePlaceholders(caseCfg.subject(), placeholders);
        String body = resolvePlaceholders(caseCfg.body(), placeholders);

        MarkdownConverter mdConverter = new MarkdownConverter();
        String htmlBody = mdConverter.toHtml(body);
        EmailMessage msg = new EmailMessage(to, cc, subject, htmlBody, true,
                attachments != null ? attachments : List.of());

        EmailConfig emailConfig = new EmailConfig(
                smtpConfig.smtpHost(), smtpConfig.smtpPort(),
                smtpConfig.useStartTls(), smtpConfig.useImplicitSsl(),
                smtpConfig.authMode(),
                secureData.username(), secureData.credential(),
                smtpConfig.connectionTimeoutMs(),
                Map.of("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3"));

        try {
            EmailSender sender = new EmailSender(emailConfig);
            sender.send(msg);
            System.out.println("Email sent successfully for case " + caseIndex);
            return true;
        } catch (EmailSendingException e) {
            System.err.println("Email send failed: " + e.getMessage());
            return false;
        }
    }

    private static String resolvePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || text.isBlank()) return text;
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    // ========== Serialization ==========

    public String toJson() {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("emailEnabled", emailEnabled);
            root.put("senderName", senderName != null ? senderName : "");
            if (smtpConfig != null) {
                root.set("smtpConfig", objectMapper.valueToTree(smtpConfig));
            }
            ArrayNode casesArray = objectMapper.createArrayNode();
            if (caseConfigs != null) {
                for (EmailCaseConfig c : caseConfigs) {
                    casesArray.add(objectMapper.valueToTree(c));
                }
            }
            root.set("cases", casesArray);
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    // ========== Private Load/Write ==========

    private void loadAll() {
        loadEmailConfigJson();
        loadEncryptedData();
    }

    private void loadEmailConfigJson() {
        String raw = fileManager.getJsonData(EMAIL_CONFIG_FILE);
        if (raw == null) {
            emailEnabled = false;
            senderName = null;
            smtpConfig = null;
            caseConfigs = new ArrayList<>();
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            emailEnabled = root.has("emailEnabled") && root.get("emailEnabled").asBoolean();
            senderName = root.has("senderName") ? root.get("senderName").asText() : "";
            if (root.has("smtpConfig") && !root.get("smtpConfig").isNull()) {
                smtpConfig = objectMapper.treeToValue(root.get("smtpConfig"), EmailSmtpConfig.class);
            } else {
                smtpConfig = null;
            }
            caseConfigs = new ArrayList<>();
            if (root.has("cases") && root.get("cases").isArray()) {
                for (JsonNode caseNode : root.get("cases")) {
                    caseConfigs.add(objectMapper.treeToValue(caseNode, EmailCaseConfig.class));
                }
            }
        } catch (JsonProcessingException e) {
            emailEnabled = false;
            senderName = null;
            smtpConfig = null;
            caseConfigs = new ArrayList<>();
        }
    }

    private void writeEmailConfigJson() {
        fileManager.saveJsonMainDataPath(toJson(), EMAIL_CONFIG_FILE);
    }

    private void loadEncryptedData() {
        try {
            Optional<String> decrypted = CredentialStore.loadEncryptedJson(secureDataPath);
            if (decrypted.isPresent()) {
                secureData = objectMapper.readValue(decrypted.get(), EmailSecureData.class);
            } else {
                secureData = null;
            }
        } catch (Exception e) {
            System.err.println("Failed to load encrypted email data: " + e.getMessage());
            secureData = null;
        }
    }

    private void writeEncryptedData() {
        if (secureData == null) return;
        try {
            String json = objectMapper.writeValueAsString(secureData);
            CredentialStore.saveEncryptedJson(json, secureDataPath);
        } catch (Exception e) {
            System.err.println("Failed to save encrypted email data: " + e.getMessage());
        }
    }
}
