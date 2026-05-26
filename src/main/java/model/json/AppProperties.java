package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppProperties(
        @JsonProperty("consecutiveTransaction") int consecutiveTransaction,
        @JsonProperty("motelName") String motelName,
        @JsonProperty("motelAddress") String motelAddress,
        @JsonProperty("motelID") String motelID,
        @JsonProperty("printerName") String printerName,
        @JsonProperty("version") int version,
        @JsonProperty("roomsPerTower") List<TowerConfig> roomsPerTower
) {
    public AppProperties {
        if (roomsPerTower == null) roomsPerTower = new ArrayList<>();
    }

    public AppProperties() {
        this(0, "", "", "", null, 2, new ArrayList<>());
    }
}
