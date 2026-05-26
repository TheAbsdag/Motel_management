package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a single room's configuration within the motel grid, including its
 * custom time slot pricing data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomConfigData(
        @JsonProperty("roomString") String roomString,
        @JsonProperty("roomFloor") int roomFloor,
        @JsonProperty("roomNumber") int roomNumber,
        @JsonProperty("customTimeData") List<TimeSlotConfig> customTimeData
) {}
