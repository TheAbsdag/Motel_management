package controller.sub;

import java.util.List;
import model.modelManagers.IHistoryService;
import model.dto.TurnHistoryData;
import view.HistoryView;

/**
 * Controls the turn history viewing and printing operations.
 *
 * <p>Handles:
 * <ul>
 *   <li>Displaying historical turn records</li>
 *   <li>Viewing detailed activity for a selected historical turn</li>
 *   <li>Printing historical turn reports</li>
 * </ul>
 */
public class HistoryController {

    private final IHistoryService historyService;
    private final HistoryView historyView;
    private final Runnable onBack;
    private boolean isListAdjusting = false;
    private List<TurnHistoryData> cachedHistory;

    /**
     * @param historyService the history service
     * @param historyView  the history view panel
     * @param onBack       callback to return to management options view
     */
    public HistoryController(IHistoryService historyService, HistoryView historyView, Runnable onBack) {
        this.historyService = historyService;
        this.historyView = historyView;
        this.onBack = onBack;
    }

    /** Registers action listeners for the history view. */
    public void initListeners() {
        historyView.getBackButton().addActionListener(e -> onBack.run());
        historyView.getTurnDetailsButton().addActionListener(e -> turnHistoryDetails());
        historyView.getTurnDetailsView().getBackButton().addActionListener(e -> closeHistoryDetails());

        // Print checkbox listeners for history turn details (shared utility)
        var printListener = ControllerUtils.createPrintCheckboxListener(
                historyView.getTurnDetailsView().getNoPrintCheckBox(),
                historyView.getTurnDetailsView().getSummarizedPrintCheckBox(),
                historyView.getTurnDetailsView().getDetailedPrintCheckBox(),
                historyView.getTurnDetailsView().getPrintButton(),
                null);
        historyView.getTurnDetailsView().getNoPrintCheckBox().addItemListener(printListener);
        historyView.getTurnDetailsView().getSummarizedPrintCheckBox().addItemListener(printListener);
        historyView.getTurnDetailsView().getDetailedPrintCheckBox().addItemListener(printListener);

        historyView.getTurnDetailsView().getPrintButton().addActionListener(e -> printHistoryTurn());

        // Table scrolling (touch-friendly, shared utility)
        historyView.getUpButton().addActionListener(e ->
                ControllerUtils.scrollTable(historyView.getTurnHistoryTable(), -1));
        historyView.getDownButton().addActionListener(e ->
                ControllerUtils.scrollTable(historyView.getTurnHistoryTable(), 1));

        // Turn history table selection listener
        historyView.getTurnHistoryTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = historyView.getTurnHistoryTable().getSelectedRow();
                if (selectedRow != -1 && !isListAdjusting) {
                    isListAdjusting = true;
                    TurnHistoryData selected = cachedHistory.get(selectedRow);
                    historyView.getTurnStartLabel().setText(selected.getStartString());
                    historyView.getTurnEndLabel().setText(selected.getEndString());
                    historyView.getTurnDateLabel().setText(selected.getStartDate());
                    historyView.getDurationLabel().setText(selected.getDuration());
                    historyView.getTurnDetailsButton().setEnabled(true);
                    isListAdjusting = false;
                }
            }
        });
    }

    /** Opens the history view with turn history data. */
    public void openView() {
        cachedHistory = historyService.getTurnHistoryDataList();
        historyView.setTurnHistoryDetails(cachedHistory);
        historyView.getTurnDetailsButton().setEnabled(false);
    }

    /** Opens the popup with detailed activity for the selected historical turn. */
    public void turnHistoryDetails() {
        int selectedRow = historyView.getTurnHistoryTable().getSelectedRow();
        TurnHistoryData selected = cachedHistory.get(selectedRow);
        historyView.getTurnDetailsView().setTurnDetailsData(selected);
        historyView.getPopupTurn().setVisible(true);
        historyView.getTurnDetailsView().getPrintButton().setEnabled(false);
    }

    /** Closes the history details popup. */
    public void closeHistoryDetails() {
        historyView.getPopupTurn().setVisible(false);
    }

    /** Prints the selected historical turn report. */
    public void printHistoryTurn() {
        int selectedRow = historyView.getTurnHistoryTable().getSelectedRow();
        if (historyView.getTurnDetailsView().getNoPrintCheckBox().isSelected()) {
            historyService.turnHistoryPrint(1, selectedRow);
        } else if (historyView.getTurnDetailsView().getSummarizedPrintCheckBox().isSelected()) {
            historyService.turnHistoryPrint(2, selectedRow);
        } else if (historyView.getTurnDetailsView().getDetailedPrintCheckBox().isSelected()) {
            historyService.turnHistoryPrint(3, selectedRow);
        }
    }}
