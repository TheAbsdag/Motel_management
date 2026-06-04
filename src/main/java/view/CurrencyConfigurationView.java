package view;

import java.awt.Font;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.json.CurrencyConfig;
import net.miginfocom.swing.MigLayout;
import view.interfaces.DirtyTrackable;

public class CurrencyConfigurationView extends JPanel implements DirtyTrackable {

    private boolean hasUnsavedChanges;
    private final String[] currencyCodes = {"COP", "USD", "EUR", "JPY", "GBP"};
    private final String[] currencySymbols = {"$", "$", "\u20AC", "\u00A5", "\u00A3"};
    private final int[] currencyDecimals = {0, 2, 2, 0, 2};

    private JLabel titleLabel;
    private JLabel codeLabel;
    private JComboBox<String> codeComboBox;
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

    public CurrencyConfigurationView() {
        hasUnsavedChanges = false;
        initComponents();
        wireListeners();
    }

    private void initComponents() {
        titleLabel = new JLabel("CONFIGURACION MONEDA");
        titleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        codeLabel = new JLabel("CODIGO:");
        codeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        codeComboBox = new JComboBox<>(currencyCodes);
        codeComboBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        symbolLabel = new JLabel("SIMBOLO:");
        symbolLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        symbolTextField = new JTextField();
        symbolTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        decimalsLabel = new JLabel("DECIMALES:");
        decimalsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        decimalsTextField = new JTextField();
        decimalsTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        positionLabel = new JLabel("POSICION SIMBOLO:");
        positionLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        beforeRadio = new JRadioButton("Antes ($100)");
        beforeRadio.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
        afterRadio = new JRadioButton("Despues (100$)");
        afterRadio.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
        ButtonGroup positionGroup = new ButtonGroup();
        positionGroup.add(beforeRadio);
        positionGroup.add(afterRadio);
        beforeRadio.setSelected(true);

        previewLabel = new JLabel("Vista previa: $40,000");
        previewLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));

        backButton = new JButton("VOLVER");
        backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        saveButton = new JButton("GUARDAR");
        saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));

        setLayout(new MigLayout(
            "fill,hidemode 3",
            "[fill][grow,fill][fill]",
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]"));

        add(titleLabel, "cell 0 0 2 1");

        add(codeLabel, "cell 0 1");
        add(codeComboBox, "cell 1 1,growy");

        add(symbolLabel, "cell 0 2");
        add(symbolTextField, "cell 1 2,growy");

        add(decimalsLabel, "cell 0 3");
        add(decimalsTextField, "cell 1 3,growy");

        add(positionLabel, "cell 0 4");
        add(beforeRadio, "cell 1 4");
        add(afterRadio, "cell 2 4");

        add(previewLabel, "cell 0 5 2 1,alignx center");

        add(backButton, "cell 0 6,growy");
        add(saveButton, "cell 2 6,growy");
    }

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
        String formatted = view.helpers.CurrencyFormatter.format(40000L, cfg);
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
