package controller.sub;

import java.util.List;
import model.modelManagers.MotelManagement;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import model.turn.ActivityType;
import model.turn.TurnDetails;
import view.TurnManagerView;
import view.UserGUI;
import view.helpers.DialogHelper;

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
    private final Runnable saveMainFiles;
    private final Runnable saveBackupFilesTransaction;
    private boolean isListAdjusting = false;

    /**
     * @param motelManager the model
     * @param turnManagerView the turn management view
     * @param userInterface   the main window for view switching
     * @param onBack          callback to return to management options view
     * @param saveMainFiles   callback to save main data files
     * @param saveBackupFilesTransaction callback to save backup for transaction
     */
    public TurnController(MotelManagement motelManager, TurnManagerView turnManagerView,
                          UserGUI userInterface, Runnable onBack,
                          Runnable saveMainFiles, Runnable saveBackupFilesTransaction) {
        this.motelManager = motelManager;
        this.turnManagerView = turnManagerView;
        this.userInterface = userInterface;
        this.onBack = onBack;
        this.saveMainFiles = saveMainFiles;
        this.saveBackupFilesTransaction = saveBackupFilesTransaction;
    }

    /** Registers action listeners for the turn manager view and turn select view. */
    public void initListeners() {
        // Turn select buttons (registered here since TurnController manages turn start)
        userInterface.getTurnSelect().getTurn1Button().addActionListener(e -> startTurn(1));
        userInterface.getTurnSelect().getTurn2Button().addActionListener(e -> startTurn(2));
        userInterface.getTurnSelect().getTurn3Button().addActionListener(e -> startTurn(3));

        // Turn management buttons
        turnManagerView.getBackButton().addActionListener(e -> onBack.run());
        turnManagerView.getPrintButton().addActionListener(e -> printCurrentTurn());
        turnManagerView.getEndTurnButton().addActionListener(e -> endTurn());

        // Print checkbox listeners (shared utility)
        var printListener = ControllerUtils.createPrintCheckboxListener(
                turnManagerView.getNoPrintCheckBox(),
                turnManagerView.getSummarizedPrintCheckBox(),
                turnManagerView.getDetailedPrintCheckBox(),
                turnManagerView.getPrintButton(),
                turnManagerView.getEndTurnButton());
        turnManagerView.getNoPrintCheckBox().addItemListener(printListener);
        turnManagerView.getSummarizedPrintCheckBox().addItemListener(printListener);
        turnManagerView.getDetailedPrintCheckBox().addItemListener(printListener);

        // Table scrolling (touch-friendly, shared utility)
        turnManagerView.getUpButton().addActionListener(e ->
                ControllerUtils.scrollTable(turnManagerView.getTurnDetailsTable(), -1));
        turnManagerView.getDownButton().addActionListener(e ->
                ControllerUtils.scrollTable(turnManagerView.getTurnDetailsTable(), 1));

        // Turn details table selection listener — enables delete for sale and room types
        // that have not already been refunded
        turnManagerView.getTurnDetailsTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = turnManagerView.getTurnDetailsTable().getSelectedRow();
                if (selectedRow != -1 && !isListAdjusting) {
                    isListAdjusting = true;
                    TurnActivityData selectedItem = turnManagerView.getCurrentSelectedItem(selectedRow);
                    String changeType = selectedItem.getChangeType();
                    boolean enabled = false;
                    if (("sale".equals(changeType) || "room".equals(changeType))
                            && !selectedItem.isRefunded()) {
                        enabled = true;
                    }
                    turnManagerView.getRefundButton().setEnabled(enabled);
                    isListAdjusting = false;
                }
            }
        });
        turnManagerView.getRefundButton().addActionListener(e -> deleteRegisterFromTurn());
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
        TurnDetails totals = motelManager.getCurrentTurnDetailedInfo();

        turnManagerView.setTurnDetailsData(activities,
                totals.getTotalRooms(), totals.getTotalItems(), totals.getTotalSales(),
                totals.getTotalRefunds(), totals.getTotalSpending(), totals.getTotalTurn(),
                totals.getTotalBankTransfers(), totals.getTotalDeposits(), totals.getTotalNet());
        turnManagerView.getNoPrintCheckBox().setSelected(false);
        turnManagerView.getSummarizedPrintCheckBox().setSelected(false);
        turnManagerView.getDetailedPrintCheckBox().setSelected(false);
        turnManagerView.getRefundButton().setEnabled(false);
        turnManagerView.getBackButton().setEnabled(true);
        userInterface.setTurnManagerView();
    }

    /**
     * Prints the current turn report WITHOUT ending the turn (mid-turn printing).
     * Options: no print, summarized, or detailed.
     */
    public void printCurrentTurn() {
        motelManager.timeInformationUpdate();
        if (turnManagerView.getNoPrintCheckBox().isSelected()) {
            motelManager.turnPrintNoEnd(1);
        } else if (turnManagerView.getSummarizedPrintCheckBox().isSelected()) {
            motelManager.turnPrintNoEnd(2);
        } else if (turnManagerView.getDetailedPrintCheckBox().isSelected()) {
            motelManager.turnPrintNoEnd(3);
        }
    }

    /**
     * Ends the current turn: asks for confirmation, ends the turn,
     * prints based on selected checkbox options, saves history,
     * clears backups, and transitions to turn select view.
     */
    public void endTurn() {
        boolean turnEndConfirmation = DialogHelper.confirmTurnEnd();
        if (!turnEndConfirmation) {
            return;
        }
        motelManager.timeInformationUpdate();
        turnManagerView.getBackButton().setEnabled(false);
        turnManagerView.getEndTurnButton().setEnabled(false);
        motelManager.turnEnded();
        if (turnManagerView.getNoPrintCheckBox().isSelected()) {
            motelManager.turnEndPrint(1);
        } else if (turnManagerView.getSummarizedPrintCheckBox().isSelected()) {
            motelManager.turnEndPrint(2);
        } else if (turnManagerView.getDetailedPrintCheckBox().isSelected()) {
            motelManager.turnEndPrint(3);
        }
        userInterface.setTurnSelect();
    }

    // ========== Turn Details ==========

    /** Refunds a room booking or item sale from the current turn by creating a refund entry. */
    public void deleteRegisterFromTurn() {
        int row = turnManagerView.getTurnDetailsTable().getSelectedRow();
        if (row == -1) return;

        TurnActivityData selectedItem = turnManagerView.getCurrentSelectedItem(row);
        String changeType = selectedItem.getChangeType();
        if (!"sale".equals(changeType) && !"room".equals(changeType)) return;
        if (selectedItem.isRefunded()) return;

        ActivityType actType = ActivityType.fromString(changeType);
        long itemID = actType == ActivityType.SALE ? selectedItem.getItemID() : 0;
        long itemQty = actType == ActivityType.SALE ? selectedItem.getQuantity() : 0;

        motelManager.refundItemSale(selectedItem.getConsecutiveTrans(), actType, itemID, itemQty);

        // Refresh the table
        List<TurnActivityData> activities = motelManager.getTurnActivityDataList();
        TurnDetails totals = motelManager.getCurrentTurnDetailedInfo();
        turnManagerView.setTurnDetailsData(activities,
                totals.getTotalRooms(), totals.getTotalItems(), totals.getTotalSales(),
                totals.getTotalRefunds(), totals.getTotalSpending(), totals.getTotalTurn(),
                totals.getTotalBankTransfers(), totals.getTotalDeposits(), totals.getTotalNet());
        turnManagerView.getRefundButton().setEnabled(false);
        saveMainFiles.run();
        saveBackupFilesTransaction.run();
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

}
