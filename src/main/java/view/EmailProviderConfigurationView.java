/*
 * Created by JFormDesigner on Wed Jun 03 09:33:15 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author SECC
 */
public class EmailProviderConfigurationView extends JPanel {
    public EmailProviderConfigurationView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	providerConfigurationTitleLabel = new JLabel();
	emailInformativeLabel = new JLabel();
	emailTextField = new JTextField();
	verifyEmailConnectionButton = new JButton();
	nameInformativeLabel = new JLabel();
	nameTextField = new JTextField();
	providerInformativeLabel = new JLabel();
	providerSelectionInformativeLabel = new JLabel();
	providerComboBox = new JComboBox();
	providerPanel = new JPanel();
	backButton = new JButton();
	saveButton = new JButton();
	appPasswordPanel = new JPanel();
	appPasswordInformativeLabel = new JLabel();
	appPasswordProviderInformativeLabel = new JLabel();
	appPasswordProviderComboBox = new JComboBox();
	appPasswordInputInformativeLabel = new JLabel();
	appPasswordInputTextField = new JPasswordField();
	smtpPanel = new JPanel();
	smtpInformativeLabel = new JLabel();
	smtpHostInformativeLabel = new JLabel();
	smtpHostTextField = new JTextField();
	smtpPortInformativeLabel = new JLabel();
	smtpPortTextField = new JTextField();
	smtpUserInformativeLabel = new JLabel();
	smtpUserTextField = new JTextField();
	smtpPasswordInformativeLabel = new JLabel();
	smtpPasswordPasswordField = new JPasswordField();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[grow,fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[50]" +
	    "[50]" +
	    "[50]" +
	    "[60]" +
	    "[grow]" +
	    "[100]"));

	//---- providerConfigurationTitleLabel ----
	providerConfigurationTitleLabel.setText("CONFIGURAR PROOVEDOR");
	providerConfigurationTitleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(providerConfigurationTitleLabel, "cell 0 0 3 1");

	//---- emailInformativeLabel ----
	emailInformativeLabel.setText("CORREO:");
	emailInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(emailInformativeLabel, "cell 0 1,alignx center,growx 0");

	//---- emailTextField ----
	emailTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(emailTextField, "cell 1 1,growy");

	//---- verifyEmailConnectionButton ----
	verifyEmailConnectionButton.setText("VERIFICAR CONEXION");
	verifyEmailConnectionButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(verifyEmailConnectionButton, "cell 2 1 1 2,growy");

	//---- nameInformativeLabel ----
	nameInformativeLabel.setText("NOMBRE:");
	nameInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(nameInformativeLabel, "cell 0 2,alignx center,growx 0");

	//---- nameTextField ----
	nameTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(nameTextField, "cell 1 2,growy");

	//---- providerInformativeLabel ----
	providerInformativeLabel.setText("PROOVEDOR");
	providerInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	providerInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(providerInformativeLabel, "cell 0 3 3 1");

	//---- providerSelectionInformativeLabel ----
	providerSelectionInformativeLabel.setText("SELECCIONE PROOVEDOR:");
	providerSelectionInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(providerSelectionInformativeLabel, "cell 0 4");

	//---- providerComboBox ----
	providerComboBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(providerComboBox, "cell 1 4,growy");

	//======== providerPanel ========
	{
	    providerPanel.setLayout(new MigLayout(
		"fill,hidemode 3",
		// columns
		"[fill]",
		// rows
		"[]"));
	}
	add(providerPanel, "cell 0 5 3 1,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(backButton, "cell 0 6,growy");

	//---- saveButton ----
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(saveButton, "cell 2 6,growy");

	//======== appPasswordPanel ========
	{
	    appPasswordPanel.setLayout(new MigLayout(
		"fill,hidemode 3",
		// columns
		"[fill]" +
		"[grow,fill]",
		// rows
		"[]" +
		"[]" +
		"[]"));

	    //---- appPasswordInformativeLabel ----
	    appPasswordInformativeLabel.setText("CONTRASE\u00d1A APLICACI\u00d3N");
	    appPasswordInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    appPasswordInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    appPasswordPanel.add(appPasswordInformativeLabel, "cell 0 0 2 1,growy");

	    //---- appPasswordProviderInformativeLabel ----
	    appPasswordProviderInformativeLabel.setText("PROOVEDOR:");
	    appPasswordProviderInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    appPasswordPanel.add(appPasswordProviderInformativeLabel, "cell 0 1,alignx center,growx 0");

	    //---- appPasswordProviderComboBox ----
	    appPasswordProviderComboBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    appPasswordPanel.add(appPasswordProviderComboBox, "cell 1 1,growy");

	    //---- appPasswordInputInformativeLabel ----
	    appPasswordInputInformativeLabel.setText("CONTRASE\u00d1A");
	    appPasswordInputInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    appPasswordPanel.add(appPasswordInputInformativeLabel, "cell 0 2,alignx center,growx 0");

	    //---- appPasswordInputTextField ----
	    appPasswordInputTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    appPasswordPanel.add(appPasswordInputTextField, "cell 1 2,growy");
	}

	//======== smtpPanel ========
	{
	    smtpPanel.setLayout(new MigLayout(
		"fill,hidemode 3",
		// columns
		"[fill]" +
		"[grow,fill]",
		// rows
		"[]" +
		"[]" +
		"[]" +
		"[]" +
		"[]"));

	    //---- smtpInformativeLabel ----
	    smtpInformativeLabel.setText("SMTP PERSONALIZADO");
	    smtpInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    smtpInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    smtpPanel.add(smtpInformativeLabel, "cell 0 0 2 1,growy");

	    //---- smtpHostInformativeLabel ----
	    smtpHostInformativeLabel.setText("HOST:");
	    smtpHostInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    smtpPanel.add(smtpHostInformativeLabel, "cell 0 1,alignx center,growx 0");

	    //---- smtpHostTextField ----
	    smtpHostTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    smtpPanel.add(smtpHostTextField, "cell 1 1,growy");

	    //---- smtpPortInformativeLabel ----
	    smtpPortInformativeLabel.setText("PUERTO:");
	    smtpPortInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    smtpPanel.add(smtpPortInformativeLabel, "cell 0 2,alignx center,growx 0");

	    //---- smtpPortTextField ----
	    smtpPortTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    smtpPanel.add(smtpPortTextField, "cell 1 2,growy");

	    //---- smtpUserInformativeLabel ----
	    smtpUserInformativeLabel.setText("USUARIO:");
	    smtpUserInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    smtpPanel.add(smtpUserInformativeLabel, "cell 0 3,alignx center,growx 0");

	    //---- smtpUserTextField ----
	    smtpUserTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    smtpPanel.add(smtpUserTextField, "cell 1 3,growy");

	    //---- smtpPasswordInformativeLabel ----
	    smtpPasswordInformativeLabel.setText("CONTRASE\u00d1A:");
	    smtpPasswordInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    smtpPanel.add(smtpPasswordInformativeLabel, "cell 0 4,alignx center,growx 0");

	    //---- smtpPasswordPasswordField ----
	    smtpPasswordPasswordField.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    smtpPanel.add(smtpPasswordPasswordField, "cell 1 4,growy");
	}
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel providerConfigurationTitleLabel;
    private JLabel emailInformativeLabel;
    private JTextField emailTextField;
    private JButton verifyEmailConnectionButton;
    private JLabel nameInformativeLabel;
    private JTextField nameTextField;
    private JLabel providerInformativeLabel;
    private JLabel providerSelectionInformativeLabel;
    private JComboBox providerComboBox;
    private JPanel providerPanel;
    private JButton backButton;
    private JButton saveButton;
    private JPanel appPasswordPanel;
    private JLabel appPasswordInformativeLabel;
    private JLabel appPasswordProviderInformativeLabel;
    private JComboBox appPasswordProviderComboBox;
    private JLabel appPasswordInputInformativeLabel;
    private JPasswordField appPasswordInputTextField;
    private JPanel smtpPanel;
    private JLabel smtpInformativeLabel;
    private JLabel smtpHostInformativeLabel;
    private JTextField smtpHostTextField;
    private JLabel smtpPortInformativeLabel;
    private JTextField smtpPortTextField;
    private JLabel smtpUserInformativeLabel;
    private JTextField smtpUserTextField;
    private JLabel smtpPasswordInformativeLabel;
    private JPasswordField smtpPasswordPasswordField;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
