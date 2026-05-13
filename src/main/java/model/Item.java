package model;
/**
 *
 * @author Santiago
 */
public class Item {
    private String name;
    private long price;
    private long quantity;
    private long itemID;
    
    public Item(String name, long value, long quantity, long itemID){
        this.itemID = itemID;
        this.name = name;
        this.price = value;
        this.quantity = quantity;
    }
    
    public void itemSold(long quantitySold){
        quantity = quantity - quantitySold;
    }
    
    public void itemAdded(long quantityAdded){
        quantity  = quantity + quantityAdded;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @return the price
     */
    public long getPrice() {
        return price;
    }

    /**
     * @return the quantity
     */
    public long getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the itemID
     */
    public long getItemID() {
        return itemID;
    }


}
