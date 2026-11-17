package model.turn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stock of one product at a point in time (turn opening or closing).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventorySnapshotItem(
        @JsonProperty("itemID") long itemID,
        @JsonProperty("itemName") String itemName,
        @JsonProperty("quantity") long quantity,
        @JsonProperty("minimum") long minimum
) {

    @JsonCreator
    public static InventorySnapshotItem createFromJson(
            @JsonProperty("itemID") long itemID,
            @JsonProperty("itemName") String itemName,
            @JsonProperty("quantity") long quantity,
            @JsonProperty("minimum") long minimum) {
        return new InventorySnapshotItem(itemID, itemName, quantity, minimum);
    }
}
