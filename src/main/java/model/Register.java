package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;

/**
 * Manages the motel inventory and the active selling cart list.
 *
 * <p>Inventory is stored as a list of {@link Item} objects. The selling list
 * is stored as a raw {@link JSONArray} for direct serialization; items can
 * be added, removed, and consumed for transaction recording.
 *
 * @author Santiago
 */
public class Register {

    private ArrayList<Item> inventory;
    private JSONArray sellingList;
    private boolean sellingListConsumed = false;

    private long historyID;

    /**
     * Creates an empty register with no inventory history ID.
     */
    public Register() {
        sellingList = new JSONArray();
        inventory = new ArrayList<Item>();
    }

    /**
     * Creates a register associated with a history record.
     *
     * @param historyID the turn history ID this register belongs to
     */
    public Register(long historyID) {
        this.historyID = historyID;
        sellingList = new JSONArray();
        inventory = new ArrayList<>();
    }

    /**
     * Creates a new inventory item with a suggested ID, auto-incrementing if the ID
     * is already in use.
     *
     * @param name        item display name
     * @param value       unit price
     * @param quantity    initial stock
     * @param itemIDInput suggested item ID
     */
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

    /**
     * Creates a new inventory item with an auto-generated ID.
     *
     * @param name     item display name
     * @param value    unit price
     * @param quantity initial stock
     */
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
     * Removes an item from the inventory by matching its ID.
     *
     * @param item the item to remove
     */
    public void deleteItemInformation(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == item.getItemID()) {
                inventory.remove(i);
            }
        }
    }

    /**
     * Removes an item from the inventory by its numeric ID.
     *
     * @param itemID the unique item identifier
     */
    public void deleteItemById(long itemID) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == itemID) {
                inventory.remove(i);
                break;
            }
        }
    }

    /**
     * Updates an existing inventory item in-place. Returns {@code false} if no
     * item with the matching ID is found.
     *
     * @param item the item with updated values
     * @return {@code true} if the item was found and updated
     */
    public boolean saveItemInformation(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == item.getItemID()) {
                inventory.set(i, item);
                return true;
            }
        }
        return false;
    }

    /**
     * Clears the selling list and resets the consumed flag.
     */
    public void newSellingList() {
        sellingList.clear();
        sellingListConsumed = false;
    }

    /**
     * Removes an item from the selling list by its ID.
     *
     * @param item the item to remove from the cart
     */
    public void removeFromList(Item item) {
        for (int i = 0; i < sellingList.length(); i++) {
            JSONObject itemRegister = sellingList.getJSONObject(i);
            if (itemRegister.getLong("itemID") == item.getItemID()) {
                sellingList.remove(i);
                break;
            }

        }
    }

    /**
     * Adds an item (or increases its quantity if already present) to the selling list.
     *
     * @param item     the item to add
     * @param quantity quantity to add
     */
    public void addItemToList(Item item, long quantity) {
        boolean alreadyExists = false;
        for (int i = 0; i < sellingList.length(); i++) {
            JSONObject sellingItem = sellingList.getJSONObject(i);
            if (item.getItemID() == sellingItem.getLong("itemID")) {
                long newQuantity = quantity + sellingItem.getLong("quantity");
                long newPrice = item.getPrice() * newQuantity;
                sellingItem.put("quantity", newQuantity);
                sellingItem.put("price", newPrice);
                alreadyExists = true;
                sellingList.put(i, sellingItem);
                break;
            }
        }
        if (!alreadyExists) {
            JSONObject itemRegister = new JSONObject();
            itemRegister.put("itemName", item.getName());
            itemRegister.put("itemID", item.getItemID());
            long finalPrice = quantity * item.getPrice();
            itemRegister.put("quantity", quantity);
            itemRegister.put("price", finalPrice);
            sellingList.put(itemRegister);
        }
    }

    /**
     * Adds a courtesy (zero-price) item to the selling list.
     *
     * @param item     the item to give as courtesy
     * @param quantity quantity to give
     */
    public void addCourtesyItemToList(Item item, long quantity) {
        JSONObject newItem = new JSONObject();
        newItem.put("quantity", quantity);
        newItem.put("itemName", item.getName());
        newItem.put("itemID", item.getItemID());
        newItem.put("price", 0);
        sellingList.put(newItem);
    }

    /**
     * Consumes the current selling list by decrementing inventory quantities
     * and returns the list for transaction recording.
     * Safe against double-call: subsequent calls return an empty list.
     */
    public JSONArray consumeRegisterListForSale() {
        if (sellingListConsumed) {
            return new JSONArray();
        }
        sellingListConsumed = true;
        for (int i = 0; i < sellingList.length(); i++) {
            JSONObject itemSold = sellingList.getJSONObject(i);
            for (int j = 0; j < inventory.size(); j++) {
                if (itemSold.getInt("itemID") == (inventory.get(j).getItemID())) {
                    inventory.get(j).itemSold(itemSold.getInt("quantity"));
                }
            }
        }
        return sellingList;
    }

    JSONArray getCurrentRegisterList() {
        return sellingList;
    }

    /**
     * Serialises the full inventory into a JSON object.
     *
     * @return a JSON object with an {@code "inventoryItems"} array
     */
    public JSONObject getInventoryData() {
        JSONObject output = new JSONObject();
        JSONArray inventoryArray = new JSONArray();
        for (int i = 0; i < inventory.size(); i++) {
            JSONObject item = new JSONObject();
            item.put("itemID", inventory.get(i).getItemID());
            item.put("price", inventory.get(i).getPrice());
            item.put("itemName", inventory.get(i).getName());
            item.put("quantity", inventory.get(i).getQuantity());
            inventoryArray.put(item);
        }

        output.put("inventoryItems", inventoryArray);
        return output;
    }

    /**
     * Looks up an item by its unique identifier.
     *
     * @param itemID the item ID to search for
     * @return the matching item, or {@code null} if not found
     */
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

    /**
     * Returns all inventory items as a typed list of DTOs.
     */
    public List<InventoryItemData> getInventoryItemDataList() {
        List<InventoryItemData> result = new ArrayList<>();
        for (Item item : inventory) {
            result.add(new InventoryItemData(item.getItemID(), item.getName(), item.getPrice(), item.getQuantity()));
        }
        return result;
    }

    /**
     * Returns the current selling list as a typed list of DTOs.
     */
    public List<SellingItemData> getSellingItemDataList() {
        List<SellingItemData> result = new ArrayList<>();
        for (int i = 0; i < sellingList.length(); i++) {
            JSONObject obj = sellingList.getJSONObject(i);
            result.add(new SellingItemData(
                    obj.getLong("itemID"),
                    obj.getString("itemName"),
                    obj.getLong("quantity"),
                    obj.getLong("price"),
                    obj.getLong("price") == 0
            ));
        }
        return result;
    }

    /**
     * Calculates the total price of all items currently in the selling list.
     *
     * @return total price in the selling list
     */
    public long getTotalPriceRegisterList() {
        long totalPrice = 0;
        for (int i = 0; i < sellingList.length(); i++) {
            JSONObject itemRegister = sellingList.getJSONObject(i);
            totalPrice += itemRegister.getLong("price");
        }
        return totalPrice;
    }

}
