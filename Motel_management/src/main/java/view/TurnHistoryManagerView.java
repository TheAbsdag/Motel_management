/*
 * Created by JFormDesigner on Sat Jun 08 11:15:03 COT 2024
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author Santiago
 */
public class TurnHistoryManagerView extends JPanel {

    public TurnHistoryManagerView(){
        initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	turnDetailsPanel = new JPanel();
	turnNumberInformativeLabel = new JLabel();
	turnNumberLabel = new JLabel();
	turnStartInformativeLabel = new JLabel();
	turnStartLabel = new JLabel();
	turnEndInformativeLabel = new JLabel();
	turnEndLabel = new JLabel();
	noPrintCheckBox = new JCheckBox();
	summarizedPrintCheckBox = new JCheckBox();
	detailedPrintCheckBox = new JCheckBox();
	backButton = new JButton();
	printButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "hidemode 3",
	    // columns
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
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
	    "[25]" +
	    "[grow]"));

	//======== turnDetailsPanel ========
	{
	    turnDetailsPanel.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[grow,fill]"));
	}
	add(turnDetailsPanel, "cell 0 0 5 7,growy");

	//---- turnNumberInformativeLabel ----
	turnNumberInformativeLabel.setText("TURNO:");
	turnNumberInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(turnNumberInformativeLabel, "cell 5 0");

	//---- turnNumberLabel ----
	turnNumberLabel.setText("N");
	turnNumberLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnNumberLabel, "cell 6 0");

	//---- turnStartInformativeLabel ----
	turnStartInformativeLabel.setText("Inicio:");
	turnStartInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnStartInformativeLabel, "cell 5 1");

	//---- turnStartLabel ----
	turnStartLabel.setText("XXXX-XX-XX - XX:XX");
	turnStartLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnStartLabel, "cell 6 1");

	//---- turnEndInformativeLabel ----
	turnEndInformativeLabel.setText("Final");
	turnEndInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnEndInformativeLabel, "cell 5 2");

	//---- turnEndLabel ----
	turnEndLabel.setText("XXXX-XX-XX - XX:XX");
	turnEndLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnEndLabel, "cell 6 2");

	//---- noPrintCheckBox ----
	noPrintCheckBox.setText("NO IMPRIMIR");
	noPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(noPrintCheckBox, "cell 5 4 2 1,growy");

	//---- summarizedPrintCheckBox ----
	summarizedPrintCheckBox.setText("RESUMIDO");
	summarizedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(summarizedPrintCheckBox, "cell 5 5 2 1,growy");

	//---- detailedPrintCheckBox ----
	detailedPrintCheckBox.setText("DETALLADO");
	detailedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(detailedPrintCheckBox, "cell 5 6 2 1,growy");

	//---- backButton ----
	backButton.setText("CERRAR");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(backButton, "cell 0 7,growy");

	//---- printButton ----
	printButton.setText("IMPRIMIR");
	printButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(printButton, "cell 5 7 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel turnDetailsPanel;
    private JLabel turnNumberInformativeLabel;
    private JLabel turnNumberLabel;
    private JLabel turnStartInformativeLabel;
    private JLabel turnStartLabel;
    private JLabel turnEndInformativeLabel;
    private JLabel turnEndLabel;
    private JCheckBox noPrintCheckBox;
    private JCheckBox summarizedPrintCheckBox;
    private JCheckBox detailedPrintCheckBox;
    private JButton backButton;
    private JButton printButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the turnDetailsPanel
     */
    public JPanel getTurnDetailsPanel() {
        return turnDetailsPanel;
    }

    /**
     * @return the turnNumberInformativeLabel
     */
    public JLabel getTurnNumberInformativeLabel() {
        return turnNumberInformativeLabel;
    }

    /**
     * @return the turnNumberLabel
     */
    public JLabel getTurnNumberLabel() {
        return turnNumberLabel;
    }

    /**
     * @return the turnStartInformativeLabel
     */
    public JLabel getTurnStartInformativeLabel() {
        return turnStartInformativeLabel;
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
     * @return the noPrintCheckBox
     */
    public JCheckBox getNoPrintCheckBox() {
        return noPrintCheckBox;
    }

    /**
     * @return the summarizedPrintCheckBox
     */
    public JCheckBox getSummarizedPrintCheckBox() {
        return summarizedPrintCheckBox;
    }

    /**
     * @return the detailedPrintCheckBox
     */
    public JCheckBox getDetailedPrintCheckBox() {
        return detailedPrintCheckBox;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }

    /**
     * @return the printButton
     */
    public JButton getPrintButton() {
        return printButton;
    }

}
