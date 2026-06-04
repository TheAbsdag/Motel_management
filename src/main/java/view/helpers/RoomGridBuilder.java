package view.helpers;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Manages the room button grid data structure ({@code ArrayList<ArrayList<ArrayList<JButton>>>})
 * shared by {@code FloorView}, {@code RoomChangeView}, and {@code FloorConfigurationView}.
 */
public class RoomGridBuilder {

    private final ArrayList<ArrayList<ArrayList<JButton>>> grid;
    private final CardLayout cardLayout;
    private final JPanel containerPanel;

    public RoomGridBuilder(CardLayout cardLayout, JPanel containerPanel) {
        this.grid = new ArrayList<>();
        this.cardLayout = cardLayout;
        this.containerPanel = containerPanel;
    }

    public ArrayList<ArrayList<ArrayList<JButton>>> getGrid() {
        return grid;
    }

    public void clear() {
        grid.clear();
        containerPanel.removeAll();
    }

    public void createButtonsForTowers(int[][] roomsPerTower, Font buttonFont, int titleFontSize) {
        grid.clear();
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
                        new Font("SEGOE UI BLACK", Font.BOLD, titleFontSize)));

                ArrayList<JButton> floorButtons = new ArrayList<>();
                for (int room = 0; room < roomsPerFloor[floor]; room++) {
                    JButton button = new JButton(String.valueOf(room + 1));
                    button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    button.setBackground(Color.WHITE);
                    button.setForeground(Color.BLACK);
                    button.setFocusPainted(false);
                    button.setFont(buttonFont);
                    floorButtons.add(button);
                    floorButtonPanel.add(button);
                }

                towerFloors.add(floorButtons);
                containerPanel.add(floorButtonPanel, "Tower" + tower + "Floor" + floor);
            }

            grid.add(towerFloors);
        }
    }

    public void switchFloor(int towerIndex, int floorIndex) {
        if (towerIndex >= 0 && towerIndex < grid.size()) {
            ArrayList<ArrayList<JButton>> currentTower = grid.get(towerIndex);
            if (floorIndex >= 0 && floorIndex < currentTower.size()) {
                show(towerIndex, floorIndex);
            }
        }
    }

    public void switchTower(int towerIndex) {
        if (towerIndex >= 0 && towerIndex < grid.size()) {
            show(towerIndex, 0);
        }
    }

    public void show(int towerIndex, int floorIndex) {
        if (towerIndex >= 0 && towerIndex < grid.size()
                && floorIndex >= 0 && floorIndex < grid.get(towerIndex).size()) {
            cardLayout.show(containerPanel, "Tower" + towerIndex + "Floor" + floorIndex);
        }
    }

    public void setRoomAppearance(int tower, int floor, int room, String text, Color bg) {
        if (tower < grid.size() && floor < grid.get(tower).size() && room < grid.get(tower).get(floor).size()) {
            JButton btn = grid.get(tower).get(floor).get(room);
            btn.setText(text);
            btn.setBackground(bg);
        }
    }

    public void setRoomText(int tower, int floor, int room, String text) {
        if (tower < grid.size() && floor < grid.get(tower).size() && room < grid.get(tower).get(floor).size()) {
            grid.get(tower).get(floor).get(room).setText(text);
        }
    }

    public void onRoomClick(int tower, int floor, int room, Runnable action) {
        if (tower < grid.size() && floor < grid.get(tower).size() && room < grid.get(tower).get(floor).size()) {
            grid.get(tower).get(floor).get(room).addActionListener(e -> action.run());
        }
    }

    public int getRoomCount(int tower, int floor) {
        if (tower < grid.size() && floor < grid.get(tower).size()) {
            return grid.get(tower).get(floor).size();
        }
        return 0;
    }

    public int getTowerCount() {
        return grid.size();
    }

    public int getFloorCount(int tower) {
        if (tower < grid.size()) {
            return grid.get(tower).size();
        }
        return 0;
    }

    public boolean isEmpty() {
        return grid.isEmpty();
    }
}