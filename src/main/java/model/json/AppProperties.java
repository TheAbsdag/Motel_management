package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Top-level application configuration DTO mapping the {@code applicationProperties.json} file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AppProperties(
        @JsonProperty("consecutiveTransaction") int consecutiveTransaction,
        @JsonProperty("motelName") String motelName,
        @JsonProperty("motelAddress") String motelAddress,
        @JsonProperty("motelID") String motelID,
        @JsonProperty("printerName") String printerName,
        @JsonProperty("version") int version,
        @JsonProperty("roomsPerTower") List<TowerConfig> roomsPerTower,
        @JsonProperty("currencyConfig") CurrencyConfig currencyConfig
) {
    public AppProperties {
        if (roomsPerTower == null) roomsPerTower = new ArrayList<>();
        if (currencyConfig == null) currencyConfig = CurrencyConfig.defaultConfig();
    }

    public AppProperties() {
        this(0, "", "", "", null, 3, new ArrayList<>(), CurrencyConfig.defaultConfig());
    }
}
