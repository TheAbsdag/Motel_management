
package view;

import javax.swing.JFrame;

/**
 *
 * @author Santiago
 */
public class UserGUI  extends JFrame{
     public UserGUI(){
    initComponents();
}
    
     private void initComponents() {
        // Configurar la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        //setResizable(false);
        
        //Inicializando las diferentes opciones
        setVisible(true);
    }
}
