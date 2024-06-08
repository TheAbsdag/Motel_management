package view;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Santiago
 */
public class FloorView extends JPanel {

    //Related items for the floor selection
    private ArrayList<ArrayList<JButton>> roomButtonGrid;
    private JPanel containerPanel;
    private CardLayout cardLayout;

    //All items for the sidepanel
    private JPanel sidePanel;
    private JLabel floorTimeLabel;
    private JLabel floorDateLabel;
    private JButton floorUpButton;
    private JButton floorDownButton;
    private JButton managementOptionsButton;
    private JButton receptionSellButton;
    private int currentFloorIndex;

    public FloorView() {
        initComponents();
    }

    private void initComponents() {
        currentFloorIndex = 0;
        roomButtonGrid = new ArrayList<>();

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        floorTimeLabel = new JLabel();
        sidePanel = new JPanel();
        floorTimeLabel = new JLabel("Tiempo");
        floorTimeLabel.setFont(new Font("SEGOE UI BLACK", Font.BOLD, 32));
        floorTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        floorDateLabel = new JLabel("Fecha");
        floorDateLabel.setFont(new Font("SEGOE UI BLACK", Font.BOLD, 32));
        floorDateLabel.setHorizontalAlignment(SwingConstants.CENTER);

        floorUpButton = new JButton("Subir");
        floorDownButton = new JButton("Bajar");
        managementOptionsButton = new JButton("OPCIONES");
        receptionSellButton = new JButton("VENTA RECEPCION");

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        //Adding the room interface
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(containerPanel, gbc);

        //creating sidepanel.
        sidePanel.setLayout(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8;
        gbc.gridwidth = 2;
        sidePanel.add(floorTimeLabel, gbc);

        gbc.gridy = 1;
        sidePanel.add(floorDateLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 1;
        sidePanel.add(floorUpButton, gbc);
        gbc.gridx = 1;
        sidePanel.add(floorDownButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        sidePanel.add(new JLabel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        sidePanel.add(receptionSellButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        sidePanel.add(new JLabel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        sidePanel.add(managementOptionsButton, gbc);

        //Adding the sidepanel to the main panel
        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(sidePanel, gbc);

    }

    public void createButtonsForFloor(int[] roomsPerFloor) {
        for (int floor = 0; floor < roomsPerFloor.length; floor++) {
            JPanel floorButtonPanel = new JPanel();

            floorButtonPanel.setLayout(new GridLayout(5, 5));
            floorButtonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "PISO " + (floor + 1), TitledBorder.CENTER, TitledBorder.TOP, new Font("SEGOE UI BLACK", Font.BOLD, 32)));

            //Creating a temporal list for the buttons of the current floor
            ArrayList<JButton> floorButtons = new ArrayList<>();
            for (int room = 0; room < roomsPerFloor[floor]; room++) {
                JButton button = new JButton();
                button.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Set black border
                button.setBackground(Color.WHITE); // Set white background
                button.setForeground(Color.WHITE); // Set white font color
                button.setFocusPainted(false); // Remove focus border
                button.setFont(new Font("SEGOE UI BLACK", Font.BOLD, 24));
                floorButtons.add(button);
                floorButtonPanel.add(button);
            }

            roomButtonGrid.add(floorButtons);
            containerPanel.add(floorButtonPanel, "Floor " + floor);
        }
        cardLayout.show(containerPanel, "Floor 0");
    }

    public void switchFloor(int floorIndex) {
        if (floorIndex >= 0 && floorIndex < roomButtonGrid.size()) {
            currentFloorIndex = floorIndex;
            cardLayout.show(containerPanel, "Floor " + currentFloorIndex);
        }
    }

    /**
     * @return the roomButtonGrid
     */
    public ArrayList<ArrayList<JButton>> getRoomButtonGrid() {
        return roomButtonGrid;
    }

    /**
     * @return the floorButtonPanel
     */
    /**
     * @return the floorTimeLabel
     */
    public JLabel getFloorTimeLabel() {
        return floorTimeLabel;
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
     * @return the managementOptionsButton
     */
    public JButton getManagementOptionsButton() {
        return managementOptionsButton;
    }

    /**
     * @return the receptionSellButton
     */
    public JButton getReceptionSellButton() {
        return receptionSellButton;
    }

    /**
     * @return the currentFloorIndex
     */
    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    /**
     * @return the floorDateLabel
     */
    public JLabel getFloorDateLabel() {
        return floorDateLabel;
    }

}
