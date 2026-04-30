package view;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import model.RoomStatus;

/**
 * Visual dashboard showing all rooms across all towers and floors,
 * color-coded by current status.
 * @author Santiago
 */
public class RoomSummaryView extends JPanel {

    private Map<String, JLabel> roomLabels;

    public RoomSummaryView() {
        roomLabels = new HashMap<>();
        initComponents();
    }

    /**
     * Rebuilds the room summary display with current room data.
     *
     * @param roomStatusData 3D array [tower][floor][room] of RoomStatus codes
     * @param roomStringsData 3D array [tower][floor][room] of room display strings
     * @param overtimeData    3D array [tower][floor][room] true if room is in overtime
     */
    public void updateRoomSummary(int[][][] roomStatusData, String[][][] roomStringsData, boolean[][][] overtimeData) {
        containerPanel.removeAll();
        roomLabels.clear();

        StringBuilder colSpec = new StringBuilder("[grow, fill]");
        for (int t = 1; t < roomStatusData.length; t++) {
            colSpec.append("[grow, fill]");
        }

        JPanel mainTowerPanel = new JPanel(new MigLayout(
            "fillx, insets 20, gap 30",
            colSpec.toString(),
            "[grow, fill]"));

        for (int tower = 0; tower < roomStatusData.length; tower++) {
            JPanel towerPanel = createTowerPanel(tower, roomStatusData[tower], roomStringsData[tower], overtimeData[tower]);
            mainTowerPanel.add(towerPanel, "grow");
        }

        JScrollPane scrollPane = new JScrollPane(mainTowerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        containerPanel.add(scrollPane, "grow");
        containerPanel.revalidate();
        containerPanel.repaint();
    }

    private JPanel createTowerPanel(int towerIndex, int[][] towerStatusData, String[][] towerStringsData, boolean[][] overtimeData) {
        JPanel towerPanel = new JPanel(new MigLayout("fill, insets 15", "[grow, fill]", "[]10[]"));
        towerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 150), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        towerPanel.setBackground(new Color(240, 240, 245));

        JLabel towerLabel = new JLabel("TORRE " + (towerIndex + 1));
        towerLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 18));
        towerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        towerLabel.setForeground(new Color(60, 60, 100));
        towerLabel.setOpaque(true);
        towerLabel.setBackground(new Color(200, 200, 220));
        towerLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        towerPanel.add(towerLabel, "growx, wrap");

        JPanel floorsPanel = new JPanel(new MigLayout("fill, insets 10, gapy 15", "[grow, fill]", "[]0[]"));

        for (int floor = 0; floor < towerStatusData.length; floor++) {
            JPanel floorPanel = createFloorPanel(towerIndex, floor, towerStatusData[floor], towerStringsData[floor], overtimeData[floor]);
            floorsPanel.add(floorPanel, "growx, wrap");
        }

        towerPanel.add(floorsPanel, "grow");
        return towerPanel;
    }

    private JPanel createFloorPanel(int tower, int floor, int[] floorStatusData, String[] floorStringsData, boolean[] overtimeData) {
        JPanel floorPanel = new JPanel(new MigLayout("fill, insets 8", "[grow, fill]", "[]5[]"));

        JLabel floorLabel = new JLabel("Piso " + (floor + 1));
        floorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        floorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        floorLabel.setForeground(new Color(80, 80, 120));
        floorPanel.add(floorLabel, "growx, wrap");

        int columns = Math.min(5, floorStatusData.length);
        StringBuilder colConstraint = new StringBuilder();
        for (int c = 0; c < columns; c++) {
            colConstraint.append("[50!]");
        }
        JPanel roomsPanel = new JPanel(new MigLayout("fill, insets 5, gap 3", colConstraint.toString(), "[]0[]"));

        for (int room = 0; room < floorStatusData.length; room++) {
            JLabel roomLabel = createRoomLabel(floorStatusData[room], floorStringsData[room], overtimeData[room]);
            String roomKey = tower + "-" + floor + "-" + room;
            roomLabels.put(roomKey, roomLabel);
            roomsPanel.add(roomLabel, "w 50!, h 40!");
        }

        floorPanel.add(roomsPanel, "growx");
        return floorPanel;
    }

    private JLabel createRoomLabel(int statusCode, String roomString, boolean isOvertime) {
        JLabel roomLabel = new JLabel(roomString, SwingConstants.CENTER);
        roomLabel.setOpaque(true);
        roomLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        roomLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 12));

        RoomStatus status = RoomStatus.fromCode(statusCode);
        switch (status) {
            case FREE:
                roomLabel.setBackground(new Color(39, 174, 96));
                roomLabel.setForeground(Color.WHITE);
                roomLabel.setToolTipText("Disponible - " + roomString);
                break;
            case CLEANING:
                roomLabel.setBackground(new Color(84, 153, 199));
                roomLabel.setForeground(Color.WHITE);
                roomLabel.setToolTipText("Limpieza - " + roomString);
                break;
            case OCCUPIED:
                if (isOvertime) {
                    roomLabel.setBackground(new Color(241, 196, 15));
                    roomLabel.setForeground(Color.BLACK);
                    roomLabel.setToolTipText("Sobretiempo - " + roomString);
                } else {
                    roomLabel.setBackground(new Color(231, 76, 60));
                    roomLabel.setForeground(Color.WHITE);
                    roomLabel.setToolTipText("Ocupada - " + roomString);
                }
                break;
            default:
                roomLabel.setBackground(Color.GRAY);
                roomLabel.setForeground(Color.WHITE);
                roomLabel.setToolTipText("Estado desconocido - " + roomString);
        }

        return roomLabel;
    }

    // --- Getters ---

    public JLabel getTimeLabel() { return timeLabel; }
    public JLabel getDateLabel() { return dateLabel; }
    public JButton getBackButton() { return backButton; }
    public JPanel getContainerPanel() { return containerPanel; }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
        containerPanel = new JPanel();
        timeLabel = new JLabel();
        dateLabel = new JLabel();
        backButton = new JButton();

        //======== this ========
        setLayout(new MigLayout(
            "fill,hidemode 3",
            // columns
            "[grow,fill]" +
            "[grow,fill]" +
            "[grow,fill]" +
            "[grow,fill]",
            // rows
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]"));

        //======== containerPanel ========
        containerPanel.setLayout(new MigLayout("fill,hidemode 3", "[fill]", "[]"));
        add(containerPanel, "cell 0 0 3 7,growy");

        //---- timeLabel ----
        timeLabel.setText("TIME");
        timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        add(timeLabel, "cell 3 0");

        //---- dateLabel ----
        dateLabel.setText("DATE");
        dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        add(dateLabel, "cell 3 1");

        //---- backButton ----
        backButton.setText("VOLVER");
        backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(backButton, "cell 0 7,growy");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel containerPanel;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JButton backButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
