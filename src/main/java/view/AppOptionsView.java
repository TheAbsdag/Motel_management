/*
 * Created by JFormDesigner on Wed May 13 10:40:51 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;
import view.interfaces.TimeLabelInterface;

/**
 * @author SECC
 */
public class AppOptionsView extends JPanel implements TimeLabelInterface{
    public AppOptionsView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	optionsInformativeLabel = new JLabel();
	dateInformationLabel = new JLabel();
	dateLabel = new JLabel();
	printerOptionsButton = new JButton();
	timeInformationLabel = new JLabel();
	timeLabel = new JLabel();
	dataConfigurationButton = new JButton();
	dateAndTimeConfigurationButton = new JButton();
	floorConfigurationButton = new JButton();
	saveConfigurationButton = new JButton();
	exportConfigurationButton = new JButton();
	currencyConfigButton = new JButton();
	backButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[shrink 0,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- optionsInformativeLabel ----
	optionsInformativeLabel.setText("OPCIONES PROGRAMA");
	optionsInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(optionsInformativeLabel, "cell 0 0");

	//---- dateInformationLabel ----
	dateInformationLabel.setText("FECHA");
	dateInformationLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(dateInformationLabel, "cell 0 1");

	//---- dateLabel ----
	dateLabel.setText("XXXX");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(dateLabel, "cell 1 1");

	//---- printerOptionsButton ----
	printerOptionsButton.setText("CONFIGURAR IMPRESORA");
	printerOptionsButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(printerOptionsButton, "cell 2 1,growy");

	//---- timeInformationLabel ----
	timeInformationLabel.setText("HORA");
	timeInformationLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(timeInformationLabel, "cell 0 2");

	//---- timeLabel ----
	timeLabel.setText("XXXX");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(timeLabel, "cell 1 2");

	//---- dataConfigurationButton ----
	dataConfigurationButton.setText("CONFIGURAR DATOS");
	dataConfigurationButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(dataConfigurationButton, "cell 2 2,growy");

	//---- dateAndTimeConfigurationButton ----
	dateAndTimeConfigurationButton.setText("CONFIGURAR FECHA Y HORA");
	dateAndTimeConfigurationButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(dateAndTimeConfigurationButton, "cell 0 3,growy");

	//---- floorConfigurationButton ----
	floorConfigurationButton.setText("CONFIGURAR PISOS");
	floorConfigurationButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(floorConfigurationButton, "cell 2 3,growy");

	//---- saveConfigurationButton ----
	saveConfigurationButton.setText("CONFIGURAR GUARDADO");
	saveConfigurationButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(saveConfigurationButton, "cell 2 4,growy");

	//---- exportConfigurationButton ----
	exportConfigurationButton.setText("CONFIGURAR EXPORTACION");
	exportConfigurationButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(exportConfigurationButton, "cell 2 5,growy");

	//---- currencyConfigButton ----
	currencyConfigButton.setText("CONFIGURAR MONEDA");
	currencyConfigButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(currencyConfigButton, "cell 2 6,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(backButton, "cell 0 7,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel optionsInformativeLabel;
    private JLabel dateInformationLabel;
    private JLabel dateLabel;
    private JButton printerOptionsButton;
    private JLabel timeInformationLabel;
    private JLabel timeLabel;
    private JButton dataConfigurationButton;
    private JButton dateAndTimeConfigurationButton;
    private JButton floorConfigurationButton;
    private JButton saveConfigurationButton;
    private JButton exportConfigurationButton;
    private JButton currencyConfigButton;
    private JButton backButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }

    // ========== Encapsulated listener registration ==========

    /** Registers a listener for the printer options button. */
    public void onPrinterOptions(Runnable action) {
        printerOptionsButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the motel data configuration button. */
    public void onDataConfiguration(Runnable action) {
        dataConfigurationButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the date/time configuration button. */
    public void onDateTimeConfiguration(Runnable action) {
        dateAndTimeConfigurationButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the floor configuration button. */
    public void onFloorConfiguration(Runnable action) {
        floorConfigurationButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the save configuration button. */
    public void onSaveConfiguration(Runnable action) {
        saveConfigurationButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the export configuration button. */
    public void onExportConfiguration(Runnable action) {
        exportConfigurationButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the currency configuration button. */
    public void onCurrencyConfiguration(Runnable action) {
        currencyConfigButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the back/return button. */
    public void onBackButton(Runnable action) {
        backButton.addActionListener(e -> action.run());
    }
}
