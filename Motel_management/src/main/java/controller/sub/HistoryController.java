package controller.sub;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import model.MotelManagement;
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
     * @param motelManager the model
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
        historyView.getBackButton().addActionListener(e -> onBack.run());
        historyView.getTurnDetailsButton().addActionListener(e -> turnHistoryDetails());
        historyView.getTurnDetailsView().getBackButton().addActionListener(e -> closeHistoryDetails());

        // Print checkbox listeners for history turn details
        historyView.getTurnDetailsView().getNoPrintCheckBox().addItemListener(new PrintCheckboxListener(historyView));
        historyView.getTurnDetailsView().getSummarizedPrintCheckBox().addItemListener(new PrintCheckboxListener(historyView));
        historyView.getTurnDetailsView().getDetailedPrintCheckBox().addItemListener(new PrintCheckboxListener(historyView));

        historyView.getTurnDetailsView().getPrintButton().addActionListener(e -> printHistoryTurn());

        // Table scrolling (touch-friendly, replaces Robot simulation)
        historyView.getUpButton().addActionListener(e -> scrollTable(historyView.getTurnHistoryTable(), -1));
        historyView.getDownButton().addActionListener(e -> scrollTable(historyView.getTurnHistoryTable(), 1));

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
        cachedHistory = motelManager.getTurnHistoryDataList();
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
            motelManager.turnHistoryPrint(1, selectedRow);
        } else if (historyView.getTurnDetailsView().getSummarizedPrintCheckBox().isSelected()) {
            motelManager.turnHistoryPrint(2, selectedRow);
        } else if (historyView.getTurnDetailsView().getDetailedPrintCheckBox().isSelected()) {
            motelManager.turnHistoryPrint(3, selectedRow);
        }
    }

    // ========== Table Scrolling (Touch-Friendly) ==========

    /** Scrolls the table by one row in the given direction. */
    private void scrollTable(JTable table, int direction) {
        int currentRow = table.getSelectedRow();
        int targetRow = Math.max(0, Math.min(currentRow + direction, table.getRowCount() - 1));
        if (targetRow >= 0) {
            table.setRowSelectionInterval(targetRow, targetRow);
            table.scrollRectToVisible(table.getCellRect(targetRow, 0, true));
        }
    }

    // ========== Print Checkbox Listener ==========

    /**
     * Manages the mutually exclusive print checkboxes in the HistoryView's
     * TurnDetailsView (embedded popup).
     */
    private static class PrintCheckboxListener implements ItemListener {
        private final HistoryView view;

        PrintCheckboxListener(HistoryView view) {
            this.view = view;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            JCheckBox selected = (JCheckBox) e.getSource();
            if (selected.isSelected()) {
                view.getTurnDetailsView().getPrintButton().setEnabled(true);
                if (selected != view.getTurnDetailsView().getNoPrintCheckBox())
                    view.getTurnDetailsView().getNoPrintCheckBox().setSelected(false);
                if (selected != view.getTurnDetailsView().getSummarizedPrintCheckBox())
                    view.getTurnDetailsView().getSummarizedPrintCheckBox().setSelected(false);
                if (selected != view.getTurnDetailsView().getDetailedPrintCheckBox())
                    view.getTurnDetailsView().getDetailedPrintCheckBox().setSelected(false);
            }
        }
    }
}
