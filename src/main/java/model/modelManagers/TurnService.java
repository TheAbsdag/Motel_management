package model.modelManagers;

import model.CartItem;
import model.ProgramConfig;
import model.Room;
import model.Turn;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import model.turn.ActivityType;
import model.turn.ExtraChangeType;
import model.turn.SaleActivity;
import model.turn.TurnDetails;
import org.json.JSONObject;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Encapsulates turn lifecycle, reporting, spending, extra-changes, and refunds.
 * Coordinates between {@link Turn}, {@link Printer}, {@link ProgramConfig},
 * and {@link FileManager}.
 */
public class TurnService {

    private final Turn turn;
    private final ProgramConfig programConfig;
    private final Printer printer;
    private final FileManager files;

    public TurnService(Turn turn, ProgramConfig programConfig, Printer printer, FileManager files) {
        this.turn = turn;
        this.programConfig = programConfig;
        this.printer = printer;
        this.files = files;
    }

    public void setNewTurn(int turnNumber, Instant currentTime) {
        turn.setNewTurn(turnNumber, currentTime);
    }

    public void turnEnded(Instant currentTime) {
        turn.turnEnd(currentTime);
    }

    public long getTurnNumber() {
        return turn.getTurnNumber();
    }

    public List<TurnActivityData> getTurnActivityDataList() {
        return turn.getActivityDataList();
    }

    public TurnDetails getCurrentTurnDetailedInfo() {
        return turn.getDetailedTurnInformation();
    }

    public List<TurnSummaryItemData> getTurnSummaryDataList() {
        return turn.getSummaryDataList();
    }

    public void turnPrintNoEnd(int option) {
        TurnDetails details = turn.getDetailedTurnInformation();
        switch (option) {
            case 2 -> printer.printSummarizedCurrentTurn(details);
            case 3 -> printer.printDetailedCurrentTurn(details);
            default -> { /* no printing */ }
        }
    }

    public void turnEndPrint(int option, ZonedDateTime localizedTime) {
        TurnDetails details = turn.getDetailedTurnInformation();
        files.saveHistoryData(details.toJson(), "turn", localizedTime);
        TurnReportGenerator.generateReport(details);
        switch (option) {
            case 1 -> { printer.printSummarizedTurn(details, true);  printer.printDetailedTurn(details, true); }
            case 2 -> { printer.printSummarizedTurn(details, false); printer.printDetailedTurn(details, true); }
            case 3 -> { printer.printSummarizedTurn(details, true);  printer.printDetailedTurn(details, false); }
            default -> { /* no printing */ }
        }
        files.clearBackupFiles();
    }

    public void turnHistoryPrint(int option, TurnDetails details) {
        switch (option) {
            case 1 -> { printer.printSummarizedTurn(details, true);  printer.printDetailedTurn(details, true); }
            case 2 -> { printer.printSummarizedTurn(details, false); printer.printDetailedTurn(details, true); }
            case 3 -> { printer.printSummarizedTurn(details, true);  printer.printDetailedTurn(details, false); }
            default -> { /* no printing */ }
        }
    }

    public void addSpendingTransaction(String conceptSpending, long value, int consecutiveTrans, Instant currentTime) {
        turn.registerSpendingTransaction(conceptSpending, value * -1L, consecutiveTrans, currentTime);
    }

    public void addExtraChangeTransaction(String description, long value, ExtraChangeType changeType,
                                          int consecutiveTrans, Instant currentTime) {
        turn.registerExtraChangeTransaction(description, value * -1L, changeType, consecutiveTrans, currentTime);
    }

    public SaleActivity saveTransactionInformation(List<CartItem> sellingItems, Room roomSoldTo,
                                                    Instant currentTime, int consecutiveTrans) {
        return turn.saveTransactionInformation(sellingItems, roomSoldTo, currentTime, consecutiveTrans);
    }

    public int getConsecutiveTransaction() {
        return programConfig.getConsecutiveTransaction();
    }

    public void addConsecutiveTransaction() {
        programConfig.addConsecutiveTransaction();
    }

    public model.turn.TurnActivity findActivity(int consecutiveTrans, ActivityType changeType) {
        return turn.findActivity(consecutiveTrans, changeType);
    }

    public void reverseItemSaleFromTurn(model.turn.TurnActivity turnActivity, long itemID, long quantity) {
        turn.reverseItemSaleFromTurn(turnActivity, itemID, quantity);
    }

    public void refundTransactionFromTurn(model.turn.TurnActivity activity, int consecutiveTrans,
                                          Instant currentTime, long itemID, long itemQty) {
        turn.refundTransactionFromTurn(activity, consecutiveTrans, currentTime, itemID, itemQty);
    }

    public JSONObject getDetailedTurnInformationAsJson() {
        return turn.getDetailedTurnInformationAsJson();
    }
}
