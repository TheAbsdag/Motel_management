package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomConfigData(
        @JsonProperty("roomString") String roomString,
        @JsonProperty("roomFloor") int roomFloor,
        @JsonProperty("roomNumber") int roomNumber,
        @JsonProperty("customTimeData") List<TimeSlotConfig> customTimeData
) {}
