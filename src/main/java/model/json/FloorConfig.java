package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a floor within a tower, containing its room configurations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FloorConfig(
        @JsonProperty("floor") int floor,
        @JsonProperty("rooms") List<RoomConfigData> rooms
) {
    public FloorConfig {
        if (rooms == null) rooms = new ArrayList<>();
    }
}
