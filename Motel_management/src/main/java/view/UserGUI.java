package view;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Santiago
 */
public class UserGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private TurnSelectView turnSelectView;
    private FloorView floorView;
    private RoomView roomView;
    private SellingView sellingView;
    private ManagementSelectView managementSelection;
    private InventoryManagementView inventoryView;
    private HistoryView historyView;
    private TurnManagerView turnManagerView;
    
    private Map<String, JLabel> timeLabels;
    private Map<String, JLabel> dateLabels;

    public UserGUI() {
        timeLabels = new HashMap<>();
        dateLabels = new HashMap<>();
        initComponents();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

         // Creation of the different views, and assignation to the cardLayout
        turnSelectView = new TurnSelectView();
        addView(turnSelectView, "turnSelectView");

        floorView = new FloorView();
        addView(floorView, "floorView");

        roomView = new RoomView();
        addView(roomView, "roomView");

        sellingView = new SellingView();
        addView(sellingView, "sellingView");

        managementSelection = new ManagementSelectView();
        addView(managementSelection, "managementSelectView");

        inventoryView = new InventoryManagementView();
        addView(inventoryView, "inventoryView");

        historyView = new HistoryView();
        addView(historyView, "historyView");

        turnManagerView = new TurnManagerView();
        addView(turnManagerView, "turnManagerView");

        //Default window configuration
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        //setResizable(false);
        add(mainPanel);
        setVisible(true);
    }
     private void addView(JPanel panel, String name) {
        panel.setName(name);
        mainPanel.add(panel, name);
        if (panel instanceof FloorView) {
            timeLabels.put(name, ((FloorView) panel).getFloorTimeLabel());
            dateLabels.put(name, ((FloorView) panel).getFloorDateLabel());
        } else if (panel instanceof TurnSelectView) {
            timeLabels.put(name, ((TurnSelectView) panel).getTimeLabel());
            dateLabels.put(name, ((TurnSelectView) panel).getDateLabel());
        } else if (panel instanceof RoomView) {
            timeLabels.put(name, ((RoomView) panel).getTimeLabel());
            dateLabels.put(name, ((RoomView) panel).getDateLabel());
        } 
        else if (panel instanceof SellingView) {
            timeLabels.put(name, ((SellingView) panel).getTimeLabel());
            dateLabels.put(name, ((SellingView) panel).getDateLabel());
        } 
        else if (panel instanceof ManagementSelectView) {
            timeLabels.put(name, ((ManagementSelectView) panel).getTimeLabel());
            dateLabels.put(name, ((ManagementSelectView) panel).getDateLabel());
        } 
         else if (panel instanceof TurnManagerView) {
            timeLabels.put(name, ((TurnManagerView) panel).getTimeLabel());
            dateLabels.put(name, ((TurnManagerView) panel).getDateLabel());
        }
        else if (panel instanceof InventoryManagementView) {
            timeLabels.put(name, ((InventoryManagementView) panel).getTimeLabel());
            dateLabels.put(name, ((InventoryManagementView) panel).getDateLabel());
        }
        else if (panel instanceof HistoryView) {
            timeLabels.put(name, ((HistoryView) panel).getTimeLabel());
            dateLabels.put(name, ((HistoryView) panel).getDateLabel());
        }
    }

    public void setupFloors(int[] arr) {
        floorView.createButtonsForFloor(arr);
    }
    private String getCurrentCard() {
        for (Component comp : mainPanel.getComponents()) {
            if (comp.isVisible()) {
                return ((JPanel) comp).getName();
            }
        }
        return null;
    }
    
    public boolean isFloorShown() {
        boolean output = false;
        if(getCurrentCard() == "floorView"){
            output = true;
        }
        return output;
    }
    
    public boolean isRoomShown() {
        boolean output = false;
        if(getCurrentCard() == "roomView"){
            output = true;
        }
        return output;
    }

    public void updateDateTime(String timeShown, String dateShown) { 
        String currentCard = getCurrentCard();
        if(currentCard != null){
            JLabel timeLabel = timeLabels.get(currentCard);
            JLabel dateLabel = dateLabels.get(currentCard);
            if( timeLabel != null && dateLabel != null){
                timeLabel.setText(timeShown);
                dateLabel.setText(dateShown);
            }
        }
    }
    
    public void setFloorView() {
        cardLayout.show(mainPanel, "floorView");
    }

    public void setTurnSelectView() {
        cardLayout.show(mainPanel, "turnSelectView");

    }

    public void setTurnManagerView() {
        cardLayout.show(mainPanel, "turnManagerView");
    }

    public void setRoomView() {
        cardLayout.show(mainPanel, "roomView");
    }

    public void setManagementSelection() {
        cardLayout.show(mainPanel, "managementSelectView");
    }
    
    public void setInventoryView(){
        cardLayout.show(mainPanel, "inventoryView");
    }
    
    public void setSellingView(){
        cardLayout.show(mainPanel, "sellingView");
    }
    
    public void setHistoryView(){
        cardLayout.show(mainPanel, "historyView");
    }

    /**
     * @return the turnSelectView
     */
    public TurnSelectView getTurnSelectView() {
        return turnSelectView;
    }

    /**
     * @return the floorView
     */
    public FloorView getFloorView() {
        return floorView;
    }

    /**
     * @return the roomView
     */
    public RoomView getRoomView() {
        return roomView;
    }

    /**
     * @return the sellingView
     */
    public SellingView getSellingView() {
        return sellingView;
    }

    /**
     * @return the managementSelection
     */
    public ManagementSelectView getManagementSelection() {
        return managementSelection;
    }

    /**
     * @return the inventoryView
     */
    public InventoryManagementView getInventoryView() {
        return inventoryView;
    }

    /**
     * @return the historyView
     */
    public HistoryView getHistoryView() {
        return historyView;
    }

    /**
     * @return the turnManagerView
     */
    public TurnManagerView getTurnManagerView() {
        return turnManagerView;
    }

}
