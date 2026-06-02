/*
 * Created by JFormDesigner on Wed May 20 07:24:49 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author SECC
 */
public class ExportConfigurationView extends JPanel {
    public ExportConfigurationView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	label1 = new JLabel();
	emailConfigButton = new JButton();
	whatsappConfigButton = new JButton();
	backButton = new JButton();

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
	    "[]" +
	    "[]"));

	//---- label1 ----
	label1.setText("CONFIGURACION EXPORTACION REPORTES");
	add(label1, "cell 0 0");

	//---- emailConfigButton ----
	emailConfigButton.setText("CORREO");
	add(emailConfigButton, "cell 0 2,growy");

	//---- whatsappConfigButton ----
	whatsappConfigButton.setText("WHATSAPP");
	add(whatsappConfigButton, "cell 1 2,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(backButton, "cell 0 5,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel label1;
    private JButton emailConfigButton;
    private JButton whatsappConfigButton;
    private JButton backButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /** Registers a listener for the email config button. */
    public void onEmailConfigButton(Runnable action) {
        emailConfigButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the WhatsApp config button (placeholder). */
    public void onWhatsappConfigButton(Runnable action) {
        whatsappConfigButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the back button. */
    public void onBackButton(Runnable action) {
        backButton.addActionListener(e -> action.run());
    }
}
