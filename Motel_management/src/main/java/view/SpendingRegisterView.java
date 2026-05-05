package view;

import view.helpers.FocusHighlighter;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.MigLayout;

/**
 * View for registering spending/expenses during a turn.
 * @author Santiago
 */
public class SpendingRegisterView extends JPanel {

    public SpendingRegisterView() {
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
        long newPrice = Long.parseLong(valueTextField.getText()) + value;
        if (newPrice >= 0L) {
            valueTextField.setText(String.valueOf(newPrice));
        }
    }

    // --- Getters ---

    public JLabel getSpendingRegisterAnnouncementLabel() { return spendingRegisterAnnouncementLabel; }
    public JLabel getDescriptionInformativeLabel() { return descriptionInformativeLabel; }
    public JTextField getDescriptionChangeText() { return descriptionChangeText; }
    public JLabel getValueLabel() { return valueLabel; }
    public JTextField getValueTextField() { return valueTextField; }
    public JButton getMinusSmallValueButton() { return minusSmallValueButton; }
    public JButton getAddSmallValueButton() { return addSmallValueButton; }
    public JButton getMinusBigValueButton() { return minusBigValueButton; }
    public JButton getAddBigValueButton() { return addBigValueButton; }
    public JButton getCancellationButton() { return cancellationButton; }
    public JButton getConfirmationButton() { return confirmationButton; }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
        spendingRegisterAnnouncementLabel = new JLabel();
        descriptionInformativeLabel = new JLabel();
        descriptionChangeText = new JTextField();
        valueLabel = new JLabel();
        valueTextField = new JTextField();
        ((AbstractDocument) valueTextField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        minusSmallValueButton = new JButton();
        addSmallValueButton = new JButton();
        minusBigValueButton = new JButton();
        addBigValueButton = new JButton();
        cancellationButton = new JButton();
        confirmationButton = new JButton();

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

        //---- spendingRegisterAnnouncementLabel ----
        spendingRegisterAnnouncementLabel.setText("REGISTRO GASTOS");
        spendingRegisterAnnouncementLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(spendingRegisterAnnouncementLabel, "cell 0 0");

        //---- descriptionInformativeLabel ----
        descriptionInformativeLabel.setText("CONCEPTO");
        descriptionInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(descriptionInformativeLabel, "cell 0 1,growy");

        //---- descriptionChangeText ----
        descriptionChangeText.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(descriptionChangeText, "cell 1 1 3 1,growy");

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

        //---- cancellationButton ----
        cancellationButton.setText("CANCELAR - VOLVER");
        cancellationButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(cancellationButton, "cell 0 5,growy");

        //---- confirmationButton ----
        confirmationButton.setText("CONFIRMAR");
        confirmationButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(confirmationButton, "cell 3 5,growy");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel spendingRegisterAnnouncementLabel;
    private JLabel descriptionInformativeLabel;
    private JTextField descriptionChangeText;
    private JLabel valueLabel;
    private JTextField valueTextField;
    private JButton minusSmallValueButton;
    private JButton addSmallValueButton;
    private JButton minusBigValueButton;
    private JButton addBigValueButton;
    private JButton cancellationButton;
    private JButton confirmationButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
