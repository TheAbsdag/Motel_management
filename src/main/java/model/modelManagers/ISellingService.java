package model.modelManagers;

import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import org.json.JSONObject;
import java.util.List;

/**
 * Interface for inventory management and selling-cart operations.
 */
public interface ISellingService {

    void restartSaleManager();

    JSONObject getInventoryData();

    boolean saveItemInformation(InventoryItemData item);

    void newItemCreated(String name, long price, long quantity);

    void deleteItemFromInventory(long itemID);

    void addItemToSelling(long itemID, long quantity, boolean courtesySale);

    void removeItemToSelling(long itemID);

    long getCurrentTotalPriceSellingList();

    List<InventoryItemData> getInventoryItemDataList();

    List<SellingItemData> getSellingItemDataList();
}
