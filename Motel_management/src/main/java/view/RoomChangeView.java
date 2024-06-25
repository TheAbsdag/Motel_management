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
    
    private ArrayList <ArrayList<JButton>> roomButtonGrid;
    private CardLayout cardLayout;
    private int currentFloorIndex;
    
    public RoomChangeView() {
	initComponents();
        initCustomComponents();
    }
    
    private void initCustomComponents() {
        currentFloorIndex = 0;
        cardLayout = new CardLayout();
        roomButtonPanel.setLayout(cardLayout);
        roomButtonGrid = new ArrayList<>();
    }
    
    public void createButtonsForRoomChange(int[] roomsPerFloor){
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
            roomButtonPanel.add(floorButtonPanel, "Floor " + floor);
        }
        cardLayout.show(roomButtonPanel, "Floor 0");
    }
    
    public void switchFloor(int floorIndex) {
        if (floorIndex >= 0 && floorIndex < roomButtonGrid.size()) {
            currentFloorIndex = floorIndex;
            cardLayout.show(roomButtonPanel, "Floor " + currentFloorIndex);
        }
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	roomButtonPanel = new JPanel();
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
	add(roomButtonPanel, "cell 0 0 2 5,growy");

	//---- timeLabel ----
	timeLabel.setText("00:00 am");
	timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(timeLabel, "cell 2 0 2 1");

	//---- dateLabel ----
	dateLabel.setText("21 DE JULIO 2020");
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(dateLabel, "cell 2 1 2 1");

	//---- upButton ----
	upButton.setIcon(new ImageIcon(getClass().getResource("/up.png")));
	add(upButton, "cell 2 2,growy");

	//---- downButton ----
	downButton.setIcon(new ImageIcon(getClass().getResource("/down.png")));
	add(downButton, "cell 3 2,growy");

	//---- selectedInformativeLabel ----
	selectedInformativeLabel.setText("SELECCIONADA:");
	selectedInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(selectedInformativeLabel, "cell 2 3");

	//---- selectedLabel ----
	selectedLabel.setText("204");
	selectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
	selectedLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(selectedLabel, "cell 3 3");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(backButton, "cell 0 5,growy");

	//---- confirmButton ----
	confirmButton.setText("CONFIRMAR ");
	confirmButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(confirmButton, "cell 2 5 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel roomButtonPanel;
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
     * @return the roomButtonGrid
     */
    public ArrayList <ArrayList<JButton>> getRoomButtonGrid() {
        return roomButtonGrid;
    }

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
 
}
