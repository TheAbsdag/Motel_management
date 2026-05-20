/*
 * Created by JFormDesigner on Thu May 14 15:25:49 GMT-05:00 2026
 */

package view;

import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author SECC
 */
public class DataSavingConfigurationView extends JPanel {
    public DataSavingConfigurationView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	label1 = new JLabel();
	backButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
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
	label1.setText("OPCIONES GUARDADO ");
	add(label1, "cell 0 0");

	//---- backButton ----
	backButton.setText("VOLVER");
	add(backButton, "cell 0 4,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel label1;
    private JButton backButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    public JButton getBackButton() {
        return backButton;
    }
}
