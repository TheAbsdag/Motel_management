package view;

import javax.swing.JFrame;

/**
 *
 * @author Santiago
 */
public class UserGUI extends JFrame {
    
    private TurnSelectView turnSelectView;
    private FloorView floorView;
    private RoomView roomView;

    public UserGUI() {
        initComponents();
    }

    private void initComponents() {
        //Creation of the different views
        turnSelectView = new TurnSelectView();
        floorView = new FloorView();
        roomView = new RoomView();
        //Default window configuration
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        //setResizable(false);

        setVisible(true);
    }

    public void setupFloors() {
        
    }
}
