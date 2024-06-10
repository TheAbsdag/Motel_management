/*
 * Created by JFormDesigner on Fri Jun 07 00:47:34 COT 2024
 */

package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.*;

/**
 * @author Santiago
 */
public class SellingView extends JPanel {
    public SellingView() {
	initComponents();
    }
    
    //Required additional special class to showcase the details for the items

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	dateLabel = new JLabel();
	timeLabel = new JLabel();
	itemListPanel = new JPanel();
	registerListPanel = new JPanel();
	quantityTextField = new JTextField();
	 ((AbstractDocument) quantityTextField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
	addQuantityButton = new JButton();
	removeQuantityButton = new JButton();
	itemDeleteButton = new JButton();
	addItemButton = new JButton();
	printingCheckBox = new JCheckBox();
	backButton = new JButton();
	sellingToLabel = new JLabel();
	finishSaleButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "hidemode 3",
	    // columns
	    "[175,fill]" +
	    "[106,grow,fill]" +
	    "[118,fill]" +
	    "[117,fill]" +
	    "[112,fill]" +
	    "[136,fill]" +
	    "[139,grow,fill]",
	    // rows
	    "[33]" +
	    "[51]" +
	    "[80]" +
	    "[70]" +
	    "[72]" +
	    "[51]" +
	    "[77]" +
	    "[146]"));

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	dateLabel.setForeground(Color.yellow);
	add(dateLabel, "cell 0 0 3 1");

	//---- timeLabel ----
	timeLabel.setText("time:");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
	add(timeLabel, "cell 4 0 3 1");

	//======== itemListPanel ========
	{
	    itemListPanel.setBackground(new Color(0x75c0f1));
	    itemListPanel.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[389,grow,shrink 0,fill]"));
	}
	add(itemListPanel, "cell 0 1 3 6");

	//======== registerListPanel ========
	{
	    registerListPanel.setBackground(new Color(0xccebc7));
	    registerListPanel.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[fill]",
		// rows
		"[261,fill]"));
	}
	add(registerListPanel, "cell 4 1 3 4");

	//---- quantityTextField ----
	quantityTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	quantityTextField.setText("N");
	quantityTextField.setHorizontalAlignment(SwingConstants.CENTER);
	add(quantityTextField, "cell 3 2");

	//---- addQuantityButton ----
	addQuantityButton.setText("+");
	addQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(addQuantityButton, "cell 3 3,growy");

	//---- removeQuantityButton ----
	removeQuantityButton.setText("-");
	removeQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(removeQuantityButton, "cell 3 4,growy");

	//---- itemDeleteButton ----
	itemDeleteButton.setText("BORRAR");
	itemDeleteButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(itemDeleteButton, "cell 6 5");

	//---- addItemButton ----
	addItemButton.setText("A\u00d1ADIR");
	addItemButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(addItemButton, "cell 3 6,growy");

	//---- printingCheckBox ----
	printingCheckBox.setText("IMPRIMIR");
	printingCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(printingCheckBox, "cell 5 6 2 1,grow");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(backButton, "cell 0 7,growy");

	//---- sellingToLabel ----
	sellingToLabel.setText("VENDIENDO A: XXX");
	sellingToLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	sellingToLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(sellingToLabel, "cell 1 7 4 1");

	//---- finishSaleButton ----
	finishSaleButton.setText("COMPLETAR");
	finishSaleButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(finishSaleButton, "cell 5 7 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel dateLabel;
    private JLabel timeLabel;
    private JPanel itemListPanel;
    private JPanel registerListPanel;
    private JTextField quantityTextField;
    private JButton addQuantityButton;
    private JButton removeQuantityButton;
    private JButton itemDeleteButton;
    private JButton addItemButton;
    private JCheckBox printingCheckBox;
    private JButton backButton;
    private JLabel sellingToLabel;
    private JButton finishSaleButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the dateLabel
     */
    public JLabel getDateLabel() {
        return dateLabel;
    }

    /**
     * @return the timeLabel
     */
    public JLabel getTimeLabel() {
        return timeLabel;
    }

    /**
     * @return the itemListPanel
     */
    public JPanel getItemListPanel() {
        return itemListPanel;
    }

    /**
     * @return the registerListPanel
     */
    public JPanel getRegisterListPanel() {
        return registerListPanel;
    }

    /**
     * @return the quantityTextField
     */
    public JTextField getQuantityTextField() {
        return quantityTextField;
    }

    /**
     * @return the itemDeleteButton
     */
    public JButton getItemDeleteButton() {
        return itemDeleteButton;
    }

    /**
     * @return the printingCheckBox
     */
    public JCheckBox getPrintingCheckBox() {
        return printingCheckBox;
    }

    /**
     * @return the sellingToLabel
     */
    public JLabel getSellingToLabel() {
        return sellingToLabel;
    }

    /**
     * @return the finishSaleButton
     */
    public JButton getFinishSaleButton() {
        return finishSaleButton;
    }

    /**
     * @return the addQuantityButton
     */
    public JButton getAddQuantityButton() {
        return addQuantityButton;
    }

    /**
     * @return the removeQuantityButton
     */
    public JButton getRemoveQuantityButton() {
        return removeQuantityButton;
    }

    /**
     * @return the addItemButton
     */
    public JButton getAddItemButton() {
        return addItemButton;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }

}
