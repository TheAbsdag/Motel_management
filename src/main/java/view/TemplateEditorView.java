/*
 * Created by JFormDesigner on Tue Sep 15 12:00:00 COT 2026
 */

package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.BadLocationException;
import model.print.PrintFieldRegistry;
import model.print.PrintSegment;
import model.print.PrintTemplate;
import model.print.PrintTemplateType;
import model.print.SegmentType;
import model.print.TemplateRenderer;
import net.miginfocom.swing.MigLayout;
import view.helpers.FocusHighlighter;
import view.helpers.TouchScrollHandler;

/**
 * Editor for the printable layout of receipts and turn reports.
 *
 * <p>Shows the template as a list of lines, each one built from styled segments, next to
 * a live preview rendered with the same styles the printer uses. The view keeps no
 * template state: the controller reads the edited values back through this API and
 * decides what to save.
 *
 * <p>The preview draws a sheet as wide as the paper the printer lays the receipt out to,
 * in real screen pixels, so a line that would wrap on paper wraps in the preview too.
 *
 * @author SECC
 */
public class TemplateEditorView extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(TemplateEditorView.class.getName());
    private static final List<String> DEFAULT_BANDS = List.of("HEADER", "BODY", "FOOTER");

    /** Zoom steps offered by the preview, from 50% to 300%. */
    private static final double[] ZOOM_STEPS = {0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 3.0};
    private static final int DEFAULT_ZOOM_INDEX = 2;

    /** Paper widths offered for comparison instead of the declared one. */
    private static final String PAPER_DECLARED = "Declarado";
    private static final String PAPER_58 = "58 mm";
    private static final String PAPER_80 = "80 mm";
    private static final String PAPER_LETTER = "Carta";
    private static final List<String> PAPER_OPTIONS = List.of(PAPER_DECLARED, PAPER_58, PAPER_80, PAPER_LETTER);

    private static final double POINTS_PER_MM = 72.0 / 25.4;
    private static final int DEFAULT_ROLL_WIDTH_MM = 80;
    private static final int DEFAULT_ROLL_HEIGHT_MM = 297;
    /** Declared widths above this are a sheet, not the roll a receipt is printed on. */
    private static final int MAX_ROLL_WIDTH_MM = 120;
    private static final Color WARNING_COLOR = new Color(0xC6, 0x28, 0x28);

    private final SegmentTableModel segmentTableModel = new SegmentTableModel();
    private boolean populating;
    private PrintTemplateType currentType = PrintTemplateType.ROOM_RECEIPT;

    private int zoomIndex = DEFAULT_ZOOM_INDEX;
    private double declaredPaperWidthPoints = DEFAULT_ROLL_WIDTH_MM * POINTS_PER_MM;
    private double declaredPaperHeightPoints = DEFAULT_ROLL_HEIGHT_MM * POINTS_PER_MM;
    private boolean declaredPaper;
    private Color infoForeground;
    /**
     * Sheet the preview is drawn on. Built in code because JFormDesigner cannot nest a
     * container that has children inside a {@link JScrollPane}: a preview pane given straight
     * to the viewport would be stretched to it, and the receipt would wrap where the window
     * ends instead of where the paper ends.
     */
    private JPanel paperPanel;

    private Runnable templateTypeAction;
    private Runnable lineSelectedAction;
    private Runnable segmentsEditedAction;
    private Runnable lineOptionsAction;
    private Runnable addLineAction;
    private Runnable duplicateLineAction;
    private Runnable deleteLineAction;
    private Runnable moveLineUpAction;
    private Runnable moveLineDownAction;
    private Runnable addSegmentAction;
    private Runnable deleteSegmentAction;
    private Runnable saveAction;
    private Runnable restoreDefaultAction;
    private Runnable deleteTemplateAction;
    private Runnable backAction;
    private Runnable zoomAction;
    private Runnable printTestAction;

    public TemplateEditorView() {
        initComponents();
        initCustomComponents();
    }

    private void initCustomComponents() {
        paperPanel = new JPanel(new MigLayout("insets 0"));
        paperPanel.setBackground(Color.WHITE);
        scrollPanePreview.setViewportView(paperPanel);
        paperPanel.add(previewPane, "grow");

        scrollPaneLines.setPreferredSize(new java.awt.Dimension(360, 150));
        scrollPaneSegments.setPreferredSize(new java.awt.Dimension(360, 140));
        scrollPanePreview.setPreferredSize(new java.awt.Dimension(320, 420));
        // Grey around the sheet, so the paper edge is visible where the text wraps.
        scrollPanePreview.getViewport().setBackground(new Color(0xE4, 0xE4, 0xE4));
        typeComboBox.setModel(new DefaultComboBoxModel<>(PrintTemplateType.values()));
        typeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof PrintTemplateType type ? type.displayName() : "");
                return this;
            }
        });

        paperCombo.setModel(new DefaultComboBoxModel<>(PAPER_OPTIONS.toArray(new String[0])));
        infoForeground = previewInfoLabel.getForeground();
        bandComboBox.setModel(new DefaultComboBoxModel<>(DEFAULT_BANDS.toArray(new String[0])));
        segmentsTable.setModel(segmentTableModel);
        configureSegmentTable();
        TemplateRenderer.applyReceiptStyles(previewPane.getStyledDocument());
        setLineControlsEnabled(false);
        updatePreviewSheet();

        FocusHighlighter.applyToAll(this);
        TouchScrollHandler.attach(scrollPaneLines);
        TouchScrollHandler.attach(scrollPaneSegments);
        TouchScrollHandler.attach(scrollPanePreview);
        wireListeners();
    }

    private void configureSegmentTable() {
        segmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        segmentsTable.setRowHeight(26);
        segmentsTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JComboBox<>(SegmentType.values())));
        segmentsTable.getColumnModel().getColumn(1).setCellEditor(new SegmentValueEditor());
        segmentsTable.getColumnModel().getColumn(2).setCellEditor(
                new DefaultCellEditor(new JComboBox<>(TemplateRenderer.RECEIPT_STYLES.toArray(new String[0]))));
        segmentsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        segmentsTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        segmentsTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        segmentsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
    }

    private void wireListeners() {
        typeComboBox.addActionListener(event -> {
            PrintTemplateType selected = getSelectedTemplateType();
            if (selected != null && selected != currentType) {
                currentType = selected;
                fire(templateTypeAction);
            }
        });
        linesList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fire(lineSelectedAction);
            }
        });
        bandComboBox.addActionListener(event -> fire(lineOptionsAction));
        centeredCheckBox.addActionListener(event -> fire(lineOptionsAction));
        spacersSpinner.addChangeListener(event -> fire(lineOptionsAction));
        segmentTableModel.addTableModelListener(event -> fire(segmentsEditedAction));

        addLineButton.addActionListener(event -> fire(addLineAction));
        duplicateLineButton.addActionListener(event -> fire(duplicateLineAction));
        deleteLineButton.addActionListener(event -> fire(deleteLineAction));
        moveLineUpButton.addActionListener(event -> fire(moveLineUpAction));
        moveLineDownButton.addActionListener(event -> fire(moveLineDownAction));
        addSegmentButton.addActionListener(event -> fire(addSegmentAction));
        deleteSegmentButton.addActionListener(event -> fire(deleteSegmentAction));
        saveButton.addActionListener(event -> fire(saveAction));
        restoreButton.addActionListener(event -> fire(restoreDefaultAction));
        deleteTemplateButton.addActionListener(event -> fire(deleteTemplateAction));
        backButton.addActionListener(event -> fire(backAction));
        printTestButton.addActionListener(event -> fire(printTestAction));

        negativeZoomButton.addActionListener(event -> changeZoom(-1));
        positiveZoomButton.addActionListener(event -> changeZoom(1));
        paperCombo.addActionListener(event -> {
            if (!populating) {
                updatePreviewSheet();
            }
        });
    }

    /** Runs a callback unless the view is being populated programmatically. */
    private void fire(Runnable action) {
        if (action != null && !populating) {
            action.run();
        }
    }

    // ========== Template type ==========

    /** @return the template type currently selected in the editor */
    public PrintTemplateType getSelectedTemplateType() {
        return (PrintTemplateType) typeComboBox.getSelectedItem();
    }

    /** Selects a template type without notifying the change callback. */
    public void setSelectedTemplateType(PrintTemplateType type) {
        populating = true;
        try {
            currentType = type;
            typeComboBox.setSelectedItem(type);
        } finally {
            populating = false;
        }
    }

    /** Registers the callback fired when the user picks another template type. */
    public void onTemplateTypeChange(Runnable action) {
        this.templateTypeAction = action;
    }

    // ========== Status and preview ==========

    /** Sets the status line describing the loaded template. */
    public void setStatusText(String text) {
        statusLabel.setText(text);
    }

    /** Renders the given template into the preview pane using the provided field values. */
    public void renderPreview(PrintTemplate template, Map<String, String> data) {
        if (template == null) {
            return;
        }
        try {
            previewPane.setText("");
            TemplateRenderer.applyReceiptStyles(previewPane.getStyledDocument(), previewZoom());
            TemplateRenderer.render(template, data, previewPane.getStyledDocument());
        } catch (BadLocationException e) {
            LOGGER.log(Level.WARNING, "No se pudo generar la vista previa de la plantilla", e);
        }
        updatePreviewSheet();
    }

    // ========== Preview sheet ==========

    /**
     * Sets the paper the configured printer lays the document out to.
     *
     * @param widthPoints  declared width in points
     * @param heightPoints declared height in points
     * @param declared     whether the printer declared the size; {@code false} means the
     *                     80 mm roll default is being shown
     */
    public void setDeclaredPaper(double widthPoints, double heightPoints, boolean declared) {
        this.declaredPaperWidthPoints = widthPoints;
        this.declaredPaperHeightPoints = heightPoints;
        this.declaredPaper = declared;
        updatePreviewSheet();
    }

    /** @return the zoom currently applied to the preview */
    public double previewZoom() {
        return ZOOM_STEPS[zoomIndex];
    }

    private void changeZoom(int direction) {
        int next = Math.max(0, Math.min(ZOOM_STEPS.length - 1, zoomIndex + direction));
        if (next == zoomIndex) {
            return;
        }
        zoomIndex = next;
        fire(zoomAction);
        updatePreviewSheet();
    }

    /**
     * Resizes the sheet to the paper width and re-measures how tall the content is at that
     * width, which is what the printed receipt will do.
     */
    private void updatePreviewSheet() {
        int sheetWidth = pixels(effectivePaperWidthPoints());
        int pageHeight = Math.max(1, pixels(effectivePaperHeightPoints()));

        previewPane.setPreferredSize(null);
        previewPane.setSize(new Dimension(sheetWidth, Integer.MAX_VALUE));
        int contentHeight = Math.max(1, previewPane.getPreferredSize().height);

        paperPanel.setPreferredSize(new Dimension(sheetWidth, Math.max(pageHeight, contentHeight)));
        paperPanel.revalidate();
        paperPanel.repaint();
        updatePreviewInfo(pageHeight, contentHeight);
    }

    private void updatePreviewInfo(int pageHeight, int contentHeight) {
        int pages = Math.max(1, (int) Math.ceil(contentHeight / (double) pageHeight));
        previewInfoLabel.setText(String.format("PAPEL %d\u00d7%d mm (%s) \u00b7 %s \u00b7 ZOOM %d%%",
                millimetres(effectivePaperWidthPoints()), millimetres(effectivePaperHeightPoints()),
                paperOrigin(), pages == 1 ? "1 P\u00c1GINA" : pages + " P\u00c1GINAS - NO CABE",
                Math.round(previewZoom() * 100)));
        previewInfoLabel.setForeground(pages == 1 ? infoForeground : WARNING_COLOR);
    }

    private String paperOrigin() {
        Object selected = paperCombo.getSelectedItem();
        if (PAPER_DECLARED.equals(selected) || selected == null) {
            if (!declaredPaper) {
                return "sin declarar: " + DEFAULT_ROLL_WIDTH_MM + " mm";
            }
            return looksLikeRoll() ? "declarado" : "declarado, no es rollo";
        }
        return "manual";
    }

    private boolean looksLikeRoll() {
        return declaredPaperWidthPoints <= MAX_ROLL_WIDTH_MM * POINTS_PER_MM;
    }

    private double effectivePaperWidthPoints() {
        Object selected = paperCombo.getSelectedItem();
        if (PAPER_58.equals(selected)) {
            return 58 * POINTS_PER_MM;
        }
        if (PAPER_80.equals(selected)) {
            return DEFAULT_ROLL_WIDTH_MM * POINTS_PER_MM;
        }
        if (PAPER_LETTER.equals(selected)) {
            return 8.5 * 72;
        }
        return declaredPaperWidthPoints;
    }

    private double effectivePaperHeightPoints() {
        return PAPER_LETTER.equals(paperCombo.getSelectedItem()) ? 11 * 72 : declaredPaperHeightPoints;
    }

    /** Converts points to screen pixels, so the sheet has the physical size of the paper. */
    private int pixels(double points) {
        double dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        double pixelsPerPoint = dpi > 0 ? dpi / 72.0 : 1.0;
        return (int) Math.round(points * pixelsPerPoint * previewZoom());
    }

    private static int millimetres(double points) {
        return (int) Math.round(points / POINTS_PER_MM);
    }

    /** Registers the callback fired when the zoom changes, so the text is re-rendered. */
    public void onZoomChange(Runnable action) {
        this.zoomAction = action;
    }

    /** Registers the callback of the test print button. */
    public void onPrintTest(Runnable action) {
        this.printTestAction = action;
    }

    // ========== Lines ==========

    /** Replaces the line list, selecting the given index when it is valid. */
    public void setLines(List<String> labels, int selectedIndex) {
        populating = true;
        try {
            DefaultListModel<String> model = new DefaultListModel<>();
            labels.forEach(model::addElement);
            linesList.setModel(model);
            if (selectedIndex >= 0 && selectedIndex < labels.size()) {
                linesList.setSelectedIndex(selectedIndex);
            } else {
                linesList.clearSelection();
            }
        } finally {
            populating = false;
        }
    }

    /** @return the selected line index, or {@code -1} when nothing is selected */
    public int getSelectedLineIndex() {
        return linesList.getSelectedIndex();
    }

    /** Registers the callback fired when the selected line changes. */
    public void onLineSelected(Runnable action) {
        this.lineSelectedAction = action;
    }

    /** Registers the callbacks of the line buttons. */
    public void onAddLine(Runnable action) {
        this.addLineAction = action;
    }

    /** Registers the callback of the duplicate line button. */
    public void onDuplicateLine(Runnable action) {
        this.duplicateLineAction = action;
    }

    /** Registers the callback of the delete line button. */
    public void onDeleteLine(Runnable action) {
        this.deleteLineAction = action;
    }

    /** Registers the callbacks used to reorder lines. */
    public void onMoveLine(Runnable moveUp, Runnable moveDown) {
        this.moveLineUpAction = moveUp;
        this.moveLineDownAction = moveDown;
    }

    // ========== Line options ==========

    /** Enables the controls that act on a line. */
    public void setLineControlsEnabled(boolean enabled) {
        bandComboBox.setEnabled(enabled);
        centeredCheckBox.setEnabled(enabled);
        spacersSpinner.setEnabled(enabled);
        segmentsTable.setEnabled(enabled);
        addSegmentButton.setEnabled(enabled);
        deleteSegmentButton.setEnabled(enabled);
        duplicateLineButton.setEnabled(enabled);
        deleteLineButton.setEnabled(enabled);
        moveLineUpButton.setEnabled(enabled);
        moveLineDownButton.setEnabled(enabled);
    }

    /** Applies the options of the selected line without notifying the change callback. */
    public void setLineOptions(String band, boolean centered, int spacerAfter) {
        populating = true;
        try {
            if (!DEFAULT_BANDS.contains(band)) {
                bandComboBox.addItem(band);
            }
            bandComboBox.setSelectedItem(band);
            centeredCheckBox.setSelected(centered);
            spacersSpinner.setValue(spacerAfter);
        } finally {
            populating = false;
        }
    }

    /** @return the band the selected line belongs to */
    public String getSelectedBand() {
        Object selected = bandComboBox.getSelectedItem();
        return selected != null ? selected.toString() : DEFAULT_BANDS.get(1);
    }

    /** @return whether the selected line is centered */
    public boolean isLineCentered() {
        return centeredCheckBox.isSelected();
    }

    /** @return the number of spacer rows configured for the selected line */
    public int getSpacerAfter() {
        return (Integer) spacersSpinner.getValue();
    }

    /** Registers the callback fired when a line option changes. */
    public void onLineOptionsChanged(Runnable action) {
        this.lineOptionsAction = action;
    }

    // ========== Segments ==========

    /** Replaces the segment table content, selecting the given row when it is valid. */
    public void setSegments(List<PrintSegment> segments, int selectedIndex) {
        populating = true;
        try {
            segmentTableModel.setSegments(segments);
            if (selectedIndex >= 0 && selectedIndex < segments.size()) {
                segmentsTable.setRowSelectionInterval(selectedIndex, selectedIndex);
            } else {
                segmentsTable.clearSelection();
            }
        } finally {
            populating = false;
        }
    }

    /** @return the segments currently shown in the table */
    public List<PrintSegment> getSegments() {
        return segmentTableModel.getSegments();
    }

    /** @return the selected segment index, or {@code -1} when nothing is selected */
    public int getSelectedSegmentIndex() {
        return segmentsTable.getSelectedRow();
    }

    /** Registers the callbacks of the segment buttons. */
    public void onAddSegment(Runnable addAction, Runnable deleteAction) {
        this.addSegmentAction = addAction;
        this.deleteSegmentAction = deleteAction;
    }

    /** Registers the callback fired when a segment is edited in the table. */
    public void onSegmentsEdited(Runnable action) {
        this.segmentsEditedAction = action;
    }

    // ========== File actions ==========

    /** Registers the callback of the save button. */
    public void onSave(Runnable action) {
        this.saveAction = action;
    }

    /** Registers the callback of the restore default button. */
    public void onRestoreDefault(Runnable action) {
        this.restoreDefaultAction = action;
    }

    /** Registers the callback of the delete template button. */
    public void onDeleteTemplate(Runnable action) {
        this.deleteTemplateAction = action;
    }

    /** Registers the callback of the back button. */
    public void onBack(Runnable action) {
        this.backAction = action;
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco (SANTIAGO CASTELBLANCO)
	titleLabel = new JLabel();
	typeLabel = new JLabel();
	typeComboBox = new JComboBox();
	statusLabel = new JLabel();
	linesLabel = new JLabel();
	scrollPaneLines = new JScrollPane();
	linesList = new JList();
	addLineButton = new JButton();
	scrollPanePreview = new JScrollPane();
	previewPane = new JTextPane();
	moveLineUpButton = new JButton();
	duplicateLineButton = new JButton();
	moveLineDownButton = new JButton();
	deleteLineButton = new JButton();
	segmentsLabel = new JLabel();
	addSegmentButton = new JButton();
	scrollPaneSegments = new JScrollPane();
	segmentsTable = new JTable();
	deleteSegmentButton = new JButton();
	bandLabel = new JLabel();
	bandComboBox = new JComboBox();
	centeredCheckBox = new JCheckBox();
	printTestButton = new JButton();
	negativeZoomButton = new JButton();
	positiveZoomButton = new JButton();
	spacersLabel = new JLabel();
	spacersSpinner = new JSpinner();
	paperLabel = new JLabel();
	paperCombo = new JComboBox();
	previewInfoLabel = new JLabel();
	backButton = new JButton();
	restoreButton = new JButton();
	deleteTemplateButton = new JButton();
	saveButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[grow,fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[grow,fill]" +
	    "[fill]" +
	    "[]" +
	    "[grow]" +
	    "[]" +
	    "[fill]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- titleLabel ----
	titleLabel.setText("EDITOR DE PLANTILLAS DE IMPRESI\u00d3N");
	titleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(titleLabel, "cell 0 0 6 1,align center center,grow 0 0");

	//---- typeLabel ----
	typeLabel.setText("PLANTILLA:");
	typeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
	add(typeLabel, "cell 0 1");

	//---- typeComboBox ----
	typeComboBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
	add(typeComboBox, "cell 1 1,growy");

	//---- statusLabel ----
	statusLabel.setText("DISE\u00d1O EST\u00c1NDAR");
	statusLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(statusLabel, "cell 2 1 4 1");

	//---- linesLabel ----
	linesLabel.setText("L\u00cdNEAS:");
	linesLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
	add(linesLabel, "cell 0 2");

	//======== scrollPaneLines ========
	{

	    //---- linesList ----
	    linesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    linesList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
	    scrollPaneLines.setViewportView(linesList);
	}
	add(scrollPaneLines, "cell 1 2 1 3,growy");

	//---- addLineButton ----
	addLineButton.setIcon(new ImageIcon(getClass().getResource("/images/add.png")));
	addLineButton.setText("A\u00d1ADIR");
	addLineButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(addLineButton, "cell 2 2,growy");

	//======== scrollPanePreview ========
	{

	    //---- previewPane ----
	    previewPane.setEditable(false);
	    previewPane.setFont(new Font("Calibri", Font.PLAIN, 11));
	    previewPane.setBackground(Color.WHITE);
	    scrollPanePreview.setViewportView(previewPane);
	}
	add(scrollPanePreview, "cell 3 2 3 6,growy");

	//---- moveLineUpButton ----
	moveLineUpButton.setIcon(new ImageIcon(getClass().getResource("/images/arrow-up.png")));
	moveLineUpButton.setText("SUBIR");
	moveLineUpButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(moveLineUpButton, "cell 0 3,growy");

	//---- duplicateLineButton ----
	duplicateLineButton.setIcon(new ImageIcon(getClass().getResource("/images/duplicate.png")));
	duplicateLineButton.setText("DUPLICAR");
	duplicateLineButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(duplicateLineButton, "cell 2 3,growy");

	//---- moveLineDownButton ----
	moveLineDownButton.setIcon(new ImageIcon(getClass().getResource("/images/arrow-down.png")));
	moveLineDownButton.setText("BAJAR");
	moveLineDownButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(moveLineDownButton, "cell 0 4,growy");

	//---- deleteLineButton ----
	deleteLineButton.setIcon(new ImageIcon(getClass().getResource("/images/trash.png")));
	deleteLineButton.setText("ELIMINAR");
	deleteLineButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(deleteLineButton, "cell 2 4,growy");

	//---- segmentsLabel ----
	segmentsLabel.setText("SEGMENTOS:");
	segmentsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
	add(segmentsLabel, "cell 0 5");

	//---- addSegmentButton ----
	addSegmentButton.setIcon(new ImageIcon(getClass().getResource("/images/add.png")));
	addSegmentButton.setText("+ SEGMENTO");
	addSegmentButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(addSegmentButton, "cell 0 6");

	//======== scrollPaneSegments ========
	{
	    scrollPaneSegments.setViewportView(segmentsTable);
	}
	add(scrollPaneSegments, "cell 1 5 2 3,growy");

	//---- deleteSegmentButton ----
	deleteSegmentButton.setIcon(new ImageIcon(getClass().getResource("/images/minus.png")));
	deleteSegmentButton.setText("- SEGMENTO");
	deleteSegmentButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(deleteSegmentButton, "cell 0 7,growy");

	//---- bandLabel ----
	bandLabel.setText("BANDA:");
	bandLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(bandLabel, "cell 0 8");

	//---- bandComboBox ----
	bandComboBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(bandComboBox, "cell 1 8,growy");

	//---- centeredCheckBox ----
	centeredCheckBox.setText("CENTRADA");
	centeredCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(centeredCheckBox, "cell 2 8 2 1");

	//---- printTestButton ----
	printTestButton.setIcon(new ImageIcon(getClass().getResource("/images/print.png")));
	printTestButton.setText("IMPRIMIR PRUEBA");
	printTestButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(printTestButton, "cell 4 8");
	negativeZoomButton.setIcon(new ImageIcon(getClass().getResource("/images/zoom-out.png")));
	add(negativeZoomButton, "cell 5 8,growy");
	positiveZoomButton.setIcon(new ImageIcon(getClass().getResource("/images/zoom-in.png")));
	add(positiveZoomButton, "cell 5 8,growy");

	//---- spacersLabel ----
	spacersLabel.setText("ESPACIADORES:");
	spacersLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(spacersLabel, "cell 0 9");

	//---- paperLabel ----
	paperLabel.setText("PAPEL PREVIA:");
	paperLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(paperLabel, "cell 2 9,alignx right");

	//---- paperCombo ----
	paperCombo.setFont(new Font("Segoe UI Black", Font.PLAIN, 13));
	add(paperCombo, "cell 3 9,growy");

	//---- previewInfoLabel ----
	previewInfoLabel.setText("PAPEL -");
	previewInfoLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 12));
	add(previewInfoLabel, "cell 4 9 2 1");
	add(spacersSpinner, "cell 1 9,growy");

	//---- backButton ----
	backButton.setIcon(new ImageIcon(getClass().getResource("/images/back.png")));
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
	add(backButton, "cell 0 10,growy");

	//---- restoreButton ----
	restoreButton.setIcon(new ImageIcon(getClass().getResource("/images/restore.png")));
	restoreButton.setText("RESTAURAR POR DEFECTO");
	restoreButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
	add(restoreButton, "cell 1 10 2 1,growy");

	//---- deleteTemplateButton ----
	deleteTemplateButton.setIcon(new ImageIcon(getClass().getResource("/images/trash.png")));
	deleteTemplateButton.setText("BORRAR PLANTILLA");
	deleteTemplateButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
	add(deleteTemplateButton, "cell 4 10,growy");

	//---- saveButton ----
	saveButton.setIcon(new ImageIcon(getClass().getResource("/images/save.png")));
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
	add(saveButton, "cell 5 10,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco (SANTIAGO CASTELBLANCO)
    private JLabel titleLabel;
    private JLabel typeLabel;
    private JComboBox typeComboBox;
    private JLabel statusLabel;
    private JLabel linesLabel;
    private JScrollPane scrollPaneLines;
    private JList linesList;
    private JButton addLineButton;
    private JScrollPane scrollPanePreview;
    private JTextPane previewPane;
    private JButton moveLineUpButton;
    private JButton duplicateLineButton;
    private JButton moveLineDownButton;
    private JButton deleteLineButton;
    private JLabel segmentsLabel;
    private JButton addSegmentButton;
    private JScrollPane scrollPaneSegments;
    private JTable segmentsTable;
    private JButton deleteSegmentButton;
    private JLabel bandLabel;
    private JComboBox bandComboBox;
    private JCheckBox centeredCheckBox;
    private JButton printTestButton;
    private JButton negativeZoomButton;
    private JButton positiveZoomButton;
    private JLabel spacersLabel;
    private JSpinner spacersSpinner;
    private JLabel paperLabel;
    private JComboBox paperCombo;
    private JLabel previewInfoLabel;
    private JButton backButton;
    private JButton restoreButton;
    private JButton deleteTemplateButton;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * Editable table of the segments of the selected line.
     */
    private static final class SegmentTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"Tipo", "Valor / Campo", "Estilo", "Negrita"};

        private List<PrintSegment> segments = new ArrayList<>();

        void setSegments(List<PrintSegment> segments) {
            this.segments = new ArrayList<>(segments);
            fireTableDataChanged();
        }

        List<PrintSegment> getSegments() {
            return segments;
        }

        PrintSegment getSegment(int row) {
            return segments.get(row);
        }

        @Override
        public int getRowCount() {
            return segments.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> SegmentType.class;
                case 3 -> Boolean.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PrintSegment segment = segments.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> segment.type();
                case 1 -> segment.text();
                case 2 -> segment.style();
                case 3 -> segment.bold();
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            PrintSegment segment = segments.get(rowIndex);
            segments.set(rowIndex, switch (columnIndex) {
                case 0 -> new PrintSegment((SegmentType) value, segment.text(), segment.style(), segment.bold());
                case 1 -> new PrintSegment(segment.type(), toText(value), segment.style(), segment.bold());
                case 2 -> new PrintSegment(segment.type(), segment.text(), toText(value), segment.bold());
                case 3 -> new PrintSegment(segment.type(), segment.text(), segment.style(), Boolean.TRUE.equals(value));
                default -> segment;
            });
            fireTableRowsUpdated(rowIndex, rowIndex);
        }

        private static String toText(Object value) {
            if (value instanceof PrintFieldRegistry.FieldDef field) {
                return field.dataKey();
            }
            return value != null ? value.toString() : "";
        }
    }

    /**
     * Cell editor of the value column: a combo of the fields available to the current
     * template for FIELD segments, a plain text field for literal text.
     */
    private final class SegmentValueEditor extends AbstractCellEditor implements TableCellEditor {

        private final JTextField textEditor = new JTextField();
        private final JComboBox<PrintFieldRegistry.FieldDef> fieldEditor = new JComboBox<>();
        private boolean editingField;

        SegmentValueEditor() {
            fieldEditor.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setText(value instanceof PrintFieldRegistry.FieldDef field ? field.label() : "");
                    return this;
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            editingField = segmentTableModel.getSegment(row).type() == SegmentType.FIELD;
            if (!editingField) {
                textEditor.setText(value != null ? value.toString() : "");
                return textEditor;
            }
            fieldEditor.setModel(new DefaultComboBoxModel<>(availableFields()));
            fieldEditor.setSelectedItem(matchingField(value));
            return fieldEditor;
        }

        @Override
        public Object getCellEditorValue() {
            return editingField ? fieldEditor.getSelectedItem() : textEditor.getText();
        }

        private PrintFieldRegistry.FieldDef[] availableFields() {
            List<PrintFieldRegistry.FieldDef> fields = PrintFieldRegistry.getFieldsForType(currentType);
            return fields.toArray(new PrintFieldRegistry.FieldDef[0]);
        }

        private PrintFieldRegistry.FieldDef matchingField(Object value) {
            String key = value != null ? value.toString() : "";
            for (int i = 0; i < fieldEditor.getItemCount(); i++) {
                PrintFieldRegistry.FieldDef field = fieldEditor.getItemAt(i);
                if (field.dataKey().equals(key)) {
                    return field;
                }
            }
            return new PrintFieldRegistry.FieldDef(key, key, "");
        }
    }
}
