package view;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import view.interfaces.TimeLabelInterface;

/**
 * Full email configuration form with provider-specific fields, credential
 * input, receivers list management, and a test email section.
 */
public class EmailConfigurationView extends JPanel implements TimeLabelInterface {

    private static final String[] PROVIDERS = {"APP PASSWORD", "SMTP PERSONALIZADO"};
    private static final String[] APP_PASS_PROVIDERS = {"GMAIL", "OUTLOOK"};

    private JLabel titleLabel;
    private JComboBox<String> providerCombo;
    private JTextField emailField;
    private JTextField displayNameField;
    private JLabel providerLabel;
    private JLabel emailLabel;
    private JLabel displayNameLabel;

    private JPanel providerCards;
    private CardLayout providerCardLayout;

    private JPanel appPasswordPanel;
    private JLabel appPassProviderLabel;
    private JComboBox<String> appPassProviderCombo;
    private JPanel appPassSubCards;
    private CardLayout appPassSubCardLayout;

    private JPanel gmailAppPassCard;
    private JLabel gmailAppPassLabel;
    private JPasswordField gmailAppPassField;

    private JPanel outlookAppPassCard;
    private JLabel outlookAppPassLabel;
    private JPasswordField outlookAppPassField;

    private JPanel smtpPanel;
    private JLabel smtpHostLabel;
    private JTextField smtpHostField;
    private JLabel smtpPortLabel;
    private JTextField smtpPortField;
    private JLabel smtpUserLabel;
    private JTextField smtpUserField;
    private JLabel smtpPassLabel;
    private JPasswordField smtpPassField;
    private JCheckBox smtpTlsCheck;
    private JCheckBox smtpSslCheck;

    private JLabel receiversLabel;
    private DefaultListModel<String> receiversListModel;
    private JList<String> receiversList;
    private JScrollPane receiversScrollPane;
    private JButton addReceiverButton;
    private JButton removeReceiverButton;

    private JLabel testSeparator;
    private JLabel testRecipientLabel;
    private JTextField testRecipientField;
    private JLabel testSubjectLabel;
    private JTextField testSubjectField;
    private JCheckBox htmlCheck;
    private JScrollPane testBodyScroll;
    private JTextArea testBodyArea;

    private JButton testButton;
    private JButton saveButton;
    private JButton backButton;

    public EmailConfigurationView() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill,hidemode 3,insets 20",
                "[right][grow,fill][grow,fill]",
                "[]" +
                "[]" +
                "[]" +
                "[]" +
                "[]" +
                "[150:150:200,grow,fill]" +
                "[grow,fill]" +
                "[]" +
                "[]" +
                "[]" +
                "[100,grow,fill]" +
                "[]"));

        titleLabel = new JLabel("CONFIGURACION DE CORREO");
        titleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, "cell 0 0 3 1,growx");

        providerLabel = new JLabel("PROVEEDOR:");
        providerLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        add(providerLabel, "cell 0 2");

        providerCombo = new JComboBox<>(PROVIDERS);
        providerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(providerCombo, "cell 1 2");

        emailLabel = new JLabel("CORREO:");
        emailLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        add(emailLabel, "cell 0 3");

        emailField = new JTextField(30);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(emailField, "cell 1 3");

        displayNameLabel = new JLabel("NOMBRE:");
        displayNameLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        add(displayNameLabel, "cell 0 4");

        displayNameField = new JTextField(30);
        displayNameField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(displayNameField, "cell 1 4");

        buildReceiversSection();
        receiversLabel = new JLabel("DESTINATARIOS GUARDADOS:");
        receiversLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        add(receiversLabel, "cell 0 5,aligny top");

        add(receiversScrollPane, "cell 1 5,grow");

        JPanel receiversButtonPanel = new JPanel(new MigLayout("insets 0", "[grow,fill]", "[]" + "[]"));
        addReceiverButton = new JButton("AGREGAR");
        addReceiverButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
        addReceiverButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this,
                    "Ingrese el correo del destinatario:", "AGREGAR DESTINATARIO",
                    javax.swing.JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isBlank()) {
                String email = input.trim();
                if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                    JOptionPane.showMessageDialog(this,
                            "El correo ingresado no es valido:\n" + email,
                            "ERROR",
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
                receiversListModel.addElement(email);
            }
        });
        receiversButtonPanel.add(addReceiverButton, "cell 0 0");
        removeReceiverButton = new JButton("QUITAR");
        removeReceiverButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
        removeReceiverButton.addActionListener(e -> {
            int selected = receiversList.getSelectedIndex();
            if (selected >= 0) {
                receiversListModel.remove(selected);
            }
        });
        receiversButtonPanel.add(removeReceiverButton, "cell 0 1");
        add(receiversButtonPanel, "cell 2 5,aligny top");

        providerCardLayout = new CardLayout();
        providerCards = new JPanel(providerCardLayout);
        add(providerCards, "cell 0 6 3 1,grow");

        buildAppPasswordPanel();
        buildSmtpPanel();

        providerCards.add(appPasswordPanel, "0");
        providerCards.add(smtpPanel, "1");
        providerCardLayout.show(providerCards, "0");

        testSeparator = new JLabel("--- PRUEBA DE ENVIO ---");
        testSeparator.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
        testSeparator.setHorizontalAlignment(SwingConstants.CENTER);
        add(testSeparator, "cell 0 7 3 1,growx");

        testRecipientLabel = new JLabel("DESTINATARIO:");
        testRecipientLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        add(testRecipientLabel, "cell 0 8");

        testRecipientField = new JTextField(30);
        testRecipientField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(testRecipientField, "cell 1 8 2 1");

        testSubjectLabel = new JLabel("ASUNTO:");
        testSubjectLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        add(testSubjectLabel, "cell 0 9");

        testSubjectField = new JTextField("Prueba de configuracion correo");
        testSubjectField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(testSubjectField, "cell 1 9 2 1");

        htmlCheck = new JCheckBox("ES HTML");
        htmlCheck.setSelected(true);
        htmlCheck.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
        add(htmlCheck, "cell 2 9,alignx right");

        testBodyArea = new JTextArea(8, 40);
        testBodyArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        testBodyArea.setWrapStyleWord(true);
        testBodyArea.setLineWrap(true);
        testBodyArea.setText(getDefaultTemplate());
        testBodyScroll = new JScrollPane(testBodyArea);
        testBodyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(testBodyScroll, "cell 0 10 3 1,grow");

        testButton = new JButton("ENVIAR PRUEBA");
        testButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        add(testButton, "cell 0 11");

        saveButton = new JButton("GUARDAR CONFIGURACION");
        saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        add(saveButton, "cell 1 11");

        backButton = new JButton("VOLVER");
        backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        add(backButton, "cell 2 11");
    }

    private void buildReceiversSection() {
        receiversListModel = new DefaultListModel<>();
        receiversList = new JList<>(receiversListModel);
        receiversList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        receiversList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        receiversScrollPane = new JScrollPane(receiversList);
        receiversScrollPane.setPreferredSize(new Dimension(200, 80));
        receiversScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    private void buildAppPasswordPanel() {
        appPasswordPanel = new JPanel(new MigLayout("fill,insets 10",
                "[right][grow,fill]", "[]" + "[grow,fill]"));
        appPasswordPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("APP PASSWORD"));

        appPassProviderLabel = new JLabel("PROVEEDOR:");
        appPassProviderLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        appPasswordPanel.add(appPassProviderLabel, "cell 0 0");

        appPassProviderCombo = new JComboBox<>(APP_PASS_PROVIDERS);
        appPassProviderCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        appPasswordPanel.add(appPassProviderCombo, "cell 1 0");

        appPassSubCardLayout = new CardLayout();
        appPassSubCards = new JPanel(appPassSubCardLayout);
        appPasswordPanel.add(appPassSubCards, "cell 0 1 2 1,grow");

        buildGmailAppPassCard();
        buildOutlookAppPassCard();

        appPassSubCards.add(gmailAppPassCard, "0");
        appPassSubCards.add(outlookAppPassCard, "1");
        appPassSubCardLayout.show(appPassSubCards, "0");
    }

    private void buildGmailAppPassCard() {
        gmailAppPassCard = new JPanel(new MigLayout("fill,insets 0", "[right][grow,fill]", "[]"));
        gmailAppPassLabel = new JLabel("CONTRASENA APLICACION:");
        gmailAppPassLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        gmailAppPassCard.add(gmailAppPassLabel, "cell 0 0");
        gmailAppPassField = new JPasswordField(30);
        gmailAppPassField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gmailAppPassCard.add(gmailAppPassField, "cell 1 0");
    }

    private void buildOutlookAppPassCard() {
        outlookAppPassCard = new JPanel(new MigLayout("fill,insets 0", "[right][grow,fill]", "[]"));
        outlookAppPassLabel = new JLabel("CONTRASENA APLICACION:");
        outlookAppPassLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        outlookAppPassCard.add(outlookAppPassLabel, "cell 0 0");
        outlookAppPassField = new JPasswordField(30);
        outlookAppPassField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        outlookAppPassCard.add(outlookAppPassField, "cell 1 0");
    }

    private void buildSmtpPanel() {
        smtpPanel = new JPanel(new MigLayout("fill,insets 10", "[right][grow,fill]", "[]" + "[]" + "[]" + "[]" + "[]"));
        smtpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("SMTP PERSONALIZADO"));

        smtpHostLabel = new JLabel("HOST:");
        smtpHostLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        smtpPanel.add(smtpHostLabel, "cell 0 0");
        smtpHostField = new JTextField(30);
        smtpHostField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        smtpPanel.add(smtpHostField, "cell 1 0");

        smtpPortLabel = new JLabel("PUERTO:");
        smtpPortLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        smtpPanel.add(smtpPortLabel, "cell 0 1");
        smtpPortField = new JTextField("587", 6);
        smtpPortField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        smtpPanel.add(smtpPortField, "cell 1 1");

        smtpUserLabel = new JLabel("USUARIO:");
        smtpUserLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        smtpPanel.add(smtpUserLabel, "cell 0 2");
        smtpUserField = new JTextField(30);
        smtpUserField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        smtpPanel.add(smtpUserField, "cell 1 2");

        smtpPassLabel = new JLabel("CONTRASENA:");
        smtpPassLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        smtpPanel.add(smtpPassLabel, "cell 0 3");
        smtpPassField = new JPasswordField(30);
        smtpPassField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        smtpPanel.add(smtpPassField, "cell 1 3");

        smtpTlsCheck = new JCheckBox("USAR TLS");
        smtpTlsCheck.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        smtpTlsCheck.setSelected(true);
        smtpPanel.add(smtpTlsCheck, "cell 1 4");

        smtpSslCheck = new JCheckBox("USAR SSL");
        smtpSslCheck.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        smtpPanel.add(smtpSslCheck, "cell 1 5");
    }

    public void onProviderChanged(Consumer<Integer> action) {
        providerCombo.addActionListener(e -> action.accept(providerCombo.getSelectedIndex()));
    }

    public void onSaveConfig(Runnable action) {
        saveButton.addActionListener(e -> action.run());
    }

    public void onTestEmail(Runnable action) {
        testButton.addActionListener(e -> action.run());
    }

    public void onBackButton(Runnable action) {
        backButton.addActionListener(e -> action.run());
    }

    public void showProviderCard(int index) {
        if (index >= 0 && index < PROVIDERS.length) {
            providerCardLayout.show(providerCards, String.valueOf(index));
        }
    }

    public String getEmail() { return emailField.getText().trim(); }
    public void setEmail(String v) { emailField.setText(v); }

    public String getDisplayName() { return displayNameField.getText().trim(); }
    public void setDisplayName(String v) { displayNameField.setText(v); }

    public int getSelectedProvider() { return providerCombo.getSelectedIndex(); }
    public void setSelectedProvider(int idx) { providerCombo.setSelectedIndex(idx); }

    public int getAppPassProvider() { return appPassProviderCombo.getSelectedIndex(); }
    public void setAppPassProvider(int idx) { appPassProviderCombo.setSelectedIndex(idx); }

    public void onAppPassProviderChanged(Consumer<Integer> action) {
        appPassProviderCombo.addActionListener(e -> action.accept(appPassProviderCombo.getSelectedIndex()));
    }

    public void showAppPassSubCard(int index) {
        if (index >= 0 && index < APP_PASS_PROVIDERS.length) {
            appPassSubCardLayout.show(appPassSubCards, String.valueOf(index));
        }
    }

    public char[] getGmailAppPassword() { return gmailAppPassField.getPassword(); }
    public void setGmailAppPassword(char[] v) { gmailAppPassField.setText(new String(v)); }

    public char[] getOutlookAppPassword() { return outlookAppPassField.getPassword(); }
    public void setOutlookAppPassword(char[] v) { outlookAppPassField.setText(new String(v)); }

    public String getSmtpHost() { return smtpHostField.getText().trim(); }
    public void setSmtpHost(String v) { smtpHostField.setText(v); }

    public String getSmtpPort() { return smtpPortField.getText().trim(); }
    public void setSmtpPort(String v) { smtpPortField.setText(v); }

    public String getSmtpUser() { return smtpUserField.getText().trim(); }
    public void setSmtpUser(String v) { smtpUserField.setText(v); }

    public char[] getSmtpPassword() { return smtpPassField.getPassword(); }
    public void setSmtpPassword(char[] v) { smtpPassField.setText(new String(v)); }

    public boolean isSmtpTls() { return smtpTlsCheck.isSelected(); }
    public void setSmtpTls(boolean v) { smtpTlsCheck.setSelected(v); }

    public boolean isSmtpSsl() { return smtpSslCheck.isSelected(); }
    public void setSmtpSsl(boolean v) { smtpSslCheck.setSelected(v); }

    public List<String> getReceivers() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < receiversListModel.size(); i++) {
            list.add(receiversListModel.get(i));
        }
        return list;
    }

    public void setReceivers(List<String> receivers) {
        receiversListModel.clear();
        if (receivers != null) {
            receiversListModel.addAll(receivers);
        }
    }

    public String getTestRecipient() { return testRecipientField.getText().trim(); }
    public void setTestRecipient(String v) { testRecipientField.setText(v); }

    public String getTestSubject() { return testSubjectField.getText().trim(); }
    public void setTestSubject(String v) { testSubjectField.setText(v); }

    public String getTestBody() { return testBodyArea.getText(); }
    public void setTestBody(String v) { testBodyArea.setText(v); }

    public boolean isHtml() { return htmlCheck.isSelected(); }
    public void setHtml(boolean v) { htmlCheck.setSelected(v); }

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
    }

    private static String getDefaultTemplate() {
        return "<html><body>\n"
                + "<h2>Prueba de Configuraci\u00f3n de Correo</h2>\n"
                + "<p>Este es un correo de prueba enviado desde el Sistema de Gesti\u00f3n Hotelera.</p>\n"
                + "<p>Si recibe este mensaje, la configuraci\u00f3n de correo electr\u00f3nico es correcta.</p>\n"
                + "<hr>\n"
                + "<p><strong>Motel:</strong> {motelName}<br>\n"
                + "<strong>Fecha:</strong> {date}<br>\n"
                + "<strong>Remitente:</strong> {senderName} &lt;{senderEmail}&gt;</p>\n"
                + "<hr>\n"
                + "<p>Atentamente,<br>Sistema de Gesti\u00f3n Hotelera</p>\n"
                + "</body></html>";
    }
}
