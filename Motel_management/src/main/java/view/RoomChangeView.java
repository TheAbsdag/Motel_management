/*
 * Created by JFormDesigner on Thu Jun 20 18:05:36 COT 2024
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
public class RoomChangeView extends JPanel {

    private ArrayList<ArrayList<ArrayList<JButton>>> roomButtonGridByTower;
    private int currentFloorIndex;
    private int currentTowerIndex;
    private CardLayout cardLayout;

    public RoomChangeView() {
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
        getRoomButtonGridByTower().clear();
        roomButtonPanel.removeAll();
        roomButtonPanel.setLayout(cardLayout);

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
                roomButtonPanel.add(floorButtonPanel, "Tower" + tower + "Floor" + floor);
            }

            getRoomButtonGridByTower().add(towerFloors);
        }

        cardLayout.show(roomButtonPanel, "Tower0Floor0");
        updateTowerLabel();
    }

    public void switchFloor(int floorIndex) {
        if (getCurrentTowerIndex() >= 0 && getCurrentTowerIndex() < getRoomButtonGridByTower().size()) {
            ArrayList<ArrayList<JButton>> currentTower = getRoomButtonGridByTower().get(getCurrentTowerIndex());
            if (floorIndex >= 0 && floorIndex < currentTower.size()) {
                currentFloorIndex = floorIndex;
                switchToCurrentTowerAndFloor();
            }
        }
    }

    public void switchTower(int towerIndex) {
        if (towerIndex >= 0 && towerIndex < getRoomButtonGridByTower().size()) {
            currentTowerIndex = towerIndex;
            // Reset to floor 0 when switching towers
            currentFloorIndex = 0;
            switchToCurrentTowerAndFloor();
            updateTowerLabel();
        }
    }

    private void switchToCurrentTowerAndFloor() {
        cardLayout.show(roomButtonPanel, "Tower" + getCurrentTowerIndex() + "Floor" + currentFloorIndex);
    }

    private void updateTowerLabel() {
        getTowerLabelInforfmation().setText("TORRE: " + (getCurrentTowerIndex() + 1));
    }

    /**
     * Hides and disables all tower-specific UI in single-tower mode.
     * Call this when the rooms array contains only one tower.
     */
    public void setSingleTowerMode() {
        getPreviousTowerButton().setVisible(false);
        getPreviousTowerButton().setEnabled(false);
        getNextTowerButton().setVisible(false);
        getNextTowerButton().setEnabled(false);
        getTowerLabelInforfmation().setVisible(false);
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	roomButtonPanel = new JPanel();
	towerLabelInforfmation = new JLabel();
	previousTowerButton = new JButton();
	nextTowerButton = new JButton();
	timeLabel = new JLabel();
	dateLabel = new JLabel();
	upButton = new JButton();
	downButton = new JButton();
	selectedInformativeLabel = new JLabel();
	selectedLabel = new JLabel();
	backButton = new JButton();
	confirmButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[235,fill]" +
	    "[501,fill]" +
	    "[133,fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[110]" +
	    "[40]" +
	    "[]"));

	//======== roomButtonPanel ========
	{
	    roomButtonPanel.setLayout(null);

	    {
		// compute preferred size
		Dimension preferredSize = new Dimension();
		for(int i = 0; i < roomButtonPanel.getComponentCount(); i++) {
		    Rectangle bounds = roomButtonPanel.getComponent(i).getBounds();
		    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
		    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
		}
		Insets insets = roomButtonPanel.getInsets();
		preferredSize.width += insets.right;
		preferredSize.height += insets.bottom;
		roomButtonPanel.setMinimumSize(preferredSize);
		roomButtonPanel.setPreferredSize(preferredSize);
	    }
	}
	add(roomButtonPanel, "cell 0 0 2 6,growy");

	//---- towerLabelInforfmation ----
	towerLabelInforfmation.setText("TOWER: N ");
	towerLabelInforfmation.setHorizontalAlignment(SwingConstants.CENTER);
	towerLabelInforfmation.setFont(new Font("Segoe UI Black", Font.BOLD, 30));
	add(towerLabelInforfmation, "cell 2 0 2 1");

	//---- previousTowerButton ----
	previousTowerButton.setIcon(new ImageIcon(getClass().getResource("/left.png")));
	add(previousTowerButton, "cell 2 1,growy");

	//---- nextTowerButton ----
	nextTowerButton.setIcon(new ImageIcon(getClass().getResource("/right.png")));
	add(nextTowerButton, "cell 3 1,growy");

	//---- timeLabel ----
	timeLabel.setText("00:00 am");
	timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(timeLabel, "cell 2 2 2 1");

	//---- dateLabel ----
	dateLabel.setText("21 DE JULIO 2020");
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(dateLabel, "cell 2 3 2 1");

	//---- upButton ----
	upButton.setIcon(new ImageIcon(getClass().getResource("/up.png")));
	add(upButton, "cell 2 4,growy");

	//---- downButton ----
	downButton.setIcon(new ImageIcon(getClass().getResource("/down.png")));
	add(downButton, "cell 3 4,growy");

	//---- selectedInformativeLabel ----
	selectedInformativeLabel.setText("SELECCIONADA:");
	selectedInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(selectedInformativeLabel, "cell 2 5");

	//---- selectedLabel ----
	selectedLabel.setText("204");
	selectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
	selectedLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(selectedLabel, "cell 3 5");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(backButton, "cell 0 6,growy");

	//---- confirmButton ----
	confirmButton.setText("CONFIRMAR ");
	confirmButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(confirmButton, "cell 2 6 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel roomButtonPanel;
    private JLabel towerLabelInforfmation;
    private JButton previousTowerButton;
    private JButton nextTowerButton;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JButton upButton;
    private JButton downButton;
    private JLabel selectedInformativeLabel;
    private JLabel selectedLabel;
    private JButton backButton;
    private JButton confirmButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the currentFloorIndex
     */
    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    /**
     * @return the roomButtonPanel
     */
    public JPanel getRoomButtonPanel() {
        return roomButtonPanel;
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
     * @return the upButton
     */
    public JButton getUpButton() {
        return upButton;
    }

    /**
     * @return the downButton
     */
    public JButton getDownButton() {
        return downButton;
    }

    /**
     * @return the selectedInformativeLabel
     */
    public JLabel getSelectedInformativeLabel() {
        return selectedInformativeLabel;
    }

    /**
     * @return the selectedLabel
     */
    public JLabel getSelectedLabel() {
        return selectedLabel;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }

    /**
     * @return the confirmButton
     */
    public JButton getConfirmButton() {
        return confirmButton;
    }

    /**
     * @return the roomButtonGridByTower
     */
    public ArrayList<ArrayList<ArrayList<JButton>>> getRoomButtonGridByTower() {
        return roomButtonGridByTower;
    }

    /**
     * @return the currentTowerIndex
     */
    public int getCurrentTowerIndex() {
        return currentTowerIndex;
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

}
