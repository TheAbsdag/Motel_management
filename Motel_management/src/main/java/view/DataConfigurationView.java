package view;

import javax.swing.*;
import net.miginfocom.swing.*;
/*
 * Created by JFormDesigner on Sun Apr 26 16:51:17 GMT-05:00 2026
 */



/**
 * @author SECC
 */
public class DataConfigurationView extends JPanel {
    public DataConfigurationView() {
	initComponents();
        FocusHighlighter.applyToAll(this);
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	titleLabel = new JLabel();
	button1 = new JButton();
	nameLabel = new JLabel();
	textField1 = new JTextField();
	addressLabel = new JLabel();
	textField4 = new JTextField();
	idLabel = new JLabel();
	textField2 = new JTextField();
	additionalInfoLabel = new JLabel();
	textField3 = new JTextField();

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
	    "[]" +
	    "[]"));

	//---- titleLabel ----
	titleLabel.setText("CONFIGURACION INICIAL");
	add(titleLabel, "cell 0 0 2 1,growy");

	//---- button1 ----
	button1.setText("Configurar habitaciones");
	add(button1, "cell 3 0,growy");

	//---- nameLabel ----
	nameLabel.setText("NOMBRE:");
	add(nameLabel, "cell 0 1");
	add(textField1, "cell 1 1");

	//---- addressLabel ----
	addressLabel.setText("DIRECCION");
	add(addressLabel, "cell 0 2");
	add(textField4, "cell 1 2");

	//---- idLabel ----
	idLabel.setText("NIT");
	add(idLabel, "cell 0 3");
	add(textField2, "cell 1 3");

	//---- additionalInfoLabel ----
	additionalInfoLabel.setText("DESCRIPCI\u00d3N ADICIONAL");
	add(additionalInfoLabel, "cell 0 4");
	add(textField3, "cell 1 4");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel titleLabel;
    private JButton button1;
    private JLabel nameLabel;
    private JTextField textField1;
    private JLabel addressLabel;
    private JTextField textField4;
    private JLabel idLabel;
    private JTextField textField2;
    private JLabel additionalInfoLabel;
    private JTextField textField3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
