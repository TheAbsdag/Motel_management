package controller;

import model.MotelManagement;
import view.UserGUI;

/**
 *
 * @author Santiago
 */
public class Controller {
    
    private UserGUI userInterface;
    private MotelManagement motelManager;

    public Controller(MotelManagement motelManager, UserGUI userInterface) {
        this.motelManager = motelManager;
        this.userInterface = userInterface;
    }

    public void start() {
        userInterface.setVisible(true);
        userInterface.setupFloors();
    }
    
}
