package controller.sub;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import model.email.config.CredentialStore;
import model.email.config.AuthMode;
import model.email.config.EmailConfig;
import model.email.config.EmailSecureData;
import model.email.dto.EmailMessage;
import model.email.service.EmailSender;
import model.json.ObjectMapperFactory;
import model.modelManagers.FileManager;
import model.modelManagers.MotelManagement;
import view.EmailConfigurationView;
import view.ExportConfigurationView;
import view.helpers.DialogHelper;

/**
 * Orchestrates email configuration UI actions: loading/saving config,
 * provider switching, credential management, and test email sending.
 * <p>
 * Sensitive data (username, credential, receivers) is stored in a single
 * encrypted file ({@code email-secure.dat}) while non-sensitive SMTP
 * settings are stored in plain JSON ({@code email-config}).
 */
public class EmailController {

    private static final Logger LOG = System.getLogger(EmailController.class.getName());
    private static final String CONFIG_FILE = "email-config";
    private static final String EMAIL_SECURE_FILE = "email-secure.dat";

    private final MotelManagement motelManager;
    private final FileManager fileManager;
    private final EmailConfigurationView emailView;
    private final ExportConfigurationView exportView;
    private final Runnable onBackToExport;
    private final Runnable onShowEmailConfig;
    private final Runnable onSaveMainFiles;
    private final Runnable onSaveBackupFiles;

    private EmailConfig currentConfig;
    private EmailSecureData currentSecureData;
    private boolean isFirstTime;

    public EmailController(MotelManagement motelManager,
                           FileManager fileManager,
                           EmailConfigurationView emailView,
                           ExportConfigurationView exportView,
                           Runnable onBackToExport,
                           Runnable onShowEmailConfig,
                           Runnable onSaveMainFiles,
                           Runnable onSaveBackupFiles) {
        this.motelManager = motelManager;
        this.fileManager = fileManager;
        this.emailView = emailView;
        this.exportView = exportView;
        this.onBackToExport = onBackToExport;
        this.onShowEmailConfig = onShowEmailConfig;
        this.onSaveMainFiles = onSaveMainFiles;
        this.onSaveBackupFiles = onSaveBackupFiles;
    }

    public void initListeners() {
        emailView.onProviderChanged(this::handleProviderChange);
        emailView.onAppPassProviderChanged(this::handleAppPassProviderChange);
        emailView.onTestEmail(this::handleTestEmail);
        emailView.onSaveConfig(this::handleSaveConfig);
        emailView.onBackButton(onBackToExport);

        exportView.onEmailConfigButton(() -> {
            if (isFirstTime()) {
                showFirstTimeSetup();
            }
            populateView();
            onShowEmailConfig.run();
        });
        exportView.onWhatsappConfigButton(() -> {/* placeholder: not yet implemented */});
    }

    public void populateView() {
        try {
            loadConfig();
            if (currentConfig == null) {
                isFirstTime = true;
                return;
            }

            boolean secureLoaded = loadSecureData();
            if (!secureLoaded) {
                showSecureDataCorruptionWarning();
                currentConfig = null;
                currentSecureData = null;
                isFirstTime = true;
                return;
            }

            isFirstTime = false;
            populateFieldsFromConfig();
            emailView.setReceivers(currentSecureData.receivers());
            if (emailView.getTestRecipient().isBlank() && !emailView.getEmail().isBlank()) {
                emailView.setTestRecipient(emailView.getEmail());
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error loading email config", e);
            showSecureDataCorruptionWarning();
            currentConfig = null;
            currentSecureData = null;
            isFirstTime = true;
        }
    }

    private boolean isFirstTime() {
        return currentConfig == null;
    }

    private void showFirstTimeSetup() {
        DialogHelper.showInfoMessage(
            "No se ha encontrado configuracion de correo.\n"
            + "Seleccione un proveedor, ingrese sus datos y guarde la configuracion.\n"
            + "Luego podra probar el envio con el boton ENVIAR PRUEBA.",
            "CONFIGURACION INICIAL DE CORREO");
    }

    private void showSecureDataCorruptionWarning() {
        DialogHelper.showInfoMessage(
            "No se pudieron leer los datos de configuracion de correo.\n"
            + "Esto puede deberse a que el archivo de configuracion haya sido\n"
            + "modificado, este corrupto, o que el programa se este ejecutando\n"
            + "en un equipo diferente.\n\n"
            + "Por favor, configure el correo nuevamente.",
            "ERROR DE LECTURA");
    }

    private void loadConfig() {
        String json = fileManager.getJsonData(CONFIG_FILE);
        if (json != null && !json.isBlank()) {
            try {
                currentConfig = ObjectMapperFactory.get().readValue(json, EmailConfig.class);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to parse email config, using defaults", e);
                currentConfig = null;
            }
        }
    }

    private boolean loadSecureData() {
        try {
            Path securePath = Path.of(FileManager.PATH, "data", EMAIL_SECURE_FILE);
            Optional<String> json = CredentialStore.loadEncryptedJson(securePath);
            if (json.isEmpty()) {
                LOG.log(Level.WARNING, "Email secure data file not found");
                return false;
            }
            currentSecureData = ObjectMapperFactory.get().readValue(json.get(), EmailSecureData.class);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to load email secure data", e);
            return false;
        }
    }

    private void populateFieldsFromConfig() {
        String username = currentSecureData.username();
        emailView.setEmail(username);
        emailView.setDisplayName(username);

        switch (currentConfig.authMode()) {
            case PASSWORD: {
                String host = currentConfig.smtpHost().toLowerCase();
                if (host.contains("gmail")) {
                    emailView.setSelectedProvider(0);
                    emailView.showProviderCard(0);
                    emailView.setAppPassProvider(0);
                    emailView.showAppPassSubCard(0);
                } else if (host.contains("office365") || host.contains("outlook")) {
                    emailView.setSelectedProvider(0);
                    emailView.showProviderCard(0);
                    emailView.setAppPassProvider(1);
                    emailView.showAppPassSubCard(1);
                } else {
                    emailView.setSelectedProvider(1);
                    emailView.showProviderCard(1);
                    emailView.setSmtpHost(currentConfig.smtpHost());
                    emailView.setSmtpPort(String.valueOf(currentConfig.smtpPort()));
                    emailView.setSmtpUser(username);
                    emailView.setSmtpTls(currentConfig.useStartTls());
                    emailView.setSmtpSsl(currentConfig.useImplicitSsl());
                }
                break;
            }
            default:
                emailView.setSelectedProvider(1);
                emailView.showProviderCard(1);
                break;
        }

        String cred = currentSecureData.credential();
        if (cred != null && !cred.isBlank()) {
            int provider = emailView.getSelectedProvider();
            switch (provider) {
                case 0 -> {
                    int sub = emailView.getAppPassProvider();
                    switch (sub) {
                        case 0 -> emailView.setGmailAppPassword(cred.toCharArray());
                        case 1 -> emailView.setOutlookAppPassword(cred.toCharArray());
                    }
                }
                case 1 -> emailView.setSmtpPassword(cred.toCharArray());
            }
        }
    }

    private void handleProviderChange(int index) {
        emailView.showProviderCard(index);
        switch (index) {
            case 0:
                emailView.showAppPassSubCard(0);
                emailView.setSmtpHost("smtp.gmail.com");
                break;
            case 1:
                if (emailView.getSmtpHost().isEmpty()) {
                    emailView.setSmtpHost("");
                }
                if (emailView.getSmtpPort().isEmpty()) {
                    emailView.setSmtpPort("587");
                }
                emailView.setSmtpTls(true);
                break;
        }
    }

    private void handleAppPassProviderChange(int index) {
        emailView.showAppPassSubCard(index);
        switch (index) {
            case 0 -> emailView.setSmtpHost("smtp.gmail.com");
            case 1 -> emailView.setSmtpHost("smtp.office365.com");
        }
    }

    private void handleSaveConfig() {
        String email = emailView.getEmail();
        if (email.isBlank()) {
            DialogHelper.showInfoMessage("El campo CORREO no puede estar vacio", "ERROR");
            return;
        }

        boolean confirm = DialogHelper.confirmDialog("Guardar configuracion de correo?", "CONFIRMACION");
        if (!confirm) return;

        EmailConfig config = buildConfigFromFields();
        if (config == null) return;

        String credential = getCredentialFromFields();
        List<String> receivers = emailView.getReceivers();
        EmailSecureData secureData = new EmailSecureData(email, credential, receivers);

        try {
            String json = ObjectMapperFactory.get().writeValueAsString(config);
            fileManager.saveJsonMainDataPath(json, CONFIG_FILE);

            String secureJson = ObjectMapperFactory.get().writeValueAsString(secureData);
            Path securePath = Path.of(FileManager.PATH, "data", EMAIL_SECURE_FILE);
            CredentialStore.saveEncryptedJson(secureJson, securePath);

            currentConfig = config;
            currentSecureData = secureData;
            isFirstTime = false;
            onSaveMainFiles.run();
            onSaveBackupFiles.run();

            DialogHelper.showInfoMessage("Configuracion de correo guardada exitosamente", "GUARDADO");
        } catch (Exception e) {
            LOG.log(Level.ERROR, "Failed to save email config", e);
            DialogHelper.showInfoMessage("Error al guardar configuracion: " + e.getMessage(), "ERROR");
        }
    }

    private String getCredentialFromFields() {
        int provider = emailView.getSelectedProvider();
        return switch (provider) {
            case 0 -> {
                int sub = emailView.getAppPassProvider();
                yield switch (sub) {
                    case 0 -> new String(emailView.getGmailAppPassword());
                    case 1 -> new String(emailView.getOutlookAppPassword());
                    default -> "";
                };
            }
            case 1 -> new String(emailView.getSmtpPassword());
            default -> "";
        };
    }

    private EmailConfig buildConfigFromFields() {
        int provider = emailView.getSelectedProvider();
        String email = emailView.getEmail();

        return switch (provider) {
            case 0 -> {
                int subProvider = emailView.getAppPassProvider();
                yield switch (subProvider) {
                    case 0 -> {
                        String password = new String(emailView.getGmailAppPassword());
                        if (password.isBlank()) {
                            DialogHelper.showInfoMessage("CONTRASENA requerida para Gmail", "ERROR");
                            yield null;
                        }
                        yield EmailConfig.gmailAppPassword(email, password);
                    }
                    case 1 -> {
                        String password = new String(emailView.getOutlookAppPassword());
                        if (password.isBlank()) {
                            DialogHelper.showInfoMessage("CONTRASENA requerida para Outlook", "ERROR");
                            yield null;
                        }
                        yield EmailConfig.outlookPassword(email, password);
                    }
                    default -> {
                        DialogHelper.showInfoMessage("Seleccione un proveedor valido", "ERROR");
                        yield null;
                    }
                };
            }
            case 1 -> {
                String host = emailView.getSmtpHost();
                String portStr = emailView.getSmtpPort();
                String user = emailView.getSmtpUser();
                String pass = new String(emailView.getSmtpPassword());
                boolean tls = emailView.isSmtpTls();
                boolean ssl = emailView.isSmtpSsl();
                if (host.isBlank() || user.isBlank() || pass.isBlank()) {
                    DialogHelper.showInfoMessage("Todos los campos SMTP deben estar llenos", "ERROR");
                    yield null;
                }
                int port = parsePort(portStr, ssl ? 465 : 587);
                yield EmailConfig.builder()
                        .smtpHost(host)
                        .smtpPort(port)
                        .useStartTls(!ssl && tls)
                        .useImplicitSsl(ssl)
                        .authMode(AuthMode.PASSWORD)
                        .username(user)
                        .credential(pass)
                        .extraProperty("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
                        .build();
            }
            default -> {
                DialogHelper.showInfoMessage("Seleccione un proveedor valido", "ERROR");
                yield null;
            }
        };
    }

    private void handleTestEmail() {
        EmailConfig config = buildConfigFromFields();
        if (config == null) return;

        String recipient = emailView.getTestRecipient();
        if (recipient.isBlank()) {
            DialogHelper.showInfoMessage("Ingrese un correo de destino para la prueba", "ERROR");
            return;
        }

        String subject = emailView.getTestSubject();
        if (subject.isBlank()) {
            subject = "Prueba de configuracion correo";
        }

        String body = emailView.getTestBody();
        boolean isHtml = emailView.isHtml();

        body = replaceTemplatePlaceholders(body, config.username());

        final String finalSubject = subject;
        final String finalBody = body;

        CompletableFuture.runAsync(() -> {
            try {
                EmailMessage msg = new EmailMessage(recipient, null, finalSubject, finalBody, isHtml, null);
                new EmailSender(config).send(msg);
                javax.swing.SwingUtilities.invokeLater(() ->
                        DialogHelper.showInfoMessage("Correo de prueba enviado exitosamente a " + recipient,
                                "CORREO ENVIADO"));
            } catch (Exception e) {
                LOG.log(Level.ERROR, "Test email failed", e);
                javax.swing.SwingUtilities.invokeLater(() ->
                        DialogHelper.showInfoMessage("Error al enviar correo de prueba: " + e.getMessage(),
                                "ERROR CORREO"));
            }
        });
    }

    private static String replaceTemplatePlaceholders(String body, String senderEmail) {
        String currentDate = java.time.ZonedDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy HH:mm:ss"));
        return body.replace("{motelName}", "")
                .replace("{date}", currentDate)
                .replace("{senderName}", "")
                .replace("{senderEmail}", senderEmail);
    }

    private static int parsePort(String str, int defaultPort) {
        try {
            int p = Integer.parseInt(str.trim());
            if (p > 0 && p <= 65535) return p;
        } catch (NumberFormatException ignored) {}
        return defaultPort;
    }
}
