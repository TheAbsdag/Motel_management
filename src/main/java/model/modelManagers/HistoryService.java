package model.modelManagers;

import model.Turn;
import model.dto.TurnHistoryData;
import model.turn.TurnDetails;
import model.json.ObjectMapperFactory;
import com.fasterxml.jackson.databind.JsonNode;

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

public class HistoryService {

    private final FileManager files;
    private final ZoneId zoneID;
    private final List<Turn> turnHistory = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(HistoryService.class.getName());

    public HistoryService(FileManager files, ZoneId zoneID) {
        this.files = files;
        this.zoneID = zoneID;
    }

    public String getHistoryData() {
        List<String> currentHistory = files.getHistoryFiles();
        turnHistory.clear();
        for (String jsonStr : currentHistory) {
            Turn newTurn = new Turn(zoneID, jsonStr);
            turnHistory.add(newTurn);
        }
        return files.getHistoryFiles().isEmpty() ? "[]" : String.join("\n", files.getHistoryFiles());
    }

    public void generateHistoryTurnReport(int selectedRow) {
        TurnDetails details = turnHistory.get(selectedRow).getDetailedTurnInformation();
        TurnReportGenerator.generateReport(details);
    }

    public List<TurnHistoryData> getTurnHistoryDataList() {
        List<String> rawHistory = files.getHistoryFiles();
        List<TurnHistoryData> result = new ArrayList<>();
        for (String jsonStr : rawHistory) {
            try {
                JsonNode currentTurn = ObjectMapperFactory.get().readTree(jsonStr);
                int turnNum = currentTurn.get("turnNumber").asInt();
                Turn historyTurn = new Turn(zoneID, jsonStr);
                turnHistory.add(historyTurn);

                ZonedDateTime turnStartZ = ZonedDateTime.parse(currentTurn.get("turnStart").asText());
                ZonedDateTime turnEndZ = ZonedDateTime.parse(currentTurn.get("turnEnd").asText());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                Duration durationRaw = Duration.between(turnStartZ, turnEndZ);
                long hours = durationRaw.toHours();
                long minutes = durationRaw.minusHours(hours).toMinutes();
                String duration = hours + ":" + minutes;

                long totalSales = currentTurn.has("totalSales") ? currentTurn.get("totalSales").asLong() : 0L;
                long totalItems = currentTurn.has("totalItems") ? currentTurn.get("totalItems").asLong() : 0L;
                long totalRooms = currentTurn.has("totalRooms") ? currentTurn.get("totalRooms").asLong() : 0L;
                long totalRefunds = currentTurn.has("totalRefunds") ? currentTurn.get("totalRefunds").asLong() : 0L;
                long totalSpending = currentTurn.has("totalSpending") ? currentTurn.get("totalSpending").asLong() : 0L;
                long totalTurnVal = currentTurn.has("totalTurn") ? currentTurn.get("totalTurn").asLong() : 0L;
                long totalBankTransfers = currentTurn.has("totalBankTransfers") ? currentTurn.get("totalBankTransfers").asLong() : 0L;
                long totalDeposits = currentTurn.has("totalDeposits") ? currentTurn.get("totalDeposits").asLong() : 0L;
                long totalNet = currentTurn.has("totalNet") ? currentTurn.get("totalNet").asLong() : 0L;

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
                logger.log(Level.WARNING, "Skipping malformed history entry", e);
            }
        }
        return result;
    }

    public Turn getHistoryTurn(int selectedRow) {
        return turnHistory.get(selectedRow);
    }
}
