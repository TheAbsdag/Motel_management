/*
 * Created by JFormDesigner on Wed Jun 03 09:56:19 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import net.miginfocom.swing.*;
import view.customListRenderes.CheckboxListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ButtonGroup;
import javax.swing.Timer;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author SECC
 */
public class EmailCaseConfigurationView extends JPanel {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{[^}]+\\}");
    private static final Pattern NUMBERED_VARIABLE_PATTERN = Pattern.compile("\\{(\\d+)\\}");

    private final int caseIndex;
    private DefaultListModel<CheckableItem> attachmentModel;

    private JTextComponent lastFocusedText;
    private javax.swing.Timer highlightTimer;

    public EmailCaseConfigurationView(int caseIndex) {
	this.caseIndex = caseIndex;
	initComponents();
	wireAttachmentCheckboxList();
	setCaseName(caseIndex);
	setupReceiverRadioGroup();
	trackTextFocus();
	setupVariableInsertion();
	setupVariablesTable();
	wireDocumentListener();
	wireHighlightTimer();
	addVariableButton.addActionListener(e -> insertNextVariable());
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
	subjectScrollPane = new JScrollPane();
	subjectTextField = new JTextPane();
	bodyInformativeLabel = new JLabel();
	previewBodyButton = new JButton();
	markdownHelpButton = new JButton();
	availableVariablesInformativeLabel = new JLabel();
	addVariableButton = new JButton();
	bodyScrollPane = new JScrollPane();
	bodyTextArea = new JTextPane();
	variablesScrollPane = new JScrollPane();
	variablesTable = new JTable();
	attachmentInformativeLabel = new JLabel();
	attachmentListScrollPane = new JScrollPane();
	attachmentList = new JList();
	variableListTextPane = new JScrollPane();
	availableVariablesList = new JList();
	backButton = new JButton();
	saveButton = new JButton();

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
	    "[]" +
	    "[]" +
	    "[]" +
	    "[100:n,grow]" +
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

	//======== subjectScrollPane ========
	{

	    //---- subjectTextField ----
	    subjectTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	    subjectScrollPane.setViewportView(subjectTextField);
	}
	add(subjectScrollPane, "cell 0 6 2 1,growy");

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

	//---- addVariableButton ----
	addVariableButton.setText("A\u00d1ADIR VARIABLE");
	addVariableButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(addVariableButton, "cell 3 7");

	//======== bodyScrollPane ========
	{
	    bodyScrollPane.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

	    //---- bodyTextArea ----
	    bodyTextArea.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	    bodyScrollPane.setViewportView(bodyTextArea);
	}
	add(bodyScrollPane, "cell 0 8 2 1,growy");

	//======== variablesScrollPane ========
	{
	    variablesScrollPane.setViewportView(variablesTable);
	}
	add(variablesScrollPane, "cell 2 8 2 1");

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

	//======== variableListTextPane ========
	{
	    variableListTextPane.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

	    //---- availableVariablesList ----
	    availableVariablesList.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	    variableListTextPane.setViewportView(availableVariablesList);
	}
	add(variableListTextPane, "cell 3 10,growy");

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
    private JScrollPane subjectScrollPane;
    private JTextPane subjectTextField;
    private JLabel bodyInformativeLabel;
    private JButton previewBodyButton;
    private JButton markdownHelpButton;
    private JLabel availableVariablesInformativeLabel;
    private JButton addVariableButton;
    private JScrollPane bodyScrollPane;
    private JTextPane bodyTextArea;
    private JScrollPane variablesScrollPane;
    private JTable variablesTable;
    private JLabel attachmentInformativeLabel;
    private JScrollPane attachmentListScrollPane;
    private JList attachmentList;
    private JScrollPane variableListTextPane;
    private JList availableVariablesList;
    private JButton backButton;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    private DefaultComboBoxModel<VariableOption> variableComboModel;
    private VariablesTableModel variablesTableModel;
    private boolean suppressTableRebuild = false;

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

    public record VariableOption(String label, String placeholder) {
	@Override public String toString() { return label; }
    }

    private static class VariablesTableModel extends AbstractTableModel {
	private final List<VariableEntry> rows = new ArrayList<>();
	private final String[] columnNames = {"Variable", "Vinculado a"};

	record VariableEntry(String variable, VariableOption linkedOption) {}

	void setRows(List<VariableEntry> newRows) {
	    rows.clear();
	    rows.addAll(newRows);
	    fireTableDataChanged();
	}

	List<VariableEntry> getRows() { return new ArrayList<>(rows); }

	@Override public int getRowCount() { return rows.size(); }
	@Override public int getColumnCount() { return 2; }
	@Override public String getColumnName(int col) { return columnNames[col]; }

	@Override
	public Object getValueAt(int row, int col) {
	    VariableEntry entry = rows.get(row);
	    return switch (col) {
		case 0 -> entry.variable();
		case 1 -> entry.linkedOption();
		default -> null;
	    };
	}

	@Override
	public boolean isCellEditable(int row, int col) { return true; }

	@Override
	public void setValueAt(Object value, int row, int col) {
	    VariableEntry entry = rows.get(row);
	    switch (col) {
		case 0 -> {
		    String newVar = (String) value;
		    rows.set(row, new VariableEntry(newVar, entry.linkedOption()));
		}
		case 1 -> {
		    VariableOption opt = (VariableOption) value;
		    rows.set(row, new VariableEntry(entry.variable(), opt));
		}
	    }
	    fireTableCellUpdated(row, col);
	}
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

    public void onPreviewBodyButton(Runnable action) {
	previewBodyButton.addActionListener(e -> action.run());
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

    public void setAvailableVariables(String[] variables) {
	availableVariablesList.setListData(variables);
    }

    public DefaultListModel<CheckableItem> getAttachmentModel() {
	return attachmentModel;
    }

    public void refreshAttachmentList() {
	attachmentList.repaint();
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
	suppressTableRebuild = true;
	subjectTextField.setText(subject);
	suppressTableRebuild = false;
    }

    public void setBody(String body) {
	suppressTableRebuild = true;
	bodyTextArea.setText(body);
	suppressTableRebuild = false;
    }

    private void trackTextFocus() {
	FocusAdapter fl = new FocusAdapter() {
	    @Override
	    public void focusGained(FocusEvent e) {
		lastFocusedText = (JTextComponent) e.getSource();
	    }
	};
	subjectTextField.addFocusListener(fl);
	bodyTextArea.addFocusListener(fl);
    }

    private void setupVariableInsertion() {
	availableVariablesList.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
		    int idx = availableVariablesList.locationToIndex(e.getPoint());
		    if (idx >= 0) {
			String variable = availableVariablesList.getModel().getElementAt(idx).toString();
			JTextComponent target = lastFocusedText != null ? lastFocusedText : bodyTextArea;
			target.replaceSelection(variable);
		    }
		}
	    }
	});
    }

    private void setupVariablesTable() {
	variablesTableModel = new VariablesTableModel();
	variableComboModel = new DefaultComboBoxModel<>();
	JComboBox<VariableOption> comboBox = new JComboBox<>(variableComboModel);
	comboBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));

	variablesTable.setModel(variablesTableModel);
	variablesTable.setDefaultEditor(VariableOption.class, new DefaultCellEditor(comboBox));
	variablesTable.setDefaultRenderer(VariableOption.class, new DefaultTableCellRenderer() {
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value,
		    boolean isSelected, boolean hasFocus, int row, int col) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		if (value instanceof VariableOption opt) {
		    String text = opt.label();
		    setText(text);
		    setToolTipText(text);
		}
		return this;
	    }
	});
	variablesTable.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
	variablesTable.setRowHeight(28);
    }

    public void rebuildVariablesTable() {
	if (suppressTableRebuild) return;
	String subject = subjectTextField.getText();
	String body = bodyTextArea.getText();

	Map<String, String> labelByPlaceholder = new HashMap<>();
	for (int i = 0; i < variableComboModel.getSize(); i++) {
	    VariableOption opt = variableComboModel.getElementAt(i);
	    labelByPlaceholder.put(opt.placeholder(), opt.label());
	}

	Pattern pattern = VARIABLE_PATTERN;
	Set<String> seen = new LinkedHashSet<>();

	Matcher m = pattern.matcher(subject);
	while (m.find()) seen.add(m.group());

	m = pattern.matcher(body);
	while (m.find()) seen.add(m.group());

	List<VariablesTableModel.VariableEntry> entries = new ArrayList<>();
	for (String token : seen) {
	    String mappedLabel = labelByPlaceholder.get(token);
	    VariableOption linked = mappedLabel != null
		    ? new VariableOption(mappedLabel, token) : null;
	    entries.add(new VariablesTableModel.VariableEntry(token, linked));
	}
	variablesTableModel.setRows(entries);
    }

    private void highlightVariables(JTextPane pane) {
	StyledDocument doc = pane.getStyledDocument();
	String text = pane.getText();
	if (text == null || text.isEmpty()) return;

	Style defaultStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
	doc.setCharacterAttributes(0, Math.max(0, doc.getLength()), defaultStyle, true);

	Style variableStyle = doc.getStyle("variableHighlight");
	if (variableStyle == null) {
	    variableStyle = doc.addStyle("variableHighlight", null);
	    StyleConstants.setForeground(variableStyle, new Color(0, 100, 200));
	    StyleConstants.setBold(variableStyle, true);
	}

	Matcher m = VARIABLE_PATTERN.matcher(text);
	while (m.find()) {
	    doc.setCharacterAttributes(m.start(), m.end() - m.start(), variableStyle, false);
	}
    }

    private void wireHighlightTimer() {
	highlightTimer = new javax.swing.Timer(150, e -> {
	    highlightVariables(subjectTextField);
	    highlightVariables(bodyTextArea);
	});
	highlightTimer.setRepeats(false);
    }

    private void wireDocumentListener() {
	DocumentListener dl = new DocumentListener() {
	    @Override public void insertUpdate(DocumentEvent e) {
		if (!suppressTableRebuild) rebuildVariablesTable();
		highlightTimer.restart();
	    }
	    @Override public void removeUpdate(DocumentEvent e) {
		if (!suppressTableRebuild) rebuildVariablesTable();
		highlightTimer.restart();
	    }
	    @Override public void changedUpdate(DocumentEvent e) {
		if (!suppressTableRebuild) rebuildVariablesTable();
		highlightTimer.restart();
	    }
	};
	subjectTextField.getDocument().addDocumentListener(dl);
	bodyTextArea.getDocument().addDocumentListener(dl);
    }

    private void insertNextVariable() {
	String fullText = subjectTextField.getText() + bodyTextArea.getText();
	int maxN = 0;
	Matcher m = NUMBERED_VARIABLE_PATTERN.matcher(fullText);
	while (m.find()) {
	    int n = Integer.parseInt(m.group(1));
	    if (n > maxN) maxN = n;
	}
	String nextVar = "{" + (maxN + 1) + "}";
	JTextComponent target = lastFocusedText != null ? lastFocusedText : bodyTextArea;
	target.replaceSelection(nextVar);
    }

    public Map<String, String> getVariableMappings() {
	Map<String, String> mappings = new HashMap<>();
	for (var entry : variablesTableModel.getRows()) {
	    if (entry.linkedOption() != null) {
		mappings.put(entry.variable(), entry.linkedOption().placeholder());
	    }
	}
	return mappings;
    }

    public void setVariableOptions(List<VariableOption> options) {
	variableComboModel.removeAllElements();
	for (VariableOption opt : options) {
	    variableComboModel.addElement(opt);
	}
    }
}
