package model.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a tower within the motel room grid configuration.
 *
 * @param towerNumber     tower identifier
 * @param towerFloors     number of floors in the tower
 * @param towerRooms      rooms of each floor
 * @param defaultTimeData time and price a new room on this tower starts from, or
 *                        {@code null} for a tower saved before per-tower pricing
 *                        existed (the built-in defaults are used then)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TowerConfig(
        @JsonProperty("towerNumber") int towerNumber,
        @JsonProperty("towerFloors") int towerFloors,
        @JsonProperty("towerRooms") List<FloorConfig> towerRooms,
        @JsonProperty("defaultTimeData") List<TimeSlotConfig> defaultTimeData
) {

    /**
     * Builds a tower that keeps using the built-in pricing defaults.
     *
     * @param towerNumber tower identifier
     * @param towerFloors number of floors in the tower
     * @param towerRooms  rooms of each floor
     */
    public TowerConfig(int towerNumber, int towerFloors, List<FloorConfig> towerRooms) {
        this(towerNumber, towerFloors, towerRooms, null);
    }

    /**
     * Copy of this tower with another room list, keeping its pricing default.
     *
     * @param rooms rooms of each floor
     * @return a new tower with the same identity and default
     */
    public TowerConfig withTowerRooms(List<FloorConfig> rooms) {
        return new TowerConfig(towerNumber, towerFloors, rooms, defaultTimeData);
    }

    /**
     * Copy of this tower with another pricing default for new rooms.
     *
     * @param timeData time slots a new room starts from
     * @return a new tower with the same rooms
     */
    public TowerConfig withDefaultTimeData(List<TimeSlotConfig> timeData) {
        return new TowerConfig(towerNumber, towerFloors, towerRooms, timeData);
    }
}
