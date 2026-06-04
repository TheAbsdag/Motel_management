package view.helpers;

import javax.swing.text.*;
import model.json.CurrencyConfig;

public class NumericDocumentFilter extends DocumentFilter {

    private final CurrencyConfig currencyConfig;

    public NumericDocumentFilter() {
        this(CurrencyConfig.defaultConfig());
    }

    public NumericDocumentFilter(CurrencyConfig currencyConfig) {
        this.currencyConfig = currencyConfig != null ? currencyConfig : CurrencyConfig.defaultConfig();
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
        String resultText = currentText.substring(0, offset) + string + currentText.substring(offset);
        if (isValid(string, resultText)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
        String resultText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
        if (isValid(text, resultText)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
    }

    private boolean isDecimalAllowed() {
        return currencyConfig.decimalPlaces() > 0;
    }

    private boolean isValid(String inserted, String fullResult) {
        if (inserted == null || inserted.isEmpty()) return true;
        if (!isDecimalAllowed()) {
            try {
                Long.parseLong(inserted);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        String decimalChars = inserted.replaceAll("[0-9.,]", "");
        if (!decimalChars.isEmpty()) return false;
        int dots = 0;
        for (char c : fullResult.toCharArray()) {
            if (c == '.' || c == ',') dots++;
            if (dots > 1) return false;
        }
        return true;
    }
}