package model;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Room} — status state machine and time tracking.
 */
class RoomTest {

    private Room room;
    private Instant now;

    @BeforeEach
    void setUp() {
        room = new Room("1-105", 0, 5, 1);
        now = Instant.now();
    }

    // ========== Initial State ==========

    @Test
    void shouldBeFreeOnCreation() {
        assertThat(room.getStatus()).isEqualTo(RoomStatus.FREE);
        assertThat(room.getRoomString()).isEqualTo("1-105");
        assertThat(room.getFloorNumber()).isEqualTo(0);
        assertThat(room.getRoomNumber()).isEqualTo(5);
        assertThat(room.getTowerNumber()).isEqualTo(1);
    }

    // ========== Booking ==========

    @Test
    void shouldSetOccupiedWhenBooked() {
        room.setRoomStatus(RoomStatus.OCCUPIED, now, 12);

        assertThat(room.getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(room.getService()).isEqualTo(12);
        assertThat(room.getStartStatus()).isEqualTo(now);
        assertThat(room.getExtension()).isZero();
    }

    @Test
    void shouldCalculateEndStatusOnBooking() {
        room.setRoomStatus(RoomStatus.OCCUPIED, now, 3);

        assertThat(room.getEndStatus())
                .isNotNull()
                .isEqualTo(now.plus(java.time.Duration.ofHours(3)));
    }

    @Test
    void shouldReturnNullEndStatusWhenFree() {
        assertThat(room.getEndStatus()).isNull();
    }

    @Test
    void shouldReturnNullEndStatusWhenCleaning() {
        room.setRoomStatus(RoomStatus.CLEANING, now);

        assertThat(room.getEndStatus()).isNull();
    }

    // ========== Cleaning ==========

    @Test
    void shouldSetCleaningWithStartTime() {
        room.setRoomStatus(RoomStatus.CLEANING, now);

        assertThat(room.getStatus()).isEqualTo(RoomStatus.CLEANING);
        assertThat(room.getStartStatus()).isEqualTo(now);
        assertThat(room.getService()).isZero();
        assertThat(room.getExtension()).isZero();
    }

    // ========== Free ==========

    @Test
    void shouldSetFreeWithNoTimeTracking() {
        // Book room first, then free it
        room.setRoomStatus(RoomStatus.OCCUPIED, now, 3);
        room.setRoomStatus(RoomStatus.FREE);

        assertThat(room.getStatus()).isEqualTo(RoomStatus.FREE);
        assertThat(room.getService()).isZero();
        assertThat(room.getExtension()).isZero();
    }

    // ========== Time Extension ==========

    @Test
    void shouldExtendTimeWhenOccupied() {
        room.setRoomStatus(RoomStatus.OCCUPIED, now, 6);
        Instant originalEnd = room.getEndStatus();

        room.extendRoomTime(3);

        assertThat(room.getExtension()).isEqualTo(3);
        assertThat(room.getEndStatus()).isEqualTo(originalEnd.plus(java.time.Duration.ofHours(3)));
    }

    @Test
    void shouldAccumulateExtensions() {
        room.setRoomStatus(RoomStatus.OCCUPIED, now, 3);

        room.extendRoomTime(3);
        room.extendRoomTime(6);

        assertThat(room.getExtension()).isEqualTo(9);
    }

    // ========== Status Transitions ==========

    @Test
    void shouldTransitionFromFreeToOccupiedToCleaningToFree() {
        // FREE -> OCCUPIED
        room.setRoomStatus(RoomStatus.OCCUPIED, now, 3);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.OCCUPIED);

        // OCCUPIED -> CLEANING
        room.setRoomStatus(RoomStatus.CLEANING, now.plus(java.time.Duration.ofHours(3)));
        assertThat(room.getStatus()).isEqualTo(RoomStatus.CLEANING);

        // CLEANING -> FREE
        room.setRoomStatus(RoomStatus.FREE);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.FREE);
    }

    @Test
    void shouldSetExtensionViaSetter() {
        room.setExtension(6);
        assertThat(room.getExtension()).isEqualTo(6);
    }
}
