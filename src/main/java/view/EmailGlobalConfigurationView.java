/*
 * Created by JFormDesigner on Wed Jun 03 10:21:27 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.BorderFactory;
import net.miginfocom.swing.*;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @author SECC
 */
public class EmailGlobalConfigurationView extends JPanel {
    private JList<?> activeList;

    public EmailGlobalConfigurationView() {
	initComponents();
	trackActiveList();
	ensureModels();
	receiverList.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 120, 215), 3));
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

    private void trackActiveList() {
	javax.swing.event.ListSelectionListener updateRemove = e -> updateRemoveButton();
	FocusAdapter fl = new FocusAdapter() {
	    @Override
	    public void focusGained(FocusEvent e) {
		JList<?> prev = activeList;
		activeList = (JList<?>) e.getSource();
		if (prev != null) prev.setBorder(null);
		activeList.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 120, 215), 3));
		updateRemoveButton();
	    }
	};
	for (JList list : new JList[]{receiverList, carbonCopyList, bindCarbonCopyList}) {
	    list.addFocusListener(fl);
	    list.addListSelectionListener(updateRemove);
	}
	activeList = receiverList;
	removeReceiverButton.setEnabled(false);
    }

    private void updateRemoveButton() {
	boolean valid = activeList != null
		&& activeList.getSelectedIndex() >= 0
		&& activeList.getModel().getSize() > 0;
	removeReceiverButton.setEnabled(valid);
    }

    private void ensureModels() {
	if (!(receiverList.getModel() instanceof DefaultListModel)) {
	    receiverList.setModel(new DefaultListModel<String>());
	}
	if (!(carbonCopyList.getModel() instanceof DefaultListModel)) {
	    carbonCopyList.setModel(new DefaultListModel<String>());
	}
	if (!(bindCarbonCopyList.getModel() instanceof DefaultListModel)) {
	    bindCarbonCopyList.setModel(new DefaultListModel<String>());
	}
    }


    public void onBackButton(Runnable action) {
	backButton.addActionListener(e -> action.run());
    }

    public void onSaveButton(Runnable action) {
	saveButton.addActionListener(e -> action.run());
    }

    public void onAddReceiverButton(Runnable action) {
	addReceiverButton.addActionListener(e -> action.run());
    }

    public void onRemoveReceiverButton(Runnable action) {
	removeReceiverButton.addActionListener(e -> action.run());
    }

    public String getSenderName() {
	return senderNameTextField.getText().trim();
    }

    public void setSenderName(String name) {
	senderNameTextField.setText(name);
    }

    public JList<?> getActiveList() {
	return activeList;
    }

    public JList getReceiverList() {
	return receiverList;
    }

    public JList getCarbonCopyList() {
	return carbonCopyList;
    }

    public JList getBindCarbonCopyList() {
	return bindCarbonCopyList;
    }

    public void clearActiveSelection() {
	activeList.clearSelection();
	for (JList list : new JList[]{receiverList, carbonCopyList, bindCarbonCopyList}) {
	    list.setBorder(null);
	}
	receiverList.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 120, 215), 3));
	activeList = receiverList;
	updateRemoveButton();
    }
}
