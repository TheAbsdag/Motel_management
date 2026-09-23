package model.print;

import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Renders a {@link PrintTemplate} into the styled document used for printing and
 * for the template editor preview.
 *
 * <p>Call {@link #applyReceiptStyles(StyledDocument)} once per document to register
 * the receipt styles; the editor and {@code Printer} share this single definition so
 * the preview matches the printed output.
 */
public final class TemplateRenderer {

    /** Style applied when a segment declares no style or an unknown one. */
    public static final String DEFAULT_STYLE = "DefaultStyle";
    /** Style used for spacer rows. */
    public static final String FILLER_STYLE = "FillerStyle";
    /** Paragraph style used to center a line. */
    public static final String CENTERED_STYLE = "CenteredStyle";
    /** Suffix of the bold variant of a style. */
    public static final String BOLD_SUFFIX = "Bold";

    /** Style names offered by the template editor, matching the thermal receipt layout. */
    public static final List<String> RECEIPT_STYLES = List.of(
            "LargeStyle", "HeaderStyle", DEFAULT_STYLE, "TransactionStyle",
            "FooterStyle", "SecondLastStyle", FILLER_STYLE);

    private static final String EMPTY_LINE_LABEL = "(vacía)";
    private static final String FONT_FAMILY = "Calibri";

    private TemplateRenderer() {}

    /**
     * Registers the receipt styles on the document.
     *
     * @param doc the document to configure; existing styles with the same name are replaced
     */
    public static void applyReceiptStyles(StyledDocument doc) {
        applyReceiptStyles(doc, 1);
    }

    /**
     * Registers the receipt styles on the document, scaled for a preview zoom level.
     *
     * <p>Scaling the styles together with the width of the preview sheet is what keeps the
     * preview wrapping where the printed receipt wraps: at scale {@code 1} the sizes are
     * exactly the ones the printer uses.
     *
     * @param doc   the document to configure; existing styles with the same name are replaced
     * @param scale size multiplier applied to every style
     */
    public static void applyReceiptStyles(StyledDocument doc, double scale) {
        addStyle(doc, "HeaderStyle", 10, false, scale);
        addStyle(doc, "LargeStyle", 19, false, scale);
        addStyle(doc, DEFAULT_STYLE, 10, false, scale);
        addStyle(doc, "TransactionStyle", 9, false, scale);
        addStyle(doc, "FooterStyle", 8, false, scale);
        addStyle(doc, "SecondLastStyle", 7, false, scale);

        addStyle(doc, "HeaderStyle" + BOLD_SUFFIX, 10, true, scale);
        addStyle(doc, "LargeStyle" + BOLD_SUFFIX, 19, true, scale);
        addStyle(doc, DEFAULT_STYLE + BOLD_SUFFIX, 10, true, scale);
        addStyle(doc, "TransactionStyle" + BOLD_SUFFIX, 9, true, scale);
        addStyle(doc, "FooterStyle" + BOLD_SUFFIX, 8, true, scale);
        addStyle(doc, "SecondLastStyle" + BOLD_SUFFIX, 7, true, scale);

        addStyle(doc, FILLER_STYLE, 1, false, scale);

        Style centered = doc.addStyle(CENTERED_STYLE, null);
        StyleConstants.setAlignment(centered, StyleConstants.ALIGN_CENTER);
    }

    /**
     * Renders every band and line of the template into the document.
     *
     * @param template the template to render
     * @param data     field values, as built by {@link PrintDataBuilder}
     * @param doc      the target document, already configured with receipt styles
     * @throws BadLocationException if the document rejects an insertion
     */
    public static void render(PrintTemplate template, Map<String, String> data, StyledDocument doc)
            throws BadLocationException {
        for (PrintBand band : template.bands()) {
            for (PrintLine line : band.lines()) {
                renderLine(line, data, doc);
            }
        }
    }

    /**
     * Builds the one-line description of a template line used by the editor's line list.
     *
     * @param line the line to describe
     * @return the literal text with fields shown as {@code «key»}, or a placeholder when empty
     */
    public static String describe(PrintLine line) {
        StringBuilder description = new StringBuilder();
        for (PrintSegment segment : line.segments()) {
            String text = segment.text() != null ? segment.text() : "";
            if (segment.type() == SegmentType.FIELD) {
                description.append('«').append(text).append('»');
            } else {
                description.append(text);
            }
        }
        String result = description.toString().replace('\n', ' ').trim();
        return result.isEmpty() ? EMPTY_LINE_LABEL : result;
    }

    private static void renderLine(PrintLine line, Map<String, String> data, StyledDocument doc)
            throws BadLocationException {
        int start = doc.getLength();
        boolean hasContent = false;

        for (PrintSegment segment : line.segments()) {
            String resolved = switch (segment.type()) {
                case TEXT -> segment.text() != null ? segment.text() : "";
                case FIELD -> data.getOrDefault(segment.text(), "");
            };
            if (!resolved.isEmpty()) {
                doc.insertString(doc.getLength(), resolved, getStyle(doc, segment.style(), segment.bold()));
                hasContent = true;
            }
        }

        if (hasContent) {
            doc.insertString(doc.getLength(), "\n", getStyle(doc, DEFAULT_STYLE, false));
            centerParagraph(doc, start, line.centered());
        }

        for (int spacer = 0; spacer < line.spacerAfter(); spacer++) {
            doc.insertString(doc.getLength(), " \n", getStyle(doc, FILLER_STYLE, false));
        }
    }

    private static void centerParagraph(StyledDocument doc, int start, boolean centered) {
        if (!centered) {
            return;
        }
        Style centeredStyle = doc.getStyle(CENTERED_STYLE);
        if (centeredStyle != null) {
            doc.setParagraphAttributes(start, doc.getLength() - start, centeredStyle, false);
        }
    }

    private static Style getStyle(StyledDocument doc, String styleName, boolean bold) {
        String name = styleName != null ? styleName : DEFAULT_STYLE;
        if (bold) {
            Style boldStyle = doc.getStyle(name + BOLD_SUFFIX);
            if (boldStyle != null) {
                return boldStyle;
            }
        }
        Style base = doc.getStyle(name);
        if (base != null) {
            return base;
        }
        return doc.getStyle(DEFAULT_STYLE);
    }

    private static void addStyle(StyledDocument doc, String name, int fontSize, boolean bold, double scale) {
        Style style = doc.addStyle(name, null);
        StyleConstants.setFontSize(style, scaledFontSize(fontSize, scale));
        StyleConstants.setFontFamily(style, FONT_FAMILY);
        if (bold) {
            StyleConstants.setBold(style, true);
        }
    }

    /** Scales a receipt font size, keeping the smallest style visible at any zoom. */
    private static int scaledFontSize(int fontSize, double scale) {
        return Math.max(1, (int) Math.round(fontSize * scale));
    }
}
