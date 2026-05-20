/*
 * Created by JFormDesigner on Thu May 14 15:26:14 GMT-05:00 2026
 */

package view;

import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author SECC
 */
public class TimeConfigurationView extends JPanel {
    public TimeConfigurationView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	label1 = new JLabel();
	button1 = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- label1 ----
	label1.setText("CONFIGURACION TIEMPO ACTUAL");
	add(label1, "cell 0 0");

	//---- button1 ----
	button1.setText("VOLVER");
	add(button1, "cell 0 4,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel label1;
    private JButton button1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    public JButton getBackButton() {
        return button1;
    }
}
