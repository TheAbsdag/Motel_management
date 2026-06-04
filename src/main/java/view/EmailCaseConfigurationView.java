/*
 * Created by JFormDesigner on Wed Jun 03 09:56:19 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;
import view.interfaces.TimeLabelInterface;
import view.customListRenderes.CheckboxListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ButtonGroup;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author SECC
 */
public class EmailCaseConfigurationView extends JPanel implements TimeLabelInterface {
    private final int caseIndex;
    private DefaultListModel<CheckableItem> attachmentModel;

    public EmailCaseConfigurationView(int caseIndex) {
	this.caseIndex = caseIndex;
	initComponents();
	wireAttachmentCheckboxList();
	setCaseName(caseIndex);
	setupReceiverRadioGroup();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	caseConfigurationMainLabel = new JLabel();
	currentCaseLabel = new JLabel();
	caseEnabledCheckBox = new JCheckBox();
	receiverInformativeLabel = new JLabel();
	globalReceiverConfigurationRadioButton = new JRadioButton();
	specificReceiverRadioButton = new JRadioButton();
	receiverScrollPane = new JScrollPane();
	specificReceiversList = new JList();
	addAdditionalReceiverButton = new JButton();
	removeAdditionalReceiverButton = new JButton();
	subjectInformativeLabel = new JLabel();
	subjectTextField = new JTextField();
	bodyInformativeLabel = new JLabel();
	previewBodyButton = new JButton();
	markdownHelpButton = new JButton();
	availableVariablesInformativeLabel = new JLabel();
	bodyScrollPane = new JScrollPane();
	bodyTextArea = new JTextArea();
	variableListTextPane = new JScrollPane();
	availableVariablesList = new JList();
	attachmentInformativeLabel = new JLabel();
	attachmentListScrollPane = new JScrollPane();
	attachmentList = new JList();
	backButton = new JButton();
	saveButton = new JButton();

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
	    "[]" +
	    "[]" +
	    "[]" +
	    "[grow]" +
	    "[]" +
	    "[]" +
	    "[70]"));

	//---- caseConfigurationMainLabel ----
	caseConfigurationMainLabel.setText("CONFIGURACION CASO:");
	caseConfigurationMainLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(caseConfigurationMainLabel, "cell 0 0 3 1,alignx center,growx 0");

	//---- currentCaseLabel ----
	currentCaseLabel.setText("CASO");
	currentCaseLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(currentCaseLabel, "cell 0 0 3 1");

	//---- caseEnabledCheckBox ----
	caseEnabledCheckBox.setText("HABILITAR CASO");
	caseEnabledCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(caseEnabledCheckBox, "cell 0 1");

	//---- receiverInformativeLabel ----
	receiverInformativeLabel.setText("DESTINATARIO:");
	receiverInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(receiverInformativeLabel, "cell 0 2");

	//---- globalReceiverConfigurationRadioButton ----
	globalReceiverConfigurationRadioButton.setText("USAR CONFIGURACION GLOBAL");
	globalReceiverConfigurationRadioButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(globalReceiverConfigurationRadioButton, "cell 0 3");

	//---- specificReceiverRadioButton ----
	specificReceiverRadioButton.setText("A\u00d1ADIR ESPECIFICOS:");
	specificReceiverRadioButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(specificReceiverRadioButton, "cell 0 4");

	//======== receiverScrollPane ========
	{

	    //---- specificReceiversList ----
	    specificReceiversList.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	    receiverScrollPane.setViewportView(specificReceiversList);
	}
	add(receiverScrollPane, "cell 1 4");

	//---- addAdditionalReceiverButton ----
	addAdditionalReceiverButton.setText("A\u00d1ADIR");
	addAdditionalReceiverButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(addAdditionalReceiverButton, "cell 2 4,growy");

	//---- removeAdditionalReceiverButton ----
	removeAdditionalReceiverButton.setText("ELIMINAR");
	removeAdditionalReceiverButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(removeAdditionalReceiverButton, "cell 2 4,growy");

	//---- subjectInformativeLabel ----
	subjectInformativeLabel.setText("ASUNTO:");
	subjectInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(subjectInformativeLabel, "cell 0 5");

	//---- subjectTextField ----
	subjectTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(subjectTextField, "cell 0 6 2 1");

	//---- bodyInformativeLabel ----
	bodyInformativeLabel.setText("CUERPO (Markdown):");
	bodyInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(bodyInformativeLabel, "cell 0 7");

	//---- previewBodyButton ----
	previewBodyButton.setText("PREVISUALIZAR");
	previewBodyButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(previewBodyButton, "cell 0 7,growy");

	//---- markdownHelpButton ----
	markdownHelpButton.setText("Ayuda Markdown");
	markdownHelpButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(markdownHelpButton, "cell 1 7,growy");

	//---- availableVariablesInformativeLabel ----
	availableVariablesInformativeLabel.setText("VARIABLES DISPONIBLES");
	availableVariablesInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(availableVariablesInformativeLabel, "cell 2 7");

	//======== bodyScrollPane ========
	{
	    bodyScrollPane.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

	    //---- bodyTextArea ----
	    bodyTextArea.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	    bodyScrollPane.setViewportView(bodyTextArea);
	}
	add(bodyScrollPane, "cell 0 8 2 1,growy");

	//======== variableListTextPane ========
	{
	    variableListTextPane.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

	    //---- availableVariablesList ----
	    availableVariablesList.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	    variableListTextPane.setViewportView(availableVariablesList);
	}
	add(variableListTextPane, "cell 2 8,growy");

	//---- attachmentInformativeLabel ----
	attachmentInformativeLabel.setText("ADJUNTOS DISPONIBLES PARA EL CASO:");
	attachmentInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(attachmentInformativeLabel, "cell 0 9");

	//======== attachmentListScrollPane ========
	{
	    attachmentListScrollPane.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

	    //---- attachmentList ----
	    attachmentList.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	    attachmentListScrollPane.setViewportView(attachmentList);
	}
	add(attachmentListScrollPane, "cell 0 10");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(backButton, "cell 0 11,growy");

	//---- saveButton ----
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(saveButton, "cell 2 11,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel caseConfigurationMainLabel;
    private JLabel currentCaseLabel;
    private JCheckBox caseEnabledCheckBox;
    private JLabel receiverInformativeLabel;
    private JRadioButton globalReceiverConfigurationRadioButton;
    private JRadioButton specificReceiverRadioButton;
    private JScrollPane receiverScrollPane;
    private JList specificReceiversList;
    private JButton addAdditionalReceiverButton;
    private JButton removeAdditionalReceiverButton;
    private JLabel subjectInformativeLabel;
    private JTextField subjectTextField;
    private JLabel bodyInformativeLabel;
    private JButton previewBodyButton;
    private JButton markdownHelpButton;
    private JLabel availableVariablesInformativeLabel;
    private JScrollPane bodyScrollPane;
    private JTextArea bodyTextArea;
    private JScrollPane variableListTextPane;
    private JList availableVariablesList;
    private JLabel attachmentInformativeLabel;
    private JScrollPane attachmentListScrollPane;
    private JList attachmentList;
    private JButton backButton;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
    }

    public static class CheckableItem {
	private final String name;
	private boolean selected;

	public CheckableItem(String name, boolean selected) {
	    this.name = name;
	    this.selected = selected;
	}

	public String getName() { return name; }
	public boolean isSelected() { return selected; }
	public void setSelected(boolean selected) { this.selected = selected; }

	@Override
	public String toString() { return name; }
    }

    private void wireAttachmentCheckboxList() {
	attachmentModel = new DefaultListModel<>();
	if (caseIndex == 2) {
	    attachmentModel.addElement(new CheckableItem("Resumen PDF", false));
	    attachmentModel.addElement(new CheckableItem("Detalle PDF", false));
	    attachmentModel.addElement(new CheckableItem("Reporte XLSX", false));
	} else {
	    attachmentModel.addElement(new CheckableItem("Recibo PDF", false));
	}
	attachmentList.setModel(attachmentModel);
	attachmentList.setCellRenderer(new CheckboxListCellRenderer());
	attachmentList.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		int index = attachmentList.locationToIndex(e.getPoint());
		if (index >= 0) {
		    CheckableItem item = attachmentModel.getElementAt(index);
		    item.setSelected(!item.isSelected());
		    attachmentList.repaint();
		}
	    }
	});
    }

    private void setCaseName(int caseIndex) {
	switch (caseIndex) {
	    case 0 -> currentCaseLabel.setText("HABITACIONES");
	    case 1 -> currentCaseLabel.setText("VENTAS");
	    case 2 -> currentCaseLabel.setText("REPORTE TURNOS");
	}
    }

    private void setupReceiverRadioGroup() {
	ButtonGroup group = new ButtonGroup();
	group.add(globalReceiverConfigurationRadioButton);
	group.add(specificReceiverRadioButton);
	globalReceiverConfigurationRadioButton.setSelected(true);
	specificReceiversList.setEnabled(false);
	addAdditionalReceiverButton.setEnabled(false);
	removeAdditionalReceiverButton.setEnabled(false);
	globalReceiverConfigurationRadioButton.addActionListener(e -> {
	    specificReceiversList.setEnabled(false);
	    addAdditionalReceiverButton.setEnabled(false);
	    removeAdditionalReceiverButton.setEnabled(false);
	});
	specificReceiverRadioButton.addActionListener(e -> {
	    specificReceiversList.setEnabled(true);
	    addAdditionalReceiverButton.setEnabled(true);
	    removeAdditionalReceiverButton.setEnabled(true);
	});
    }

    public void onBackButton(Runnable action) {
	backButton.addActionListener(e -> action.run());
    }

    public void onSaveButton(Runnable action) {
	saveButton.addActionListener(e -> action.run());
    }

    public void onMarkdownHelp(Runnable action) {
	markdownHelpButton.addActionListener(e -> action.run());
    }

    public boolean isCaseEnabled() {
	return caseEnabledCheckBox.isSelected();
    }

    public boolean isUsingGlobalReceivers() {
	return globalReceiverConfigurationRadioButton.isSelected();
    }

    public String getSubject() {
	return subjectTextField.getText().trim();
    }

    public String getBody() {
	return bodyTextArea.getText();
    }

    public JList getAvailableVariablesList() {
	return availableVariablesList;
    }

    public DefaultListModel<CheckableItem> getAttachmentModel() {
	return attachmentModel;
    }

    public DefaultListModel<String> getSpecificReceiversModel() {
	if (!(specificReceiversList.getModel() instanceof DefaultListModel)) {
	    specificReceiversList.setModel(new DefaultListModel<String>());
	}
	return (DefaultListModel<String>) specificReceiversList.getModel();
    }

    public void setCaseEnabled(boolean enabled) {
	caseEnabledCheckBox.setSelected(enabled);
    }

    public void setUseGlobalReceivers(boolean useGlobal) {
	globalReceiverConfigurationRadioButton.setSelected(useGlobal);
	specificReceiverRadioButton.setSelected(!useGlobal);
	if (useGlobal) {
	    specificReceiversList.setEnabled(false);
	    addAdditionalReceiverButton.setEnabled(false);
	    removeAdditionalReceiverButton.setEnabled(false);
	} else {
	    specificReceiversList.setEnabled(true);
	    addAdditionalReceiverButton.setEnabled(true);
	    removeAdditionalReceiverButton.setEnabled(true);
	}
    }

    public void setSubject(String subject) {
	subjectTextField.setText(subject);
    }

    public void setBody(String body) {
	bodyTextArea.setText(body);
    }
}
