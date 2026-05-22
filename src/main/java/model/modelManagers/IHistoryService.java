package model.modelManagers;

import model.dto.TurnHistoryData;
import org.json.JSONArray;
import java.util.List;

/**
 * Interface for historical turn browsing, DTO transformation, and printing.
 */
public interface IHistoryService {

    JSONArray getHistoryData();

    void generateHistoryTurnReport(int selectedRow);

    List<TurnHistoryData> getTurnHistoryDataList();

    void turnHistoryPrint(int option, int selectedRow);
}
