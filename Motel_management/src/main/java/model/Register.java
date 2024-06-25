package model;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Santiago
 */
public class Register {

    private ArrayList<Item> inventory;
    private JSONArray sellingList;

    private long historyID;

    public Register() {
        sellingList = new JSONArray();
        inventory = new ArrayList<Item>();
    }

    public Register(long historyID) {
        this.historyID = historyID;
        sellingList = new JSONArray();
        inventory = new ArrayList<>();
    }

    public void createItem(String name, long value, long quantity, long itemIDInput) {
        long itemID = itemIDInput;
        //Double take so each item has unique ID
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == itemID) {
                itemID++;
                i = 0;
            }
        }
        Item newItem = new Item(name, value, quantity, itemID);
        inventory.add(newItem);
    }

    public void createNewItem(String name, long value, long quantity) {
        long itemID = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == itemID) {
                itemID++;
                i = 0;
            }
        }
        Item newItem = new Item(name, value, quantity, itemID);
        inventory.add(newItem);
    }

    public void deleteItemInformation(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == item.getItemID()) {
                inventory.remove(i);
            }
        }
    }

    public void saveItemInformation(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == item.getItemID()) {
                inventory.set(i, item);
            }
        }
    }

    public void newSellingList() {
        sellingList.clear();
    }

    public void removeFromList(Item item) {
        for (int i = 0; i < sellingList.length(); i++) {
            JSONObject itemRegister = sellingList.getJSONObject(i);
            if (itemRegister.getLong("itemID") == item.getItemID()) {
                sellingList.remove(i);
                break;
            }

        }
    }

    public void addItemToList(Item item, long quantity) {
        boolean alreadyExists = false;
        for (int i = 0; i < sellingList.length(); i++) {
            JSONObject sellingItem = sellingList.getJSONObject(i);
            if (item.getItemID() == sellingItem.getLong("itemID")) {
                sellingItem.put("quantity", quantity + sellingItem.getLong("quantity"));
                sellingItem.put("price", item.getPrice() * sellingItem.getLong("quantity"));
                alreadyExists = true;
                sellingList.remove(i);
                sellingList.put(sellingItem);
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

    public void addCourtesyItemToList(Item item, long quantity) {
        JSONObject newItem = new JSONObject();
        newItem.put("quantity", quantity);
        newItem.put("itemName", item.getName());
        newItem.put("itemID", item.getItemID());
        newItem.put("quantity", quantity);
        newItem.put("price", 0);
        sellingList.put(newItem);
    }

    public JSONArray getRegisterListSaleMade() {
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

    //Method to be used exclusively for information retreival in history
    public void setRegisterInformation(JSONArray registerInfo) {

    }

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

    public long getTotalPriceRegisterList() {
        long totalPrice = 0;
        for (int i = 0; i < sellingList.length(); i++) {
            JSONObject itemRegister = sellingList.getJSONObject(i);
            totalPrice += itemRegister.getLong("price");
        }
        return totalPrice;
    }

}
