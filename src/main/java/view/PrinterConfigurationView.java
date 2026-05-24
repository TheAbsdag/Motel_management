/*
 * Created by JFormDesigner on Sat Jan 11 23:24:56 COT 2025
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author Santiago
 */
public class PrinterConfigurationView extends JPanel {

    // ========== Encapsulated API ==========

    /** Sets the current printer used label. */
    public void setPrinterUsedText(String text) { printerUsedLabel.setText(text); }
    /** Sets the selected printer label. */
    public void setSelectedPrinterText(String text) { selectedPrinterLabel.setText(text); }
    /** Sets the printer list model. */
    public void setPrinterListModel(DefaultListModel<String> model) { printerList.setModel(model); }
    /** Returns the selected index in the printer list. */
    public int getSelectedPrinterIndex() { return printerList.getSelectedIndex(); }
    /** Registers a list selection listener on the printer list. */
    public void onPrinterListSelection(javax.swing.event.ListSelectionListener listener) {
        printerList.addListSelectionListener(listener);
    }
    /** Registers a listener for the confirm printer button. */
    public void onConfirmPrinterButton(Runnable action) { confirmPrinterButton.addActionListener(e -> action.run()); }
    /** Enables or disables the confirm printer button. */
    public void setConfirmPrinterEnabled(boolean enabled) { confirmPrinterButton.setEnabled(enabled); }
    /** Registers a listener for the back button. */
    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }
    /** Enables or disables the back button. */
    public void setBackEnabled(boolean enabled) { backButton.setVisible(enabled); backButton.setEnabled(enabled); }
    /**
     * Re-wires the confirm and back buttons for first-boot flow.
     * Removes existing listeners, disables back, and calls the provided
     * completion callback when the user confirms the printer selection.
     */
    public void setFirstBootConfirmAction(Runnable onCompleted) {
        for (var al : confirmPrinterButton.getActionListeners()) {
            confirmPrinterButton.removeActionListener(al);
        }
        for (var al : backButton.getActionListeners()) {
            backButton.removeActionListener(al);
        }
        setBackEnabled(false);
        onConfirmPrinterButton(() -> onCompleted.run());
    }
    public PrinterConfigurationView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	configurationInfoLabel = new JLabel();
	printerUsedInformationLabel = new JLabel();
	printerUsedLabel = new JLabel();
	printerInfoLabel = new JLabel();
	scrollPane1 = new JScrollPane();
	printerList = new JList();
	selectedPrinterLabel = new JLabel();
	confirmPrinterButton = new JButton();
	backButton = new JButton();
	button1 = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- configurationInfoLabel ----
	configurationInfoLabel.setText("CONFIGURACI\u00d3N IMPRESORA");
	configurationInfoLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(configurationInfoLabel, "cell 0 0 2 1,align center center,grow 0 0");

	//---- printerUsedInformationLabel ----
	printerUsedInformationLabel.setText("IMPRESORA ACTUAL");
	printerUsedInformationLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(printerUsedInformationLabel, "cell 0 1");

	//---- printerUsedLabel ----
	printerUsedLabel.setText("PRINTER");
	printerUsedLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(printerUsedLabel, "cell 1 1 2 1");

	//---- printerInfoLabel ----
	printerInfoLabel.setText("IMPRESORA:");
	printerInfoLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(printerInfoLabel, "cell 0 2,align center center,grow 0 0");

	//======== scrollPane1 ========
	{

	    //---- printerList ----
	    printerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    printerList.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	    scrollPane1.setViewportView(printerList);
	}
	add(scrollPane1, "cell 1 2,growy");

	//---- selectedPrinterLabel ----
	selectedPrinterLabel.setText("PRINTER");
	selectedPrinterLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(selectedPrinterLabel, "cell 2 2");

	//---- confirmPrinterButton ----
	confirmPrinterButton.setText("CONFIRMAR");
	confirmPrinterButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(confirmPrinterButton, "cell 3 2,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(backButton, "cell 0 4,growy");

	//---- button1 ----
	button1.setText("CONFIGURAR IMPRESION");
	add(button1, "cell 3 4,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel configurationInfoLabel;
    private JLabel printerUsedInformationLabel;
    private JLabel printerUsedLabel;
    private JLabel printerInfoLabel;
    private JScrollPane scrollPane1;
    private JList printerList;
    private JLabel selectedPrinterLabel;
    private JButton confirmPrinterButton;
    private JButton backButton;
    private JButton button1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
