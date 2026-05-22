package model.modelManagers;

import model.Turn;
import model.dto.TurnHistoryData;
import model.turn.TurnDetails;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulates historical turn browsing, DTO transformation, and report generation.
 */
public class HistoryService {

    private final FileManager files;
    private final ZoneId zoneID;
    private final List<Turn> turnHistory = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(HistoryService.class.getName());

    public HistoryService(FileManager files, ZoneId zoneID) {
        this.files = files;
        this.zoneID = zoneID;
    }

    public JSONArray getHistoryData() {
        JSONArray currentHistory = files.getHistoryFiles();
        turnHistory.clear();
        for (int i = 0; i < currentHistory.length(); i++) {
            JSONObject currentTurn = currentHistory.getJSONObject(i);
            JSONArray activityArray = currentTurn.getJSONArray("turnActivity");
            Instant start = ZonedDateTime.parse(currentTurn.getString("turnStart")).toInstant();
            Instant end = ZonedDateTime.parse(currentTurn.getString("turnEnd")).toInstant();
            int turnNum = currentTurn.getInt("turnNumber");
            Turn newTurn = new Turn(start, end, turnNum, zoneID, activityArray);
            turnHistory.add(newTurn);
        }
        return currentHistory;
    }

    public void generateHistoryTurnReport(int selectedRow) {
        TurnDetails details = turnHistory.get(selectedRow).getDetailedTurnInformation();
        TurnReportGenerator.generateReport(details);
    }

    public List<TurnHistoryData> getTurnHistoryDataList() {
        JSONArray rawHistory = files.getHistoryFiles();
        List<TurnHistoryData> result = new ArrayList<>();
        for (int i = 0; i < rawHistory.length(); i++) {
            JSONObject currentTurn = rawHistory.getJSONObject(i);
            try {
                JSONArray activityArray = currentTurn.getJSONArray("turnActivity");
                Instant start = ZonedDateTime.parse(currentTurn.getString("turnStart")).toInstant();
                Instant end = ZonedDateTime.parse(currentTurn.getString("turnEnd")).toInstant();
                int turnNum = currentTurn.getInt("turnNumber");
                Turn historyTurn = new Turn(start, end, turnNum, zoneID, activityArray);
                turnHistory.add(historyTurn);

                ZonedDateTime turnStartZ = ZonedDateTime.parse(currentTurn.getString("turnStart"));
                ZonedDateTime turnEndZ = ZonedDateTime.parse(currentTurn.getString("turnEnd"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                Duration durationRaw = Duration.between(turnStartZ, turnEndZ);
                long hours = durationRaw.toHours();
                long minutes = durationRaw.minusHours(hours).toMinutes();
                String duration = hours + ":" + minutes;

                long totalSales = currentTurn.optLong("totalSales");
                long totalItems = currentTurn.optLong("totalItems");
                long totalRooms = currentTurn.optLong("totalRooms");
                long totalRefunds = currentTurn.optLong("totalRefunds");
                long totalSpending = currentTurn.optLong("totalSpending");
                long totalTurnVal = currentTurn.optLong("totalTurn");
                long totalBankTransfers = currentTurn.optLong("totalBankTransfers");
                long totalDeposits = currentTurn.optLong("totalDeposits");
                long totalNet = currentTurn.optLong("totalNet");

                result.add(new TurnHistoryData(
                        turnNum, turnStartZ, turnEndZ,
                        totalSales, totalItems, totalRooms,
                        totalRefunds, totalSpending, totalTurnVal,
                        totalBankTransfers, totalDeposits, totalNet,
                        turnStartZ.format(dateFormatter), duration,
                        turnStartZ.format(formatter), turnEndZ.format(formatter),
                        historyTurn.getActivityDataList()
                ));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Skipping malformed history entry at index " + i, e);
            }
        }
        return result;
    }

    /**
     * Returns the turn at the given index from the cached history list.
     * {@link #getHistoryData()} or {@link #getTurnHistoryDataList()} must be called first.
     */
    public Turn getHistoryTurn(int selectedRow) {
        return turnHistory.get(selectedRow);
    }
}
