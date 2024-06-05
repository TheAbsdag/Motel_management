package model;
/**
 *
 * @author Santiago
 */
public class Item {
    private String name;
    private int price;
    private int quantity;
    private int itemID;
    
    public Item(String name, int value, int quantity, int itemID){
        this.itemID = itemID;
        this.name = name;
        this.price = value;
        this.quantity = quantity;
    }
    
    public void itemSold(int quantitySold){
        quantity = quantity - quantitySold;
    }
    
    public void itemAdded(int quantityAdded){
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
    public int getPrice() {
        return price;
    }

    /**
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the itemID
     */
    public int getItemID() {
        return itemID;
    }


}
