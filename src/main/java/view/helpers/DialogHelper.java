package view.helpers;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import model.RoomTime;
import net.miginfocom.swing.MigLayout;

/**
 * Shared dialog utilities for confirmations and information messages.
 * Touch-friendly: uses large fonts suitable for kiosk-style touchscreen use.
 */
public final class DialogHelper {

    private static final Font MESSAGE_FONT = new Font("Segoe UI Black", Font.PLAIN, 22);
    private static final Font BUTTON_FONT  = new Font("Segoe UI Black", Font.PLAIN, 28);
    private static final Font FIELD_FONT   = new Font("Segoe UI Black", Font.PLAIN, 28);

    /** Duration units offered by {@link #showTowerPricingDialog}, in combo box order. */
    private static final String[] UNIT_NAMES = {"SEGUNDOS", "MINUTOS", "HORAS"};
    private static final TimeUnit[] UNIT_VALUES = {TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS};

    static {
        UIManager.put("OptionPane.yesButtonText",    "SI");
        UIManager.put("OptionPane.noButtonText",     "NO");
        UIManager.put("OptionPane.okButtonText",     "ACEPTAR");
        UIManager.put("OptionPane.cancelButtonText", "CANCELAR");
        UIManager.put("OptionPane.messageFont", MESSAGE_FONT);
        UIManager.put("OptionPane.buttonFont",  BUTTON_FONT);
        UIManager.put("Button.font",            BUTTON_FONT);
    }

    private DialogHelper() { }

    private static JComponent styledMessage(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(MESSAGE_FONT);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFocusable(false);
        textArea.setColumns(35);
        textArea.setMinimumSize(new Dimension(400, textArea.getPreferredSize().height));
        return textArea;
    }

    // ========== Dialogs ==========

    public static boolean confirmPrinting() {
        int response = JOptionPane.showConfirmDialog(
                null,
                styledMessage("¿ESTA SEGURO DE NO IMPRIMIR RECIBO?"),
                "CONFIRMACION",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return response == JOptionPane.YES_OPTION;
    }

    public static boolean confirmTurnEnd() {
        int response = JOptionPane.showConfirmDialog(
                null,
                styledMessage("¿ESTA SEGURO DE TERMINAR EL TURNO?"),
                "CONFIRMACION",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return response == JOptionPane.YES_OPTION;
    }

    public static void showInfoMessage(String message, String title) {
        JOptionPane.showMessageDialog(
                null,
                styledMessage(message),
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void showHtmlMessage(String html, String title) {
        JLabel label = new JLabel(html);
        JOptionPane.showMessageDialog(null, label, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorMessage(String message, String title) {
        JOptionPane.showMessageDialog(
                null,
                styledMessage(message),
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static boolean confirmDialog(String message, String title) {
        int response = JOptionPane.showConfirmDialog(
                null,
                styledMessage(message),
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return response == JOptionPane.YES_OPTION;
    }

    public static Integer showNumericInputDialog(String message, String title, String defaultValue) {
        JTextField textField = new JTextField(10);
        textField.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        textField.setText(defaultValue != null ? defaultValue : "");
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null || string.matches("\\d*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null || text.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
            }
        });

        JComponent label = styledMessage(message);
        JPanel panel = new JPanel(new BorderLayout(15, 20));
        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String text = textField.getText().trim();
            if (!text.isEmpty()) {
                return Integer.parseInt(text);
            }
        }
        return null;
    }

    /**
     * Asks for the duration and the price of the 3 room time slots, prefilled with the
     * values given. Used by the floor configuration screen to price a whole tower and to
     * set what its new rooms start from. Invalid input is reported and asked again
     * instead of being dropped.
     *
     * @param towerName tower the values are entered for, shown in the dialog
     * @param current   the tower's current time slots, or null for the built-in defaults
     * @return the 3 time slots entered, or null when the dialog is cancelled
     */
    public static RoomTime[] showTowerPricingDialog(String towerName, RoomTime[] current) {
        RoomTime[] prefill = current != null && current.length == 3 ? current : RoomTime.getDefaultTimeSlots();

        JPanel panel = new JPanel(new BorderLayout(15, 20));
        panel.add(styledMessage("TIEMPOS Y PRECIOS DE " + towerName
                + "\nSe aplican a todas sus habitaciones y a las habitaciones nuevas."), BorderLayout.NORTH);

        JPanel grid = new JPanel(new MigLayout("insets 0, fillx", "[][grow,fill][][grow,fill]"));
        grid.add(fieldLabel("DURACION"), "cell 1 0");
        grid.add(fieldLabel("UNIDAD"), "cell 2 0");
        grid.add(fieldLabel("VALOR"), "cell 3 0");

        List<JTextField> durationFields = new ArrayList<>();
        List<JComboBox<String>> unitBoxes = new ArrayList<>();
        List<JTextField> priceFields = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            JTextField durationField = numericField(prefill[i].getTimeSeconds());
            JComboBox<String> unitBox = new JComboBox<>(UNIT_NAMES);
            unitBox.setFont(MESSAGE_FONT);
            unitBox.setSelectedIndex(unitIndexFor(prefill[i].getTimeSeconds()));
            JTextField priceField = numericField(prefill[i].getPrice());

            int row = i + 1;
            grid.add(fieldLabel("TIEMPO " + (i + 1)), "cell 0 " + row);
            grid.add(durationField, "cell 1 " + row);
            grid.add(unitBox, "cell 2 " + row);
            grid.add(priceField, "cell 3 " + row);

            durationFields.add(durationField);
            unitBoxes.add(unitBox);
            priceFields.add(priceField);
        }
        panel.add(grid, BorderLayout.CENTER);

        while (true) {
            int result = JOptionPane.showConfirmDialog(null, panel, "TIEMPOS Y PRECIOS",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            RoomTime[] slots = new RoomTime[3];
            boolean valid = true;
            for (int i = 0; i < 3; i++) {
                TimeUnit unit = UNIT_VALUES[unitBoxes.get(i).getSelectedIndex()];
                long seconds = InputParser.parseDurationSeconds(durationFields.get(i).getText(), unit);
                long price = InputParser.parseLongSafe(priceFields.get(i).getText());
                if (seconds <= 0 || price <= 0) {
                    valid = false;
                    break;
                }
                slots[i] = new RoomTime(price, seconds);
            }
            if (valid) {
                return slots;
            }
            showErrorMessage("La duracion y el valor de cada tiempo deben ser mayores a cero.",
                    "DATOS INVALIDOS");
        }
    }

    /** Digits-only field showing a duration or a price. */
    private static JTextField numericField(long value) {
        JTextField field = new JTextField(String.valueOf(value), 7);
        field.setFont(FIELD_FONT);
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        return field;
    }

    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MESSAGE_FONT);
        return label;
    }

    /** Combo box index matching a duration: hours when exact, minutes when exact, seconds otherwise. */
    private static int unitIndexFor(long seconds) {
        if (seconds >= 3600 && seconds % 3600 == 0) return 2;
        if (seconds >= 60 && seconds % 60 == 0) return 1;
        return 0;
    }
}
