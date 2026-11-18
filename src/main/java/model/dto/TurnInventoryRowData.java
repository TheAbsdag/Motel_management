package model.dto;

/**
 * One product row of the inventory movement summary of a turn.
 *
 * @param itemID   product identifier
 * @param itemName product name at the moment the row is built
 * @param received stock the turn started with
 * @param increase units added by manual increases (restock, product creation)
 * @param sold     units sold with a price
 * @param courtesy units given as a courtesy (price zero)
 * @param current  current stock
 */
public record TurnInventoryRowData(
        long itemID,
        String itemName,
        long received,
        long increase,
        long sold,
        long courtesy,
        long current
) {

    /**
     * @return total units that entered the turn ({@code received + increase})
     */
    public long totalIn() {
        return received + increase;
    }
}
