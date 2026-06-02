package view.helpers;

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

/**
 * Shared dialog utilities for confirmations and information messages.
 * Touch-friendly: uses large fonts suitable for kiosk-style touchscreen use.
 */
public final class DialogHelper {

    private static final Font MESSAGE_FONT = new Font("Segoe UI Black", Font.PLAIN, 22);
    private static final Font BUTTON_FONT  = new Font("Segoe UI Black", Font.PLAIN, 28);

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
}
