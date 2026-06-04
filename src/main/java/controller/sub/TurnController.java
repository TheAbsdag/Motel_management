package controller.sub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.ProgramConfig;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import model.modelManagers.EmailConfigurationService;
import model.modelManagers.MotelManagement;
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
        userInterface.getTurnSelect().onTurn1Button(() -> startTurn(1));
        userInterface.getTurnSelect().onTurn2Button(() -> startTurn(2));
        userInterface.getTurnSelect().onTurn3Button(() -> startTurn(3));

        // Turn management buttons
        turnManagerView.onBackButton(() -> onBack.run());
        turnManagerView.onPrintButton(() -> printCurrentTurn());
        turnManagerView.onEndTurnButton(() -> endTurn());

        // Print checkbox listeners
        turnManagerView.setupPrintCheckboxes();

        // Table scrolling (touch-friendly)
        turnManagerView.onUpButton(() -> turnManagerView.scrollTurnDetailsTable(-1));
        turnManagerView.onDownButton(() -> turnManagerView.scrollTurnDetailsTable(1));

        // Turn details table selection listener — enables delete for sale and room types
        // that have not already been refunded
        turnManagerView.onTurnDetailsSelection(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = turnManagerView.getSelectedDetailRow();
                if (selectedRow != -1 && !isListAdjusting) {
                    isListAdjusting = true;
                    TurnActivityData selectedItem = turnManagerView.getCurrentSelectedItem(selectedRow);
                    String changeType = selectedItem.getChangeType();
                    boolean enabled = false;
                    if (("sale".equals(changeType) || "room".equals(changeType))
                            && !selectedItem.isRefunded()) {
                        enabled = true;
                    }
                    turnManagerView.setRefundEnabled(enabled);
                    isListAdjusting = false;
                }
            }
        });
        turnManagerView.onRefundButton(() -> deleteRegisterFromTurn());
        turnManagerView.onSummarizedTurn(() -> showCurrentSummarizedTurn());
        turnManagerView.onBackFromSummarizedTurn(() -> closeSummarizedPopup());
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
        turnManagerView.setEndTurnEnabled(false);
        turnManagerView.setPrintEnabled(false);

        List<TurnActivityData> activities = motelManager.getTurnActivityDataList();
        TurnDetails totals = motelManager.getCurrentTurnDetailedInfo();

        turnManagerView.setTurnDetailsData(activities,
                totals.getTotalRooms(), totals.getTotalItems(), totals.getTotalSales(),
                totals.getTotalRefunds(), totals.getTotalSpending(), totals.getTotalTurn(),
                totals.getTotalBankTransfers(), totals.getTotalDeposits(), totals.getTotalNet());
        turnManagerView.setNoPrintSelected(false);
        turnManagerView.setSummarizedPrintSelected(false);
        turnManagerView.setDetailedPrintSelected(false);
        turnManagerView.setRefundEnabled(false);
        turnManagerView.setBackEnabled(true);
        userInterface.setTurnManagerView();
    }

    /**
     * Prints the current turn report WITHOUT ending the turn (mid-turn printing).
     * Options: no print, summarized, or detailed.
     */
    public void printCurrentTurn() {
        motelManager.timeInformationUpdate();
        if (turnManagerView.isNoPrintSelected()) {
            motelManager.turnPrintNoEnd(1);
        } else if (turnManagerView.isSummarizedPrintSelected()) {
            motelManager.turnPrintNoEnd(2);
        } else if (turnManagerView.isDetailedPrintSelected()) {
            motelManager.turnPrintNoEnd(3);
        }
    }

    /**
     * Ends the current turn: asks for confirmation, ends the turn,
     * prints based on selected checkbox options, saves history,
     * clears backups, and attempts email report if configured.
     */
    public void endTurn() {
        // --- Email pre-check (walidaci\u00f3n de conexi\u00f3n antes de terminar) ---
        EmailConfigurationService emailSvc = motelManager.getEmailConfigurationService();
        if (emailSvc.isEmailEnabled() && emailSvc.validateCaseConfig(2)) {
            var smtpOpt = emailSvc.loadSmtpConfig();
            var secureOpt = emailSvc.loadSecureData();
            if (smtpOpt.isPresent() && secureOpt.isPresent()) {
                boolean connOk = emailSvc.verifyConnection(
                        smtpOpt.get(), secureOpt.get().username(), secureOpt.get().credential());
                if (!connOk) {
                    boolean proceed = DialogHelper.confirmDialog(
                            "Error de conexi\u00f3n de correo.\n\u00bfDesea continuar?",
                            "CORREO ELECTR\u00d3NICO");
                    if (!proceed) return;
                }
            }
        }

        // --- Normal turn end flow ---
        boolean turnEndConfirmation = DialogHelper.confirmTurnEnd();
        if (!turnEndConfirmation) {
            return;
        }
        motelManager.timeInformationUpdate();
        turnManagerView.setBackEnabled(false);
        turnManagerView.setEndTurnEnabled(false);
        motelManager.turnEnded();
        if (turnManagerView.isNoPrintSelected()) {
            motelManager.turnEndPrint(1);
        } else if (turnManagerView.isSummarizedPrintSelected()) {
            motelManager.turnEndPrint(2);
        } else if (turnManagerView.isDetailedPrintSelected()) {
            motelManager.turnEndPrint(3);
        }

        // --- Email send attempt ---
        attemptTurnEmail();

        userInterface.setTurnSelect();
    }

    private void attemptTurnEmail() {
        EmailConfigurationService emailSvc = motelManager.getEmailConfigurationService();
        if (!emailSvc.isEmailEnabled() || !emailSvc.validateCaseConfig(2)) return;

        TurnDetails details = motelManager.getCurrentTurnDetailedInfo();
        if (details == null) return;

        ProgramConfig cfg = motelManager.getProgramConfig();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{motelName}", cfg.getMotelName());
        placeholders.put("{motelAddress}", cfg.getMotelAddress());
        placeholders.put("{motelID}", cfg.getMotelID());
        placeholders.put("{turnNumber}", String.valueOf(details.getTurnNumber()));
        placeholders.put("{turnStart}", details.getTurnStart() != null ? details.getTurnStart().toString() : "");
        placeholders.put("{turnEnd}", details.getTurnEnd() != null ? details.getTurnEnd().toString() : "");
        placeholders.put("{totalRooms}", String.valueOf(details.getTotalRooms()));
        placeholders.put("{totalItems}", String.valueOf(details.getTotalItems()));
        placeholders.put("{totalSales}", String.valueOf(details.getTotalSales()));
        placeholders.put("{totalRefunds}", String.valueOf(details.getTotalRefunds()));
        placeholders.put("{totalSpending}", String.valueOf(details.getTotalSpending()));
        placeholders.put("{totalTurn}", String.valueOf(details.getTotalTurn()));
        placeholders.put("{totalBankTransfers}", String.valueOf(details.getTotalBankTransfers()));
        placeholders.put("{totalDeposits}", String.valueOf(details.getTotalDeposits()));
        placeholders.put("{totalNet}", String.valueOf(details.getTotalNet()));
        placeholders.put("{consecutiveTrans}", String.valueOf(motelManager.getTurnService().getConsecutiveTransaction()));
        placeholders.put("{date}", java.time.LocalDate.now().toString());

        boolean sent = emailSvc.sendCaseEmail(2, placeholders, List.of());
        if (!sent) {
            DialogHelper.showInfoMessage(
                    "Error al enviar correo de reporte de turno", "CORREO");
        }
    }

    // ========== Turn Details ==========

    /** Refunds a room booking or item sale from the current turn by creating a refund entry. */
    public void deleteRegisterFromTurn() {
        int row = turnManagerView.getSelectedDetailRow();
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
        turnManagerView.setRefundEnabled(false);
        saveMainFiles.run();
        saveBackupFilesTransaction.run();
    }

    /** Shows the summarized turn popup. */
    public void showCurrentSummarizedTurn() {
        turnManagerView.updateSummarizedTurnData(motelManager.getTurnSummaryDataList());
        turnManagerView.showSummarizedPopup(true);
    }

    /** Closes the summarized turn popup. */
    public void closeSummarizedPopup() {
        turnManagerView.showSummarizedPopup(false);
    }

}
