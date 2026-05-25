package view;

import view.helpers.NavigationState;
import java.awt.*;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;
import view.interfaces.TimeLabelInterface;

/**
 * @author Santiago
 */
public class FloorView extends JPanel implements TimeLabelInterface {

    private ArrayList<ArrayList<ArrayList<JButton>>> roomButtonGridByTower;
    private final NavigationState nav;
    private CardLayout cardLayout;

    // Overtime warning components
    private ArrayList<String> currentWarnings = new ArrayList<>();
    private JList<String> warningList;
    private DefaultListModel<String> warningListModel;
    private Timer scrollTimer;
    private Timer resetTimer;
    private boolean scrollingDown = true;
    private int currentScrollIndex = 0;

    public FloorView(NavigationState nav) {
        this.nav = nav;
        initCustomComponents();
        initComponents();
        initWarningList();
    }

    private void initCustomComponents() {
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
                        new Font("SEGOE UI BLACK", Font.BOLD, 28)));

                ArrayList<JButton> floorButtons = new ArrayList<>();
                for (int room = 0; room < roomsPerFloor[floor]; room++) {
                    JButton button = new JButton("" + (room + 1));
                    button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    button.setBackground(Color.WHITE);
                    button.setForeground(Color.BLACK);
                    button.setFocusPainted(false);
                    button.setFont(new Font("SEGOE UI BLACK", Font.BOLD, 20));
                    floorButtons.add(button);
                    floorButtonPanel.add(button);
                }

                towerFloors.add(floorButtons);
                containerPanel.add(floorButtonPanel, "Tower" + tower + "Floor" + floor);
            }

            roomButtonGridByTower.add(towerFloors);
        }

        if (roomsPerTower.length == 1) {
            towerLabelInformation.setVisible(false);
            towerLabel.setVisible(false);
            previousTowerButton.setVisible(false);
            nextTowerButton.setVisible(false);
        } else {
            towerLabelInformation.setVisible(true);
            towerLabel.setVisible(true);
            previousTowerButton.setVisible(true);  
            nextTowerButton.setVisible(true);
        }

        cardLayout.show(containerPanel, "Tower0Floor0");
        updateTowerLabel();
        updateFloorLabel();
    }

    public void switchFloor(int floorIndex) {
        if (nav.getCurrentTowerIndex() >= 0 && nav.getCurrentTowerIndex() < roomButtonGridByTower.size()) {
            ArrayList<ArrayList<JButton>> currentTower = roomButtonGridByTower.get(nav.getCurrentTowerIndex());
            if (floorIndex >= 0 && floorIndex < currentTower.size()) {
                nav.setCurrentFloorIndex(floorIndex);
                switchToCurrentTowerAndFloor();
                updateFloorLabel();
            }
        }
    }

    public void switchTower(int towerIndex) {
        if (towerIndex >= 0 && towerIndex < roomButtonGridByTower.size()) {
            nav.setCurrentTowerIndex(towerIndex);
            nav.setCurrentFloorIndex(0);
            switchToCurrentTowerAndFloor();
            updateTowerLabel();
            updateFloorLabel();
        }
    }

    private void switchToCurrentTowerAndFloor() {
        cardLayout.show(containerPanel, "Tower" + nav.getCurrentTowerIndex() + "Floor" + nav.getCurrentFloorIndex());
    }

    private void updateTowerLabel() {
        towerLabel.setText(String.valueOf(nav.getCurrentTowerIndex() + 1));
    }

    private void updateFloorLabel() {
        floorLabel.setText(String.valueOf(nav.getCurrentFloorIndex() + 1));
    }

    // ========== Overtime Warning ==========

    private void initWarningList() {
        warningListModel = new DefaultListModel<>();
        warningList = new JList<>(warningListModel);
        warningList.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
        warningList.setSelectionMode(0);
        warningList.setFixedCellHeight(warningList.getFontMetrics(warningList.getFont()).getHeight());
        warningScrollPane.setViewportView(warningList);
        scrollTimer = new Timer(2000, e -> autoScrollWarnings());
        resetTimer = new Timer(2000, e -> resetScrollPosition());
        resetTimer.setRepeats(false);
    }

    private void resetScrollPosition() {
        int visibleRows = getVisibleRowCount();
        int totalRows = warningListModel.getSize();
        warningList.ensureIndexIsVisible(0);
        if (visibleRows - 1 < totalRows) {
            warningList.ensureIndexIsVisible(visibleRows - 1);
        }
    }

    public void updateWarnings(List<String> newWarnings) {
        if (!currentWarnings.equals(newWarnings)) {
            currentWarnings = new ArrayList<>(newWarnings);
            warningListModel.removeAllElements();

            for (String warning : newWarnings) {
                warningListModel.addElement(warning);
            }

            if (newWarnings.size() > getVisibleRowCount()) {
                if (!scrollTimer.isRunning()) {
                    currentScrollIndex = 0;
                    scrollingDown = true;
                    scrollTimer.start();
                }
            } else {
                scrollTimer.stop();
                warningList.ensureIndexIsVisible(0);
            }
        }
    }

    private int getVisibleRowCount() {
        JViewport viewport = warningScrollPane.getViewport();
        int viewportHeight = viewport.getExtentSize().height;
        int rowHeight = warningList.getFixedCellHeight();
        return rowHeight > 0 ? viewportHeight / rowHeight : 3;
    }

    private void autoScrollWarnings() {
        if (warningListModel.getSize() == 0) {
            scrollTimer.stop();
        } else {
            final int visibleRows = getVisibleRowCount();
            final int totalRows = warningListModel.getSize();
            if (totalRows <= visibleRows) {
                scrollTimer.stop();
            } else {
                warningList.ensureIndexIsVisible(currentScrollIndex);
                if (currentScrollIndex + visibleRows - 1 < totalRows) {
                    warningList.ensureIndexIsVisible(currentScrollIndex + visibleRows - 1);
                }

                currentScrollIndex++;
                if (currentScrollIndex + visibleRows > totalRows) {
                    currentScrollIndex = 0;
                    resetTimer.start();
                }
            }
        }
    }

    // ========== initComponents (JFormDesigner) ==========

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	containerPanel = new JPanel();
	dateLabel = new JLabel();
	timeLabel = new JLabel();
	turnNumberInformativeLabel = new JLabel();
	turnNumberLabel = new JLabel();
	warningIconLabel = new JLabel();
	warningScrollPane = new JScrollPane();
	towerLabelInformation = new JLabel();
	towerLabel = new JLabel();
	previousTowerButton = new JButton();
	nextTowerButton = new JButton();
	floorLabelInformation = new JLabel();
	floorLabel = new JLabel();
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
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[grow]" +
	    "[grow]" +
	    "[]"));

	//======== containerPanel ========
	{
	    containerPanel.setLayout(null);
	    containerPanel.setLayout(cardLayout);
	}
	add(containerPanel, "cell 0 0 1 9,grow");

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(dateLabel, "cell 1 0 2 1,growy");

	//---- timeLabel ----
	timeLabel.setText("time");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(timeLabel, "cell 1 1,growy");

	//---- turnNumberInformativeLabel ----
	turnNumberInformativeLabel.setText("TURNO:");
	turnNumberInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	turnNumberInformativeLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
	add(turnNumberInformativeLabel, "cell 2 1");

	//---- turnNumberLabel ----
	turnNumberLabel.setText("X");
	turnNumberLabel.setHorizontalAlignment(SwingConstants.LEFT);
	turnNumberLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
	add(turnNumberLabel, "cell 2 1");

	//---- warningIconLabel ----
	warningIconLabel.setMinimumSize(new Dimension(120, 120));
	warningIconLabel.setMaximumSize(new Dimension(120, 120));
	warningIconLabel.setIconTextGap(0);
	add(warningIconLabel, "cell 1 2,growy");

	//---- warningScrollPane ----
	warningScrollPane.setPreferredSize(new Dimension(120, 150));
	warningScrollPane.setMaximumSize(new Dimension(120, 250));
	warningScrollPane.setMinimumSize(new Dimension(120, 120));
	warningScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	warningScrollPane.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	warningScrollPane.setAlignmentX(0.0F);
	add(warningScrollPane, "cell 2 2,grow");

	//---- towerLabelInformation ----
	towerLabelInformation.setText("TORRE: ");
	towerLabelInformation.setHorizontalAlignment(SwingConstants.CENTER);
	towerLabelInformation.setFont(new Font("Segoe UI Black", Font.BOLD, 30));
	add(towerLabelInformation, "cell 1 3 2 1");

	//---- towerLabel ----
	towerLabel.setText("X");
	towerLabel.setHorizontalAlignment(SwingConstants.LEFT);
	towerLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 30));
	add(towerLabel, "cell 1 3 2 1");

	//---- previousTowerButton ----
	previousTowerButton.setIcon(new ImageIcon(getClass().getResource("/images/left.png")));
	previousTowerButton.setMargin(new Insets(0, 0, 0, 0));
	previousTowerButton.setPreferredSize(new Dimension(120, 120));
	add(previousTowerButton, "cell 1 4,growy");

	//---- nextTowerButton ----
	nextTowerButton.setIcon(new ImageIcon(getClass().getResource("/images/right.png")));
	nextTowerButton.setMargin(new Insets(0, 0, 0, 0));
	nextTowerButton.setPreferredSize(new Dimension(120, 120));
	add(nextTowerButton, "cell 2 4,growy");

	//---- floorLabelInformation ----
	floorLabelInformation.setText("PISO: ");
	floorLabelInformation.setHorizontalAlignment(SwingConstants.CENTER);
	floorLabelInformation.setFont(new Font("Segoe UI Black", Font.BOLD, 30));
	add(floorLabelInformation, "cell 1 5 2 1");

	//---- floorLabel ----
	floorLabel.setText("X");
	floorLabel.setHorizontalAlignment(SwingConstants.LEFT);
	floorLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 30));
	add(floorLabel, "cell 1 5 2 1");

	//---- floorUpButton ----
	floorUpButton.setIcon(new ImageIcon(getClass().getResource("/images/up.png")));
	floorUpButton.setPreferredSize(new Dimension(120, 120));
	add(floorUpButton, "cell 1 6,growy");

	//---- floorDownButton ----
	floorDownButton.setIcon(new ImageIcon(getClass().getResource("/images/down.png")));
	floorDownButton.setPreferredSize(new Dimension(120, 120));
	add(floorDownButton, "cell 2 6,grow");

	//---- receptionSellButton ----
	receptionSellButton.setText("VENTA RECEPCION");
	receptionSellButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(receptionSellButton, "cell 1 7 2 1,growy");

	//---- managementOptionsButton ----
	managementOptionsButton.setText("OPCIONES");
	managementOptionsButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(managementOptionsButton, "cell 1 8 2 1,growy");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel containerPanel;
    private JLabel dateLabel;
    private JLabel timeLabel;
    private JLabel turnNumberInformativeLabel;
    private JLabel turnNumberLabel;
    private JLabel warningIconLabel;
    private JScrollPane warningScrollPane;
    private JLabel towerLabelInformation;
    private JLabel towerLabel;
    private JButton previousTowerButton;
    private JButton nextTowerButton;
    private JLabel floorLabelInformation;
    private JLabel floorLabel;
    private JButton floorUpButton;
    private JButton floorDownButton;
    private JButton receptionSellButton;
    private JButton managementOptionsButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    // ========== Encapsulated API ==========

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }

    public int getCurrentFloorIndex() { return nav.getCurrentFloorIndex(); }
    public int getCurrentTowerIndex() { return nav.getCurrentTowerIndex(); }
    public void setTurnNumber(long number) { turnNumberLabel.setText(String.valueOf(number)); }
    public void setWarningVisible(boolean visible) { warningIconLabel.setVisible(visible); }
    public boolean isWarningVisible() { return warningIconLabel.isVisible(); }

    // -- Room grid access (encapsulated: no raw JButton exposure) --

    /**
     * Sets the visible text and background color of a specific room button.
     * Text may contain HTML formatting (e.g. {@code <html><center>101<br>SOBRETIEMPO</center></html>}).
     *
     * @param tower  zero-based tower index
     * @param floor  zero-based floor index
     * @param room   zero-based room position
     * @param text   the text to display on the button (supports HTML)
     * @param bg     the background color to apply
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
     * Registers a click listener for a specific room button.
     * The callback is fired each time the button is clicked and receives the
     * tower, floor, and room coordinates as context.
     *
     * @param tower   zero-based tower index
     * @param floor   zero-based floor index
     * @param room    zero-based room position
     * @param action  the callback to invoke (ignores the event object)
     */
    public void onRoomClick(int tower, int floor, int room, Runnable action) {
        if (tower < roomButtonGridByTower.size()
                && floor < roomButtonGridByTower.get(tower).size()
                && room < roomButtonGridByTower.get(tower).get(floor).size()) {
            roomButtonGridByTower.get(tower).get(floor).get(room)
                    .addActionListener(e -> action.run());
        }
    }

    /**
     * Returns the number of rooms on the given floor of a tower.
     * Useful for iterating over rooms without exposing the grid.
     */
    public int getRoomCount(int tower, int floor) {
        if (tower < roomButtonGridByTower.size()
                && floor < roomButtonGridByTower.get(tower).size()) {
            return roomButtonGridByTower.get(tower).get(floor).size();
        }
        return 0;
    }

    // -- Navigation button listeners --

    /** Registers a listener for the floor up button. */
    public void onFloorUp(Runnable action) { floorUpButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the floor down button. */
    public void onFloorDown(Runnable action) { floorDownButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the previous tower button. */
    public void onPreviousTower(Runnable action) { previousTowerButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the next tower button. */
    public void onNextTower(Runnable action) { nextTowerButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the management options button. */
    public void onManagementOptions(Runnable action) { managementOptionsButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the reception sell button. */
    public void onReceptionSell(Runnable action) { receptionSellButton.addActionListener(e -> action.run()); }
}

