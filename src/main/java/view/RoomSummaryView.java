package view;

import view.helpers.TouchScrollHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import model.ProgramConfig;
import model.RoomStatus;
import view.interfaces.TimeLabelInterface;

/**
 * Visual dashboard showing all rooms across all towers and floors,
 * color-coded by current status.
 *
 * <p>The grid is built once per layout shape ([towers][floors][rooms]) and
 * refreshed in place afterwards, so the scroll position and the touch drag
 * survive the periodic clock updates.</p>
 *
 * @author Santiago
 */
public class RoomSummaryView extends JPanel implements TimeLabelInterface {

    private static final Color FREE_BACKGROUND = new Color(30, 132, 73);
    private static final Color CLEANING_BACKGROUND = new Color(36, 113, 163);
    private static final Color OCCUPIED_BACKGROUND = new Color(192, 57, 43);
    private static final Color OVERTIME_BACKGROUND = new Color(241, 196, 15);
    private static final Color UNKNOWN_BACKGROUND = Color.GRAY;

    private static final Color TOWER_BACKGROUND = new Color(240, 240, 245);
    private static final Color TOWER_BORDER = new Color(100, 100, 150);
    private static final Color TOWER_TITLE_BACKGROUND = new Color(200, 200, 220);
    private static final Color FLOOR_TEXT = new Color(80, 80, 120);

    private JPanel summaryPanel;
    private JLabel[][][] roomLabels = new JLabel[0][][];

    public RoomSummaryView() {
        initComponents();
        summaryPanel = new JPanel();
        scrollPane.setViewportView(summaryPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        TouchScrollHandler.attach(scrollPane);
    }

    /**
     * Rebuilds the room summary display with current room data.
     * Only the labels' text, colors and tooltips change when the layout shape
     * stays the same.
     *
     * @param roomStatusData 3D array [tower][floor][room] of RoomStatus codes
     * @param roomStringsData 3D array [tower][floor][room] of room display strings
     * @param overtimeData    3D array [tower][floor][room] true if room is in overtime
     */
    public void updateRoomSummary(int[][][] roomStatusData, String[][][] roomStringsData, boolean[][][] overtimeData) {
        if (!matchesShape(roomStatusData)) {
            buildGrid(roomStatusData);
        }

        int free = 0;
        int cleaning = 0;
        int occupied = 0;
        int overtime = 0;

        for (int tower = 0; tower < roomStatusData.length; tower++) {
            for (int floor = 0; floor < roomStatusData[tower].length; floor++) {
                for (int room = 0; room < roomStatusData[tower][floor].length; room++) {
                    int statusCode = roomStatusData[tower][floor][room];
                    boolean isOvertime = overtimeData[tower][floor][room];
                    applyRoomAppearance(roomLabels[tower][floor][room], statusCode,
                            roomStringsData[tower][floor][room], isOvertime);
                    switch (statusCode) {
                        case 1 -> free++;
                        case 2 -> cleaning++;
                        case 3 -> {
                            occupied++;
                            if (isOvertime) {
                                overtime++;
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
        }
        updateLegend(free, cleaning, occupied, overtime);
    }

    private boolean matchesShape(int[][][] statusData) {
        if (roomLabels == null || roomLabels.length != statusData.length) {
            return false;
        }
        for (int tower = 0; tower < statusData.length; tower++) {
            if (roomLabels[tower] == null || roomLabels[tower].length != statusData[tower].length) {
                return false;
            }
            for (int floor = 0; floor < statusData[tower].length; floor++) {
                if (roomLabels[tower][floor] == null
                        || roomLabels[tower][floor].length != statusData[tower][floor].length) {
                    return false;
                }
            }
        }
        return true;
    }

    private void buildGrid(int[][][] statusData) {
        summaryPanel.removeAll();
        roomLabels = new JLabel[statusData.length][][];
        summaryPanel.setLayout(new MigLayout("fill, insets 15 20, gap 20",
                "[grow,fill]".repeat(statusData.length), "[grow,fill]"));

        for (int tower = 0; tower < statusData.length; tower++) {
            roomLabels[tower] = new JLabel[statusData[tower].length][];
            summaryPanel.add(createTowerPanel(tower, statusData[tower]), "grow");
        }
        summaryPanel.revalidate();
        summaryPanel.repaint();
    }

    private JPanel createTowerPanel(int tower, int[][] floorRoomCounts) {
        JPanel towerPanel = new JPanel(new MigLayout("fill, insets 10, gapy 6", "[grow,fill]", "[]6[grow,fill]"));
        towerPanel.setBackground(TOWER_BACKGROUND);
        towerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TOWER_BORDER, 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel towerLabel = new JLabel("TORRE " + ProgramConfig.humanNumber(tower), SwingConstants.CENTER);
        towerLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 18));
        towerLabel.setOpaque(true);
        towerLabel.setBackground(TOWER_TITLE_BACKGROUND);
        towerLabel.setForeground(new Color(60, 60, 100));
        towerLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        towerPanel.add(towerLabel, "growx, wrap");

        JPanel floorsPanel = new JPanel(new MigLayout("fill, insets 0, gapy 8",
                "[grow,fill]", "[grow,fill]".repeat(Math.max(1, floorRoomCounts.length))));
        floorsPanel.setOpaque(false);
        for (int floor = 0; floor < floorRoomCounts.length; floor++) {
            String constraint = floor == floorRoomCounts.length - 1 ? "grow" : "grow, wrap";
            floorsPanel.add(createFloorPanel(tower, floor, floorRoomCounts[floor].length), constraint);
        }
        towerPanel.add(floorsPanel, "grow");
        return towerPanel;
    }

    private JPanel createFloorPanel(int tower, int floor, int rooms) {
        JPanel floorPanel = new JPanel(new MigLayout("fill, insets 0", "[grow,fill]", "[]2[grow,fill]"));
        floorPanel.setOpaque(false);

        JLabel floorLabel = new JLabel("PISO " + ProgramConfig.humanNumber(floor));
        floorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        floorLabel.setForeground(FLOOR_TEXT);
        floorPanel.add(floorLabel, "growx, wrap");

        roomLabels[tower][floor] = new JLabel[rooms];
        JPanel roomsPanel = new JPanel(new GridLayout(0, columnsFor(rooms), 4, 4));
        roomsPanel.setOpaque(false);
        for (int room = 0; room < rooms; room++) {
            JLabel roomLabel = createRoomLabel();
            roomLabels[tower][floor][room] = roomLabel;
            roomsPanel.add(roomLabel);
        }
        floorPanel.add(roomsPanel, "grow");
        return floorPanel;
    }

    /**
     * Keeps every room on at most two rows, so wide floors never overflow the
     * tower card horizontally.
     */
    private static int columnsFor(int rooms) {
        if (rooms <= 5) {
            return Math.max(1, rooms);
        }
        return Math.min(6, (int) Math.ceil(rooms / 2.0));
    }

    private JLabel createRoomLabel() {
        JLabel roomLabel = new JLabel("", SwingConstants.CENTER);
        roomLabel.setOpaque(true);
        roomLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 14));
        roomLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        return roomLabel;
    }

    private static void applyRoomAppearance(JLabel roomLabel, int statusCode, String roomString, boolean isOvertime) {
        Color background;
        Color foreground = Color.WHITE;
        String state;

        RoomStatus status;
        try {
            status = RoomStatus.fromCode(statusCode);
        } catch (IllegalArgumentException e) {
            status = null;
        }

        if (status == null) {
            background = UNKNOWN_BACKGROUND;
            state = "Estado desconocido";
        } else {
            switch (status) {
                case FREE -> {
                    background = FREE_BACKGROUND;
                    state = "Disponible";
                }
                case CLEANING -> {
                    background = CLEANING_BACKGROUND;
                    state = "Limpieza";
                }
                case OCCUPIED -> {
                    if (isOvertime) {
                        background = OVERTIME_BACKGROUND;
                        foreground = Color.BLACK;
                        state = "Sobretiempo";
                    } else {
                        background = OCCUPIED_BACKGROUND;
                        state = "Ocupada";
                    }
                }
                default -> {
                    background = UNKNOWN_BACKGROUND;
                    state = "Estado desconocido";
                }
            }
        }

        roomLabel.setText(roomString);
        roomLabel.setBackground(background);
        roomLabel.setForeground(foreground);
        roomLabel.setToolTipText(state + " - " + roomString);
    }

    private void updateLegend(int free, int cleaning, int occupied, int overtime) {
        legendLabel.setText("<html>"
                + "<font color='#1E8449'>&#9679;</font> LIBRES " + free
                + "&nbsp;&nbsp;<font color='#2471A3'>&#9679;</font> LIMPIEZA " + cleaning
                + "&nbsp;&nbsp;<font color='#C0392B'>&#9679;</font> OCUPADAS " + occupied
                + "&nbsp;&nbsp;<font color='#B7950B'>&#9679;</font> SOBRETIEMPO " + overtime
                + "</html>");
    }

    // --- Getters ---

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }

    /** Registers a listener for the back button. */
    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
        titleLabel = new JLabel();
        timeLabel = new JLabel();
        dateLabel = new JLabel();
        scrollPane = new JScrollPane();
        footerPanel = new JPanel();
        backButton = new JButton();
        legendLabel = new JLabel();

        //======== this ========
        setLayout(new MigLayout(
            "fill,hidemode 3,insets 12 16,gap 10",
            // columns
            "[grow,fill]" +
            "[fill]" +
            "[fill]",
            // rows
            "[]" +
            "[grow,fill]" +
            "[]"));

        //---- titleLabel ----
        titleLabel.setText("RESUMEN HABITACIONES");
        titleLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        add(titleLabel, "cell 0 0");

        //---- timeLabel ----
        timeLabel.setText("TIME");
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
        add(timeLabel, "cell 1 0,growx");

        //---- dateLabel ----
        dateLabel.setText("DATE");
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        add(dateLabel, "cell 2 0,growx");

        //---- scrollPane ----
        add(scrollPane, "cell 0 1 3 1,grow");

        //======== footerPanel ========
        {
            footerPanel.setLayout(new MigLayout(
                "fill,hidemode 3,gap 10",
                // columns
                "[220!,fill]" +
                "[grow,fill]",
                // rows
                "[]"));

            //---- backButton ----
            backButton.setText("VOLVER");
            backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
            footerPanel.add(backButton, "cell 0 0,h 56!");

            //---- legendLabel ----
            legendLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            legendLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 12));
            footerPanel.add(legendLabel, "cell 1 0,growx");
        }
        add(footerPanel, "cell 0 2 3 1,growx");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel titleLabel;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JScrollPane scrollPane;
    private JPanel footerPanel;
    private JButton backButton;
    private JLabel legendLabel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
