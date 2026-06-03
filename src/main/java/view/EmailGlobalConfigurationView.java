/*
 * Created by JFormDesigner on Wed Jun 03 10:21:27 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author SECC
 */
public class EmailGlobalConfigurationView extends JPanel {
    public EmailGlobalConfigurationView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	titleLabel = new JLabel();
	senderNameInformativeLabel = new JLabel();
	senderNameTextField = new JTextField();
	receiverInformativeLabel = new JLabel();
	receiverScrollPane = new JScrollPane();
	receiverList = new JList();
	addReceiverButton = new JButton();
	carbonCopyInformativeLabel = new JLabel();
	carbonCopyScrollPane = new JScrollPane();
	carbonCopyList = new JList();
	removeReceiverButton = new JButton();
	blindCarbonCopyInformativeLabel = new JLabel();
	blindCarbonCopyScrollPane = new JScrollPane();
	bindCarbonCopyList = new JList();
	backButton = new JButton();
	saveButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[70:n,fill]" +
	    "[grow,fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[80:80]"));

	//---- titleLabel ----
	titleLabel.setText("CONFIGURACION GLOBAL CORREO");
	titleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(titleLabel, "cell 0 0 2 1");

	//---- senderNameInformativeLabel ----
	senderNameInformativeLabel.setText("NOMBRE REMITENTE");
	senderNameInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(senderNameInformativeLabel, "cell 0 1");

	//---- senderNameTextField ----
	senderNameTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(senderNameTextField, "cell 1 1");

	//---- receiverInformativeLabel ----
	receiverInformativeLabel.setText("DESTINATARIOS");
	receiverInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(receiverInformativeLabel, "cell 0 2");

	//======== receiverScrollPane ========
	{
	    receiverScrollPane.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));

	    //---- receiverList ----
	    receiverList.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	    receiverScrollPane.setViewportView(receiverList);
	}
	add(receiverScrollPane, "cell 1 2");

	//---- addReceiverButton ----
	addReceiverButton.setText("A\u00d1ADIR");
	addReceiverButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(addReceiverButton, "cell 2 2,growy");

	//---- carbonCopyInformativeLabel ----
	carbonCopyInformativeLabel.setText("CC");
	carbonCopyInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(carbonCopyInformativeLabel, "cell 0 3");

	//======== carbonCopyScrollPane ========
	{
	    carbonCopyScrollPane.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));

	    //---- carbonCopyList ----
	    carbonCopyList.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	    carbonCopyScrollPane.setViewportView(carbonCopyList);
	}
	add(carbonCopyScrollPane, "cell 1 3");

	//---- removeReceiverButton ----
	removeReceiverButton.setText("QUITAR");
	removeReceiverButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(removeReceiverButton, "cell 2 3,growy");

	//---- blindCarbonCopyInformativeLabel ----
	blindCarbonCopyInformativeLabel.setText("BCC");
	blindCarbonCopyInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(blindCarbonCopyInformativeLabel, "cell 0 4");

	//======== blindCarbonCopyScrollPane ========
	{
	    blindCarbonCopyScrollPane.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));

	    //---- bindCarbonCopyList ----
	    bindCarbonCopyList.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	    blindCarbonCopyScrollPane.setViewportView(bindCarbonCopyList);
	}
	add(blindCarbonCopyScrollPane, "cell 1 4");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(backButton, "cell 0 5,growy");

	//---- saveButton ----
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(saveButton, "cell 2 5,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel titleLabel;
    private JLabel senderNameInformativeLabel;
    private JTextField senderNameTextField;
    private JLabel receiverInformativeLabel;
    private JScrollPane receiverScrollPane;
    private JList receiverList;
    private JButton addReceiverButton;
    private JLabel carbonCopyInformativeLabel;
    private JScrollPane carbonCopyScrollPane;
    private JList carbonCopyList;
    private JButton removeReceiverButton;
    private JLabel blindCarbonCopyInformativeLabel;
    private JScrollPane blindCarbonCopyScrollPane;
    private JList bindCarbonCopyList;
    private JButton backButton;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
