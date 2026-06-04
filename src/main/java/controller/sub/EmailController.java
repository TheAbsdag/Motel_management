package controller.sub;

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
import model.modelManagers.EmailConfigurationService;
import view.EmailCaseConfigurationView;
import view.EmailConfigurationHubView;
import view.EmailGlobalConfigurationView;
import view.EmailProviderConfigurationView;
import view.ExportConfigurationView;
import view.UserGUI;
import view.helpers.DialogHelper;

public class EmailController {

    private final EmailConfigurationHubView emailHubView;
    private final ExportConfigurationView exportView;
    private final UserGUI userInterface;
    private final Runnable onBackToExport;
    private final EmailConfigurationService emailService;
    private boolean isLoading = false;

    private static final int CASE_ROOM = 0;
    private static final int CASE_ITEM = 1;
    private static final int CASE_TURN = 2;

    private static final java.util.Map<Integer, String[]> CASE_VARIABLES = java.util.Map.of(
        CASE_ROOM, new String[]{
            "{motelName}", "{motelAddress}", "{motelID}",
            "{roomString}", "{towerNumber}", "{floorNumber}", "{date}"
        },
        CASE_ITEM, new String[]{
            "{motelName}", "{motelAddress}", "{motelID}",
            "{totalPrice}", "{date}"
        },
        CASE_TURN, new String[]{
            "{motelName}", "{motelAddress}", "{motelID}",
            "{turnNumber}", "{turnStart}", "{turnEnd}",
            "{totalRooms}", "{totalItems}", "{totalSales}",
            "{totalRefunds}", "{totalSpending}", "{totalTurn}",
            "{totalBankTransfers}", "{totalDeposits}", "{totalNet}",
            "{consecutiveTrans}", "{date}"
        }
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
            userInterface.setEmailConfigView();
        });

        // Hub → subview navigation
        emailHubView.onProviderButton(() -> userInterface.setEmailProviderView());
        emailHubView.onGeneralConfigurationButton(() -> userInterface.setEmailGlobalSettingsView());
        emailHubView.onRoomSaleCaseButton(() -> userInterface.setEmailRoomCaseView());
        emailHubView.onSaleCaseButton(() -> userInterface.setEmailItemCaseView());
        emailHubView.onTurnCaseButton(() -> userInterface.setEmailTurnCaseView());
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
        userInterface.getEmailProviderView().onBackButton(() -> userInterface.setEmailConfigView());
        userInterface.getEmailRoomCaseView().onBackButton(() -> userInterface.setEmailConfigView());
        userInterface.getEmailItemCaseView().onBackButton(() -> userInterface.setEmailConfigView());
        userInterface.getEmailTurnCaseView().onBackButton(() -> userInterface.setEmailConfigView());
        userInterface.getEmailGlobalSettingsView().onBackButton(() -> userInterface.setEmailConfigView());

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
        }

        if (credential.isBlank()) {
            DialogHelper.showInfoMessage("Debe ingresar una contrase\u00f1a para verificar la conexi\u00f3n", "ERROR");
            return;
        }

        boolean useTls = port != 465;
        boolean useSsl = port == 465;

        EmailSmtpConfig smtp = new EmailSmtpConfig(host, port, useTls, useSsl, AuthMode.PASSWORD, 5000);

        boolean ok = emailService.verifyConnection(smtp, username, credential);
        if (ok) {
            DialogHelper.showInfoMessage("Conexi\u00f3n exitosa", "VERIFICAR");
        } else {
            DialogHelper.showErrorMessage("Error de conexi\u00f3n. Revise los datos del proveedor", "ERROR");
        }
    }

    // ========== Case Save ==========

    private void onCaseSave(int caseIndex) {
        EmailCaseConfigurationView view = getCaseView(caseIndex);
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
            cases.add(new EmailCaseConfig(cases.size(), false, true, List.of(), "", "", List.of()));
        }
        cases.set(caseIndex, new EmailCaseConfig(caseIndex, enabled, useGlobal,
                specificReceivers, subject, body, attachments));

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
        });

        EmailGlobalConfigurationView globalView = userInterface.getEmailGlobalSettingsView();
        emailService.loadSenderName().ifPresent(globalView::setSenderName);
        emailService.loadSecureData().ifPresent(secure -> {
            fillListModel(globalView.getReceiverList(), secure.receivers());
            fillListModel(globalView.getCarbonCopyList(), secure.cc());
            fillListModel(globalView.getBindCarbonCopyList(), secure.bcc());
            globalView.clearActiveSelection();
        });

        emailService.loadCaseConfigs().ifPresent(cases -> {
            for (int i = 0; i < cases.size(); i++) {
                EmailCaseConfig cfg = cases.get(i);
                EmailCaseConfigurationView caseView = getCaseView(i);
                caseView.setCaseEnabled(cfg.enabled());
                caseView.setUseGlobalReceivers(cfg.useGlobalReceivers());
                caseView.setSubject(cfg.subject());
                caseView.setBody(cfg.body());
                caseView.setAvailableVariables(CASE_VARIABLES.getOrDefault(i, new String[0]));
                DefaultListModel<String> recvModel = caseView.getSpecificReceiversModel();
                recvModel.clear();
                for (String r : cfg.specificReceivers()) {
                    recvModel.addElement(r);
                }
                var attModel = caseView.getAttachmentModel();
                for (int ai = 0; ai < attModel.size(); ai++) {
                    var item = attModel.getElementAt(ai);
                    item.setSelected(cfg.attachments().contains(item.getName()));
                }
                emailHubView.setCaseEnabled(i, cfg.enabled());
            }
        });
        emailHubView.setEmailFeatureEnabled(emailService.isEmailEnabled());
        updateHubStatus();
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
}
