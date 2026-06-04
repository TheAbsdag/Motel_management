package controller.sub;

import java.util.List;
import model.modelManagers.MotelManagement;
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

    private final MotelManagement motelManager;
    private final HistoryView historyView;
    private final Runnable onBack;
    private boolean isListAdjusting = false;
    private List<TurnHistoryData> cachedHistory;

    /**
     * @param historyService the history service
     * @param historyView  the history view panel
     * @param onBack       callback to return to management options view
     */
    public HistoryController(MotelManagement motelManager, HistoryView historyView, Runnable onBack) {
        this.motelManager = motelManager;
        this.historyView = historyView;
        this.onBack = onBack;
    }

    /** Registers action listeners for the history view. */
    public void initListeners() {
        historyView.onBackButton(() -> onBack.run());
        historyView.onTurnDetailsButton(() -> turnHistoryDetails());
        historyView.getTurnDetailsView().onBackButton(() -> historyView.showPopupTurn(false));

        // Print checkbox listeners for history turn details
        historyView.getTurnDetailsView().setupPrintCheckboxes();

        historyView.getTurnDetailsView().onPrintButton(() -> printHistoryTurn());

        // Table scrolling (touch-friendly)
        historyView.onUpButton(() -> historyView.scrollTurnHistoryTable(-1));
        historyView.onDownButton(() -> historyView.scrollTurnHistoryTable(1));

        // Turn history table selection listener
        historyView.onTurnHistorySelection(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = historyView.getSelectedTurnRow();
                if (selectedRow != -1 && !isListAdjusting) {
                    isListAdjusting = true;
                    TurnHistoryData selected = cachedHistory.get(selectedRow);
                    historyView.setSelectedTurnInfo(selected.getStartString(),
                            selected.getEndString(), selected.getStartDate(),
                            selected.getDuration());
                    historyView.setTurnDetailsEnabled(true);
                    isListAdjusting = false;
                }
            }
        });
    }

    /** Opens the history view with turn history data. */
    public void openView() {
        cachedHistory = motelManager.getTurnHistoryDataList();
        historyView.setTurnHistoryDetails(cachedHistory);
        historyView.setTurnDetailsEnabled(false);
    }

    /** Opens the popup with detailed activity for the selected historical turn. */
    public void turnHistoryDetails() {
        int selectedRow = historyView.getSelectedTurnRow();
        TurnHistoryData selected = cachedHistory.get(selectedRow);
        historyView.getTurnDetailsView().setTurnDetailsData(selected);
        historyView.showPopupTurn(true);
        historyView.getTurnDetailsView().setPrintEnabled(false);
    }

    /** Prints the selected historical turn report. */
    public void printHistoryTurn() {
        int selectedRow = historyView.getSelectedTurnRow();
        if (historyView.getTurnDetailsView().isNoPrintSelected()) {
            motelManager.turnHistoryPrint(1, selectedRow);
        } else if (historyView.getTurnDetailsView().isSummarizedPrintSelected()) {
            motelManager.turnHistoryPrint(2, selectedRow);
        } else if (historyView.getTurnDetailsView().isDetailedPrintSelected()) {
            motelManager.turnHistoryPrint(3, selectedRow);
        }
    }
}
