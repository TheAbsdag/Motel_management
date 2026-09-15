/*
 * Created by JFormDesigner on Tue Sep 15 12:09:26 GMT-05:00 2026
 */

package view;

import java.awt.Font;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.json.CurrencyConfig;
import net.miginfocom.swing.MigLayout;
import view.helpers.CurrencyFormatter;
import view.interfaces.DirtyTrackable;

/**
 * @author SECC
 */
public class CurrencyConfigurationView extends JPanel implements DirtyTrackable {

    private boolean hasUnsavedChanges;
    private ButtonGroup positionButtonGroup;
    private final String[] currencyCodes = {"COP", "USD", "EUR", "JPY", "GBP"};
    private final String[] currencySymbols = {"$", "$", "\u20AC", "\u00A5", "\u00A3"};
    private final int[] currencyDecimals = {0, 2, 2, 0, 2};

    public CurrencyConfigurationView() {
        initCustomComponents();
        initComponents();
    }

    private void initCustomComponents() {
        hasUnsavedChanges = false;
        positionButtonGroup = new ButtonGroup();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco (SANTIAGO CASTELBLANCO)
	titleLabel = new JLabel();
	codeLabel = new JLabel();
	codeComboBox = new JComboBox();
	symbolLabel = new JLabel();
	symbolTextField = new JTextField();
	decimalsLabel = new JLabel();
	decimalsTextField = new JTextField();
	positionLabel = new JLabel();
	beforeRadio = new JRadioButton();
	afterRadio = new JRadioButton();
	previewLabel = new JLabel();
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
	    "[]" +
	    "[]"));

	//---- titleLabel ----
	titleLabel.setText("CONFIGURACION MONEDA");
	titleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(titleLabel, "cell 0 0 2 1");

	//---- codeLabel ----
	codeLabel.setText("CODIGO:");
	codeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(codeLabel, "cell 0 1");

	//---- codeComboBox ----
	codeComboBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(codeComboBox, "cell 1 1,growy");

	//---- symbolLabel ----
	symbolLabel.setText("SIMBOLO:");
	symbolLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(symbolLabel, "cell 0 2");

	//---- symbolTextField ----
	symbolTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(symbolTextField, "cell 1 2,growy");

	//---- decimalsLabel ----
	decimalsLabel.setText("DECIMALES:");
	decimalsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(decimalsLabel, "cell 0 3");

	//---- decimalsTextField ----
	decimalsTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(decimalsTextField, "cell 1 3,growy");

	//---- positionLabel ----
	positionLabel.setText("POSICION SIMBOLO:");
	positionLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(positionLabel, "cell 0 4");

	//---- beforeRadio ----
	beforeRadio.setText("Antes ($100)");
	beforeRadio.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(beforeRadio, "cell 1 4");

	//---- afterRadio ----
	afterRadio.setText("Despues (100$)");
	afterRadio.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(afterRadio, "cell 2 4");

	//---- previewLabel ----
	previewLabel.setText("Vista previa: $40,000");
	previewLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(previewLabel, "cell 0 5 2 1,alignx center");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(backButton, "cell 0 6,growy");

	//---- saveButton ----
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(saveButton, "cell 2 6,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on

        codeComboBox.setModel(new DefaultComboBoxModel<>(currencyCodes));
        positionButtonGroup.add(beforeRadio);
        positionButtonGroup.add(afterRadio);
        beforeRadio.setSelected(true);

        wireListeners();
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco (SANTIAGO CASTELBLANCO)
    private JLabel titleLabel;
    private JLabel codeLabel;
    private JComboBox codeComboBox;
    private JLabel symbolLabel;
    private JTextField symbolTextField;
    private JLabel decimalsLabel;
    private JTextField decimalsTextField;
    private JLabel positionLabel;
    private JRadioButton beforeRadio;
    private JRadioButton afterRadio;
    private JLabel previewLabel;
    private JButton backButton;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    private void wireListeners() {
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { markDirty(); updatePreview(); }
            @Override public void removeUpdate(DocumentEvent e) { markDirty(); updatePreview(); }
            @Override public void changedUpdate(DocumentEvent e) { markDirty(); updatePreview(); }
        };
        symbolTextField.getDocument().addDocumentListener(docListener);
        decimalsTextField.getDocument().addDocumentListener(docListener);

        beforeRadio.addActionListener(e -> { markDirty(); updatePreview(); });
        afterRadio.addActionListener(e -> { markDirty(); updatePreview(); });
        codeComboBox.addActionListener(e -> {
            int idx = codeComboBox.getSelectedIndex();
            if (idx >= 0) {
                symbolTextField.setText(currencySymbols[idx]);
                decimalsTextField.setText(String.valueOf(currencyDecimals[idx]));
            }
            markDirty();
            updatePreview();
        });
    }

    // ========== Data access ==========

    public String getCurrencyCode() { return (String) codeComboBox.getSelectedItem(); }

    public String getSymbol() { return symbolTextField.getText().trim(); }

    public int getDecimalPlaces() {
        try { return Integer.parseInt(decimalsTextField.getText().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    public boolean isSymbolBefore() { return beforeRadio.isSelected(); }

    public void setCurrencyCode(String code) { codeComboBox.setSelectedItem(code); }

    public void setSymbol(String s) { symbolTextField.setText(s); }

    public void setDecimalPlaces(int dp) { decimalsTextField.setText(String.valueOf(dp)); }

    public void setSymbolBefore(boolean before) {
        if (before) beforeRadio.setSelected(true); else afterRadio.setSelected(true);
    }

    public void populate(CurrencyConfig cfg) {
        setCurrencyCode(cfg.currencyCode());
        setSymbol(cfg.symbol());
        setDecimalPlaces(cfg.decimalPlaces());
        setSymbolBefore(cfg.symbolBefore());
        updatePreview();
        clearDirty();
    }

    public CurrencyConfig toConfig() {
        return new CurrencyConfig(getCurrencyCode(), getDecimalPlaces(), getSymbol(), isSymbolBefore());
    }

    private void updatePreview() {
        CurrencyConfig cfg = toConfig();
        String formatted = CurrencyFormatter.format(40000L, cfg);
        previewLabel.setText("Vista previa: " + formatted);
    }

    // ========== Listeners ==========

    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }
    public void onSaveButton(Runnable action) { saveButton.addActionListener(e -> action.run()); }
    public void removeSaveListeners() {
        for (var al : saveButton.getActionListeners()) saveButton.removeActionListener(al);
    }

    // ========== Dirty tracking ==========

    public void markDirty() { hasUnsavedChanges = true; }
    public void clearDirty() { hasUnsavedChanges = false; }
    public boolean isDirty() { return hasUnsavedChanges; }

    public void setBackEnabled(boolean enabled) {
        backButton.setVisible(enabled);
        backButton.setEnabled(enabled);
    }
}
