package model.turn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomData(
        @JsonProperty("towerNumber") int towerNumber,
        @JsonProperty("floorNumber") int floorNumber,
        @JsonProperty("roomNumber") int roomNumber,
        @JsonProperty("roomString") String roomString
) {}
