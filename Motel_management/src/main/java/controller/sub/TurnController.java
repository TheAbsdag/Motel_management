package controller.sub;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import model.MotelManagement;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import view.TurnManagerView;
import view.UserGUI;

/**
 * Controls turn management operations: start, view details, print, end, and reversals.
 *
 * <p>Handles:
 * <ul>
 *   <li>Starting a new turn (1, 2, or 3)</li>
 *   <li>Viewing turn details and summary</li>
 *   <li>Printing turn reports (summarized/detailed)</li>
 *   <li>Ending the current turn</li>
 *   <li>Reversing item sales from the current turn</li>
 * </ul>
 */
public class TurnController {

    private final MotelManagement motelManager;
    private final TurnManagerView turnManagerView;
    private final UserGUI userInterface;
    private final Runnable onBack;
    private boolean isListAdjusting = false;

    /**
     * @param motelManager the model
     * @param turnManagerView the turn management view
     * @param userInterface   the main window for view switching
     * @param onBack          callback to return to management options view
     */
    public TurnController(MotelManagement motelManager, TurnManagerView turnManagerView,
                          UserGUI userInterface, Runnable onBack) {
        this.motelManager = motelManager;
        this.turnManagerView = turnManagerView;
        this.userInterface = userInterface;
        this.onBack = onBack;
    }

    /** Registers action listeners for the turn manager view and turn select view. */
    public void initListeners() {
        // Turn select buttons (registered here since TurnController manages turn start)
        userInterface.getTurnSelectView().getTurn1Button().addActionListener(e -> startTurn(1));
        userInterface.getTurnSelectView().getTurn2Button().addActionListener(e -> startTurn(2));
        userInterface.getTurnSelectView().getTurn3Button().addActionListener(e -> startTurn(3));

        // Turn management buttons
        turnManagerView.getBackButton().addActionListener(e -> onBack.run());
        turnManagerView.getPrintButton().addActionListener(e -> printTurn());
        turnManagerView.getEndTurnButton().addActionListener(e -> turnChange());

        // Print checkbox listeners
        turnManagerView.getNoPrintCheckBox().addItemListener(new PrintCheckboxListener(turnManagerView));
        turnManagerView.getSummarizedPrintCheckBox().addItemListener(new PrintCheckboxListener(turnManagerView));
        turnManagerView.getDetailedPrintCheckBox().addItemListener(new PrintCheckboxListener(turnManagerView));

        // Table scrolling (touch-friendly, replaces Robot simulation)
        turnManagerView.getUpButton().addActionListener(e -> scrollTable(turnManagerView.getTurnDetailsTable(), -1));
        turnManagerView.getDownButton().addActionListener(e -> scrollTable(turnManagerView.getTurnDetailsTable(), 1));

        // Turn details table selection listener
        turnManagerView.getTurnDetailsTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = turnManagerView.getTurnDetailsTable().getSelectedRow();
                if (selectedRow != -1 && !isListAdjusting) {
                    isListAdjusting = true;
                    TurnActivityData selectedItem = turnManagerView.getCurrentSelectedItem(selectedRow);
                    turnManagerView.getDeleteActionButton().setEnabled("sale".equals(selectedItem.getChangeType()));
                    isListAdjusting = false;
                }
            }
        });
        turnManagerView.getDeleteActionButton().addActionListener(e -> deleteRegisterFromTurn());
        turnManagerView.getSummarizedTurnButton().addActionListener(e -> showCurrentSummarizedTurn());
        turnManagerView.getBackFromSummarizedTurn().addActionListener(e -> closeSummarizedPopup());
    }

    // ========== Turn Lifecycle ==========

    /**
     * Starts a new turn with the given number.
     * Transitions the UI to the floor view.
     */
    public void startTurn(int turnNumber) {
        motelManager.timeInformationUpdate();
        motelManager.setNewTurn(turnNumber);
        userInterface.setFloorView();
    }

    /** Opens the turn management view with current turn data. */
    public void showTurnManagement() {
        turnManagerView.getEndTurnButton().setEnabled(false);
        turnManagerView.getPrintButton().setEnabled(false);

        List<TurnActivityData> activities = motelManager.getTurnActivityDataList();
        long totalRooms = 0, totalItems = 0;
        for (TurnActivityData a : activities) {
            if ("room".equals(a.getChangeType())) totalRooms += a.getPrice();
            else if ("sale".equals(a.getChangeType())) totalItems += a.getPrice();
        }
        long totalSales = totalRooms + totalItems;

        turnManagerView.setTurnDetailsData(activities, totalRooms, totalItems, totalSales);
        turnManagerView.getNoPrintCheckBox().setSelected(false);
        turnManagerView.getSummarizedPrintCheckBox().setSelected(false);
        turnManagerView.getDetailedPrintCheckBox().setSelected(false);
        turnManagerView.getDeleteActionButton().setEnabled(false);
        turnManagerView.getBackButton().setEnabled(true);
        userInterface.setTurnManagerView();
    }

    /**
     * Prints the turn end report based on selected checkbox options.
     * Options: no print, summarized, or detailed.
     */
    public void printTurn() {
        motelManager.timeInformationUpdate();
        turnManagerView.getBackButton().setEnabled(false);
        turnManagerView.getEndTurnButton().setEnabled(true);
        motelManager.turnEnded();
        if (turnManagerView.getNoPrintCheckBox().isSelected()) {
            motelManager.turnEndPrint(1);
        } else if (turnManagerView.getSummarizedPrintCheckBox().isSelected()) {
            motelManager.turnEndPrint(2);
        } else if (turnManagerView.getDetailedPrintCheckBox().isSelected()) {
            motelManager.turnEndPrint(3);
        }
    }

    /** Transitions to turn select view after ending the current turn. */
    public void turnChange() {
        userInterface.setTurnSelectView();
    }

    // ========== Turn Details ==========

    /** Deletes a sale register from the current turn (reverses the transaction). */
    public void deleteRegisterFromTurn() {
        TurnActivityData selectedItem = turnManagerView.getCurrentSelectedItem(
                turnManagerView.getTurnDetailsTable().getSelectedRow());
        if ("sale".equals(selectedItem.getChangeType())) {
            motelManager.revertItemSale(selectedItem);
            List<TurnActivityData> activities = motelManager.getTurnActivityDataList();
            long totalRooms = 0, totalItems = 0;
            for (TurnActivityData a : activities) {
                if ("room".equals(a.getChangeType())) totalRooms += a.getPrice();
                else if ("sale".equals(a.getChangeType())) totalItems += a.getPrice();
            }
            long totalSales = totalRooms + totalItems;
            turnManagerView.setTurnDetailsData(activities, totalRooms, totalItems, totalSales);
        }
    }

    /** Shows the summarized turn popup. */
    public void showCurrentSummarizedTurn() {
        turnManagerView.updateSummarizedTurnData(motelManager.getTurnSummaryDataList());
        turnManagerView.getSummarizedPopup().setVisible(true);
    }

    /** Closes the summarized turn popup. */
    public void closeSummarizedPopup() {
        turnManagerView.getSummarizedPopup().setVisible(false);
    }

    // ========== Table Scrolling (Touch-Friendly) ==========

    /**
     * Scrolls the given table by one row.
     * Replaces the Robot-based key simulation.
     */
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
     * Manages the mutually exclusive print checkboxes (No Print / Summarized / Detailed)
     * in the TurnManagerView.
     */
    private static class PrintCheckboxListener implements ItemListener {
        private final TurnManagerView view;

        PrintCheckboxListener(TurnManagerView view) {
            this.view = view;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            JCheckBox selected = (JCheckBox) e.getSource();
            if (selected.isSelected()) {
                view.getPrintButton().setEnabled(true);
                if (selected != view.getNoPrintCheckBox()) view.getNoPrintCheckBox().setSelected(false);
                if (selected != view.getSummarizedPrintCheckBox()) view.getSummarizedPrintCheckBox().setSelected(false);
                if (selected != view.getDetailedPrintCheckBox()) view.getDetailedPrintCheckBox().setSelected(false);
            }
        }
    }
}
