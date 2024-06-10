/*
 * Created by JFormDesigner on Sat Jun 08 21:33:40 COT 2024
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author Santiago
 */
public class HistoryView extends JPanel {
    public HistoryView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	turnSelectionPanel = new JPanel();
	turnDateInformativeLabel = new JLabel();
	panelSummarizedTurn = new JPanel();
	turnDateLabel = new JLabel();
	startDateInformativeLabel = new JLabel();
	turnStartLabel = new JLabel();
	turnEndInformativeLabel = new JLabel();
	turnEndLabel = new JLabel();
	durationInformativeLabel = new JLabel();
	durationLabel = new JLabel();
	backButton = new JButton();
	timeLabel = new JLabel();
	dateLabel = new JLabel();
	turnDetailsButton = new JButton();

	//======== this ========
	setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	setLayout(new MigLayout(
	    "hidemode 3",
	    // columns
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[183,grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]",
	    // rows
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[16:n,grow]" +
	    "[32]" +
	    "[grow]"));

	//======== turnSelectionPanel ========
	{
	    turnSelectionPanel.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[grow]"));
	}
	add(turnSelectionPanel, "cell 0 0 3 8,grow");

	//---- turnDateInformativeLabel ----
	turnDateInformativeLabel.setText("FECHA:");
	turnDateInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(turnDateInformativeLabel, "cell 3 0");

	//======== panelSummarizedTurn ========
	{
	    panelSummarizedTurn.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[grow]"));
	}
	add(panelSummarizedTurn, "cell 4 0 3 8,growy");

	//---- turnDateLabel ----
	turnDateLabel.setText("XXXX-XX-XX ");
	turnDateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(turnDateLabel, "cell 3 1");

	//---- startDateInformativeLabel ----
	startDateInformativeLabel.setText("INICIO");
	startDateInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(startDateInformativeLabel, "cell 3 2");

	//---- turnStartLabel ----
	turnStartLabel.setText("XXXX-XX-XX - XX:XX AM/PM");
	turnStartLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(turnStartLabel, "cell 3 3");

	//---- turnEndInformativeLabel ----
	turnEndInformativeLabel.setText("FINAL");
	turnEndInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(turnEndInformativeLabel, "cell 3 4");

	//---- turnEndLabel ----
	turnEndLabel.setText("XXXX-XX-XX - XX:XX AM/PM");
	turnEndLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(turnEndLabel, "cell 3 5");

	//---- durationInformativeLabel ----
	durationInformativeLabel.setText("DURACION");
	durationInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(durationInformativeLabel, "cell 3 6");

	//---- durationLabel ----
	durationLabel.setText("N");
	durationLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(durationLabel, "cell 3 7");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(backButton, "cell 0 8 1 2,growy");

	//---- timeLabel ----
	timeLabel.setText("time");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(timeLabel, "cell 1 9 2 1");

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(dateLabel, "cell 3 9");

	//---- turnDetailsButton ----
	turnDetailsButton.setText("DETALLES");
	turnDetailsButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(turnDetailsButton, "cell 6 8 1 2,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel turnSelectionPanel;
    private JLabel turnDateInformativeLabel;
    private JPanel panelSummarizedTurn;
    private JLabel turnDateLabel;
    private JLabel startDateInformativeLabel;
    private JLabel turnStartLabel;
    private JLabel turnEndInformativeLabel;
    private JLabel turnEndLabel;
    private JLabel durationInformativeLabel;
    private JLabel durationLabel;
    private JButton backButton;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JButton turnDetailsButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the turnSelectionPanel
     */
    public JPanel getTurnSelectionPanel() {
        return turnSelectionPanel;
    }

    /**
     * @return the turnDateInformativeLabel
     */
    public JLabel getTurnDateInformativeLabel() {
        return turnDateInformativeLabel;
    }

    /**
     * @return the panelSummarizedTurn
     */
    public JPanel getPanelSummarizedTurn() {
        return panelSummarizedTurn;
    }

    /**
     * @return the turnDateLabel
     */
    public JLabel getTurnDateLabel() {
        return turnDateLabel;
    }

    /**
     * @return the startDateInformativeLabel
     */
    public JLabel getStartDateInformativeLabel() {
        return startDateInformativeLabel;
    }

    /**
     * @return the turnStartLabel
     */
    public JLabel getTurnStartLabel() {
        return turnStartLabel;
    }

    /**
     * @return the turnEndInformativeLabel
     */
    public JLabel getTurnEndInformativeLabel() {
        return turnEndInformativeLabel;
    }

    /**
     * @return the turnEndLabel
     */
    public JLabel getTurnEndLabel() {
        return turnEndLabel;
    }

    /**
     * @return the durationInformativeLabel
     */
    public JLabel getDurationInformativeLabel() {
        return durationInformativeLabel;
    }

    /**
     * @return the durationLabel
     */
    public JLabel getDurationLabel() {
        return durationLabel;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
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
     * @return the turnDetailsButton
     */
    public JButton getTurnDetailsButton() {
        return turnDetailsButton;
    }
}
