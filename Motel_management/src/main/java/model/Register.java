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

    private int historyID;

    public void Register() {
        sellingList = new JSONArray();
        inventory = new ArrayList<Item>();
    }

    public void Register(int historyID) {
        this.historyID = historyID;
        sellingList = new JSONArray();
        inventory = new ArrayList<Item>();
    }

    public void createItem(String name, int value, int quantity, int itemID) {
        Item newItem = new Item(name, value, quantity, itemID);
        inventory.add(newItem);
    }

    public void deleteItemInformation(Item item, int itemID) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == itemID) {
                inventory.remove(i);
            }
        }
    }

    public void saveItemInformation(Item item, int itemID) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == itemID) {
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
            if (itemRegister.getString("itemID").equals(item.getItemID())) {
                sellingList.remove(i);
            }

        }
    }

    public void addItemToList(Item item, int quantity) {
        JSONObject itemRegister = new JSONObject();
        itemRegister.put("itemName", item.getName());
        itemRegister.put("itemID", item.getItemID());
        int finalPrice = quantity * item.getPrice();
        itemRegister.put("quantity", quantity);
        itemRegister.put("price", finalPrice);
        sellingList.put(itemRegister);
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
        }

        output.put("inventoryItems", inventoryArray);
        return output;
    }
}