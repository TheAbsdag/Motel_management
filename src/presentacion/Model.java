package presentacion;
import presentacion.Model;
/**
 *
 * @author Santiago
 */
public class Model implements Runnable{
    
    private View window;


    public void start() {
        getWindow().setSize(400,400);
        getWindow().setVisible(true);
    } 

    public void run() {
        
    }

    public View getWindow() {
        if(window ==null){
            window = new View();
        }
        return window;
    }

}
