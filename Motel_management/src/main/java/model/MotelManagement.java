package model;

/**
 *
 * @author Santiago
 */
public class MotelManagement {
    private FileManager files;
    private Room[][] rooms;
    private Printer printer;
    private Register register;
    private Turn turn;
    private int currentFloor;
    
    public MotelManagement(){
        files = new FileManager();
    }
}
