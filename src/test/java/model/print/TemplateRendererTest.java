package model.print;

import java.util.List;
import java.util.Map;
import javax.swing.JTextPane;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TemplateRenderer}: template rendering into the styled receipt
 * document and the editor line descriptions.
 */
class TemplateRendererTest {

    private StyledDocument doc;

    @BeforeEach
    void setUp() throws Exception {
        JTextPane pane = new JTextPane();
        doc = pane.getStyledDocument();
        TemplateRenderer.applyReceiptStyles(doc);
    }

    /**
     * Verifies text and field segments are resolved into the document.
     * Expected: the rendered text joins literals and data values.
     * Failure: fields are printed as their key name or ignored.
     */
    @Test
    void shouldRenderTextAndFields() throws Exception {
        PrintTemplate template = template(line(false, 0,
                new PrintSegment(SegmentType.TEXT, "Total: ", "DefaultStyle", false),
                new PrintSegment(SegmentType.FIELD, "totalPrice", "DefaultStyle", true)));

        TemplateRenderer.render(template, Map.of("totalPrice", "$40.000"), doc);

        assertThat(text()).contains("Total: $40.000");
    }

    /**
     * Verifies a missing field does not print its placeholder.
     * Expected: the line keeps its literal text and drops the unresolved value.
     * Failure: receipts show empty labels for fields the transaction does not have.
     */
    @Test
    void shouldSkipFieldsWithoutData() throws Exception {
        PrintTemplate template = template(line(false, 0,
                new PrintSegment(SegmentType.TEXT, "Extension: ", "DefaultStyle", false),
                new PrintSegment(SegmentType.FIELD, "extensionDuration", "DefaultStyle", false)));

        TemplateRenderer.render(template, Map.of(), doc);

        assertThat(text()).isEqualTo("Extension: \n");
    }

    /**
     * Verifies an entirely empty line disappears instead of printing a blank row.
     * Expected: the document stays empty.
     * Failure: custom receipts print stray blank lines for unused fields.
     */
    @Test
    void shouldDropLineWithoutContent() throws Exception {
        PrintTemplate template = template(line(false, 0,
                new PrintSegment(SegmentType.FIELD, "reminderText", "DefaultStyle", false)));

        TemplateRenderer.render(template, Map.of(), doc);

        assertThat(doc.getLength()).isZero();
    }

    /**
     * Verifies spacer lines are added after a line.
     * Expected: two filler rows follow the printed text.
     * Failure: vertical spacing configured in the editor is ignored when printing.
     */
    @Test
    void shouldAddSpacersAfterLine() throws Exception {
        PrintTemplate template = template(line(false, 2,
                new PrintSegment(SegmentType.TEXT, "Hola", "DefaultStyle", false)));

        TemplateRenderer.render(template, Map.of(), doc);

        assertThat(text()).isEqualTo("Hola\n \n \n");
    }

    /**
     * Verifies a centered line is aligned to the center of the receipt.
     * Expected: the paragraph alignment is ALIGN_CENTER for centered lines only.
     * Failure: the editor offers a centering option that never reaches the printer.
     */
    @Test
    void shouldCenterLineWhenRequested() throws Exception {
        PrintTemplate template = new PrintTemplate(PrintTemplateType.TURN_SUMMARY, List.of(new PrintBand("BODY", List.of(
                line(true, 0, new PrintSegment(SegmentType.TEXT, "Inicio turno:", "DefaultStyleBold", true)),
                line(false, 0, new PrintSegment(SegmentType.TEXT, "Cant\tConcepto", "TransactionStyle", false))))));

        TemplateRenderer.render(template, Map.of(), doc);

        assertThat(alignmentAt(0)).isEqualTo(StyleConstants.ALIGN_CENTER);
        assertThat(alignmentAt(text().indexOf("Cant"))).isEqualTo(StyleConstants.ALIGN_LEFT);
    }

    /**
     * Verifies multi-line field values (item or activity blocks) keep their rows.
     * Expected: every row of the value is rendered.
     * Failure: item lists collapse into a single row.
     */
    @Test
    void shouldRenderMultiLineFieldValue() throws Exception {
        PrintTemplate template = template(line(false, 0,
                new PrintSegment(SegmentType.FIELD, "items", "TransactionStyle", false)));

        TemplateRenderer.render(template, Map.of("items", "  1 Cerveza\t$8.000\n  1 Agua\t$3.000"), doc);

        assertThat(text()).contains("Cerveza").contains("Agua");
    }

    /**
     * Verifies the bold flag selects the bold variant of a style.
     * Expected: the segment is rendered bold without changing the style name.
     * Failure: the editor's bold toggle has no effect on the printed receipt.
     */
    @Test
    void shouldApplyBoldVariant() throws Exception {
        PrintTemplate template = template(line(false, 0,
                new PrintSegment(SegmentType.TEXT, "Total", "TransactionStyle", true)));

        TemplateRenderer.render(template, Map.of(), doc);

        assertThat(StyleConstants.isBold(doc.getCharacterElement(0).getAttributes())).isTrue();
        assertThat(StyleConstants.getFontSize(doc.getCharacterElement(0).getAttributes())).isEqualTo(9);
    }

    /**
     * Verifies an unknown style name degrades to the default style.
     * Expected: the text is rendered with the default 10pt style.
     * Failure: a template referencing an unknown style breaks printing.
     */
    @Test
    void shouldFallBackToDefaultStyleForUnknownStyle() throws Exception {
        PrintTemplate template = template(line(false, 0,
                new PrintSegment(SegmentType.TEXT, "Hola", "NoExiste", false)));

        TemplateRenderer.render(template, Map.of(), doc);

        assertThat(StyleConstants.getFontSize(doc.getCharacterElement(0).getAttributes())).isEqualTo(10);
        assertThat(StyleConstants.isBold(doc.getCharacterElement(0).getAttributes())).isFalse();
    }

    /**
     * Verifies the receipt styles can be scaled for the editor preview zoom.
     * Expected: every registered size is multiplied by the scale factor.
     * Failure: the preview claims a zoom level while drawing at 100%.
     */
    @Test
    void shouldScaleStyleFontSizesForPreviewZoom() {
        StyledDocument zoomed = new JTextPane().getStyledDocument();

        TemplateRenderer.applyReceiptStyles(zoomed, 2);

        assertThat(StyleConstants.getFontSize(zoomed.getStyle("DefaultStyle"))).isEqualTo(20);
        assertThat(StyleConstants.getFontSize(zoomed.getStyle("DefaultStyle" + TemplateRenderer.BOLD_SUFFIX)))
                .isEqualTo(20);
        assertThat(StyleConstants.getFontSize(zoomed.getStyle("TransactionStyle"))).isEqualTo(18);
        assertThat(StyleConstants.getFontSize(zoomed.getStyle(TemplateRenderer.FILLER_STYLE))).isEqualTo(2);
    }

    /**
     * Verifies zooming out never collapses a style to a zero or negative size.
     * Expected: the smallest style keeps a usable size at the minimum zoom.
     * Failure: a scaled-to-zero font breaks the preview when the user zooms out.
     */
    @Test
    void shouldKeepStylesVisibleWhenZoomedOut() {
        StyledDocument zoomed = new JTextPane().getStyledDocument();

        TemplateRenderer.applyReceiptStyles(zoomed, 0.5);

        assertThat(StyleConstants.getFontSize(zoomed.getStyle(TemplateRenderer.FILLER_STYLE))).isEqualTo(1);
        assertThat(StyleConstants.getFontSize(zoomed.getStyle("LargeStyle"))).isEqualTo(10);
    }

    /**
     * Verifies the printer keeps the nominal receipt sizes.
     * Expected: the single-argument overload registers the unscaled styles.
     * Failure: preview zoom would change how real receipts are printed.
     */
    @Test
    void shouldKeepPrinterStylesAtNominalSize() {
        assertThat(StyleConstants.getFontSize(doc.getStyle("DefaultStyle"))).isEqualTo(10);
        assertThat(StyleConstants.getFontSize(doc.getStyle("LargeStyle"))).isEqualTo(19);
        assertThat(StyleConstants.getFontSize(doc.getStyle(TemplateRenderer.FILLER_STYLE))).isEqualTo(1);
    }

    /**
     * Verifies the line list label shown by the editor.
     * Expected: fields are shown between markers, newlines flattened.
     * Failure: the editor shows raw field keys mixed with the layout text.
     */
    @Test
    void shouldDescribeLineForEditor() {
        PrintLine line = line(false, 0,
                new PrintSegment(SegmentType.TEXT, "Total:\n", "DefaultStyle", false),
                new PrintSegment(SegmentType.FIELD, "totalPrice", "DefaultStyleBold", true));

        assertThat(TemplateRenderer.describe(line)).isEqualTo("Total: «totalPrice»");
    }

    /**
     * Verifies empty lines are recognizable in the editor list.
     * Expected: a placeholder instead of an empty row.
     * Failure: the editor shows blank entries the user cannot identify.
     */
    @Test
    void shouldDescribeEmptyLine() {
        assertThat(TemplateRenderer.describe(line(false, 0))).isEqualTo("(vacía)");
    }

    /**
     * Verifies the packaged default templates only reference fields the registry knows,
     * so the editor can display and translate every one of them.
     * Expected: each field key of a default template exists in PrintFieldRegistry.
     * Failure: a typo in a shipped template leaves a line that never prints any data.
     */
    @ParameterizedTest
    @EnumSource(PrintTemplateType.class)
    void shouldOnlyUseRegisteredFieldsInDefaultTemplates(PrintTemplateType type) {
        PrintTemplate template = new PrintTemplateStore(java.nio.file.Path.of(".")).defaultTemplate(type);
        List<String> registered = PrintFieldRegistry.getFieldsForType(type).stream()
                .map(PrintFieldRegistry.FieldDef::dataKey)
                .toList();

        assertThat(template).isNotNull();
        assertThat(fieldKeys(template)).isNotEmpty().isSubsetOf(registered);
    }

    /**
     * Verifies every packaged default template renders with the editor preview data.
     * Expected: the rendered receipt is not empty for any type.
     * Failure: a shipped template fails or prints nothing when the user activates it.
     */
    @ParameterizedTest
    @EnumSource(PrintTemplateType.class)
    void shouldRenderDefaultTemplatesWithSampleData(PrintTemplateType type) throws Exception {
        PrintTemplate template = new PrintTemplateStore(java.nio.file.Path.of(".")).defaultTemplate(type);
        JTextPane pane = new JTextPane();
        StyledDocument target = pane.getStyledDocument();
        TemplateRenderer.applyReceiptStyles(target);

        TemplateRenderer.render(template, PrintFieldRegistry.sampleData(type), target);

        assertThat(pane.getText()).isNotBlank();
    }

    private static List<String> fieldKeys(PrintTemplate template) {
        return template.bands().stream()
                .flatMap(band -> band.lines().stream())
                .flatMap(line -> line.segments().stream())
                .filter(segment -> segment.type() == SegmentType.FIELD)
                .map(PrintSegment::text)
                .toList();
    }

    private PrintTemplate template(PrintLine line) {
        return new PrintTemplate(PrintTemplateType.ROOM_RECEIPT, List.of(new PrintBand("BODY", List.of(line))));
    }

    private PrintLine line(boolean centered, int spacerAfter, PrintSegment... segments) {
        return new PrintLine(List.of(segments), centered, spacerAfter);
    }

    private String text() throws Exception {
        return doc.getText(0, doc.getLength());
    }

    private int alignmentAt(int offset) {
        return StyleConstants.getAlignment(doc.getParagraphElement(offset).getAttributes());
    }
}
