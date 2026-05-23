/*
 * Created by JFormDesigner on Thu May 14 15:17:47 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.miginfocom.swing.*;

/**
 * @author SECC
 */
public class MotelDataConfigurationView extends JPanel {

    private boolean hasUnsavedChanges;

    public MotelDataConfigurationView() {
        hasUnsavedChanges = false;
        initComponents();
        wireDocumentListeners();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	dataConfigurationTitleLabel = new JLabel();
	nameLabel = new JLabel();
	nameTextField = new JTextField();
	idLabel = new JLabel();
	idTextField = new JTextField();
	addressLabel = new JLabel();
	addressTextField = new JTextField();
	consecutiveTransactionInformativeLabel = new JLabel();
	conescutiveTransactionLabel = new JLabel();
	backButton = new JButton();
	saveButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[grow,fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- dataConfigurationTitleLabel ----
	dataConfigurationTitleLabel.setText("CONFIGURACION DATOS MOTEL");
	dataConfigurationTitleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(dataConfigurationTitleLabel, "cell 0 0 2 1");

	//---- nameLabel ----
	nameLabel.setText("NOMBRE:");
	nameLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(nameLabel, "cell 0 1");

	//---- nameTextField ----
	nameTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(nameTextField, "cell 1 1,growy");

	//---- idLabel ----
	idLabel.setText("NIT:");
	idLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(idLabel, "cell 0 2");

	//---- idTextField ----
	idTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(idTextField, "cell 1 2,growy");

	//---- addressLabel ----
	addressLabel.setText("DIRECCION");
	addressLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(addressLabel, "cell 0 3");

	//---- addressTextField ----
	addressTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(addressTextField, "cell 1 3,growy");

	//---- consecutiveTransactionInformativeLabel ----
	consecutiveTransactionInformativeLabel.setText("CONSECUTIVO ACTUAL:");
	consecutiveTransactionInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(consecutiveTransactionInformativeLabel, "cell 1 4");

	//---- conescutiveTransactionLabel ----
	conescutiveTransactionLabel.setText("CONS");
	conescutiveTransactionLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(conescutiveTransactionLabel, "cell 1 4");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(backButton, "cell 0 5,growy");

	//---- saveButton ----
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(saveButton, "cell 2 5,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel dataConfigurationTitleLabel;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JLabel idLabel;
    private JTextField idTextField;
    private JLabel addressLabel;
    private JTextField addressTextField;
    private JLabel consecutiveTransactionInformativeLabel;
    private JLabel conescutiveTransactionLabel;
    private JButton backButton;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    // ========== Encapsulated text access ==========

    /** Returns the current name text. */
    public String getNameText() { return nameTextField.getText().trim(); }
    /** Returns the current ID (NIT) text. */
    public String getIdText() { return idTextField.getText().trim(); }
    /** Returns the current address text. */
    public String getAddressText() { return addressTextField.getText().trim(); }
    /** Sets the name text field. */
    public void setNameText(String text) { nameTextField.setText(text); }
    /** Sets the ID (NIT) text field. */
    public void setIdText(String text) { idTextField.setText(text); }
    /** Sets the address text field. */
    public void setAddressText(String text) { addressTextField.setText(text); }
    /** Sets the consecutive transaction label. */
    public void setConsecutiveTransaction(String text) { conescutiveTransactionLabel.setText(text); }
    /** Registers a listener for the back button. */
    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }
    /** Registers a listener for the save button. */
    public void onSaveButton(Runnable action) { saveButton.addActionListener(e -> action.run()); }

    // ========== Dirty Tracking ==========

    private void wireDocumentListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { markDirty(); }
            @Override public void removeUpdate(DocumentEvent e) { markDirty(); }
            @Override public void changedUpdate(DocumentEvent e) { markDirty(); }
        };
        nameTextField.getDocument().addDocumentListener(listener);
        idTextField.getDocument().addDocumentListener(listener);
        addressTextField.getDocument().addDocumentListener(listener);
    }

    private void markDirty() {
        hasUnsavedChanges = true;
    }

    public void clearDirty() {
        hasUnsavedChanges = false;
    }

    public boolean isDirty() {
        return hasUnsavedChanges;
    }
}
