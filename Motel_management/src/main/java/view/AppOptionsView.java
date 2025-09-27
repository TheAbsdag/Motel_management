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
public class AppOptionsView extends JPanel {

    /**
     * @return the configurationInfoLabel
     */
    public JLabel getConfigurationInfoLabel() {
        return configurationInfoLabel;
    }

    /**
     * @return the printerUsedInformationLabel
     */
    public JLabel getPrinterUsedInformationLabel() {
        return printerUsedInformationLabel;
    }

    /**
     * @return the prinerUsedLabel
     */
    public JLabel getPrinterUsedLabel() {
        return printerUsedLabel;
    }

    /**
     * @return the printerInfoLabel
     */
    public JLabel getPrinterInfoLabel() {
        return printerInfoLabel;
    }

    /**
     * @return the scrollPane1
     */
    public JScrollPane getScrollPane1() {
        return scrollPane1;
    }

    /**
     * @return the pritnerList
     */
    public JList getPrinterList() {
        return printerList;
    }

    /**
     * @return the selectedPrinterLabel
     */
    public JLabel getSelectedPrinterLabel() {
        return selectedPrinterLabel;
    }

    /**
     * @return the confirmPrinterButton
     */
    public JButton getConfirmPrinterButton() {
        return confirmPrinterButton;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }
    public AppOptionsView() {
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

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
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
	configurationInfoLabel.setText("CONFIGURACI\u00d3N");
	configurationInfoLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(configurationInfoLabel, "cell 0 0,align center center,grow 0 0");

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
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
