package model;

/**
 * Represents a single sellable inventory item with a name, price, quantity, and unique ID.
 *
 * @author Santiago
 */
public class Item {
    private String name;
    private long price;
    private long quantity;
    private long itemID;
    
    /**
     * Creates an inventory item.
     *
     * @param name     display name of the item
     * @param value    unit price
     * @param quantity initial stock quantity
     * @param itemID   unique identifier
     */
    public Item(String name, long value, long quantity, long itemID){
        this.itemID = itemID;
        this.name = name;
        this.price = value;
        this.quantity = quantity;
    }
    
    /**
     * Decrements the stock quantity by the sold amount.
     *
     * @param quantitySold number of units sold
     */
    public void itemSold(long quantitySold){
        quantity = quantity - quantitySold;
    }
    
    /**
     * Increments the stock quantity by the added amount.
     *
     * @param quantityAdded number of units added
     */
    public void itemAdded(long quantityAdded){
        quantity  = quantity + quantityAdded;
    }

    /**
     * Returns the display name.
     *
     * @return item name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the unit price.
     *
     * @return item price
     */
    public long getPrice() {
        return price;
    }

    /**
     * Returns the current stock quantity.
     *
     * @return stock quantity
     */
    public long getQuantity() {
        return quantity;
    }

    /**
     * Sets the stock quantity.
     * @deprecated Item quantity setup or removed on sale, refund, and item creation, no current external quantity setup,
     * @param quantity new stock quantity
     */
    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the unique item identifier.
     *
     * @return item ID
     */
    public long getItemID() {
        return itemID;
    }

}
