package model;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import model.json.FloorConfig;
import model.json.RoomConfigData;
import model.json.TowerConfig;
import model.modelManagers.RoomManager;
import model.turn.RoomBookingActivity;
import model.turn.RoomData;
import model.turn.TurnActivity;
import model.turn.TurnDetails;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Loads the JSON produced by the legacy versions of the app (pre-Jackson,
 * hour-based {@code service}/{@code extension}, 1-based tower numbers) so an
 * installation upgrading in place keeps its data.
 */
class LegacyDataCompatibilityTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private static final String LEGACY_ROOMS_INFORMATION = """
            {"rooms":[
              {"towerNumber":0,"extension":0,"roomNumber":0,"service":12,"roomString":"1-105","floorNumber":0,"startStatus":"2026-09-23T22:42:56.198049600-05:00[America/Bogota]","status":3,"endStatus":"2026-09-24T10:42:56.198049600-05:00[America/Bogota]"},
              {"towerNumber":0,"extension":0,"roomNumber":1,"service":0,"roomString":"1-203","floorNumber":1,"startStatus":"2026-09-20T11:26:33.275673300-05:00[America/Bogota]","status":1,"endStatus":""}
            ]}""";

    private static final String LEGACY_TURN = """
            {"totalItems":0,"totalSpending":0,"totalBankTransfers":0,"turnStart":"2026-09-24T07:46:50.456806900-05:00[America/Bogota]","totalSales":40000,"turnActivity":[{"extension":0,"roomNumber":6,"servicedExtension":0,"changeType":"room","roomString":"2-407","startStatus":"2026-09-24T07:47:01.688869500-05:00[America/Bogota]","consecutiveTrans":22576,"endStatus":"2026-09-24T10:47:01.688869500-05:00[America/Bogota]","towerNumber":2,"roomStatus":3,"price":40000,"service":3,"changeDate":"2026-09-24T07:47:01.688869500-05:00[America/Bogota]","floorNumber":3,"refunded":false}],"isTurnActive":true,"totalNet":40000,"totalRooms":40000,"totalTurn":40000,"totalDeposits":0,"turnNumber":1,"totalRefunds":0}""";

    @Test
    void shouldRestoreLegacyHourBasedRoomState() {
        RoomManager roomManager = new RoomManager(BOGOTA);
        roomManager.buildRoomGrid(List.of(new TowerConfig(0, 2, List.of(
                new FloorConfig(0, List.of(new RoomConfigData("1-105", 0, 0, null))),
                new FloorConfig(1, List.of(new RoomConfigData("1-203", 1, 0, null),
                        new RoomConfigData("1-204", 1, 1, null)))))));

        roomManager.restoreRoomStates(LEGACY_ROOMS_INFORMATION);

        Room occupied = roomManager.getRoom(0, 0, 0);
        assertThat(occupied.getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(occupied.getServiceDuration()).isEqualTo(12 * 3600L);
        assertThat(occupied.getEndStatus()).isEqualTo(Instant.parse("2026-09-24T15:42:56.198049600Z"));

        Room free = roomManager.getRoom(0, 1, 1);
        assertThat(free.getStatus()).isEqualTo(RoomStatus.FREE);
    }

    @Test
    void shouldRestoreLegacyTurnWithOneBasedTowerAndHourBasedService() {
        Turn turn = new Turn(Instant.EPOCH, BOGOTA);

        assertThat(turn.setPreviousTurnJSON(LEGACY_TURN)).isTrue();

        TurnDetails details = turn.getDetailedTurnInformation();
        assertThat(details.getTurnStart().toInstant()).isEqualTo(Instant.parse("2026-09-24T12:46:50.456806900Z"));
        assertThat(details.getActivities()).hasSize(1);

        TurnActivity activity = details.getActivities().get(0);
        assertThat(activity).isInstanceOf(RoomBookingActivity.class);
        RoomBookingActivity booking = (RoomBookingActivity) activity;
        assertThat(booking.roomStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(booking.towerNumber()).isEqualTo(1);
        assertThat(booking.roomData()).isEqualTo(new RoomData(1, 3, 6, "2-407"));
        assertThat(booking.serviceDuration()).isEqualTo(3 * 3600L);
        assertThat(booking.extensionDuration()).isZero();
        assertThat(booking.servicedExtensionDuration()).isZero();
        assertThat(booking.price()).isEqualTo(40000L);

        assertThat(details.getTotalRooms()).isEqualTo(40000L);
        assertThat(details.getTotalTurn()).isEqualTo(40000L);
        assertThat(details.getTotalNet()).isEqualTo(40000L);
    }

    @Test
    void shouldNotMigrateTwiceWhenAnAlreadyMigratedTurnIsSavedAndReloaded() {
        Turn turn = new Turn(Instant.EPOCH, BOGOTA);
        turn.setPreviousTurnJSON(LEGACY_TURN);

        Turn reloaded = new Turn(Instant.EPOCH, BOGOTA);
        assertThat(reloaded.setPreviousTurnJSON(turn.getDetailedTurnInformationAsJson())).isTrue();

        TurnActivity activity = reloaded.getDetailedTurnInformation().getActivities().get(0);
        RoomBookingActivity booking = (RoomBookingActivity) activity;
        assertThat(booking.towerNumber()).isEqualTo(1);
        assertThat(booking.serviceDuration()).isEqualTo(3 * 3600L);
    }
}
