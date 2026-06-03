package controller.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import model.email.config.AuthMode;
import model.email.config.EmailCaseConfig;
import model.email.config.EmailSecureData;
import model.email.config.EmailSmtpConfig;
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

    private static final int CASE_ROOM = 0;
    private static final int CASE_ITEM = 1;
    private static final int CASE_TURN = 2;

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

        // Subview back buttons → hub
        userInterface.getEmailProviderView().onBackButton(() -> userInterface.setEmailConfigView());
        userInterface.getEmailRoomCaseView().onBackButton(() -> userInterface.setEmailConfigView());
        userInterface.getEmailItemCaseView().onBackButton(() -> userInterface.setEmailConfigView());
        userInterface.getEmailTurnCaseView().onBackButton(() -> userInterface.setEmailConfigView());
        userInterface.getEmailGlobalSettingsView().onBackButton(() -> userInterface.setEmailConfigView());

        // === Provider save ===
        userInterface.getEmailProviderView().onSaveButton(this::onProviderSave);

        // === Verify connection ===
        userInterface.getEmailProviderView().onVerifyConnection(this::onVerifyConnection);

        // === Case saves (room, item, turn) ===
        userInterface.getEmailRoomCaseView().onSaveButton(() -> onCaseSave(CASE_ROOM));
        userInterface.getEmailItemCaseView().onSaveButton(() -> onCaseSave(CASE_ITEM));
        userInterface.getEmailTurnCaseView().onSaveButton(() -> onCaseSave(CASE_TURN));

        // === Global settings save ===
        userInterface.getEmailGlobalSettingsView().onSaveButton(this::onGlobalSettingsSave);
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

        String host = view.getSmtpHost();
        int port;
        try {
            port = Integer.parseInt(view.getSmtpPort());
        } catch (NumberFormatException e) {
            port = 587;
        }
        String smtpUser = view.getSmtpUser();
        String smtpPass = view.getSmtpPassword();
        boolean useTls = port != 465;
        boolean useSsl = port == 465;

        EmailSmtpConfig smtp = new EmailSmtpConfig(
                host.isBlank() ? "smtp.gmail.com" : host,
                port, useTls, useSsl,
                smtpUser.isBlank() ? AuthMode.NONE : AuthMode.PASSWORD, 5000);

        String username = smtpUser.isBlank() ? email : smtpUser;
        String credential = smtpPass.isBlank() ? view.getAppPasswordText() : smtpPass;

        EmailSecureData existingSecure = emailService.loadSecureData().orElse(null);
        EmailSecureData secure = new EmailSecureData(
                username,
                credential,
                existingSecure != null ? existingSecure.receivers() : List.of(),
                existingSecure != null ? existingSecure.cc() : List.of(),
                existingSecure != null ? existingSecure.bcc() : List.of(),
                existingSecure != null ? existingSecure.caseSpecificReceivers() : Map.of());

        List<EmailCaseConfig> cases = emailService.loadCaseConfigs().orElse(List.of());
        String senderName = name;

        emailService.saveEmailConfig(senderName, smtp, cases);
        emailService.saveSecureData(secure);
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

        String host = view.getSmtpHost();
        int port;
        try {
            port = Integer.parseInt(view.getSmtpPort());
        } catch (NumberFormatException e) {
            port = 587;
        }
        String smtpUser = view.getSmtpUser();
        String smtpPass = view.getSmtpPassword();
        boolean useTls = port != 465;
        boolean useSsl = port == 465;

        String username = smtpUser.isBlank() ? email : smtpUser;
        String credential = smtpPass.isBlank() ? view.getAppPasswordText() : smtpPass;

        if (credential.isBlank()) {
            DialogHelper.showInfoMessage("Debe ingresar una contrase\u00f1a para verificar la conexi\u00f3n", "ERROR");
            return;
        }

        EmailSmtpConfig smtp = new EmailSmtpConfig(
                host.isBlank() ? "smtp.gmail.com" : host,
                port, useTls, useSsl, AuthMode.PASSWORD, 5000);

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

        DialogHelper.showInfoMessage("Configuraci\u00f3n guardada", "GUARDADO");
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

        DialogHelper.showInfoMessage("Configuraci\u00f3n global guardada", "GUARDADO");
    }

    // ========== Load Data Into Views ==========

    private void loadDataIntoViews() {
        EmailProviderConfigurationView providerView = userInterface.getEmailProviderView();
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
        });

        boolean masterEnabled = emailService.isEmailEnabled();
        emailHubView.setEmailStatus(masterEnabled ? "HABILITADO" : "DESHABILITADO");

        emailService.loadCaseConfigs().ifPresent(cases -> {
            for (int i = 0; i < cases.size(); i++) {
                EmailCaseConfig cfg = cases.get(i);
                EmailCaseConfigurationView caseView = getCaseView(i);
                caseView.setCaseEnabled(cfg.enabled());
                caseView.setUseGlobalReceivers(cfg.useGlobalReceivers());
                caseView.setSubject(cfg.subject());
                caseView.setBody(cfg.body());
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
