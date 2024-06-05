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
    
    //All items for the sidepanel
    private JPanel sidePanel;
    private JLabel floorTimeLabel;
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
        sidePanel = new JPanel();
        floorTimeLabel = new JLabel("Tiempo");
        floorUpButton = new JButton("Subir");
        floorDownButton = new JButton ("Bajar");
        managementOptionsButton = new JButton("OPCIONES");
        receptionSellButton = new JButton("VENTA RECEPCION");
        
        this.setLayout (new GridBagLayout());
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
        
        gbc.gridx =0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8;
        gbc.gridwidth = 2;
        sidePanel.add(floorTimeLabel, gbc);
        
        gbc.gridx =0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
         gbc.gridwidth = 1;
        sidePanel.add(floorUpButton, gbc);
        gbc.gridx =1;
        sidePanel.add(floorDownButton, gbc);
        
        
        gbc.gridx =0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        sidePanel.add(new JLabel(), gbc);
        
        gbc.gridx =0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        sidePanel.add(receptionSellButton, gbc);
        
        gbc.gridx =0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        sidePanel.add(new JLabel(), gbc);
        
        gbc.gridx =0;
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
    
    public void createButtonsForFloor(int[] roomsPerFloor){
        for(int floor = 0; floor< roomsPerFloor.length;floor++){
            JPanel floorButtonPanel = new JPanel();
            
            floorButtonPanel.setLayout( new GridLayout (5,5));
            floorButtonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "PISO "+(floor+1), TitledBorder.CENTER, TitledBorder.TOP, new Font("SEGOE UI BLACK", Font.BOLD, 30) ));
            
            //Creating a temporal list for the buttons of the current floor
            
            ArrayList<JButton> floorButtons = new ArrayList<>();
            for(int room = 0; room<roomsPerFloor[floor];room++){
                JButton button = new JButton ((floor + 1) + String.format("%02d", (room + 1)));
                floorButtons.add(button);
                floorButtonPanel.add(button);
            }
            
            roomButtonGrid.add(floorButtons);
            containerPanel.add(floorButtonPanel, "Floor "+floor);
        }
        cardLayout.show(containerPanel, "Floor 0");
    }
    
    private void switchFloor(int floorIndex){
        if(floorIndex >=0 && floorIndex < floorButtonPanel.size()){
            currentFloorIndex = floorIndex;
            cardLayout.show(containerPanel, "Floor "+currentFloorIndex);
        }
    }
}
