package model.modelManagers;

import model.CartItem;
import model.Item;
import model.Register;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import model.json.ObjectMapperFactory;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.turn.InventoryMovement;
import model.turn.InventorySnapshotItem;
import model.turn.MovementType;

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
        return register.saveItemInformation(new Item(item.name(), item.price(), item.quantity(),
                item.itemID(), item.minimum()));
    }

    public void newItemCreated(String name, long price, long quantity) {
        register.createNewItem(name, price, quantity);
    }

    /**
     * Applies a manual stock change and returns the movement it produced.
     *
     * <p>The item is replaced in the inventory in every call, so name, price and
     * minimum changes are saved too. A quantity change without a concept is rejected.
     *
     * @param updated    the full updated item (name, price, quantity, minimum)
     * @param concept    reason typed by the user; required when the quantity changes
     * @param cost       total cost of the increase, ignored for a consumption
     * @param changeDate moment of the movement
     * @return the movement, or null when the quantity did not change
     */
    public InventoryMovement applyInventoryChange(InventoryItemData updated, String concept,
                                                  long cost, ZonedDateTime changeDate) {
        Item current = register.getItemFromItemID(updated.itemID());
        if (current == null) {
            return null;
        }
        long delta = updated.quantity() - current.getQuantity();
        InventoryMovement movement = null;
        if (delta != 0) {
            String trimmed = concept == null ? "" : concept.trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Inventory quantity changes require a concept");
            }
            MovementType type = delta > 0 ? MovementType.INCREASE : MovementType.CONSUMPTION;
            movement = new InventoryMovement(changeDate, current.getItemID(), current.getName(),
                    type, Math.abs(delta), trimmed,
                    type == MovementType.INCREASE ? Math.max(0L, cost) : 0L);
        }
        register.saveItemInformation(new Item(updated.name(), updated.price(), updated.quantity(),
                updated.itemID(), updated.minimum()));
        return movement;
    }

    /**
     * Creates a product and returns the movement that records its initial stock.
     *
     * @param name       display name
     * @param price      unit price
     * @param quantity   initial quantity
     * @param minimum    low-stock minimum
     * @param changeDate moment of the creation
     * @return the movement, or null when the created product has no units
     */
    public InventoryMovement newItemCreated(String name, long price, long quantity, long minimum,
                                            ZonedDateTime changeDate) {
        Item item = register.createNewItem(name, price, quantity, minimum);
        if (quantity == 0) {
            return null;
        }
        MovementType type = quantity > 0 ? MovementType.INCREASE : MovementType.CONSUMPTION;
        return new InventoryMovement(changeDate, item.getItemID(), item.getName(), type,
                Math.abs(quantity), "CREACIÓN DE PRODUCTO", 0L);
    }

    /**
     * Deletes a product and returns the movement that writes off its stock.
     *
     * @param itemID     product to delete
     * @param changeDate moment of the deletion
     * @return the movement, or null when the product does not exist or has no units
     */
    public InventoryMovement deleteItemWithMovement(long itemID, ZonedDateTime changeDate) {
        Item item = register.getItemFromItemID(itemID);
        if (item == null) {
            return null;
        }
        InventoryMovement movement = null;
        if (item.getQuantity() != 0) {
            MovementType type = item.getQuantity() > 0 ? MovementType.CONSUMPTION : MovementType.INCREASE;
            movement = new InventoryMovement(changeDate, itemID, item.getName(), type,
                    Math.abs(item.getQuantity()), "BAJA DE PRODUCTO", 0L);
        }
        register.deleteItemById(itemID);
        return movement;
    }

    /**
     * @return the current stock of every product
     */
    public List<InventorySnapshotItem> inventorySnapshot() {
        return register.getInventorySnapshot();
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
