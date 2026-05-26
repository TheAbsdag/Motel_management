package model.modelManagers;

import model.dto.TurnHistoryData;
import java.util.List;

public interface IHistoryService {

    String getHistoryData();

    void generateHistoryTurnReport(int selectedRow);

    List<TurnHistoryData> getTurnHistoryDataList();

    void turnHistoryPrint(int option, int selectedRow);
}
