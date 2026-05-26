package model.turn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SaleItem(
        @JsonProperty("itemName") String itemName,
        @JsonProperty("itemID") long itemID,
        @JsonProperty("quantity") long quantity,
        @JsonProperty("price") long price,
        @JsonProperty("refunded") boolean refunded
) {
    public SaleItem {
    }
}
