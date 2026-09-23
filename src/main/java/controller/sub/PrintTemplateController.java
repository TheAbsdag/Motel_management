package controller.sub;

import java.util.ArrayList;
import java.util.List;
import model.modelManagers.MotelManagement;
import model.modelManagers.Printer;
import model.print.PrintBand;
import model.print.PrintFieldRegistry;
import model.print.PrintLine;
import model.print.PrintSegment;
import model.print.PrintTemplate;
import model.print.PrintTemplateStore;
import model.print.PrintTemplateType;
import model.print.SegmentType;
import model.print.TemplateRenderer;
import view.TemplateEditorView;
import view.helpers.DialogHelper;

/**
 * Controls the print template editor.
 *
 * <p>Loads the template to edit, keeps the working copy while the user reorders lines and
 * edits segments, refreshes the preview on every change, and persists or discards the
 * result. A customized template is optional: while none is saved, the printer keeps using
 * its built-in layout, which is also what {@code BORRAR PLANTILLA} restores.
 */
public class PrintTemplateController {

    private static final String STATUS_CUSTOM = "PLANTILLA PERSONALIZADA";
    private static final String STATUS_DEFAULT = "DISEÑO ESTÁNDAR (sin personalizar)";
    private static final String STATUS_DIRTY = "CAMBIOS SIN GUARDAR";
    private static final String DEFAULT_BAND = "BODY";

    private final PrintTemplateStore store;
    private final MotelManagement motelManager;
    private final TemplateEditorView view;
    private final Runnable onBack;

    private final List<EditableLine> lines = new ArrayList<>();
    private PrintTemplateType currentType = PrintTemplateType.ROOM_RECEIPT;
    private int selectedLine = -1;
    private boolean dirty;

    /**
     * A template line together with the band it belongs to.
     *
     * @param band name of the band holding the line
     * @param line the printable line
     */
    private record EditableLine(String band, PrintLine line) {}

    /**
     * @param store        the template store to read and write templates
     * @param motelManager the model, used for the printer paper size and test printing
     * @param view         the editor view
     * @param onBack       callback returning to the printer configuration view
     */
    public PrintTemplateController(PrintTemplateStore store, MotelManagement motelManager,
                                   TemplateEditorView view, Runnable onBack) {
        this.store = store;
        this.motelManager = motelManager;
        this.view = view;
        this.onBack = onBack;
    }

    /** Registers every editor action. */
    public void initListeners() {
        view.onTemplateTypeChange(() -> changeType(view.getSelectedTemplateType()));
        view.onLineSelected(this::selectLine);
        view.onLineOptionsChanged(this::applyLineOptions);
        view.onSegmentsEdited(this::applySegmentsEdit);
        view.onAddLine(this::addLine);
        view.onDuplicateLine(this::duplicateLine);
        view.onDeleteLine(this::deleteLine);
        view.onMoveLine(this::moveLineUp, this::moveLineDown);
        view.onAddSegment(this::addSegment, this::deleteSegment);
        view.onSave(this::save);
        view.onRestoreDefault(this::restoreDefault);
        view.onDeleteTemplate(this::deleteTemplate);
        view.onBack(this::back);
        view.onZoomChange(this::refreshPreview);
        view.onPrintTest(this::printTest);
    }

    /** Loads the selected template type into the editor. */
    public void showEditor() {
        PrintTemplateType selected = view.getSelectedTemplateType();
        if (selected != null) {
            currentType = selected;
        }
        view.setSelectedTemplateType(currentType);
        applyPrinterPaper();
        loadTemplate(currentType);
    }

    /** Tells the preview which paper the printer lays the receipt out to. */
    private void applyPrinterPaper() {
        Printer.PaperInfo paper = motelManager.getPrinterPaperInfo();
        view.setDeclaredPaper(paper.widthPoints(), paper.heightPoints(), paper.declared());
    }

    private void changeType(PrintTemplateType type) {
        if (type == null || type == currentType) {
            return;
        }
        if (!confirmDiscard()) {
            view.setSelectedTemplateType(currentType);
            return;
        }
        currentType = type;
        loadTemplate(type);
    }

    private void loadTemplate(PrintTemplateType type) {
        PrintTemplate template = store.loadOrDefault(type);
        lines.clear();
        if (template != null) {
            for (PrintBand band : template.bands()) {
                for (PrintLine line : band.lines()) {
                    lines.add(new EditableLine(band.name(), line));
                }
            }
        }
        selectedLine = lines.isEmpty() ? -1 : 0;
        dirty = false;
        refreshLines();
        refreshSelection();
        refreshPreview();
        refreshStatus();
    }

    private void selectLine() {
        int index = view.getSelectedLineIndex();
        if (index < 0 || index == selectedLine) {
            return;
        }
        selectedLine = index;
        refreshSelection();
    }

    private void applyLineOptions() {
        if (!hasSelectedLine()) {
            return;
        }
        EditableLine editable = lines.get(selectedLine);
        PrintLine line = editable.line();
        lines.set(selectedLine, new EditableLine(view.getSelectedBand(),
                new PrintLine(line.segments(), view.isLineCentered(), view.getSpacerAfter())));
        markDirty();
        refreshLines();
        refreshPreview();
    }

    private void applySegmentsEdit() {
        if (!hasSelectedLine()) {
            return;
        }
        EditableLine editable = lines.get(selectedLine);
        PrintLine line = editable.line();
        lines.set(selectedLine, new EditableLine(editable.band(),
                new PrintLine(List.copyOf(view.getSegments()), line.centered(), line.spacerAfter())));
        markDirty();
        refreshLines();
        refreshPreview();
    }

    private void addLine() {
        int insertAt = hasSelectedLine() ? selectedLine + 1 : lines.size();
        String band = hasSelectedLine() ? lines.get(selectedLine).band() : DEFAULT_BAND;
        PrintLine line = new PrintLine(
                List.of(new PrintSegment(SegmentType.TEXT, "  ", TemplateRenderer.DEFAULT_STYLE, false)), false, 0);
        lines.add(insertAt, new EditableLine(band, line));
        selectedLine = insertAt;
        markDirty();
        refreshLines();
        refreshSelection();
        refreshPreview();
    }

    private void duplicateLine() {
        if (!hasSelectedLine()) {
            return;
        }
        lines.add(selectedLine + 1, lines.get(selectedLine));
        selectedLine++;
        markDirty();
        refreshLines();
        refreshSelection();
        refreshPreview();
    }

    private void deleteLine() {
        if (!hasSelectedLine()) {
            return;
        }
        lines.remove(selectedLine);
        if (selectedLine >= lines.size()) {
            selectedLine = lines.size() - 1;
        }
        markDirty();
        refreshLines();
        refreshSelection();
        refreshPreview();
    }

    private void moveLineUp() {
        moveLine(-1);
    }

    private void moveLineDown() {
        moveLine(1);
    }

    private void moveLine(int offset) {
        if (!hasSelectedLine()) {
            return;
        }
        int target = selectedLine + offset;
        if (target < 0 || target >= lines.size()) {
            return;
        }
        lines.add(target, lines.remove(selectedLine));
        selectedLine = target;
        markDirty();
        refreshLines();
        refreshSelection();
        refreshPreview();
    }

    private void addSegment() {
        if (!hasSelectedLine()) {
            return;
        }
        EditableLine editable = lines.get(selectedLine);
        PrintLine line = editable.line();
        List<PrintSegment> segments = new ArrayList<>(line.segments());
        segments.add(new PrintSegment(SegmentType.TEXT, " ", TemplateRenderer.DEFAULT_STYLE, false));
        lines.set(selectedLine, new EditableLine(editable.band(),
                new PrintLine(List.copyOf(segments), line.centered(), line.spacerAfter())));
        markDirty();
        refreshLines();
        view.setSegments(segments, segments.size() - 1);
        refreshPreview();
    }

    private void deleteSegment() {
        if (!hasSelectedLine()) {
            return;
        }
        int index = view.getSelectedSegmentIndex();
        if (index < 0) {
            DialogHelper.showInfoMessage("Seleccione el segmento que desea quitar", "SEGMENTOS");
            return;
        }
        EditableLine editable = lines.get(selectedLine);
        PrintLine line = editable.line();
        List<PrintSegment> segments = new ArrayList<>(line.segments());
        segments.remove(index);
        lines.set(selectedLine, new EditableLine(editable.band(),
                new PrintLine(List.copyOf(segments), line.centered(), line.spacerAfter())));
        markDirty();
        refreshLines();
        view.setSegments(segments, -1);
        refreshPreview();
    }

    private void save() {
        if (lines.isEmpty()) {
            DialogHelper.showInfoMessage("La plantilla no tiene líneas: agréguelas antes de guardar", "GUARDAR");
            return;
        }
        if (!DialogHelper.confirmDialog("¿Guardar la plantilla de " + currentType.displayName() + "?",
                "GUARDAR PLANTILLA")) {
            return;
        }
        if (!store.save(currentType, buildTemplate())) {
            DialogHelper.showInfoMessage("No se pudo guardar la plantilla. Revise el registro de la aplicación.",
                    "ERROR");
            return;
        }
        dirty = false;
        refreshStatus();
        DialogHelper.showInfoMessage("Plantilla guardada. Se usará en la próxima impresión.", "GUARDADO");
    }

    private void restoreDefault() {
        PrintTemplate fallback = store.defaultTemplate(currentType);
        if (fallback == null) {
            DialogHelper.showInfoMessage("No hay diseño por defecto disponible para esta plantilla", "ERROR");
            return;
        }
        if (!DialogHelper.confirmDialog("¿Descartar la plantilla actual y cargar el diseño por defecto?",
                "RESTAURAR")) {
            return;
        }
        applyTemplate(fallback);
        markDirty();
        refreshLines();
        refreshSelection();
        refreshPreview();
        DialogHelper.showInfoMessage("Diseño por defecto cargado. Use GUARDAR para aplicarlo.", "RESTAURAR");
    }

    private void deleteTemplate() {
        if (!store.exists(currentType)) {
            DialogHelper.showInfoMessage("Esta plantilla no está personalizada: se usa el diseño del sistema.",
                    "BORRAR PLANTILLA");
            return;
        }
        if (!DialogHelper.confirmDialog("¿Borrar la plantilla personalizada y volver al diseño del sistema?",
                "BORRAR PLANTILLA")) {
            return;
        }
        if (!store.delete(currentType)) {
            DialogHelper.showInfoMessage("No se pudo borrar la plantilla. Revise el registro de la aplicación.", "ERROR");
            return;
        }
        loadTemplate(currentType);
        DialogHelper.showInfoMessage("Plantilla borrada. Se usará el diseño del sistema.", "BORRADO");
    }

    private void back() {
        if (!confirmDiscard()) {
            return;
        }
        dirty = false;
        onBack.run();
    }

    /**
     * Prints the template being edited with sample data, through the same print job receipts
     * use. Nothing is saved: the test does not touch the turn, the cash register or the
     * printer configuration, and it prints the unsaved copy so a layout can be checked on
     * paper before saving it.
     */
    private void printTest() {
        if (lines.isEmpty()) {
            DialogHelper.showInfoMessage("La plantilla no tiene líneas: no hay nada que imprimir", "IMPRIMIR PRUEBA");
            return;
        }
        if (!motelManager.printTestTemplate(buildTemplate())) {
            DialogHelper.showErrorMessage(
                    "No hay impresora configurada. Seleccione una en CONFIGURACIÓN IMPRESORA.", "IMPRIMIR PRUEBA");
            return;
        }
        DialogHelper.showInfoMessage("Prueba enviada a " + motelManager.getCurrentPrinterName()
                + " con datos de ejemplo. No se guardó nada.", "IMPRIMIR PRUEBA");
    }

    private boolean confirmDiscard() {
        return !dirty || DialogHelper.confirmDialog("Hay cambios sin guardar. ¿Descartarlos?",
                "CAMBIOS SIN GUARDAR");
    }

    private void applyTemplate(PrintTemplate template) {
        lines.clear();
        for (PrintBand band : template.bands()) {
            for (PrintLine line : band.lines()) {
                lines.add(new EditableLine(band.name(), line));
            }
        }
        selectedLine = lines.isEmpty() ? -1 : 0;
    }

    /** Builds the template from the edited lines, grouping consecutive lines of the same band. */
    private PrintTemplate buildTemplate() {
        List<PrintBand> bands = new ArrayList<>();
        for (EditableLine editable : lines) {
            PrintBand last = bands.isEmpty() ? null : bands.get(bands.size() - 1);
            if (last != null && last.name().equals(editable.band())) {
                List<PrintLine> merged = new ArrayList<>(last.lines());
                merged.add(editable.line());
                bands.set(bands.size() - 1, new PrintBand(last.name(), merged));
            } else {
                bands.add(new PrintBand(editable.band(), List.of(editable.line())));
            }
        }
        return new PrintTemplate(currentType, bands);
    }

    private void refreshLines() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            EditableLine editable = lines.get(i);
            labels.add(String.format("%02d [%s] %s", i + 1, editable.band(),
                    TemplateRenderer.describe(editable.line())));
        }
        view.setLines(labels, selectedLine);
    }

    private void refreshSelection() {
        boolean hasLine = hasSelectedLine();
        view.setLineControlsEnabled(hasLine);
        if (!hasLine) {
            view.setSegments(List.of(), -1);
            return;
        }
        EditableLine editable = lines.get(selectedLine);
        view.setSegments(editable.line().segments(), -1);
        view.setLineOptions(editable.band(), editable.line().centered(), editable.line().spacerAfter());
    }

    private void refreshPreview() {
        view.renderPreview(buildTemplate(), PrintFieldRegistry.sampleData(currentType));
    }

    private void refreshStatus() {
        if (dirty) {
            view.setStatusText(STATUS_DIRTY);
            return;
        }
        view.setStatusText(store.exists(currentType) ? STATUS_CUSTOM : STATUS_DEFAULT);
    }

    private void markDirty() {
        dirty = true;
        refreshStatus();
    }

    private boolean hasSelectedLine() {
        return selectedLine >= 0 && selectedLine < lines.size();
    }
}
