package view;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Santiago
 */
public class UserGUI extends JFrame {
    
    private TurnSelectView turnSelectView;
    private FloorView floorView;
    private RoomView roomView;
    CardLayout cardLayout;
    private JPanel mainPanel;

    public UserGUI() {
        initComponents();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        //Creation of the different views, and assignation to the cardLayout
        
        turnSelectView = new TurnSelectView();
        mainPanel.add(turnSelectView,"turnSelectView");
        floorView = new FloorView();
        mainPanel.add(floorView,"floorView");
        roomView = new RoomView();
        mainPanel.add(roomView,"roomView");
        //Default window configuration
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 900);
        //setResizable(false);
        setVisible(true);
        add(mainPanel);
    }

    public void setupFloors(int[] arr) {
        floorView.createButtonsForFloor(arr);
    }

    public void updateTime(String timeShown) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void setFloorView() {
        cardLayout.show(mainPanel, "floorView");
    }

}