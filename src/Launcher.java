
import presentacion.Model;

/**
 *@Author Absdag
 */
public class Launcher {
    
    private Model application;
    
    public Launcher(){
        application = new Model();
        application.start();
    }
    
    public static void main(String[] args) {
        new Launcher();
    }
}
