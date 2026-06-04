package controller.sub;

import java.awt.Window;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import model.ProgramConfig;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import model.email.config.EmailSmtpConfig;
import model.modelManagers.EmailConfigurationService;
import model.modelManagers.MotelManagement;
import model.turn.ActivityType;
import model.turn.TurnDetails;
import view.LoadingDialog;
import view.TurnManagerView;
import view.UserGUI;
import view.ViewCard;
import view.helpers.DialogHelper;
import view.helpers.FormatHelper;

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
        turnManagerView.onBackFromSummarizedTurn(() -> turnManagerView.showSummarizedPopup(false));
    }

    // ========== Turn Lifecycle ==========

    /**
     * Starts a new turn with the given number.
     * Transitions the UI to the floor view.
     */
    public void startTurn(int turnNumber) {
        motelManager.timeInformationUpdate();
        motelManager.setNewTurn(turnNumber);
userInterface.setView(ViewCard.FLOOR_VIEW);
    }

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
        userInterface.setView(ViewCard.TURN_MANAGER_VIEW);
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
     * Ends the current turn: validates email connection with loading dialog,
     * asks for confirmation, ends the turn, prints based on selected checkbox
     * options, saves history, clears backups, and attempts email report if configured.
     */
    public void endTurn() {
        EmailConfigurationService emailSvc = motelManager.getEmailConfigurationService();
        boolean emailConfigured = emailSvc.isEmailEnabled() && emailSvc.validateCaseConfig(2);
        if (emailConfigured) {
            var smtpOpt = emailSvc.loadSmtpConfig();
            var secureOpt = emailSvc.loadSecureData();
            if (smtpOpt.isPresent() && secureOpt.isPresent()) {
                Window parent = SwingUtilities.getWindowAncestor(turnManagerView);
                LoadingDialog loading = new LoadingDialog(parent, "Verificando conexi\u00f3n de correo...");
                EmailSmtpConfig smtp = smtpOpt.get();
                String username = secureOpt.get().username();
                String credential = secureOpt.get().credential();
                loading.showAsync(() -> {
                    boolean connOk = emailSvc.verifyConnection(smtp, username, credential);
                    SwingUtilities.invokeLater(() -> {
                        if (!connOk) {
                            boolean proceed = DialogHelper.confirmDialog(
                                    "Error de conexi\u00f3n de correo.\n\u00bfDesea continuar?",
                                    "CORREO ELECTR\u00d3NICO");
                            if (!proceed) return;
                        }
                        proceedWithTurnEnd(emailSvc, emailConfigured);
                    });
                });
                return;
            }
        }
        proceedWithTurnEnd(emailSvc, emailConfigured);
    }

    private void proceedWithTurnEnd(EmailConfigurationService emailSvc, boolean emailConfigured) {
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

        if (emailConfigured) {
            attemptTurnEmailWithLoading();
        }

        userInterface.setView(ViewCard.TURN_SELECT_VIEW);
    }

    private void attemptTurnEmailWithLoading() {
        Map<String, String> placeholders = buildTurnPlaceholders();
        if (placeholders == null) return;

        int turnNumber = Integer.parseInt(placeholders.getOrDefault("{turnNumber}", "0"));
        EmailConfigurationService emailSvc = motelManager.getEmailConfigurationService();
        List<String> attNames = emailSvc.loadCaseConfigs()
                .filter(cases -> 2 < cases.size())
                .map(cases -> cases.get(2).attachments())
                .orElse(List.of());
        List<Path> attachments = (attNames != null && !attNames.isEmpty())
                ? emailSvc.resolveAttachmentPaths(attNames, turnNumber)
                : List.of();

        Window parent = SwingUtilities.getWindowAncestor(turnManagerView);
        LoadingDialog loading = new LoadingDialog(parent, "Enviando correo de reporte de turno...");

        loading.showAsync(() -> {
            boolean sent = emailSvc.sendCaseEmail(2, placeholders, attachments);
            if (!sent) {
                SwingUtilities.invokeLater(() ->
                    DialogHelper.showErrorMessage(
                        "Error al enviar correo de reporte de turno", "CORREO"));
            }
        });
    }

    private Map<String, String> buildTurnPlaceholders() {
        TurnDetails details = motelManager.getCurrentTurnDetailedInfo();
        if (details == null) return null;
        ProgramConfig cfg = motelManager.getProgramConfig();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{motelName}", cfg.getMotelName());
        placeholders.put("{motelAddress}", cfg.getMotelAddress());
        placeholders.put("{motelID}", cfg.getMotelID());
        placeholders.put("{turnNumber}", String.valueOf(details.getTurnNumber()));
        placeholders.put("{turnStart}", details.getTurnStart() != null ? details.getTurnStart().toString() : "");
        placeholders.put("{turnEnd}", details.getTurnEnd() != null ? details.getTurnEnd().toString() : "");
        placeholders.put("{turnDuration}", formatDuration(details));
        placeholders.put("{totalRooms}", String.valueOf(details.getTotalRooms()));
        placeholders.put("{totalItems}", String.valueOf(details.getTotalItems()));
        placeholders.put("{totalSales}", String.valueOf(details.getTotalSales()));
        placeholders.put("{totalItemRefunds}", String.valueOf(details.getTotalItemRefunds()));
        placeholders.put("{totalRoomRefunds}", String.valueOf(details.getTotalRoomRefunds()));
        placeholders.put("{totalRefunds}", String.valueOf(details.getTotalRefunds()));
        placeholders.put("{totalSpending}", String.valueOf(details.getTotalSpending()));
        placeholders.put("{totalTurn}", String.valueOf(details.getTotalTurn()));
        placeholders.put("{totalBankTransfers}", String.valueOf(details.getTotalBankTransfers()));
        placeholders.put("{totalDeposits}", String.valueOf(details.getTotalDeposits()));
        placeholders.put("{totalNet}", String.valueOf(details.getTotalNet()));
        placeholders.put("{consecutiveTrans}", String.valueOf(motelManager.getTurnService().getConsecutiveTransaction()));
        placeholders.put("{date}", java.time.LocalDate.now().toString());
        placeholders.put("{activityTable}", buildActivityTableHtml(details));
        return placeholders;
    }

    private static String formatDuration(TurnDetails details) {
        if (details.getTurnStart() == null) return "";
        java.time.ZonedDateTime end = details.getTurnEnd() != null
                ? details.getTurnEnd() : java.time.ZonedDateTime.now();
        long seconds = java.time.Duration.between(details.getTurnStart(), end).getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    private static String buildActivityTableHtml(TurnDetails details) {
        List<model.turn.TurnActivity> activities = details.getActivities();
        if (activities == null || activities.isEmpty()) return "<p>Sin actividades</p>";

        StringBuilder table = new StringBuilder();
        table.append("<table border='1' cellpadding='4' cellspacing='0' style='border-collapse:collapse;width:100%;font-family:Segoe UI,sans-serif;font-size:12px;'>");
        table.append("<thead><tr style='background:#f0f0f0;'>");
        table.append("<th>Fecha</th><th>Habitación</th><th>Concepto</th><th>Valor</th><th>Trans</th>");
        table.append("</tr></thead><tbody>");

        for (model.turn.TurnActivity act : activities) {
            String date = act.changeDate() != null
                    ? act.changeDate().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd HH:mm"))
                    : "";
            String room = "";
            String concept = "";
            String value = "";
            String trans = String.valueOf(act.consecutiveTrans());

            if (act instanceof model.turn.RoomBookingActivity r) {
                room = r.roomString();
                concept = "Habitación";
                value = String.valueOf(r.price());
            } else if (act instanceof model.turn.SaleActivity s) {
                room = s.roomSoldTo();
                concept = "Venta (" + s.items().size() + " ítems)";
                value = String.valueOf(s.items().stream().mapToLong(model.turn.SaleItem::price).sum());
            } else if (act instanceof model.turn.RoomSwapActivity sw) {
                room = sw.originalRoom() + " → " + sw.swappedRoom();
                concept = "Cambio";
                value = "—";
            } else if (act instanceof model.turn.RefundActivity ref) {
                room = ref.refundRoom();
                concept = "Devolución " + (ref.refundType() == model.turn.RefundType.ROOM_REFUND ? "hab." : "venta");
                value = String.valueOf(ref.price());
            } else if (act instanceof model.turn.SpendingActivity sp) {
                room = "—";
                concept = "Gasto: " + sp.description();
                value = String.valueOf(sp.value());
            } else if (act instanceof model.turn.ExtraChangeActivity ec) {
                room = "—";
                concept = (ec.extraType() == model.turn.ExtraChangeType.BANK_TRANSFER ? "Transf." : "Depósito")
                        + ": " + ec.description();
                value = String.valueOf(ec.value());
            }

            String bg = (value.startsWith("-") || (act instanceof model.turn.RefundActivity))
                    ? " style='color:#c00;'" : "";
            table.append("<tr>");
            table.append("<td>").append(date).append("</td>");
            table.append("<td>").append(FormatHelper.escapeHtml(room)).append("</td>");
            table.append("<td>").append(FormatHelper.escapeHtml(concept)).append("</td>");
            table.append("<td").append(bg).append(">").append(value).append("</td>");
            table.append("<td>").append(trans).append("</td>");
            table.append("</tr>");
        }

        table.append("</tbody></table>");
        return table.toString();
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
    /**
     * Gets the current total price for selling.
     * @return the total price from the register
     */
    private long getTotalPrice() {
        return motelManager.getCurrentTotalPriceSellingList();
    }

}
