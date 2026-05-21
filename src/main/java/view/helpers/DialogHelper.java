package view.helpers;

import javax.swing.JOptionPane;

/**
 * Shared dialog utilities for confirmations and information messages.
 * Extracted from UserGUI to reduce the view's responsibility footprint.
 */
public final class DialogHelper {

    private DialogHelper() { }

    public static boolean confirmPrinting() {
        int response = JOptionPane.showConfirmDialog(
                null,
                "¿ESTA SEGURO DE NO IMPRIMIR RECIBO?",
                "CONFIRMACION",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return response == JOptionPane.YES_OPTION;
    }

    public static boolean confirmTurnEnd() {
        int response = JOptionPane.showConfirmDialog(
                null,
                "¿ESTA SEGURO DE TERMINAR EL TURNO?",
                "CONFIRMACION",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return response == JOptionPane.YES_OPTION;
    }

    public static void showInfoMessage(String message, String title) {
        JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
