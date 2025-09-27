/*
 * Created by JFormDesigner on Mon Jun 24 23:49:55 COT 2024
 */
package view;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.*;

/**
 * @author Santiago
 */
public class FloorView extends JPanel {

    private ArrayList<ArrayList<ArrayList<JButton>>> roomButtonGridByTower;
    private int currentFloorIndex;
    private int currentTowerIndex;
    private CardLayout cardLayout;

    public FloorView() {
        initCustomComponents();
        initComponents();
    }

    private void initCustomComponents() {
        currentFloorIndex = 0;
        currentTowerIndex = 0;
        roomButtonGridByTower = new ArrayList<>();
        cardLayout = new CardLayout();
    }

    public void createButtonsForTowers(int[][] roomsPerTower) {
        roomButtonGridByTower.clear();
        containerPanel.removeAll();
        containerPanel.setLayout(cardLayout);

        for (int tower = 0; tower < roomsPerTower.length; tower++) {
            int[] roomsPerFloor = roomsPerTower[tower];
            ArrayList<ArrayList<JButton>> towerFloors = new ArrayList<>();

            for (int floor = 0; floor < roomsPerFloor.length; floor++) {
                JPanel floorButtonPanel = new JPanel();
                floorButtonPanel.setLayout(new GridLayout(5, 5));
                floorButtonPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.black),
                        "TORRE " + (tower + 1) + " - PISO " + (floor + 1),
                        TitledBorder.CENTER, TitledBorder.TOP,
                        new Font("SEGOE UI BLACK", Font.BOLD, 24)));

                ArrayList<JButton> floorButtons = new ArrayList<>();
                for (int room = 0; room < roomsPerFloor[floor]; room++) {
                    JButton button = new JButton("" + (room + 1));
                    button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    button.setBackground(Color.WHITE);
                    button.setForeground(Color.BLACK); // Changed to black for visibility
                    button.setFocusPainted(false);
                    button.setFont(new Font("SEGOE UI BLACK", Font.BOLD, 18));
                    floorButtons.add(button);
                    floorButtonPanel.add(button);
                }

                towerFloors.add(floorButtons);
                containerPanel.add(floorButtonPanel, "Tower" + tower + "Floor" + floor);
            }

            roomButtonGridByTower.add(towerFloors);
        }

        cardLayout.show(containerPanel, "Tower0Floor0");
        updateTowerLabel();
    }

    public void switchFloor(int floorIndex) {
        if (currentTowerIndex >= 0 && currentTowerIndex < roomButtonGridByTower.size()) {
            ArrayList<ArrayList<JButton>> currentTower = roomButtonGridByTower.get(currentTowerIndex);
            if (floorIndex >= 0 && floorIndex < currentTower.size()) {
                currentFloorIndex = floorIndex;
                switchToCurrentTowerAndFloor();
            }
        }
    }

    public void switchTower(int towerIndex) {
        if (towerIndex >= 0 && towerIndex < roomButtonGridByTower.size()) {
            currentTowerIndex = towerIndex;
            // Reset to floor 0 when switching towers
            currentFloorIndex = 0;
            switchToCurrentTowerAndFloor();
            updateTowerLabel();
        }
    }

    private void switchToCurrentTowerAndFloor() {
        cardLayout.show(containerPanel, "Tower" + currentTowerIndex + "Floor" + currentFloorIndex);
    }

    private void updateTowerLabel() {
        towerLabelInforfmation.setText("TORRE: " + (currentTowerIndex + 1));
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	containerPanel = new JPanel();
	turnNumberLabel = new JLabel();
	towerLabelInforfmation = new JLabel();
	previousTowerButton = new JButton();
	nextTowerButton = new JButton();
	timeLabel = new JLabel();
	dateLabel = new JLabel();
	floorUpButton = new JButton();
	floorDownButton = new JButton();
	receptionSellButton = new JButton();
	managementOptionsButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[498,grow,shrink 0,fill]" +
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]"));

	//======== containerPanel ========
	{
	    containerPanel.setLayout(null);
	    containerPanel.setLayout(cardLayout);
	}
	add(containerPanel, "cell 0 0 1 8,grow");

	//---- turnNumberLabel ----
	turnNumberLabel.setText("turnN");
	turnNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
	turnNumberLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 30));
	add(turnNumberLabel, "cell 1 0 2 1");

	//---- towerLabelInforfmation ----
	towerLabelInforfmation.setText("TOWER: N ");
	towerLabelInforfmation.setHorizontalAlignment(SwingConstants.CENTER);
	towerLabelInforfmation.setFont(new Font("Segoe UI Black", Font.BOLD, 30));
	add(towerLabelInforfmation, "cell 1 1 2 1");

	//---- previousTowerButton ----
	previousTowerButton.setIcon(new ImageIcon(getClass().getResource("/left.png")));
	add(previousTowerButton, "cell 1 2,growy");

	//---- nextTowerButton ----
	nextTowerButton.setIcon(new ImageIcon(getClass().getResource("/right.png")));
	add(nextTowerButton, "cell 2 2,growy");

	//---- timeLabel ----
	timeLabel.setText("time");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(timeLabel, "cell 1 3 2 1");

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(dateLabel, "cell 1 4 2 1");

	//---- floorUpButton ----
	floorUpButton.setIcon(new ImageIcon(getClass().getResource("/up.png")));
	add(floorUpButton, "cell 1 5,growy");

	//---- floorDownButton ----
	floorDownButton.setIcon(new ImageIcon(getClass().getResource("/down.png")));
	add(floorDownButton, "cell 2 5,grow");

	//---- receptionSellButton ----
	receptionSellButton.setText("VENTA RECEPCION");
	receptionSellButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(receptionSellButton, "cell 1 6 2 1,growy");

	//---- managementOptionsButton ----
	managementOptionsButton.setText("OPCIONES");
	managementOptionsButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(managementOptionsButton, "cell 1 7 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel containerPanel;
    private JLabel turnNumberLabel;
    private JLabel towerLabelInforfmation;
    private JButton previousTowerButton;
    private JButton nextTowerButton;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JButton floorUpButton;
    private JButton floorDownButton;
    private JButton receptionSellButton;
    private JButton managementOptionsButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the currentFloorIndex
     */
    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    /**
     * @return the cardLayout
     */
    public CardLayout getCardLayout() {
        return cardLayout;
    }

    /**
     * @return the turnNumberLabel
     */
    public JLabel getTurnNumberLabel() {
        return turnNumberLabel;
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
     * @return the floorUpButton
     */
    public JButton getFloorUpButton() {
        return floorUpButton;
    }

    /**
     * @return the floorDownButton
     */
    public JButton getFloorDownButton() {
        return floorDownButton;
    }

    /**
     * @return the receptionSaleButton
     */
    public JButton getReceptionSellButton() {
        return receptionSellButton;
    }

    /**
     * @return the managementOptionsButton
     */
    public JButton getManagementOptionsButton() {
        return managementOptionsButton;
    }

    /**
     * @return the roomButtonGridByTower
     */
    public ArrayList<ArrayList<ArrayList<JButton>>> getRoomButtonGridByTower() {
        return roomButtonGridByTower;
    }

    /**
     * @return the towerLabelInforfmation
     */
    public JLabel getTowerLabelInforfmation() {
        return towerLabelInforfmation;
    }

    /**
     * @return the previousTowerButton
     */
    public JButton getPreviousTowerButton() {
        return previousTowerButton;
    }

    /**
     * @return the nextTowerButton
     */
    public JButton getNextTowerButton() {
        return nextTowerButton;
    }

    public int getCurrentTowerIndex() {
        return currentTowerIndex;
    }

}
