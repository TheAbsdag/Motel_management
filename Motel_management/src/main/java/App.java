/**
 *
 * @author Santiago
 */

import controller.Controller;
import view.UserGUI;
import model.MotelManagement;

public class App {
    
    public static void main(String[] args) {
        MotelManagement motelManager = new MotelManagement();
        UserGUI userInterface = new UserGUI();
        Controller controller = new Controller(motelManager, userInterface);
        controller.start();
    } 
}
