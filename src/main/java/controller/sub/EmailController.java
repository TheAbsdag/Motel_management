package controller.sub;

import java.awt.Window;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import model.email.config.AuthMode;
import model.email.config.EmailCaseConfig;
import model.email.config.EmailSecureData;
import model.email.config.EmailSmtpConfig;
import model.email.config.ProviderPreset;
import model.email.service.MarkdownConverter;
import model.modelManagers.EmailConfigurationService;
import view.EmailCaseConfigurationView;
import view.EmailCaseConfigurationView.VariableOption;
import view.EmailConfigurationHubView;
import view.EmailGlobalConfigurationView;
import view.EmailProviderConfigurationView;
import view.ExportConfigurationView;
import view.LoadingDialog;
import view.UserGUI;
import view.ViewCard;
import view.helpers.DialogHelper;

public class EmailController {

    private final EmailConfigurationHubView emailHubView;
    private final ExportConfigurationView exportView;
    private final UserGUI userInterface;
    private final Runnable onBackToExport;
    private final EmailConfigurationService emailService;
    private boolean isLoading = false;

    static final java.util.concurrent.ExecutorService emailExecutor =
            java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "email-sender");
                t.setDaemon(true);
                return t;
            });
    private static final long ERROR_COOLDOWN_MS = 30_000;
    private static volatile long lastEmailErrorShown = 0;

    /**
     * Sends email asynchronously for cases 0 and 1 (fire-and-forget).
     * On failure, shows at most one error dialog per ERROR_COOLDOWN_MS window.
     */
    public static void sendEmailAsync(int caseIndex, java.util.Map<String, String> placeholders,
                                      java.util.List<java.nio.file.Path> attachments,
                                      EmailConfigurationService emailSvc) {
        emailExecutor.submit(() -> {
            try {
                boolean sent = emailSvc.sendCaseEmail(caseIndex, placeholders, attachments);
                if (!sent) {
                    showErrorOnce("Error al enviar correo");
                }
            } catch (Exception e) {
                showErrorOnce("Error al enviar correo: " + e.getMessage());
            }
        });
    }

    private static void showErrorOnce(String message) {
        long now = System.currentTimeMillis();
        if (now - lastEmailErrorShown > ERROR_COOLDOWN_MS) {
            lastEmailErrorShown = now;
            SwingUtilities.invokeLater(() ->
                DialogHelper.showErrorMessage(message, "CORREO"));
        }
    }

    private static final int CASE_ROOM = 0;
    private static final int CASE_ITEM = 1;
    private static final int CASE_TURN = 2;

    private static final java.util.Map<Integer, String[]> CASE_VARIABLES = java.util.Map.of(
        CASE_ROOM, new String[]{
            "{motelName}", "{motelAddress}", "{motelID}",
            "{roomString}", "{towerNumber}", "{floorNumber}",
            "{price}", "{serviceDuration}", "{hourService}", "{dateService}",
            "{consecutiveTrans}", "{date}",
            "{register}"
        },
        CASE_ITEM, new String[]{
            "{motelName}", "{motelAddress}", "{motelID}",
            "{roomString}", "{totalPrice}",
            "{hourService}", "{dateService}",
            "{consecutiveTrans}", "{date}",
            "{register}"
        },
        CASE_TURN, new String[]{
            "{motelName}", "{motelAddress}", "{motelID}",
            "{turnNumber}", "{turnStart}", "{turnEnd}", "{turnDuration}",
            "{totalRooms}", "{totalItems}", "{totalSales}",
            "{totalItemRefunds}", "{totalRoomRefunds}", "{totalRefunds}",
            "{totalSpending}", "{totalTurn}",
            "{totalBankTransfers}", "{totalDeposits}", "{totalNet}",
            "{consecutiveTrans}", "{date}",
            "{activityTable}"
        }
    );

    private static final Map<Integer, List<VariableOption>> CASE_VARIABLE_OPTIONS = Map.of(
        CASE_ROOM, List.of(
            new VariableOption("Nombre del Motel", "{motelName}"),
            new VariableOption("Dirección del Motel", "{motelAddress}"),
            new VariableOption("ID del Motel", "{motelID}"),
            new VariableOption("Habitación", "{roomString}"),
            new VariableOption("Torre", "{towerNumber}"),
            new VariableOption("Piso", "{floorNumber}"),
            new VariableOption("Precio", "{price}"),
            new VariableOption("Duración", "{serviceDuration}"),
            new VariableOption("Hora de Ingreso", "{hourService}"),
            new VariableOption("Fecha de Ingreso", "{dateService}"),
            new VariableOption("Transacción", "{consecutiveTrans}"),
            new VariableOption("Fecha Actual", "{date}"),
            new VariableOption("Tabla Registro", "{register}")
        ),
        CASE_ITEM, List.of(
            new VariableOption("Nombre del Motel", "{motelName}"),
            new VariableOption("Dirección del Motel", "{motelAddress}"),
            new VariableOption("ID del Motel", "{motelID}"),
            new VariableOption("Habitación", "{roomString}"),
            new VariableOption("Precio Total", "{totalPrice}"),
            new VariableOption("Hora de Ingreso", "{hourService}"),
            new VariableOption("Fecha de Ingreso", "{dateService}"),
            new VariableOption("Transacción", "{consecutiveTrans}"),
            new VariableOption("Fecha Actual", "{date}"),
            new VariableOption("Tabla Registro", "{register}")
        ),
        CASE_TURN, List.of(
            new VariableOption("Nombre del Motel", "{motelName}"),
            new VariableOption("Dirección del Motel", "{motelAddress}"),
            new VariableOption("ID del Motel", "{motelID}"),
            new VariableOption("Número de Turno", "{turnNumber}"),
            new VariableOption("Inicio de Turno", "{turnStart}"),
            new VariableOption("Fin de Turno", "{turnEnd}"),
            new VariableOption("Duración del Turno", "{turnDuration}"),
            new VariableOption("Total Habitaciones", "{totalRooms}"),
            new VariableOption("Total Artículos", "{totalItems}"),
            new VariableOption("Total Ventas", "{totalSales}"),
            new VariableOption("Devoluciones Artículos", "{totalItemRefunds}"),
            new VariableOption("Devoluciones Habitaciones", "{totalRoomRefunds}"),
            new VariableOption("Total Devoluciones", "{totalRefunds}"),
            new VariableOption("Total Gastos", "{totalSpending}"),
            new VariableOption("Total Turno", "{totalTurn}"),
            new VariableOption("Transferencias Bancarias", "{totalBankTransfers}"),
            new VariableOption("Depósitos", "{totalDeposits}"),
            new VariableOption("Neto", "{totalNet}"),
            new VariableOption("Transacción", "{consecutiveTrans}"),
            new VariableOption("Fecha Actual", "{date}"),
            new VariableOption("Tabla de Actividades", "{activityTable}")
        )
    );

    public EmailController(
            EmailConfigurationHubView emailHubView,
            ExportConfigurationView exportView,
            UserGUI userInterface,
            Runnable onBackToExport,
            EmailConfigurationService emailService) {
        this.emailHubView = emailHubView;
        this.exportView = exportView;
        this.userInterface = userInterface;
        this.onBackToExport = onBackToExport;
        this.emailService = emailService;
    }

    public void initListeners() {
        // Export view → email hub
        exportView.onEmailConfigButton(() -> {
            loadDataIntoViews();
            userInterface.setView(ViewCard.EMAIL_CONFIG_VIEW);
        });

        // Hub → subview navigation
        emailHubView.onProviderButton(() -> userInterface.setView(ViewCard.EMAIL_PROVIDER_VIEW));
        emailHubView.onGeneralConfigurationButton(() -> userInterface.setView(ViewCard.EMAIL_GLOBAL_SETTINGS_VIEW));
        emailHubView.onRoomSaleCaseButton(() -> userInterface.setView(ViewCard.EMAIL_ROOM_CASE_VIEW));
        emailHubView.onSaleCaseButton(() -> userInterface.setView(ViewCard.EMAIL_ITEM_CASE_VIEW));
        emailHubView.onTurnCaseButton(() -> userInterface.setView(ViewCard.EMAIL_TURN_CASE_VIEW));
        emailHubView.onBackButton(onBackToExport);

        // === Email enable/disable toggle with confirmation ===
        emailHubView.onEmailFeatureEnableCheckBox(enabled -> {
            String msg = enabled
                    ? "¿Habilitar el sistema de correos?"
                    : "¿Deshabilitar el sistema de correos?";
            boolean confirmed = DialogHelper.confirmDialog(msg, "CORREO ELECTRÓNICO");
            if (confirmed) {
                emailService.saveEmailEnabled(enabled);
                updateHubStatus();
            } else {
                SwingUtilities.invokeLater(() -> emailHubView.setEmailFeatureEnabled(!enabled));
            }
        });

        // Subview back buttons → hub
        userInterface.getEmailProviderView().onBackButton(() -> userInterface.setView(ViewCard.EMAIL_CONFIG_VIEW));
        userInterface.getEmailRoomCaseView().onBackButton(() -> userInterface.setView(ViewCard.EMAIL_CONFIG_VIEW));
        userInterface.getEmailItemCaseView().onBackButton(() -> userInterface.setView(ViewCard.EMAIL_CONFIG_VIEW));
        userInterface.getEmailTurnCaseView().onBackButton(() -> userInterface.setView(ViewCard.EMAIL_CONFIG_VIEW));
        userInterface.getEmailGlobalSettingsView().onBackButton(() -> userInterface.setView(ViewCard.EMAIL_CONFIG_VIEW));

        // === Provider selection → toggle panel + fill SMTP from preset ===
        EmailProviderConfigurationView providerView = userInterface.getEmailProviderView();
        providerView.onProviderSelection(modeIdx -> {
            boolean appPasswordMode = modeIdx == 0;
            if (appPasswordMode && !isLoading) {
                int subIdx = providerView.getSelectedAppPasswordProviderIndex();
                if (subIdx >= 0) {
                    fillSmtpFromPreset(ProviderPreset.values()[subIdx]);
                }
            }
            providerView.showProviderSubPanel(appPasswordMode);
        });

        // === App password sub-selection → update SMTP fields ===
        providerView.onAppPasswordProviderSelection(subIdx -> {
            if (!isLoading && providerView.getSelectedProviderIndex() == 0) {
                fillSmtpFromPreset(ProviderPreset.values()[subIdx]);
            }
        });

        // === Provider save ===
        providerView.onSaveButton(this::onProviderSave);

        // === Verify connection ===
        providerView.onVerifyConnection(this::onVerifyConnection);

        // === Case saves (room, item, turn) ===
        userInterface.getEmailRoomCaseView().onSaveButton(() -> onCaseSave(CASE_ROOM));
        userInterface.getEmailItemCaseView().onSaveButton(() -> onCaseSave(CASE_ITEM));
        userInterface.getEmailTurnCaseView().onSaveButton(() -> onCaseSave(CASE_TURN));

        // === Markdown help ===
        Runnable showMarkdownHelp = () -> DialogHelper.showInfoMessage(
            "FORMATO MARKDOWN SOPORTADO:\n\n"
          + "# Título principal\n"
          + "## Subtítulo\n"
          + "### Sección\n\n"
          + "**texto en negrita**\n"
          + "*texto en cursiva*\n"
          + "__texto subrayado__\n\n"
          + "- Elemento de lista\n"
          + "1. Elemento numerado\n\n"
          + "[texto del enlace](https://url.com)\n\n"
          + "--- (línea horizontal)\n\n"
          + "Doble salto de línea = nuevo párrafo\n"
          + "Salto simple = nueva línea",
          "AYUDA MARKDOWN"
        );
        userInterface.getEmailRoomCaseView().onMarkdownHelp(showMarkdownHelp);
        userInterface.getEmailItemCaseView().onMarkdownHelp(showMarkdownHelp);
        userInterface.getEmailTurnCaseView().onMarkdownHelp(showMarkdownHelp);

        // === Preview body (converts markdown → HTML and shows in a JLabel that renders HTML) ===
        MarkdownConverter previewConverter = new MarkdownConverter();
        java.util.function.Consumer<EmailCaseConfigurationView> showPreview = caseView -> {
            String rawMarkdown = caseView.getBody();
            String html = previewConverter.toHtml(rawMarkdown);
            String styledHtml = "<html><body style='font-family:Segoe UI,sans-serif;padding:16px;'>"
                    + html + "</body></html>";
            javax.swing.JLabel label = new javax.swing.JLabel(styledHtml);
            label.setPreferredSize(new java.awt.Dimension(550, 400));
            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(label);
            scrollPane.setPreferredSize(new java.awt.Dimension(570, 420));
            javax.swing.JOptionPane.showMessageDialog(
                    javax.swing.SwingUtilities.getWindowAncestor(userInterface.getEmailRoomCaseView()),
                    scrollPane, "VISTA PREVIA - CUERPO DEL CORREO",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        };
        userInterface.getEmailRoomCaseView().onPreviewBodyButton(
                () -> showPreview.accept(userInterface.getEmailRoomCaseView()));
        userInterface.getEmailItemCaseView().onPreviewBodyButton(
                () -> showPreview.accept(userInterface.getEmailItemCaseView()));
        userInterface.getEmailTurnCaseView().onPreviewBodyButton(
                () -> showPreview.accept(userInterface.getEmailTurnCaseView()));

        // === Global settings save ===
        EmailGlobalConfigurationView globalView = userInterface.getEmailGlobalSettingsView();
        globalView.onSaveButton(this::onGlobalSettingsSave);
        globalView.onAddReceiverButton(this::onAddReceiver);
        globalView.onRemoveReceiverButton(this::onRemoveReceiver);
    }

    // ========== Provider Save ==========

    private void onProviderSave() {
        EmailProviderConfigurationView view = userInterface.getEmailProviderView();
        String email = view.getEmailText();
        String name = view.getNameText();
        if (email.isBlank()) {
            DialogHelper.showInfoMessage("Debe ingresar un correo electr\u00f3nico", "ERROR");
            return;
        }

        boolean appPasswordMode = view.getSelectedProviderIndex() == 0;

        String host;
        int port;
        String username;
        String credential;
        AuthMode authMode;

        if (appPasswordMode) {
            int subIdx = view.getSelectedAppPasswordProviderIndex();
            if (subIdx < 0) {
                DialogHelper.showInfoMessage("Seleccione un proveedor de contrase\u00f1a de aplicaci\u00f3n", "ERROR");
                return;
            }
            ProviderPreset preset = ProviderPreset.values()[subIdx];
            host = preset.getSmtpHost();
            port = preset.getSmtpPort();
            username = email;
            credential = view.getAppPasswordText();
            if ("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022".equals(credential)) {
                credential = emailService.loadSecureData()
                        .map(EmailSecureData::credential).orElse("");
            }
            authMode = AuthMode.PASSWORD;
        } else {
            host = view.getSmtpHost();
            if (host.isBlank()) {
                DialogHelper.showInfoMessage("Debe ingresar un servidor SMTP", "ERROR");
                return;
            }
            try {
                port = Integer.parseInt(view.getSmtpPort());
            } catch (NumberFormatException e) {
                port = 587;
            }
            if (port <= 0) port = 587;
            String smtpUser = view.getSmtpUser();
            username = smtpUser.isBlank() ? email : smtpUser;
            credential = view.getSmtpPassword();
            if ("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022".equals(credential)) {
                credential = emailService.loadSecureData()
                        .map(EmailSecureData::credential).orElse("");
            }
            authMode = smtpUser.isBlank() ? AuthMode.NONE : AuthMode.PASSWORD;
        }

        boolean useTls = port != 465;
        boolean useSsl = port == 465;

        EmailSmtpConfig smtp = new EmailSmtpConfig(host, port, useTls, useSsl, authMode, 5000);

        EmailSecureData existingSecure = emailService.loadSecureData().orElse(null);
        EmailSecureData secure = new EmailSecureData(
                username,
                credential,
                existingSecure != null ? existingSecure.receivers() : List.of(),
                existingSecure != null ? existingSecure.cc() : List.of(),
                existingSecure != null ? existingSecure.bcc() : List.of(),
                existingSecure != null ? existingSecure.caseSpecificReceivers() : Map.of());

        List<EmailCaseConfig> cases = emailService.loadCaseConfigs().orElse(List.of());

        emailService.saveEmailConfig(name, smtp, cases);
        emailService.saveSecureData(secure);
        updateHubStatus();
        DialogHelper.showInfoMessage("Configuraci\u00f3n de correo guardada", "GUARDADO");
    }

    // ========== Verify Connection ==========

    private void onVerifyConnection() {
        EmailProviderConfigurationView view = userInterface.getEmailProviderView();
        String email = view.getEmailText();
        if (email.isBlank()) {
            DialogHelper.showInfoMessage("Debe ingresar un correo electr\u00f3nico", "ERROR");
            return;
        }

        boolean appPasswordMode = view.getSelectedProviderIndex() == 0;

        String host;
        int port;
        String username;
        String credential;

        if (appPasswordMode) {
            int subIdx = view.getSelectedAppPasswordProviderIndex();
            if (subIdx < 0) {
                DialogHelper.showInfoMessage("Seleccione un proveedor de contrase\u00f1a de aplicaci\u00f3n", "ERROR");
                return;
            }
            ProviderPreset preset = ProviderPreset.values()[subIdx];
            host = preset.getSmtpHost();
            port = preset.getSmtpPort();
            username = email;
            credential = view.getAppPasswordText();
            if ("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022".equals(credential)) {
                credential = emailService.loadSecureData()
                        .map(EmailSecureData::credential).orElse("");
            }
        } else {
            host = view.getSmtpHost();
            if (host.isBlank()) {
                DialogHelper.showInfoMessage("Debe ingresar un servidor SMTP", "ERROR");
                return;
            }
            try {
                port = Integer.parseInt(view.getSmtpPort());
            } catch (NumberFormatException e) {
                port = 587;
            }
            if (port <= 0) port = 587;
            String smtpUser = view.getSmtpUser();
            username = smtpUser.isBlank() ? email : smtpUser;
            credential = view.getSmtpPassword();
            if ("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022".equals(credential)) {
                credential = emailService.loadSecureData()
                        .map(EmailSecureData::credential).orElse("");
            }
        }

        if (credential == null || credential.isBlank()) {
            DialogHelper.showInfoMessage("Debe ingresar una contrase\u00f1a para verificar la conexi\u00f3n", "ERROR");
            return;
        }

        boolean useTls = port != 465;
        boolean useSsl = port == 465;

        EmailSmtpConfig smtp = new EmailSmtpConfig(host, port, useTls, useSsl, AuthMode.PASSWORD, 5000);
        EmailSmtpConfig finalSmtp = smtp;
        String finalUsername = username;
        String finalCredential = credential;

        java.awt.Window parent = SwingUtilities.getWindowAncestor(view);
        LoadingDialog loading = new LoadingDialog(parent, "Verificando conexi\u00f3n SMTP...");
        loading.showAsync(() -> {
            boolean ok = emailService.verifyConnection(finalSmtp, finalUsername, finalCredential);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    DialogHelper.showInfoMessage("Conexi\u00f3n exitosa", "VERIFICAR");
                } else {
                    DialogHelper.showErrorMessage("Error de conexi\u00f3n. Revise los datos del proveedor", "ERROR");
                }
            });
        });
    }

    // ========== Case Save ==========

    private void onCaseSave(int caseIndex) {
        EmailCaseConfigurationView view = getCaseView(caseIndex);
        if (view.hasUnmappedVariables()) {
            DialogHelper.showErrorMessage(
                "Hay variables sin asignar en el texto.\n" +
                "Seleccione una variable del sistema para cada {variable}\n" +
                "en la tabla antes de guardar.",
                "VARIABLES SIN ASIGNAR");
            return;
        }
        boolean enabled = view.isCaseEnabled();
        boolean useGlobal = view.isUsingGlobalReceivers();
        List<String> specificReceivers = new ArrayList<>();
        var receiverModel = view.getSpecificReceiversModel();
        for (int i = 0; i < receiverModel.size(); i++) {
            specificReceivers.add(receiverModel.getElementAt(i));
        }
        String subject = view.getSubject();
        String body = view.getBody();
        List<String> attachments = new ArrayList<>();
        var attModel = view.getAttachmentModel();
        for (int i = 0; i < attModel.size(); i++) {
            if (attModel.getElementAt(i).isSelected()) {
                attachments.add(attModel.getElementAt(i).getName());
            }
        }

        List<EmailCaseConfig> cases = new ArrayList<>(emailService.loadCaseConfigs().orElse(List.of()));
        while (cases.size() <= caseIndex) {
            cases.add(new EmailCaseConfig(cases.size(), false, true, List.of(), "", "", List.of(), null));
        }
        cases.set(caseIndex, new EmailCaseConfig(caseIndex, enabled, useGlobal,
                specificReceivers, subject, body, attachments,
                view.getVariableMappings()));

        String senderName = emailService.loadSenderName().orElse("");
        EmailSmtpConfig smtp = emailService.loadSmtpConfig().orElse(null);
        emailService.saveEmailConfig(senderName, smtp, cases);
        updateHubStatus();
        DialogHelper.showInfoMessage("Configuraci\u00f3n guardada", "GUARDADO");
    }

    // ========== Add / Remove Receivers ==========

    private void onAddReceiver() {
        EmailGlobalConfigurationView view = userInterface.getEmailGlobalSettingsView();
        String input = JOptionPane.showInputDialog(view,
                "Ingrese el correo electr\u00f3nico:", "A\u00d1ADIR",
                JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isBlank()) {
            String email = input.trim();
            JList<?> activeList = view.getActiveList();
            DefaultListModel<String> model = (DefaultListModel<String>) activeList.getModel();
            model.addElement(email);
            activeList.setSelectedIndex(model.getSize() - 1);
        }
    }

    private void onRemoveReceiver() {
        EmailGlobalConfigurationView view = userInterface.getEmailGlobalSettingsView();
        JList<?> activeList = view.getActiveList();
        DefaultListModel<String> model = (DefaultListModel<String>) activeList.getModel();
        int idx = activeList.getSelectedIndex();
        if (idx >= 0) {
            model.remove(idx);
            int size = model.getSize();
            if (size > 0) {
                activeList.setSelectedIndex(Math.min(idx, size - 1));
            }
        }
    }

    // ========== Global Settings Save ==========

    private void onGlobalSettingsSave() {
        EmailGlobalConfigurationView view = userInterface.getEmailGlobalSettingsView();
        String senderName = view.getSenderName();

        List<String> receivers = listModelToList(view.getReceiverList());
        List<String> cc = listModelToList(view.getCarbonCopyList());
        List<String> bcc = listModelToList(view.getBindCarbonCopyList());

        EmailSecureData existing = emailService.loadSecureData().orElse(null);
        String username = existing != null ? existing.username() : "";
        String credential = existing != null ? existing.credential() : "";
        Map<Integer, List<String>> caseSpecific = existing != null
                ? existing.caseSpecificReceivers() : Map.of();

        EmailSecureData secure = new EmailSecureData(
                username, credential, receivers, cc, bcc, caseSpecific);
        emailService.saveSecureData(secure);

        EmailSmtpConfig smtp = emailService.loadSmtpConfig().orElse(null);
        List<EmailCaseConfig> cases = emailService.loadCaseConfigs().orElse(List.of());
        emailService.saveEmailConfig(senderName, smtp, cases);
        updateHubStatus();
        DialogHelper.showInfoMessage("Configuraci\u00f3n global guardada", "GUARDADO");
    }

    // ========== Load Data Into Views ==========

    private void loadDataIntoViews() {
        EmailProviderConfigurationView providerView = userInterface.getEmailProviderView();
        providerView.populateProviderCombos();

        isLoading = true;
        ProviderPreset matchedPreset = ProviderPreset.fromSmtpConfig(
                emailService.loadSmtpConfig().orElse(null));
        if (matchedPreset != ProviderPreset.CUSTOM) {
            providerView.setSelectedProviderIndex(0);
            providerView.setAppPasswordProviderIndex(matchedPreset.ordinal());
            providerView.setSmtpHost(matchedPreset.getSmtpHost());
            providerView.setSmtpPort(String.valueOf(matchedPreset.getSmtpPort()));
        } else if (emailService.loadSmtpConfig().isPresent()) {
            EmailSmtpConfig smtp = emailService.loadSmtpConfig().get();
            providerView.setSelectedProviderIndex(1);
            providerView.setSmtpHost(smtp.smtpHost());
            providerView.setSmtpPort(String.valueOf(smtp.smtpPort()));
        } else {
            providerView.setSelectedProviderIndex(1);
        }
        isLoading = false;

        emailService.loadSenderName().ifPresent(providerView::setNameText);
        emailService.loadSecureData().ifPresent(secure -> {
            providerView.setEmailText(secure.username());
            String credential = secure.credential();
            if (credential != null && !credential.isBlank()) {
                String obfuscated = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";
                if (matchedPreset != ProviderPreset.CUSTOM) {
                    providerView.setAppPasswordText(obfuscated);
                } else {
                    providerView.setSmtpPasswordText(obfuscated);
                }
            }
        });

        EmailGlobalConfigurationView globalView = userInterface.getEmailGlobalSettingsView();
        emailService.loadSenderName().ifPresent(globalView::setSenderName);
        emailService.loadSecureData().ifPresent(secure -> {
            fillListModel(globalView.getReceiverList(), secure.receivers());
            fillListModel(globalView.getCarbonCopyList(), secure.cc());
            fillListModel(globalView.getBindCarbonCopyList(), secure.bcc());
            globalView.clearActiveSelection();
        });

        for (int i = 0; i < CASE_VARIABLES.size(); i++) {
            EmailCaseConfigurationView caseView = getCaseView(i);
            caseView.setAvailableVariables(CASE_VARIABLES.getOrDefault(i, new String[0]));
            caseView.setVariableOptions(CASE_VARIABLE_OPTIONS.getOrDefault(i, List.of()));
        }

        emailService.loadCaseConfigs().ifPresent(cases -> {
            for (int i = 0; i < cases.size(); i++) {
                EmailCaseConfig cfg = cases.get(i);
                EmailCaseConfigurationView caseView = getCaseView(i);
                caseView.setCaseEnabled(cfg.enabled());
                caseView.setUseGlobalReceivers(cfg.useGlobalReceivers());
                caseView.setSubject(cfg.subject());
                caseView.setBody(cfg.body());
                caseView.rebuildVariablesTable();
                DefaultListModel<String> recvModel = caseView.getSpecificReceiversModel();
                recvModel.clear();
                for (String r : cfg.specificReceivers()) {
                    recvModel.addElement(r);
                }
                var attModel = caseView.getAttachmentModel();
                List<String> savedAttachments = cfg.attachments();
                for (int ai = 0; ai < attModel.size(); ai++) {
                    var item = attModel.getElementAt(ai);
                    item.setSelected(savedAttachments != null && savedAttachments.contains(item.getName()));
                }
                caseView.refreshAttachmentList();
                emailHubView.setCaseEnabled(i, cfg.enabled());
            }
        });
        emailHubView.setEmailFeatureEnabled(emailService.isEmailEnabled());
        updateHubStatus();

        String decryptionError = emailService.getDecryptionError();
        if (decryptionError != null) {
            int choice = JOptionPane.showOptionDialog(
                    SwingUtilities.getWindowAncestor(userInterface.getEmailProviderView()),
                    "No se pudieron cargar los datos de correo cifrados.\n" +
                    "Es posible que el archivo est\u00e9 da\u00f1ado.\n\n" +
                    "Detalle t\u00e9cnico: " + decryptionError,
                    "ERROR DE CIFRADO",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, new String[]{"Reconfigurar", "Ignorar"}, "Reconfigurar");
            if (choice == 0) {
                userInterface.setView(ViewCard.EMAIL_PROVIDER_VIEW);
            }
        }
    }

    private void fillSmtpFromPreset(ProviderPreset preset) {
        if (preset == ProviderPreset.CUSTOM) return;
        EmailProviderConfigurationView view = userInterface.getEmailProviderView();
        view.setSmtpHost(preset.getSmtpHost());
        view.setSmtpPort(String.valueOf(preset.getSmtpPort()));
    }

    private void updateHubStatus() {
        boolean masterEnabled = emailService.isEmailEnabled();
        emailHubView.setEmailFeatureEnabled(masterEnabled);
        emailHubView.setEmailStatus(masterEnabled ? "HABILITADO" : "DESHABILITADO");
        emailService.loadCaseConfigs().ifPresent(cases -> {
            for (int i = 0; i < cases.size(); i++) {
                emailHubView.setCaseEnabled(i, cases.get(i).enabled());
            }
        });
    }

    // ========== Helpers ==========

    private EmailCaseConfigurationView getCaseView(int caseIndex) {
        return switch (caseIndex) {
            case 0 -> userInterface.getEmailRoomCaseView();
            case 1 -> userInterface.getEmailItemCaseView();
            case 2 -> userInterface.getEmailTurnCaseView();
            default -> throw new IllegalArgumentException("Unknown case index: " + caseIndex);
        };
    }

    private static List<String> listModelToList(JList list) {
        var model = list.getModel();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            Object elem = model.getElementAt(i);
            if (elem != null) result.add(elem.toString());
        }
        return result;
    }

    private static void fillListModel(JList list, List<String> values) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String v : values) {
            model.addElement(v);
        }
        list.setModel(model);
    }

    /**
     * Convenience method for controllers: validates case config, resolves attachments,
     * and fires the async email in one call. Returns false if email is disabled or
     * the case config is invalid.
     */
    public static boolean trySendCaseEmail(int caseIndex, Map<String, String> placeholders,
                                            EmailConfigurationService emailSvc, int consecutive) {
        if (!emailSvc.isEmailEnabled() || !emailSvc.validateCaseConfig(caseIndex)) return false;
        List<Path> attachments = resolveCaseAttachments(emailSvc, caseIndex, consecutive);
        sendEmailAsync(caseIndex, placeholders, attachments, emailSvc);
        return true;
    }

    public static List<Path> resolveCaseAttachments(EmailConfigurationService emailSvc, int caseIndex, int consecutive) {
        List<String> names = emailSvc.loadCaseConfigs()
                .filter(cases -> caseIndex < cases.size())
                .map(cases -> cases.get(caseIndex).attachments())
                .orElse(List.of());
        if (names == null || names.isEmpty()) return List.of();
        return emailSvc.resolveAttachmentPaths(names, consecutive);
    }
}
