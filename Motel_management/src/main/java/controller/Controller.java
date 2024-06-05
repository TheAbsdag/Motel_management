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
        motelManager.prepareProgramData();
        userInterface.setupFloors(motelManager.getRoomsArray());
        userInterface.setFloorView();
        motelManager.prepareTurnRegisterData();
        
        updateLoopStart();
    }

    private void updateLoopStart() {
        while(true){
            motelManager.timeInformationUpdate();
            String timeShown  = motelManager.getCurrentLocalizedTime();
            userInterface.updateTime(timeShown);
        }
    } 
}
