package view.helpers;

import javax.swing.JButton;
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
        NumericKeypadPopup.attach(textField);
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
     * set what its new rooms start from. Each value has quick adjustment buttons, and
     * input that cannot be used is reported and asked again instead of being dropped.
     *
     * @param towerName tower the values are entered for, shown in the dialog
     * @param current   the tower's current time slots, or null for the built-in defaults
     * @return the 3 time slots entered, or null when the dialog is cancelled
     */
    public static RoomTime[] showTowerPricingDialog(String towerName, RoomTime[] current) {
        TowerPricingFields fields = createTowerPricingFields(towerName, current);
        while (true) {
            int result = JOptionPane.showConfirmDialog(null, fields.panel, "TIEMPOS Y PRECIOS",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }
            RoomTime[] slots = fields.readSlots();
            if (slots != null) {
                return slots;
            }
            showErrorMessage("La duracion y el valor de cada tiempo deben ser mayores a cero.",
                    "DATOS INVALIDOS");
        }
    }

    /**
     * Builds the content of the tower pricing dialog. Package-private so the prefill of the
     * fields and the adjustment buttons can be checked without opening a modal dialog.
     *
     * @param towerName tower the values are entered for, shown in the header
     * @param current   the values to start from, or null for the built-in defaults
     * @return the panel with the 3 slots of the dialog
     */
    static TowerPricingFields createTowerPricingFields(String towerName, RoomTime[] current) {
        RoomTime[] prefill = current != null && current.length == 3 ? current : RoomTime.getDefaultTimeSlots();

        JPanel panel = new JPanel(new BorderLayout(15, 20));
        panel.add(styledMessage("TIEMPOS Y PRECIOS DE " + towerName
                + "\nSe aplican a todas sus habitaciones y a las habitaciones nuevas."), BorderLayout.NORTH);

        JPanel grid = new JPanel(new MigLayout("insets 0, fillx", "[][grow,fill][][grow,fill][]"));
        grid.add(fieldLabel("DURACION"), "cell 1 0");
        grid.add(fieldLabel("UNIDAD"), "cell 2 0");
        grid.add(fieldLabel("VALOR"), "cell 3 0");
        grid.add(fieldLabel("AJUSTAR VALOR"), "cell 4 0");

        TowerPricingFields fields = new TowerPricingFields(panel);
        for (int i = 0; i < prefill.length; i++) {
            fields.addSlot(i, prefill[i], grid);
        }
        panel.add(grid, BorderLayout.CENTER);
        return fields;
    }

    /**
     * The panel of the tower pricing dialog and its inputs, one entry per room time slot.
     */
    static final class TowerPricingFields {

        /** Price steps of the adjustment buttons, as on the room configuration screen. */
        private static final long[] PRICE_STEPS = {-1000L, -100L, 100L, 1000L};

        private final JPanel panel;
        private final List<JTextField> durationFields = new ArrayList<>();
        private final List<JComboBox<String>> unitBoxes = new ArrayList<>();
        private final List<JTextField> priceFields = new ArrayList<>();
        private final List<JButton[]> stepButtons = new ArrayList<>();

        private TowerPricingFields(JPanel panel) {
            this.panel = panel;
        }

        private void addSlot(int index, RoomTime slot, JPanel grid) {
            // The duration is shown in the unit it is stored in, so what the dialog opens
            // with is what the tower already has: 10.800 seconds read as 3 HORAS.
            int unitIndex = unitIndexFor(slot.getTimeSeconds());
            JTextField durationField = numericField(
                    UNIT_VALUES[unitIndex].convert(slot.getTimeSeconds(), TimeUnit.SECONDS));
            JComboBox<String> unitBox = new JComboBox<>(UNIT_NAMES);
            unitBox.setFont(MESSAGE_FONT);
            unitBox.setSelectedIndex(unitIndex);
            JTextField priceField = numericField(slot.getPrice());

            JPanel steps = new JPanel(new MigLayout("insets 0", "[][][][]"));
            JButton[] stepButtonArray = new JButton[PRICE_STEPS.length];
            for (int s = 0; s < PRICE_STEPS.length; s++) {
                long step = PRICE_STEPS[s];
                JButton stepButton = new JButton(step > 0 ? "+" + step : String.valueOf(step));
                stepButton.setFont(MESSAGE_FONT);
                stepButton.addActionListener(e -> PriceAdjustmentHelper.adjust(priceField, step));
                steps.add(stepButton, "growx");
                stepButtonArray[s] = stepButton;
            }

            int row = index + 1;
            grid.add(fieldLabel("TIEMPO " + (index + 1)), "cell 0 " + row);
            grid.add(durationField, "cell 1 " + row);
            grid.add(unitBox, "cell 2 " + row);
            grid.add(priceField, "cell 3 " + row);
            grid.add(steps, "cell 4 " + row);

            durationFields.add(durationField);
            unitBoxes.add(unitBox);
            priceFields.add(priceField);
            stepButtons.add(stepButtonArray);
        }

        /**
         * @return the 3 slots as entered, or null when a duration or a price is unusable
         */
        RoomTime[] readSlots() {
            RoomTime[] slots = new RoomTime[durationFields.size()];
            for (int i = 0; i < slots.length; i++) {
                TimeUnit unit = UNIT_VALUES[unitBoxes.get(i).getSelectedIndex()];
                long seconds = InputParser.parseDurationSeconds(durationFields.get(i).getText(), unit);
                long price = InputParser.parseLongSafe(priceFields.get(i).getText());
                if (seconds <= 0 || price <= 0) {
                    return null;
                }
                slots[i] = new RoomTime(price, seconds);
            }
            return slots;
        }

        /** @return the duration field of a slot, in the unit its combo box selects */
        JTextField durationField(int index) {
            return durationFields.get(index);
        }

        /** @return the value field of a slot */
        JTextField priceField(int index) {
            return priceFields.get(index);
        }

        /** @return the adjustment buttons of a slot, in {@link #PRICE_STEPS} order */
        JButton[] priceStepButtons(int index) {
            return stepButtons.get(index);
        }
    }

    /** Digits-only field showing a duration or a price, filled from the keypad on a tap. */
    private static JTextField numericField(long value) {
        JTextField field = new JTextField(String.valueOf(value), 7);
        field.setFont(FIELD_FONT);
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        NumericKeypadPopup.attach(field);
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
