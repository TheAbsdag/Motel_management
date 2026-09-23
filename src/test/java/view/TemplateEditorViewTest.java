package view;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextPane;
import model.print.PrintLine;
import model.print.PrintSegment;
import model.print.PrintTemplate;
import model.print.PrintTemplateType;
import model.print.SegmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TemplateEditorViewTest {

    private TemplateEditorView view;

    @BeforeEach
    void setUp() {
        view = new TemplateEditorView();
    }

    /**
     * Verifies the editor offers every customizable document.
     * Expected: the type combo holds all four template types.
     * Failure: a receipt type cannot be customized from the editor.
     */
    @Test
    void shouldOfferEveryTemplateType() throws Exception {
        assertThat(combo("typeComboBox").getItemCount()).isEqualTo(PrintTemplateType.values().length);
        assertThat(view.getSelectedTemplateType()).isEqualTo(PrintTemplateType.ROOM_RECEIPT);
    }

    /**
     * Verifies the controller is notified when the user switches template type.
     * Expected: selecting another type fires the change callback.
     * Failure: the editor keeps editing the previous template type.
     */
    @Test
    void shouldInvokeCallbackWhenTemplateTypeChanges() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onTemplateTypeChange(() -> invoked.set(true));

        combo("typeComboBox").setSelectedItem(PrintTemplateType.TURN_DETAIL);

        assertThat(invoked).isTrue();
    }

    /**
     * Verifies programmatic population does not look like a user action.
     * Expected: setSelectedTemplateType() does not fire the change callback.
     * Failure: loading a template triggers a second reload or a discard prompt.
     */
    @Test
    void shouldNotNotifyOnProgrammaticTypeChange() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onTemplateTypeChange(() -> invoked.set(true));

        view.setSelectedTemplateType(PrintTemplateType.SALE_RECEIPT);

        assertThat(invoked).isFalse();
        assertThat(view.getSelectedTemplateType()).isEqualTo(PrintTemplateType.SALE_RECEIPT);
    }

    /**
     * Verifies the line list reflects the loaded template.
     * Expected: labels are shown and the requested line is selected.
     * Failure: the editor shows an empty or unsynchronized line list.
     */
    @Test
    void shouldShowLinesAndSelectRequestedOne() throws Exception {
        view.setLines(List.of("01 [HEADER] Motel", "02 [BODY] Total: «totalPrice»"), 1);

        assertThat(list("linesList").getModel().getSize()).isEqualTo(2);
        assertThat(view.getSelectedLineIndex()).isEqualTo(1);
    }

    /**
     * Verifies the controller is notified when the user picks another line.
     * Expected: selecting a line in the list fires the selection callback.
     * Failure: the segment table keeps showing the previous line.
     */
    @Test
    void shouldInvokeCallbackWhenLineIsSelected() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onLineSelected(() -> invoked.set(true));
        view.setLines(List.of("primera", "segunda"), 0);

        list("linesList").setSelectedIndex(1);

        assertThat(invoked).isTrue();
    }

    /**
     * Verifies the line options are readable back by the controller.
     * Expected: band, centering and spacers round trip through the view.
     * Failure: layout options edited in the UI never reach the template.
     */
    @Test
    void shouldRoundTripLineOptions() {
        view.setLineOptions("FOOTER", true, 3);

        assertThat(view.getSelectedBand()).isEqualTo("FOOTER");
        assertThat(view.isLineCentered()).isTrue();
        assertThat(view.getSpacerAfter()).isEqualTo(3);
    }

    /**
     * Verifies a band coming from a hand-written template is not lost.
     * Expected: an unknown band name is added to the combo instead of failing.
     * Failure: loading such a template silently moves its lines to another band.
     */
    @Test
    void shouldAcceptUnknownBand() {
        view.setLineOptions("CUSTOM", false, 0);

        assertThat(view.getSelectedBand()).isEqualTo("CUSTOM");
    }

    /**
     * Verifies the segments of a line round trip through the table.
     * Expected: the segments set by the controller are returned unchanged.
     * Failure: edits made in the editor are lost before saving.
     */
    @Test
    void shouldRoundTripSegments() {
        PrintSegment text = new PrintSegment(SegmentType.TEXT, "Total: ", "DefaultStyle", false);
        PrintSegment field = new PrintSegment(SegmentType.FIELD, "totalPrice", "DefaultStyleBold", true);

        view.setSegments(List.of(text, field), 1);

        assertThat(view.getSegments()).containsExactly(text, field);
        assertThat(view.getSelectedSegmentIndex()).isEqualTo(1);
    }

    /**
     * Verifies the controller is notified when a segment is edited in the table.
     * Expected: editing a cell fires the edited callback.
     * Failure: the preview and the dirty state ignore table edits.
     */
    @Test
    void shouldInvokeCallbackWhenSegmentIsEdited() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onSegmentsEdited(() -> invoked.set(true));
        view.setSegments(List.of(new PrintSegment(SegmentType.TEXT, "Hola", "DefaultStyle", false)), 0);

        table("segmentsTable").setValueAt("Adios", 0, 1);

        assertThat(invoked).isTrue();
        assertThat(view.getSegments().get(0).text()).isEqualTo("Adios");
    }

    /**
     * Verifies the preview renders the template with the receipt styles.
     * Expected: the preview pane shows the resolved field values.
     * Failure: the user edits a template without seeing what will be printed.
     */
    @Test
    void shouldRenderPreviewWithFieldValues() throws Exception {
        PrintLine line = new PrintLine(
                List.of(new PrintSegment(SegmentType.TEXT, "Total: ", "DefaultStyle", false),
                        new PrintSegment(SegmentType.FIELD, "totalPrice", "DefaultStyleBold", true)), false, 0);
        PrintTemplate template = new PrintTemplate(PrintTemplateType.SALE_RECEIPT,
                List.of(new model.print.PrintBand("BODY", List.of(line))));

        view.renderPreview(template, Map.of("totalPrice", "$40.000"));

        JTextPane preview = (JTextPane) field("previewPane").get(view);
        assertThat(preview.getText()).contains("Total: $40.000");
    }

    /**
     * Verifies the status line reports the loaded template state.
     * Expected: the label shows the text set by the controller.
     * Failure: the user cannot tell whether a template is customized.
     */
    @Test
    void shouldShowStatusText() throws Exception {
        view.setStatusText("PLANTILLA PERSONALIZADA");

        assertThat(((JLabel) field("statusLabel").get(view)).getText()).isEqualTo("PLANTILLA PERSONALIZADA");
    }

    /**
     * Verifies line controls start disabled until a line is selected.
     * Expected: the segment buttons and band selector are disabled on an empty template.
     * Failure: the user can edit options that belong to no line.
     */
    @Test
    void shouldDisableLineControlsWithoutSelection() {
        view.setLineControlsEnabled(false);

        assertThat(((AbstractButton) assertField("addSegmentButton")).isEnabled()).isFalse();
    }

    /**
     * Verifies the file buttons notify the controller.
     * Expected: save, restore, delete and back buttons fire their callbacks.
     * Failure: an editor action silently does nothing.
     */
    @Test
    void shouldInvokeFileActionCallbacks() throws Exception {
        AtomicBoolean save = new AtomicBoolean(false);
        AtomicBoolean restore = new AtomicBoolean(false);
        AtomicBoolean delete = new AtomicBoolean(false);
        AtomicBoolean back = new AtomicBoolean(false);
        view.onSave(() -> save.set(true));
        view.onRestoreDefault(() -> restore.set(true));
        view.onDeleteTemplate(() -> delete.set(true));
        view.onBack(() -> back.set(true));

        click("saveButton");
        click("restoreButton");
        click("deleteTemplateButton");
        click("backButton");

        assertThat(save).isTrue();
        assertThat(restore).isTrue();
        assertThat(delete).isTrue();
        assertThat(back).isTrue();
    }

    /**
     * Verifies the line and segment buttons notify the controller.
     * Expected: every editing button fires its callback.
     * Failure: adding, duplicating, reordering or removing does nothing.
     */
    @Test
    void shouldInvokeEditingCallbacks() throws Exception {
        AtomicBoolean addLine = new AtomicBoolean(false);
        AtomicBoolean duplicateLine = new AtomicBoolean(false);
        AtomicBoolean deleteLine = new AtomicBoolean(false);
        AtomicBoolean moveUp = new AtomicBoolean(false);
        AtomicBoolean moveDown = new AtomicBoolean(false);
        AtomicBoolean addSegment = new AtomicBoolean(false);
        AtomicBoolean deleteSegment = new AtomicBoolean(false);
        view.onAddLine(() -> addLine.set(true));
        view.onDuplicateLine(() -> duplicateLine.set(true));
        view.onDeleteLine(() -> deleteLine.set(true));
        view.onMoveLine(() -> moveUp.set(true), () -> moveDown.set(true));
        view.onAddSegment(() -> addSegment.set(true), () -> deleteSegment.set(true));
        view.setLines(List.of("linea"), 0);
        view.setLineControlsEnabled(true);

        click("addLineButton");
        click("duplicateLineButton");
        click("deleteLineButton");
        click("moveLineUpButton");
        click("moveLineDownButton");
        click("addSegmentButton");
        click("deleteSegmentButton");

        assertThat(addLine).isTrue();
        assertThat(duplicateLine).isTrue();
        assertThat(deleteLine).isTrue();
        assertThat(moveUp).isTrue();
        assertThat(moveDown).isTrue();
        assertThat(addSegment).isTrue();
        assertThat(deleteSegment).isTrue();
    }

    /**
     * Verifies line option changes notify the controller.
     * Expected: toggling centering fires the options callback.
     * Failure: the preview and the saved template keep the old options.
     */
    @Test
    void shouldInvokeCallbackWhenLineOptionsChange() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onLineOptionsChanged(() -> invoked.set(true));
        view.setLines(List.of("linea"), 0);
        view.setLineControlsEnabled(true);

        ((AbstractButton) assertField("centeredCheckBox")).doClick();

        assertThat(invoked).isTrue();
    }

    /**
     * Verifies the preview sheet is as wide as the paper the printer declares.
     * Expected: the sheet measures the paper width in real screen pixels.
     * Failure: the preview wraps where the viewport ends instead of where the paper ends.
     */
    @Test
    void shouldSizePreviewSheetToDeclaredPaper() throws Exception {
        view.setDeclaredPaper(226.77, 841.89, true);

        assertThat(sheetWidth()).isCloseTo(226.77 * pixelsPerPoint(), within(1.0));
        assertThat(info()).contains("80").contains("declarado");
    }

    /**
     * Verifies a printer that declares nothing still gives a usable preview.
     * Expected: the roll default is used and labelled as undeclared.
     * Failure: the editor shows a zero-width sheet when no printer is configured.
     */
    @Test
    void shouldFallBackToDefaultRollWhenNothingIsDeclared() throws Exception {
        view.setDeclaredPaper(226.77, 841.89, false);

        assertThat(sheetWidth()).isGreaterThan(100);
        assertThat(info()).contains("sin declarar");
    }

    /**
     * Verifies a sheet sized paper is flagged, since it is not what a receipt is printed on.
     * Expected: the label warns when the declared width is not a thermal roll.
     * Failure: an A4 declaration makes the preview look like the receipt fits.
     */
    @Test
    void shouldWarnWhenDeclaredPaperIsNotARoll() throws Exception {
        view.setDeclaredPaper(595.2, 841.8, true);

        assertThat(info()).contains("210").contains("no es rollo");
    }

    /**
     * Verifies the zoom buttons resize the preview.
     * Expected: each click moves the zoom one step and the sheet follows.
     * Failure: the zoom buttons do nothing.
     */
    @Test
    void shouldZoomPreviewWithTheZoomButtons() throws Exception {
        view.setDeclaredPaper(226.77, 841.89, true);
        double base = sheetWidth();

        click("positiveZoomButton");

        assertThat(sheetWidth()).isCloseTo(base * 1.25, within(2.0));
        assertThat(info()).contains("ZOOM 125%");
    }

    /**
     * Verifies the zoom keeps a usable range.
     * Expected: zooming stops at 300% and at 50%.
     * Failure: the preview can be zoomed to nothing or past any readable size.
     */
    @Test
    void shouldClampZoomRange() throws Exception {
        for (int i = 0; i < 20; i++) {
            click("positiveZoomButton");
        }
        assertThat(info()).contains("ZOOM 300%");

        for (int i = 0; i < 20; i++) {
            click("negativeZoomButton");
        }
        assertThat(info()).contains("ZOOM 50%");
    }

    /**
     * Verifies zooming asks the controller to render again, which is what rescales the text.
     * Expected: the zoom buttons fire the zoom callback.
     * Failure: the sheet grows while the text keeps its size.
     */
    @Test
    void shouldInvokeZoomCallback() throws Exception {
        AtomicBoolean zoomed = new AtomicBoolean(false);
        view.onZoomChange(() -> zoomed.set(true));

        click("positiveZoomButton");

        assertThat(zoomed).isTrue();
    }

    /**
     * Verifies the paper selector lets the user compare against another roll width.
     * Expected: the offered sizes are selectable and a narrower roll narrows the sheet.
     * Failure: the preview can only show the declared paper.
     */
    @Test
    void shouldCompareAgainstAnotherPaperWidth() throws Exception {
        view.setDeclaredPaper(595.2, 841.8, true);
        double declared = sheetWidth();

        combo("paperCombo").setSelectedItem(paperOption("58"));

        assertThat(sheetWidth()).isLessThan(declared);
        assertThat(info()).contains("58").contains("manual");
    }

    /**
     * Verifies the preview reports when the receipt no longer fits in one page.
     * Expected: the page count is shown for content taller than the paper.
     * Failure: a receipt silently splits in two when printed.
     */
    @Test
    void shouldReportPageCount() throws Exception {
        view.setDeclaredPaper(226.77, 100, true);
        List<PrintLine> lines = new java.util.ArrayList<>();
        for (int i = 0; i < 40; i++) {
            lines.add(new PrintLine(List.of(new PrintSegment(SegmentType.TEXT, "Linea " + i, "DefaultStyle", false)),
                    false, 0));
        }

        view.renderPreview(new PrintTemplate(PrintTemplateType.TURN_DETAIL,
                List.of(new model.print.PrintBand("BODY", lines))), Map.of());

        assertThat(info()).contains("PÁGINAS");
    }

    /**
     * Verifies the print test button reaches the controller.
     * Expected: clicking it fires the print callback.
     * Failure: the user cannot check a template on paper before saving it.
     */
    @Test
    void shouldInvokePrintTestCallback() throws Exception {
        AtomicBoolean printed = new AtomicBoolean(false);
        view.onPrintTest(() -> printed.set(true));

        click("printTestButton");

        assertThat(printed).isTrue();
    }

    /**
     * Verifies every action button carries its icon.
     * Expected: each icon resource resolves to a real image.
     * Failure: a renamed or missing PNG silently leaves a blank button.
     */
    @Test
    void shouldShowIconsOnActionButtons() throws Exception {
        for (String button : List.of("addLineButton", "duplicateLineButton", "deleteLineButton", "moveLineUpButton",
                "moveLineDownButton", "addSegmentButton", "deleteSegmentButton", "printTestButton",
                "negativeZoomButton", "positiveZoomButton", "saveButton", "restoreButton",
                "deleteTemplateButton", "backButton")) {
            assertThat(((AbstractButton) field(button).get(view)).getIcon()).as(button).isNotNull();
            assertThat(((AbstractButton) field(button).get(view)).getIcon().getIconWidth()).as(button).isPositive();
        }
    }

    private double sheetWidth() throws Exception {
        return ((JComponent) field("paperPanel").get(view)).getPreferredSize().getWidth();
    }

    private String info() throws Exception {
        return ((JLabel) field("previewInfoLabel").get(view)).getText();
    }

    private String paperOption(String prefix) throws Exception {
        JComboBox<?> paperCombo = (JComboBox<?>) field("paperCombo").get(view);
        for (int i = 0; i < paperCombo.getItemCount(); i++) {
            String item = String.valueOf(paperCombo.getItemAt(i));
            if (item.startsWith(prefix)) {
                return item;
            }
        }
        throw new AssertionError("No hay opción de papel que empiece por " + prefix);
    }

    /** Screen pixels per typographic point; the factor that makes the preview physically true. */
    private static double pixelsPerPoint() {
        return java.awt.Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
    }

    private Object assertField(String name) {
        try {
            return field(name).get(view);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("No se pudo leer el componente " + name, e);
        }
    }

    private void click(String fieldName) throws Exception {
        ((AbstractButton) field(fieldName).get(view)).doClick();
    }

    @SuppressWarnings("unchecked")
    private JComboBox<PrintTemplateType> combo(String fieldName) throws Exception {
        return (JComboBox<PrintTemplateType>) field(fieldName).get(view);
    }

    @SuppressWarnings("unchecked")
    private JList<String> list(String fieldName) throws Exception {
        return (JList<String>) field(fieldName).get(view);
    }

    private JTable table(String fieldName) throws Exception {
        return (JTable) field(fieldName).get(view);
    }

    private Field field(String name) throws NoSuchFieldException {
        Field declared = TemplateEditorView.class.getDeclaredField(name);
        declared.setAccessible(true);
        return declared;
    }
}
