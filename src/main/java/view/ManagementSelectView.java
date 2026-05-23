/*
 * Created by JFormDesigner on Sat Jun 08 11:07:11 COT 2024
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;
import view.interfaces.TimeLabelInterface;

/**
 * @author Santiago
 */
public class ManagementSelectView extends JPanel implements TimeLabelInterface {

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }

    // ========== Encapsulated listener registration ==========

    /** Registers a listener for the turn management button. */
    public void onTurnButton(Runnable action) { turnButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the inventory management button. */
    public void onInventoryButton(Runnable action) { inventoryButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the history button. */
    public void onHistoryButton(Runnable action) { historyButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the app options button. */
    public void onAppOptionsButton(Runnable action) { appOptionsButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the room summary button. */
    public void onRoomSummaryButton(Runnable action) { roomSummaryButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the extra changes button. */
    public void onExtraChangesButton(Runnable action) { extraChangesButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the register spending button. */
    public void onRegisterSpendingButton(Runnable action) { registerSpendingButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the back button. */
    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }

    /** Sets the management info header text. */
    public void setManagementInfo(String text) { managementInfoLabel.setText(text); }

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
        roomSummaryButton = new JButton();
        extraChangesButton = new JButton();
        registerSpendingButton = new JButton();
        backButton = new JButton();
        appOptionsButton = new JButton();

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
            "[45,grow]" +
            "[grow]" +
            "[40,grow]" +
            "[116]"));

        //---- managementInfoLabel ----
        managementInfoLabel.setText("ADMINISTRACION");
        managementInfoLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
        add(managementInfoLabel, "cell 0 0 3 1");

        //---- timeLabel ----
        timeLabel.setText("TIME");
        timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        add(timeLabel, "cell 3 0");

        //---- dateLabel ----
        dateLabel.setText("DATE");
        dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(dateLabel, "cell 4 0 2 1,growy");

        //---- turnButton ----
        turnButton.setText("TURNO");
        turnButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(turnButton, "cell 1 1,growy");

        //---- inventoryButton ----
        inventoryButton.setText("INVENTARIO");
        inventoryButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(inventoryButton, "cell 3 1,growy");

        //---- historyButton ----
        historyButton.setText("HISTORIAL");
        historyButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        historyButton.setEnabled(false);
        add(historyButton, "cell 5 1,growy");

        //---- roomSummaryButton ----
        roomSummaryButton.setText("RESUMEN HABITACIONES");
        roomSummaryButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        add(roomSummaryButton, "cell 0 2,growy");

        //---- extraChangesButton ----
        extraChangesButton.setText("TRANSFERENCIA ");
        extraChangesButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(extraChangesButton, "cell 1 3,growy");

        //---- registerSpendingButton ----
        registerSpendingButton.setText("GASTO");
        registerSpendingButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(registerSpendingButton, "cell 3 3,growy");

        //---- backButton ----
        backButton.setText("VOLVER");
        backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
        add(backButton, "cell 0 4,growy");

        //---- appOptionsButton ----
        appOptionsButton.setText("OPCIONES");
        appOptionsButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(appOptionsButton, "cell 5 4 2 1,growy");
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
    private JButton roomSummaryButton;
    private JButton extraChangesButton;
    private JButton registerSpendingButton;
    private JButton backButton;
    private JButton appOptionsButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
