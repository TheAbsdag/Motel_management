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
    private ArrayList<ArrayList<JButton>> roomButtonGrid;
    private int currentFloorIndex;
    private CardLayout cardLayout;
    
    public FloorView() {
        initCustomComponents();
	initComponents();
    }
    
    private void initCustomComponents() {
        currentFloorIndex = 0;
        roomButtonGrid = new ArrayList<>();
        cardLayout = new CardLayout();
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
                button.setFont(new Font("SEGOE UI BLACK", Font.BOLD, 18));
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
    
    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	containerPanel = new JPanel();
	turnNumberLabel = new JLabel();
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

	//---- timeLabel ----
	timeLabel.setText("time");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(timeLabel, "cell 1 1 2 1");

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(dateLabel, "cell 1 2 2 1");

	//---- floorUpButton ----
	floorUpButton.setIcon(new ImageIcon(getClass().getResource("/up.png")));
	add(floorUpButton, "cell 1 3,growy");

	//---- floorDownButton ----
	floorDownButton.setIcon(new ImageIcon(getClass().getResource("/down.png")));
	add(floorDownButton, "cell 2 3,grow");

	//---- receptionSellButton ----
	receptionSellButton.setText("VENTA RECEPCION");
	receptionSellButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(receptionSellButton, "cell 1 5 2 1,growy");

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
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JButton floorUpButton;
    private JButton floorDownButton;
    private JButton receptionSellButton;
    private JButton managementOptionsButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the roomButtonGrid
     */
    public ArrayList<ArrayList<JButton>> getRoomButtonGrid() {
        return roomButtonGrid;
    }

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

    
}
