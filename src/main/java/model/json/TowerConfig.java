package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TowerConfig(
        @JsonProperty("towerNumber") int towerNumber,
        @JsonProperty("towerFloors") int towerFloors,
        @JsonProperty("towerRooms") List<FloorConfig> towerRooms
) {}
