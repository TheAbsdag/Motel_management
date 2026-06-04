/*
 * Created by JFormDesigner on Wed Jun 03 09:15:49 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;
import view.helpers.DialogHelper;
import view.interfaces.TimeLabelInterface;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author SECC
 */
public class EmailConfigurationHubView extends JPanel implements TimeLabelInterface {
    public EmailConfigurationHubView() {
	initComponents();
	wireInformativeCheckboxes();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	titleInformativeLabel = new JLabel();
	emailStatusInformativeLabel = new JLabel();
	emailStatusLabel = new JLabel();
	emailFeatureEnableCheckBox = new JCheckBox();
	providerButton = new JButton();
	generalConfigurationButton = new JButton();
	emailCasesInformativeLabel = new JLabel();
	roomSaleCaseInformativeCheckBox = new JCheckBox();
	roomSaleCaseInformativeLabel = new JLabel();
	saleCaseInformativeCheckbox = new JCheckBox();
	saleCaseInformativeLabel = new JLabel();
	turnCaseInformativeCheckBox = new JCheckBox();
	turnCaseInformativeLabel = new JLabel();
	roomSaleCaseButton = new JButton();
	saleCaseButton = new JButton();
	turnCaseButton = new JButton();
	backButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- titleInformativeLabel ----
	titleInformativeLabel.setText("CONFIGURAR CORREO");
	titleInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(titleInformativeLabel, "cell 0 0");

	//---- emailStatusInformativeLabel ----
	emailStatusInformativeLabel.setText("ESTADO ACTUAL CORREO:");
	emailStatusInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(emailStatusInformativeLabel, "cell 0 1,alignx center,growx 0");

	//---- emailStatusLabel ----
	emailStatusLabel.setText("xxx");
	emailStatusLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(emailStatusLabel, "cell 0 1");

	//---- emailFeatureEnableCheckBox ----
	emailFeatureEnableCheckBox.setText("HABILITAR SISTEMA CORREOS");
	emailFeatureEnableCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(emailFeatureEnableCheckBox, "cell 1 1");

	//---- providerButton ----
	providerButton.setText("CONFIGURAR PROOVEDOR");
	providerButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(providerButton, "cell 2 1,growy");

	//---- generalConfigurationButton ----
	generalConfigurationButton.setText("CONFIGURACION GENERAL");
	generalConfigurationButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(generalConfigurationButton, "cell 0 2,growy");

	//---- emailCasesInformativeLabel ----
	emailCasesInformativeLabel.setText("CASOS CORREO");
	emailCasesInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(emailCasesInformativeLabel, "cell 0 3");

	//---- roomSaleCaseInformativeCheckBox ----
	roomSaleCaseInformativeCheckBox.setText("HABITACIONES");
	roomSaleCaseInformativeCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(roomSaleCaseInformativeCheckBox, "cell 0 4,alignx right,growx 0");

	//---- roomSaleCaseInformativeLabel ----
	roomSaleCaseInformativeLabel.setText("ACTIVO-INACTIVO");
	roomSaleCaseInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(roomSaleCaseInformativeLabel, "cell 0 4");

	//---- saleCaseInformativeCheckbox ----
	saleCaseInformativeCheckbox.setText("VENTAS:");
	saleCaseInformativeCheckbox.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(saleCaseInformativeCheckbox, "cell 1 4,alignx center,growx 0");

	//---- saleCaseInformativeLabel ----
	saleCaseInformativeLabel.setText("ACTIVO-INACTIVO");
	saleCaseInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(saleCaseInformativeLabel, "cell 1 4");

	//---- turnCaseInformativeCheckBox ----
	turnCaseInformativeCheckBox.setText("REPORTE TURNOS");
	turnCaseInformativeCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(turnCaseInformativeCheckBox, "cell 2 4,alignx center,growx 0");

	//---- turnCaseInformativeLabel ----
	turnCaseInformativeLabel.setText("ACTIVO-INACTIVO");
	turnCaseInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(turnCaseInformativeLabel, "cell 2 4");

	//---- roomSaleCaseButton ----
	roomSaleCaseButton.setText("CONFIGURAR HABITACIONES");
	roomSaleCaseButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(roomSaleCaseButton, "cell 0 5,growy");

	//---- saleCaseButton ----
	saleCaseButton.setText("CONFIGURAR VENTAS");
	saleCaseButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(saleCaseButton, "cell 1 5,growy");

	//---- turnCaseButton ----
	turnCaseButton.setText("CONFIGURAR REPORTE TURNOS");
	turnCaseButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(turnCaseButton, "cell 2 5,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(backButton, "cell 0 7,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel titleInformativeLabel;
    private JLabel emailStatusInformativeLabel;
    private JLabel emailStatusLabel;
    private JCheckBox emailFeatureEnableCheckBox;
    private JButton providerButton;
    private JButton generalConfigurationButton;
    private JLabel emailCasesInformativeLabel;
    private JCheckBox roomSaleCaseInformativeCheckBox;
    private JLabel roomSaleCaseInformativeLabel;
    private JCheckBox saleCaseInformativeCheckbox;
    private JLabel saleCaseInformativeLabel;
    private JCheckBox turnCaseInformativeCheckBox;
    private JLabel turnCaseInformativeLabel;
    private JButton roomSaleCaseButton;
    private JButton saleCaseButton;
    private JButton turnCaseButton;
    private JButton backButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    private void wireInformativeCheckboxes() {
	roomSaleCaseInformativeCheckBox.setEnabled(false);
	saleCaseInformativeCheckbox.setEnabled(false);
	turnCaseInformativeCheckBox.setEnabled(false);

	roomSaleCaseInformativeCheckBox.addMouseListener(new MouseAdapter() {
	    private int clickCount = 0;
	    @Override
	    public void mouseClicked(MouseEvent e) {
		clickCount++;
		if (clickCount >= 5) {
		    clickCount = 0;
		    DialogHelper.showInfoMessage("Seleccione el bot\u00f3n para configurar el caso", "HABITACIONES");
		}
	    }
	});
	saleCaseInformativeCheckbox.addMouseListener(new MouseAdapter() {
	    private int clickCount = 0;
	    @Override
	    public void mouseClicked(MouseEvent e) {
		clickCount++;
		if (clickCount >= 5) {
		    clickCount = 0;
		    DialogHelper.showInfoMessage("Seleccione el bot\u00f3n para configurar el caso", "VENTAS");
		}
	    }
	});
	turnCaseInformativeCheckBox.addMouseListener(new MouseAdapter() {
	    private int clickCount = 0;
	    @Override
	    public void mouseClicked(MouseEvent e) {
		clickCount++;
		if (clickCount >= 5) {
		    clickCount = 0;
		    DialogHelper.showInfoMessage("Seleccione el bot\u00f3n para configurar el caso", "REPORTE TURNOS");
		}
	    }
	});
    }

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
    }

    public void onProviderButton(Runnable action) {
	providerButton.addActionListener(e -> action.run());
    }

    public void onGeneralConfigurationButton(Runnable action) {
	generalConfigurationButton.addActionListener(e -> action.run());
    }

    public void onRoomSaleCaseButton(Runnable action) {
	roomSaleCaseButton.addActionListener(e -> action.run());
    }

    public void onSaleCaseButton(Runnable action) {
	saleCaseButton.addActionListener(e -> action.run());
    }

    public void onTurnCaseButton(Runnable action) {
	turnCaseButton.addActionListener(e -> action.run());
    }

    public void onBackButton(Runnable action) {
	backButton.addActionListener(e -> action.run());
    }

    public void setCaseEnabled(int caseIndex, boolean enabled) {
	switch (caseIndex) {
	    case 0 -> {
		roomSaleCaseInformativeCheckBox.setSelected(enabled);
		roomSaleCaseInformativeLabel.setText(enabled ? "ACTIVO" : "INACTIVO");
	    }
	    case 1 -> {
		saleCaseInformativeCheckbox.setSelected(enabled);
		saleCaseInformativeLabel.setText(enabled ? "ACTIVO" : "INACTIVO");
	    }
	    case 2 -> {
		turnCaseInformativeCheckBox.setSelected(enabled);
		turnCaseInformativeLabel.setText(enabled ? "ACTIVO" : "INACTIVO");
	    }
	}
    }

    public void setEmailStatus(String status) {
	emailStatusLabel.setText(status);
    }
}
