package model.modelManagers;

import model.CartItem;
import model.Item;
import model.Register;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import model.json.ObjectMapperFactory;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SellingService {

    private final Register register;
    private static final Logger logger = Logger.getLogger(SellingService.class.getName());

    public SellingService(Register register) {
        this.register = register;
    }

    public void restartSaleManager() {
        register.newSellingList();
    }

    public String getInventoryData() {
        try {
            return ObjectMapperFactory.get().writeValueAsString(register.getInventoryData());
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to serialize inventory data", e);
            return "{}";
        }
    }

    public boolean saveItemInformation(InventoryItemData item) {
        return register.saveItemInformation(new Item(item.name(), item.price(), item.quantity(), item.itemID()));
    }

    public void newItemCreated(String name, long price, long quantity) {
        register.createNewItem(name, price, quantity);
    }

    public void deleteItemFromInventory(long itemID) {
        register.deleteItemById(itemID);
    }

    public void addItemToSelling(long itemID, long quantity, boolean courtesySale) {
        if (!courtesySale) {
            register.addItemToList(register.getItemFromItemID(itemID), quantity);
        } else {
            register.addCourtesyItemToList(register.getItemFromItemID(itemID), quantity);
        }
    }

    public void removeItemToSelling(long itemID) {
        register.removeFromList(register.getItemFromItemID(itemID));
    }

    public long getCurrentTotalPriceSellingList() {
        return register.getTotalPriceRegisterList();
    }

    public List<CartItem> consumeRegisterListForSale() {
        return register.consumeRegisterListForSale();
    }

    public Item getItemFromItemID(long itemID) {
        return register.getItemFromItemID(itemID);
    }

    public List<InventoryItemData> getInventoryItemDataList() {
        return register.getInventoryItemDataList();
    }

    public List<SellingItemData> getSellingItemDataList() {
        return register.getSellingItemDataList();
    }

    /**
     * Legacy method for cratio of item from json data
     * @param name
     * @param price
     * @param quantity
     * @param itemID 
     * @deprecated 
     */
    @Deprecated
    public void createItemFromJson(String name, int price, int quantity, int itemID) {
        register.createItem(name, price, quantity, itemID);
    }
}
