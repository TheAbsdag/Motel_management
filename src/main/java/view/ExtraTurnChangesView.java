package view;

import view.helpers.NumericDocumentFilter;
import view.helpers.FocusHighlighter;
import view.helpers.PriceAdjustmentHelper;
import java.awt.Font;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.MigLayout;

/**
 * View for registering bank transfers and safe deposits during a turn.
 * @author Santiago
 */
public class ExtraTurnChangesView extends JPanel {

    public ExtraTurnChangesView() {
        initComponents();
        prepareInternalListeners();
        FocusHighlighter.applyToAll(this);
    }

    private void prepareInternalListeners() {
        addSmallValueButton.addActionListener(e -> updateValue(100L));
        addBigValueButton.addActionListener(e -> updateValue(1000L));
        minusBigValueButton.addActionListener(e -> updateValue(-1000L));
        minusSmallValueButton.addActionListener(e -> updateValue(-100L));
    }

    private void updateValue(long value) {
        PriceAdjustmentHelper.adjust(valueTextField, value);
    }

    public void clearFields() {
        descriptionText.setText("");
        valueTextField.setText("0");
        confirmationButton.setEnabled(false);
        bankTransferBox.setSelected(false);
        saveDespositBox.setSelected(false);
    }

    // --- Getters ---

    public JLabel getExtraTurnChangesInformativeLabel() { return extraTurnChangesInformativeLabel; }
    public JLabel getDescriptionLabel() { return descriptionLabel; }
    public JTextField getDescriptionText() { return descriptionText; }
    public JLabel getValueLabel() { return valueLabel; }
    public JTextField getValueTextField() { return valueTextField; }
    public JButton getMinusSmallValueButton() { return minusSmallValueButton; }
    public JButton getAddSmallValueButton() { return addSmallValueButton; }
    public JButton getMinusBigValueButton() { return minusBigValueButton; }
    public JButton getAddBigValueButton() { return addBigValueButton; }
    public JCheckBox getBankTransferBox() { return bankTransferBox; }
    public JCheckBox getSaveDespositBox() { return saveDespositBox; }
    public JButton getBackButton() { return backButton; }
    public JButton getConfirmationButton() { return confirmationButton; }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
        extraTurnChangesInformativeLabel = new JLabel();
        descriptionLabel = new JLabel();
        descriptionText = new JTextField();
        valueLabel = new JLabel();
        valueTextField = new JTextField();
        ((AbstractDocument) valueTextField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        minusSmallValueButton = new JButton();
        addSmallValueButton = new JButton();
        minusBigValueButton = new JButton();
        addBigValueButton = new JButton();
        bankTransferBox = new JCheckBox();
        saveDespositBox = new JCheckBox();
        backButton = new JButton();
        confirmationButton = new JButton();

        //======== this ========
        setLayout(new MigLayout(
            "fill,hidemode 3",
            // columns
            "[grow,fill]" +
            "[grow,fill]" +
            "[grow,fill]",
            // rows
            "[grow]" +
            "[grow]" +
            "[grow]" +
            "[grow]" +
            "[grow]" +
            "[grow]" +
            "[grow]"));

        //---- extraTurnChangesInformativeLabel ----
        extraTurnChangesInformativeLabel.setText("REGISTRO ABONO CAJA Y TRANSFERENCIAS BANCARIAS");
        extraTurnChangesInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        extraTurnChangesInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(extraTurnChangesInformativeLabel, "cell 0 0 3 1");

        //---- descriptionLabel ----
        descriptionLabel.setText("DESCRIPCION / CONCEPTO");
        descriptionLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(descriptionLabel, "cell 0 1");

        //---- descriptionText ----
        descriptionText.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(descriptionText, "cell 1 1 2 1,growy");

        //---- valueLabel ----
        valueLabel.setText("VALOR");
        valueLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(valueLabel, "cell 0 2");

        //---- valueTextField ----
        valueTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(valueTextField, "cell 1 2 2 1,growy");

        //---- minusSmallValueButton ----
        minusSmallValueButton.setText("-100");
        minusSmallValueButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(minusSmallValueButton, "cell 1 3,growy");

        //---- addSmallValueButton ----
        addSmallValueButton.setText("+100");
        addSmallValueButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(addSmallValueButton, "cell 2 3,growy");

        //---- minusBigValueButton ----
        minusBigValueButton.setText("-1000");
        minusBigValueButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(minusBigValueButton, "cell 1 4,growy");

        //---- addBigValueButton ----
        addBigValueButton.setText("+1000");
        addBigValueButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(addBigValueButton, "cell 2 4,growy");

        //---- bankTransferBox ----
        bankTransferBox.setText("Transferencia bancaria");
        bankTransferBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(bankTransferBox, "cell 0 5,growy");

        //---- saveDespositBox ----
        saveDespositBox.setText("Abono a caja");
        saveDespositBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        add(saveDespositBox, "cell 2 5,growy");

        //---- backButton ----
        backButton.setText("VOLVER");
        backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        add(backButton, "cell 0 6,growy");

        //---- confirmationButton ----
        confirmationButton.setText("CONFIRMAR");
        confirmationButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        add(confirmationButton, "cell 2 6,growy");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel extraTurnChangesInformativeLabel;
    private JLabel descriptionLabel;
    private JTextField descriptionText;
    private JLabel valueLabel;
    private JTextField valueTextField;
    private JButton minusSmallValueButton;
    private JButton addSmallValueButton;
    private JButton minusBigValueButton;
    private JButton addBigValueButton;
    private JCheckBox bankTransferBox;
    private JCheckBox saveDespositBox;
    private JButton backButton;
    private JButton confirmationButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
