/*
 * Created by JFormDesigner on Thu Jun 20 18:05:36 COT 2024
 */
package view;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.*;
import view.interfaces.TimeLabelInterface;

/**
 * @author Santiago
 */
public class RoomChangeView extends JPanel implements TimeLabelInterface {

    private ArrayList<ArrayList<ArrayList<JButton>>> roomButtonGridByTower;
    private final NavigationState nav;
    private CardLayout cardLayout;

    public RoomChangeView(NavigationState nav) {
        this.nav = nav;
        initCustomComponents();
        initComponents();
    }

    private void initCustomComponents() {
        roomButtonGridByTower = new ArrayList<>();
        cardLayout = new CardLayout();
    }

    public void createButtonsForTowers(int[][] roomsPerTower) {
        roomButtonGridByTower.clear();
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

            roomButtonGridByTower.add(towerFloors);
        }

        cardLayout.show(roomButtonPanel, "Tower0Floor0");
        updateTowerLabel();
    }

    public void switchFloor(int floorIndex) {
        if (nav.getCurrentTowerIndex() >= 0 && nav.getCurrentTowerIndex() < roomButtonGridByTower.size()) {
            ArrayList<ArrayList<JButton>> currentTower = roomButtonGridByTower.get(nav.getCurrentTowerIndex());
            if (floorIndex >= 0 && floorIndex < currentTower.size()) {
                nav.setCurrentFloorIndex(floorIndex);
                switchToCurrentTowerAndFloor();
            }
        }
    }

    public void switchTower(int towerIndex) {
        if (towerIndex >= 0 && towerIndex < roomButtonGridByTower.size()) {
            nav.setCurrentTowerIndex(towerIndex);
            nav.setCurrentFloorIndex(0);
            switchToCurrentTowerAndFloor();
            updateTowerLabel();
        }
    }

    private void switchToCurrentTowerAndFloor() {
        cardLayout.show(roomButtonPanel, "Tower" + nav.getCurrentTowerIndex() + "Floor" + nav.getCurrentFloorIndex());
    }

    private void updateTowerLabel() {
        towerLabelInforfmation.setText("TORRE: " + (nav.getCurrentTowerIndex() + 1));
    }

    /**
     * Hides and disables all tower-specific UI in single-tower mode.
     * Call this when the rooms array contains only one tower.
     */
    public void setSingleTowerMode() {
        previousTowerButton.setVisible(false);
        previousTowerButton.setEnabled(false);
        nextTowerButton.setVisible(false);
        nextTowerButton.setEnabled(false);
        towerLabelInforfmation.setVisible(false);
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

    // ========== Encapsulated API ==========

    public int getCurrentFloorIndex() { return nav.getCurrentFloorIndex(); }
    public int getCurrentTowerIndex() { return nav.getCurrentTowerIndex(); }

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }

    /** Sets the selected room label text. */
    public void setSelectedLabel(String text) { selectedLabel.setText(text); }

    // -- Room grid access (encapsulated: no raw JButton exposure) --

    /**
     * Sets the visible text and background color of a room button in the change grid.
     */
    public void setRoomAppearance(int tower, int floor, int room, String text, Color bg) {
        if (tower < roomButtonGridByTower.size()
                && floor < roomButtonGridByTower.get(tower).size()
                && room < roomButtonGridByTower.get(tower).get(floor).size()) {
            JButton btn = roomButtonGridByTower.get(tower).get(floor).get(room);
            btn.setText(text);
            btn.setBackground(bg);
        }
    }

    /**
     * Registers a click listener for a room button in the change grid.
     */
    public void onRoomClick(int tower, int floor, int room, Runnable action) {
        if (tower < roomButtonGridByTower.size()
                && floor < roomButtonGridByTower.get(tower).size()
                && room < roomButtonGridByTower.get(tower).get(floor).size()) {
            roomButtonGridByTower.get(tower).get(floor).get(room)
                    .addActionListener(e -> action.run());
        }
    }

    /** Returns the number of rooms on a given floor. */
    public int getRoomCount(int tower, int floor) {
        if (tower < roomButtonGridByTower.size()
                && floor < roomButtonGridByTower.get(tower).size()) {
            return roomButtonGridByTower.get(tower).get(floor).size();
        }
        return 0;
    }

    // -- Navigation button listeners --

    /** Registers a listener for the floor up button. */
    public void onFloorUp(Runnable action) { upButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the floor down button. */
    public void onFloorDown(Runnable action) { downButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the back button. */
    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the confirm button. */
    public void onConfirmButton(Runnable action) { confirmButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the previous tower button. */
    public void onPreviousTower(Runnable action) { previousTowerButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the next tower button. */
    public void onNextTower(Runnable action) { nextTowerButton.addActionListener(e -> action.run()); }
}

