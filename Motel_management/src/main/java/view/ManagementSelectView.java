/*
 * Created by JFormDesigner on Sat Jun 08 11:07:11 COT 2024
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author Santiago
 */
public class ManagementSelectView extends JPanel {

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }

    /**
     * @return the managementInfoLabel
     */
    public JLabel getManagementInfoLabel() {
        return managementInfoLabel;
    }

    /**
     * @return the timeLabel
     */
    public JLabel getTimeLabel() {
        return timeLabel;
    }

    /**
     * @return the dateLabel
     */
    public JLabel getDateLabel() {
        return dateLabel;
    }

    /**
     * @return the turnButton
     */
    public JButton getTurnButton() {
        return turnButton;
    }

    /**
     * @return the inventoryButton
     */
    public JButton getInventoryButton() {
        return inventoryButton;
    }

    /**
     * @return the historyButton
     */
    public JButton getHistoryButton() {
        return historyButton;
    }
    public ManagementSelectView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	managementInfoLabel = new JLabel();
	timeLabel = new JLabel();
	dateLabel = new JLabel();
	turnButton = new JButton();
	inventoryButton = new JButton();
	historyButton = new JButton();
	backButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[171,fill]" +
	    "[145,fill]" +
	    "[118,fill]" +
	    "[145,fill]" +
	    "[118,fill]" +
	    "[145,fill]" +
	    "[133,fill]",
	    // rows
	    "[121]" +
	    "[45]" +
	    "[grow]" +
	    "[40]" +
	    "[116]"));

	//---- managementInfoLabel ----
	managementInfoLabel.setText("ADMINISTRACION");
	managementInfoLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(managementInfoLabel, "cell 0 0 3 1");

	//---- timeLabel ----
	timeLabel.setText("TIME");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(timeLabel, "cell 4 0 3 1");

	//---- dateLabel ----
	dateLabel.setText("DATE");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(dateLabel, "cell 4 1 3 1,growy");

	//---- turnButton ----
	turnButton.setText("TURNO");
	turnButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(turnButton, "cell 1 2,growy");

	//---- inventoryButton ----
	inventoryButton.setText("INVENTARIO");
	inventoryButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(inventoryButton, "cell 3 2,growy");

	//---- historyButton ----
	historyButton.setText("HISTORIAL");
	historyButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(historyButton, "cell 5 2,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(backButton, "cell 0 4,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel managementInfoLabel;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JButton turnButton;
    private JButton inventoryButton;
    private JButton historyButton;
    private JButton backButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
