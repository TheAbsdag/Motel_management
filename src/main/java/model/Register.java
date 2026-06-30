package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;

/**
 * Manages the motel inventory and the active selling cart list.
 *
 * @author Santiago
 */
public class Register {

    private ArrayList<Item> inventory;
    private List<CartItem> sellingList;
    private boolean sellingListConsumed = false;

    /**
     * Not used like the constructor
     * @deprecated 
     */
    @Deprecated
    private long historyID;

    public Register() {
        sellingList = new ArrayList<>();
        inventory = new ArrayList<Item>();
    }

    /**
     *  Legacy management for history design, new history management on proress
     * @param historyID 
     * 
     * @deprecated 
     */
    @Deprecated
    public Register(long historyID) {
        this.historyID = historyID;
        sellingList = new ArrayList<>();
        inventory = new ArrayList<>();
    }

    public void createItem(String name, long value, long quantity, long itemIDInput) {
        long itemID = itemIDInput;
        HashSet<Long> usedIds = new HashSet<>();
        for (Item item : inventory) {
            usedIds.add(item.getItemID());
        }
        while (usedIds.contains(itemID)) {
            itemID++;
        }
        Item newItem = new Item(name, value, quantity, itemID);
        inventory.add(newItem);
    }

    public void createNewItem(String name, long value, long quantity) {
        long itemID = 0;
        HashSet<Long> usedIds = new HashSet<>();
        for (Item item : inventory) {
            usedIds.add(item.getItemID());
        }
        while (usedIds.contains(itemID)) {
            itemID++;
        }
        Item newItem = new Item(name, value, quantity, itemID);
        inventory.add(newItem);
    }

    /**
    *¨Method replaced by @deleteItemById
    * @deprecated
    */
    @Deprecated
    public void deleteItemInformation(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == item.getItemID()) {
                inventory.remove(i);
            }
        }
    }

    public void deleteItemById(long itemID) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == itemID) {
                inventory.remove(i);
                break;
            }
        }
    }

    public boolean saveItemInformation(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == item.getItemID()) {
                inventory.set(i, item);
                return true;
            }
        }
        return false;
    }

    public void newSellingList() {
        sellingList.clear();
        sellingListConsumed = false;
    }

    public void removeFromList(Item item) {
        sellingList.removeIf(ci -> ci.itemID() == item.getItemID());
    }

    public void addItemToList(Item item, long quantity) {
        for (int i = 0; i < sellingList.size(); i++) {
            CartItem ci = sellingList.get(i);
            if (item.getItemID() == ci.itemID()) {
                long newQuantity = quantity + ci.quantity();
                sellingList.set(i, new CartItem(ci.itemID(), ci.itemName(), newQuantity,
                        item.getPrice() * newQuantity));
                return;
            }
        }
        sellingList.add(new CartItem(item.getItemID(), item.getName(), quantity,
                quantity * item.getPrice()));
    }

    public void addCourtesyItemToList(Item item, long quantity) {
        sellingList.add(new CartItem(item.getItemID(), item.getName(), quantity, 0L));
    }

    public List<CartItem> consumeRegisterListForSale() {
        if (sellingListConsumed) {
            throw new IllegalStateException("Selling list has already been consumed for this transaction");
        }
        sellingListConsumed = true;
        for (CartItem itemSold : sellingList) {
            for (int j = 0; j < inventory.size(); j++) {
                if (itemSold.itemID() == inventory.get(j).getItemID()) {
                    inventory.get(j).itemSold(itemSold.quantity());
                }
            }
        }
        return sellingList;
    }

    /**
     * Serialises the full inventory into an InventoryData record.
     */
    public InventoryData getInventoryData() {
        List<InventoryItemJson> items = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            items.add(new InventoryItemJson(
                    inventory.get(i).getItemID(),
                    inventory.get(i).getPrice(),
                    inventory.get(i).getName(),
                    inventory.get(i).getQuantity()));
        }
        return new InventoryData(items, 2);
    }

    public Item getItemFromItemID(long itemID) {
        Item output = null;
        for (int i = 0; i < inventory.size(); i++) {
            if (itemID == inventory.get(i).getItemID()) {
                output = inventory.get(i);
                break;
            }
        }
        if (output != null) {
            return output;
        } else {
            return null;
        }
    }

    public List<InventoryItemData> getInventoryItemDataList() {
        List<InventoryItemData> result = new ArrayList<>();
        for (Item item : inventory) {
            result.add(new InventoryItemData(item.getItemID(), item.getName(), item.getPrice(), item.getQuantity()));
        }
        return result;
    }

    public List<SellingItemData> getSellingItemDataList() {
        List<SellingItemData> result = new ArrayList<>();
        for (CartItem ci : sellingList) {
            result.add(new SellingItemData(
                    ci.itemID(),
                    ci.itemName(),
                    ci.quantity(),
                    ci.price(),
                    ci.price() == 0
            ));
        }
        return result;
    }

    public long getTotalPriceRegisterList() {
        long totalPrice = 0;
        for (CartItem ci : sellingList) {
            totalPrice += ci.price();
        }
        return totalPrice;
    }

    /**
     * POJO for inventory serialization.
     */
    public record InventoryData(
            @JsonProperty("inventoryItems") List<InventoryItemJson> inventoryItems,
            @JsonProperty("version") int version
    ) {}

    /**
     * POJO for a single inventory item in serialized form.
     */
    public record InventoryItemJson(
            @JsonProperty("itemID") long itemID,
            @JsonProperty("price") long price,
            @JsonProperty("itemName") String itemName,
            @JsonProperty("quantity") long quantity
    ) {}
}
