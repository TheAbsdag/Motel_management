package model.turn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

/**
 * Manual inventory movement recorded during a turn.
 *
 * <p>Sales, courtesy sales and refunds are not stored here: they already exist as
 * {@link SaleActivity}/{@link RefundActivity} and the report reads them from there.
 * The item name is the name at the moment of the movement, so it survives renames
 * and deletions.
 *
 * <p>The date keeps its zone id on the way to JSON: the shared mapper is configured
 * with a UTC default zone, which would otherwise turn the movement date into a bare
 * instant and lose {@code America/Bogota}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryMovement(
        @JsonProperty("changeDate")
        @JsonFormat(with = JsonFormat.Feature.WRITE_DATES_WITH_ZONE_ID,
                without = JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        ZonedDateTime changeDate,
        @JsonProperty("itemID") long itemID,
        @JsonProperty("itemName") String itemName,
        @JsonProperty("movementType") MovementType movementType,
        @JsonProperty("quantity") long quantity,
        @JsonProperty("concept") String concept,
        @JsonProperty("cost") long cost
) {

    @JsonCreator
    public static InventoryMovement createFromJson(
            @JsonProperty("changeDate") ZonedDateTime changeDate,
            @JsonProperty("itemID") long itemID,
            @JsonProperty("itemName") String itemName,
            @JsonProperty("movementType") MovementType movementType,
            @JsonProperty("quantity") long quantity,
            @JsonProperty("concept") String concept,
            @JsonProperty("cost") long cost) {
        return new InventoryMovement(changeDate, itemID, itemName, movementType, quantity, concept, cost);
    }
}
