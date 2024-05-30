package view;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Santiago
 */
public class FloorView extends JPanel{
    //Related items for the floor selection
    private ArrayList<ArrayList<JButton>> roomButtonGrid;
    private ArrayList<JPanel> floorButtonPanel;
    private JPanel containerPanel;
    private CardLayout cardLayout;
    
    private JLabel floorTimeLabel;
    private JLabel informativeFloorLabel;
    private JButton floorUpButton;
    private JButton floorDownButton;
    private JButton managementOptionsButton;
    private JButton receptionSellButton;
    private int currentFloorIndex;
    
    public FloorView(){
        initComponents();
    }

    private void initComponents() {
        roomButtonGrid = new ArrayList<>();
        floorButtonPanel = new ArrayList<>();
        
        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        floorTimeLabel = new JLabel();
        informativeFloorLabel = new JLabel("Floor 0");
        
    }
    
    public void createButtonsForFloor(int[] roomsPerFloor){
        for(int floor = 0; floor< roomsPerFloor.length;floor++){
            JPanel floorButtonPanel = new JPanel();
            
            floorButtonPanel.setLayout( new GridLayout (1,roomsPerFloor[floor]));
            floorButtonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "PISO "+(floor+1), TitledBorder.CENTER, TitledBorder.TOP, new Font("SEGOE UI BLACK", Font.BOLD, 30) ));
            
            //Creating a temporal list for the buttons of the current floor
            
            ArrayList<JButton> floorButtons = new ArrayList<>();
            for(int room = 0; room<roomsPerFloor[floor];room++){
                JButton button = new JButton ((floor+1)+""+(room+1));
                floorButtons.add(button);
                floorButtonPanel.add(button);
            }
            
            roomButtonGrid.add(floorButtons);
            containerPanel.add(floorButtonPanel, "Floor "+floor);
        }
    }
    
    private void switchFloor(int floorIndex){
        if(floorIndex >=0 && floorIndex < floorButtonPanel.size()){
            currentFloorIndex = floorIndex;
            cardLayout.show(containerPanel, "Floor "+currentFloorIndex);
            informativeFloorLabel.setText("Floor "+(currentFloorIndex+1));
        }
    }
}
