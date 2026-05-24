/*
 * Created by JFormDesigner on Thu May 14 15:18:05 GMT-05:00 2026
 */

package view;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.*;

/**
 * View for configuring towers, floors, and rooms.
 *
 * <p>The left panel shows tower and floor selectors. The central
 * {@code containerPanel} uses a CardLayout with two cards:
 * <ul>
 *   <li>{@code "floorGrid"} — a grid of room buttons per floor (like {@link FloorView})</li>
 *   <li>{@code "roomConfig"} — the embedded {@link RoomConfigurationView} for editing a single room</li>
 * </ul>
 *
 * @author SECC
 */
public class FloorConfigurationView extends JPanel {

    private CardLayout containerCardLayout;
    private CardLayout floorGridLayout;
    private JPanel floorGridPanel;
    private ArrayList<ArrayList<ArrayList<JButton>>> roomButtonGridByTower;

    private int currentTowerIndex;
    private int currentFloorIndex;
    private boolean hasUnsavedChanges;

    public FloorConfigurationView() {
        initCustomComponents();
        initComponents();
    }

    private void initCustomComponents() {
        containerCardLayout = new CardLayout();
        floorGridLayout = new CardLayout();
        floorGridPanel = new JPanel(floorGridLayout);
        roomButtonGridByTower = new ArrayList<>();
        currentTowerIndex = 0;
        currentFloorIndex = 0;
        hasUnsavedChanges = false;
    }

    private void setupInternalComponents() {
        Font listFont = new Font("Segoe UI Black", Font.PLAIN, 18);
        towerList.setFont(listFont);
        towerList.setFixedCellHeight(36);
        floorList.setFont(listFont);
        floorList.setFixedCellHeight(36);
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	floorConfigurationTitleLabel = new JLabel();
	towerSelectedInformativeLabel = new JLabel();
	towerSelectedLabel = new JLabel();
	towerSelectionInformativeLabel = new JLabel();
	deleteTowerButton = new JButton();
	towerListLeftButton = new JButton();
	towerScrollPane = new JScrollPane();
	towerList = new JList();
	towerListRightButton = new JButton();
	newTowerButton = new JButton();
	floorSelectedInformativeLabel = new JLabel();
	floorSelectedLabel = new JLabel();
	deleteFloorButton = new JButton();
	containerPanel = new JPanel();
	floorListUpButton = new JButton();
	floorScrollPane = new JScrollPane();
	floorList = new JList();
	newFloorButton = new JButton();
	floorListDownButton = new JButton();
	backButton = new JButton();
	newRoomButton = new JButton();
	saveButton = new JButton();

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
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- floorConfigurationTitleLabel ----
	floorConfigurationTitleLabel.setText("CONFIGURACION HABITACIONES MOTEL");
	floorConfigurationTitleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(floorConfigurationTitleLabel, "cell 0 0 10 1");

	//---- towerSelectedInformativeLabel ----
	towerSelectedInformativeLabel.setText("TORRE");
	towerSelectedInformativeLabel.setHorizontalAlignment(SwingConstants.LEFT);
	towerSelectedInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(towerSelectedInformativeLabel, "cell 0 1 2 1");

	//---- towerSelectedLabel ----
	towerSelectedLabel.setText("X");
	towerSelectedLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(towerSelectedLabel, "cell 0 1 2 1");

	//---- towerSelectionInformativeLabel ----
	towerSelectionInformativeLabel.setText("SELECCIONE TORRE");
	towerSelectionInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	towerSelectionInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(towerSelectionInformativeLabel, "cell 2 1 8 1");

	//---- deleteTowerButton ----
	deleteTowerButton.setText("ELIMINAR");
	deleteTowerButton.setBackground(new Color(0xff6666));
	deleteTowerButton.setForeground(Color.black);
	deleteTowerButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(deleteTowerButton, "cell 0 2 2 1,growy");
	add(towerListLeftButton, "cell 3 2,growy");

	//======== towerScrollPane ========
	{

	    //---- towerList ----
	    towerList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	    towerList.setVisibleRowCount(1);
	    towerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    towerScrollPane.setViewportView(towerList);
	}
	add(towerScrollPane, "cell 4 2 4 1,growy");
	add(towerListRightButton, "cell 8 2,growy");

	//---- newTowerButton ----
	newTowerButton.setText("A\u00d1ADIR TORRE");
	newTowerButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(newTowerButton, "cell 9 2,growy");

	//---- floorSelectedInformativeLabel ----
	floorSelectedInformativeLabel.setText("PISO");
	floorSelectedInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(floorSelectedInformativeLabel, "cell 0 3 2 1");

	//---- floorSelectedLabel ----
	floorSelectedLabel.setText("X");
	floorSelectedLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(floorSelectedLabel, "cell 0 3 2 1");

	//---- deleteFloorButton ----
	deleteFloorButton.setText("ELIMINAR");
	deleteFloorButton.setBackground(new Color(0xff6666));
	deleteFloorButton.setForeground(Color.black);
	deleteFloorButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(deleteFloorButton, "cell 0 4 2 1,growy");

	//======== containerPanel ========
	{
	    containerPanel.setForeground(new Color(0x999999));
	    containerPanel.setBackground(new Color(0x999999));
	    containerPanel.setLayout(null);
	    containerPanel.setLayout(containerCardLayout);
	}
	add(containerPanel, "cell 2 3 8 7,grow");
	add(floorListUpButton, "cell 0 5,growy");

	//======== floorScrollPane ========
	{

	    //---- floorList ----
	    floorList.setVisibleRowCount(6);
	    floorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    floorScrollPane.setViewportView(floorList);
	}
	add(floorScrollPane, "cell 0 6 1 3,growy");

	//---- newFloorButton ----
	newFloorButton.setText("A\u00d1ADIR PISO");
	newFloorButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(newFloorButton, "cell 1 7,growy");
	add(floorListDownButton, "cell 0 9,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(backButton, "cell 0 10,growy");

	//---- newRoomButton ----
	newRoomButton.setText("NUEVA HABITACION");
	newRoomButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(newRoomButton, "cell 4 10 4 1,growy");

	//---- saveButton ----
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(saveButton, "cell 9 10,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on

        setupInternalComponents();

        containerPanel.add(floorGridPanel, "floorGrid");
        containerCardLayout.show(containerPanel, "floorGrid");

    }

    // ========== Room Grid Display ==========

    /**
     * Builds the room button grid for all towers/floors and shows the first.
     * Preserves the current tower/floor selection if still valid.
     * @param roomsPerTower rooms-per-floor array from the model
     */
    public void createRoomButtons(int[][] roomsPerTower) {
        createRoomButtons(roomsPerTower, true);
    }

    /**
     * @param roomsPerTower    rooms-per-floor array
     * @param preserveSelection if true, keeps the current tower/floor if still valid
     */
    public void createRoomButtons(int[][] roomsPerTower, boolean preserveSelection) {
        int savedTower = currentTowerIndex;
        int savedFloor = currentFloorIndex;

        roomButtonGridByTower.clear();
        floorGridPanel.removeAll();

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
                    button.setForeground(Color.BLACK);
                    button.setFocusPainted(false);
                    button.setFont(new Font("SEGOE UI BLACK", Font.BOLD, 18));
                    floorButtons.add(button);
                    floorButtonPanel.add(button);
                }

                towerFloors.add(floorButtons);
                floorGridPanel.add(floorButtonPanel, "Tower" + tower + "Floor" + floor);
            }

            roomButtonGridByTower.add(towerFloors);
        }

        if (preserveSelection && roomsPerTower.length > 0) {
            if (savedTower >= 0 && savedTower < roomsPerTower.length) {
                currentTowerIndex = savedTower;
            } else {
                currentTowerIndex = 0;
            }
            int[] towerFloors = roomsPerTower[currentTowerIndex];
            if (savedFloor >= 0 && savedFloor < towerFloors.length) {
                currentFloorIndex = savedFloor;
            } else {
                currentFloorIndex = 0;
            }
        } else {
            currentTowerIndex = 0;
            currentFloorIndex = 0;
        }
        switchToCurrentTowerAndFloor();
        updateTowerSelectedLabel();
        updateFloorSelectedLabel();
    }

    /**
     * Explicitly sets both tower and floor, switching the displayed grid.
     * @param towerIndex tower to show
     * @param floorIndex floor to show (clamped to valid range)
     */
    public void setTowerAndFloor(int towerIndex, int floorIndex) {
        if (roomButtonGridByTower.isEmpty()) return;
        if (towerIndex >= 0 && towerIndex < roomButtonGridByTower.size()) {
            currentTowerIndex = towerIndex;
            currentFloorIndex = Math.min(floorIndex, roomButtonGridByTower.get(towerIndex).size() - 1);
            if (currentFloorIndex < 0) currentFloorIndex = 0;
            switchToCurrentTowerAndFloor();
            updateTowerSelectedLabel();
            updateFloorSelectedLabel();
        }
    }

    // ========== Navigation ==========

    /** Shows the floor grid card in the container panel. */
    public void switchToFloorGrid() {
        containerCardLayout.show(containerPanel, "floorGrid");
    }

    /** Switches the displayed floor within the current tower. */
    public void switchFloor(int floorIndex) {
        if (roomButtonGridByTower.isEmpty()) {
            return;
        }
        if (currentTowerIndex >= 0 && currentTowerIndex < roomButtonGridByTower.size()) {
            ArrayList<ArrayList<JButton>> currentTower = roomButtonGridByTower.get(currentTowerIndex);
            if (floorIndex >= 0 && floorIndex < currentTower.size()) {
                currentFloorIndex = floorIndex;
                switchToCurrentTowerAndFloor();
                updateFloorSelectedLabel();
            }
        }
    }

    /** Switches the displayed tower (resets floor to 0). */
    public void switchTower(int towerIndex) {
        if (roomButtonGridByTower.isEmpty()) {
            return;
        }
        if (towerIndex >= 0 && towerIndex < roomButtonGridByTower.size()) {
            currentTowerIndex = towerIndex;
            currentFloorIndex = 0;
            switchToCurrentTowerAndFloor();
            updateTowerSelectedLabel();
            updateFloorSelectedLabel();
        }
    }

    private void switchToCurrentTowerAndFloor() {
        floorGridLayout.show(floorGridPanel,
                "Tower" + currentTowerIndex + "Floor" + currentFloorIndex);
    }

    private void updateTowerSelectedLabel() {
        towerSelectedLabel.setText("TORRE " + (currentTowerIndex + 1));
    }

    private void updateFloorSelectedLabel() {
        floorSelectedLabel.setText("PISO " + (currentFloorIndex + 1));
    }

    // ========== Dirty Tracking ==========

    public void markDirty() {
        hasUnsavedChanges = true;
    }

    public void clearDirty() {
        hasUnsavedChanges = false;
    }

    public boolean isDirty() {
        return hasUnsavedChanges;
    }

    // ========== Behavior Methods ==========

    public void onBackButton(Runnable action) {
        backButton.addActionListener(e -> action.run());
    }

    public void removeBackListeners() {
        for (var al : backButton.getActionListeners()) {
            backButton.removeActionListener(al);
        }
    }

    public void onSaveButton(Runnable action) {
        saveButton.addActionListener(e -> action.run());
    }

    public void removeSaveListeners() {
        for (var al : saveButton.getActionListeners()) {
            saveButton.removeActionListener(al);
        }
    }

    /** Enables or disables the back button visibility and function. */
    public void setBackEnabled(boolean enabled) {
        backButton.setVisible(enabled);
        backButton.setEnabled(enabled);
    }

    public void onDeleteTower(Runnable action) {
        deleteTowerButton.addActionListener(e -> action.run());
    }

    public void onNewTower(Runnable action) {
        newTowerButton.addActionListener(e -> action.run());
    }

    public void onDeleteFloor(Runnable action) {
        deleteFloorButton.addActionListener(e -> action.run());
    }

    public void onNewFloor(Runnable action) {
        newFloorButton.addActionListener(e -> action.run());
    }

    public void onNewRoomButton(Runnable action) {
        newRoomButton.addActionListener(e -> action.run());
    }

    public void onTowerListLeft(Runnable action) {
        towerListLeftButton.addActionListener(e -> action.run());
    }

    public void onTowerListRight(Runnable action) {
        towerListRightButton.addActionListener(e -> action.run());
    }

    public void onFloorListUp(Runnable action) {
        floorListUpButton.addActionListener(e -> action.run());
    }

    public void onFloorListDown(Runnable action) {
        floorListDownButton.addActionListener(e -> action.run());
    }

    // ========== Tower List Encapsulated Methods ==========

    public void setTowerItems(String[] items) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String item : items) model.addElement(item);
        towerList.setModel(model);
        if (model.getSize() > 0) towerList.setSelectedIndex(0);
    }

    public void selectTower(int index) {
        if (index >= 0 && index < towerList.getModel().getSize()) {
            towerList.setSelectedIndex(index);
        }
    }

    public int getSelectedTowerIndex() {
        return towerList.getSelectedIndex();
    }

    public int getTowerCount() {
        return towerList.getModel().getSize();
    }

    // ========== Floor List Encapsulated Methods ==========

    public void setFloorItems(String[] items) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String item : items) model.addElement(item);
        floorList.setModel(model);
        if (model.getSize() > 0) floorList.setSelectedIndex(0);
    }

    public void selectFloor(int index) {
        if (index >= 0 && index < floorList.getModel().getSize()) {
            floorList.setSelectedIndex(index);
        }
    }

    public int getSelectedFloorIndex() {
        return floorList.getSelectedIndex();
    }

    public int getFloorCount() {
        return floorList.getModel().getSize();
    }

    public void onTowerSelection(ListSelectionListener listener) {
        towerList.addListSelectionListener(listener);
    }

    public void onFloorSelection(ListSelectionListener listener) {
        floorList.addListSelectionListener(listener);
    }

    // ========== Retained Accessors ==========

    public int getCurrentTowerIndex() {
        return currentTowerIndex;
    }

    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    public void onRoomClick(int tower, int floor, int room, Runnable action) {
        if (tower < roomButtonGridByTower.size()
                && floor < roomButtonGridByTower.get(tower).size()
                && room < roomButtonGridByTower.get(tower).get(floor).size()) {
            roomButtonGridByTower.get(tower).get(floor).get(room)
                    .addActionListener(e -> action.run());
        }
    }

    public void setRoomButtonText(int tower, int floor, int room, String text) {
        if (tower < roomButtonGridByTower.size()
                && floor < roomButtonGridByTower.get(tower).size()
                && room < roomButtonGridByTower.get(tower).get(floor).size()) {
            roomButtonGridByTower.get(tower).get(floor).get(room).setText(text);
        }
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel floorConfigurationTitleLabel;
    private JLabel towerSelectedInformativeLabel;
    private JLabel towerSelectedLabel;
    private JLabel towerSelectionInformativeLabel;
    private JButton deleteTowerButton;
    private JButton towerListLeftButton;
    private JScrollPane towerScrollPane;
    private JList towerList;
    private JButton towerListRightButton;
    private JButton newTowerButton;
    private JLabel floorSelectedInformativeLabel;
    private JLabel floorSelectedLabel;
    private JButton deleteFloorButton;
    private JPanel containerPanel;
    private JButton floorListUpButton;
    private JScrollPane floorScrollPane;
    private JList floorList;
    private JButton newFloorButton;
    private JButton floorListDownButton;
    private JButton backButton;
    private JButton newRoomButton;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
