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
    private long minimum;
    
    /**
     * Creates an inventory item.
     *
     * @param name     display name of the item
     * @param value    unit price
     * @param quantity initial stock quantity
     * @param itemID   unique identifier
     */
    public Item(String name, long value, long quantity, long itemID) {
        this(name, value, quantity, itemID, 0L);
    }

    /**
     * Creates an inventory item with a per-product low-stock minimum.
     *
     * @param name     display name of the item
     * @param value    unit price
     * @param quantity initial stock quantity
     * @param itemID   unique identifier
     * @param minimum  quantity at or below which the product is reported as low
     */
    public Item(String name, long value, long quantity, long itemID, long minimum) {
        this.itemID = itemID;
        this.name = name;
        this.price = value;
        this.quantity = quantity;
        this.minimum = minimum;
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
    @Deprecated
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

    /**
     * Returns the low-stock minimum configured for this product.
     *
     * @return the minimum, zero when none is configured
     */
    public long getMinimum() {
        return minimum;
    }

}
