/*
 * Created by JFormDesigner on Sat May 02 22:56:40 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;
import view.interfaces.TimeLabelInterface;

/**
 * @author SECC
 */
public class TurnSelectView extends JPanel implements TimeLabelInterface {

    public TurnSelectView() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	turnSelectLabel = new JLabel();
	buttonPanel = new JPanel();
	turn1Button = new JButton();
	turn2Button = new JButton();
	turn3Button = new JButton();
	infoPanel = new JPanel();
	timeLabel = new JLabel();
	dateLabel = new JLabel();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3,insets 65 196 53 196,align center center",
	    // columns
	    "[grow,center]",
	    // rows
	    "[]" +
	    "[grow]" +
	    "[]" +
	    "[]"));

	//---- turnSelectLabel ----
	turnSelectLabel.setText("SELECCIONE TURNO");
	turnSelectLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 54));
	add(turnSelectLabel, "cell 0 0,align center,gapbottom 111");

	//======== buttonPanel ========
	{
	    buttonPanel.setLayout(new MigLayout(
		"insets 0",
		// columns
		"[186]" +
		"[39]" +
		"[186]" +
		"[39]" +
		"[186]",
		// rows
		"[162]"));

	    //---- turn1Button ----
	    turn1Button.setText("1");
	    turn1Button.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	    buttonPanel.add(turn1Button, "cell 0 0,w 186,h 162");

	    //---- turn2Button ----
	    turn2Button.setText("2");
	    turn2Button.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	    buttonPanel.add(turn2Button, "cell 2 0,w 186,h 162");

	    //---- turn3Button ----
	    turn3Button.setText("3");
	    turn3Button.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	    buttonPanel.add(turn3Button, "cell 4 0,w 186,h 162");
	}
	add(buttonPanel, "cell 0 1,align center,gapbottom 89");

	//======== infoPanel ========
	{
	    infoPanel.setLayout(new MigLayout(
		"insets 0",
		// columns
		"[grow,center]" +
		"[grow,center]",
		// rows
		"[]"));

	    //---- timeLabel ----
	    timeLabel.setText("00:00 AM");
	    timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 36));
	    timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    infoPanel.add(timeLabel, "cell 0 0");

	    //---- dateLabel ----
	    dateLabel.setText("DATE");
	    dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 36));
	    dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    infoPanel.add(dateLabel, "cell 1 0");
	}
	add(infoPanel, "cell 0 2,align center");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel turnSelectLabel;
    private JPanel buttonPanel;
    private JButton turn1Button;
    private JButton turn2Button;
    private JButton turn3Button;
    private JPanel infoPanel;
    private JLabel timeLabel;
    private JLabel dateLabel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    // ========== Encapsulated listener registration ==========

    /** Registers a listener for turn 1 button. */
    public void onTurn1Button(Runnable action) { turn1Button.addActionListener(e -> action.run()); }
    /** Registers a listener for turn 2 button. */
    public void onTurn2Button(Runnable action) { turn2Button.addActionListener(e -> action.run()); }
    /** Registers a listener for turn 3 button. */
    public void onTurn3Button(Runnable action) { turn3Button.addActionListener(e -> action.run()); }
    /** Sets the turn selection label text. to be used with i18n in future */
    public void setTurnSelectLabel(String text) { turnSelectLabel.setText(text); }

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }
}
