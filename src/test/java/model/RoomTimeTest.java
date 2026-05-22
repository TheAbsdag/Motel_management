package model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoomTime} — time slot defaults and immutability helpers.
 */
class RoomTimeTest {

    // ========== Default Time Slots ==========

    @Test
    void shouldReturnThreeDefaultSlots() {
        RoomTime[] slots = RoomTime.getDefaultTimeSlots();

        assertThat(slots).hasSize(3);
    }

    @Test
    void defaultSlot0ShouldBeThreeHoursFortyThousand() {
        RoomTime slot = RoomTime.getDefaultTimeSlots()[0];

        assertThat(slot.getTimeSeconds()).isEqualTo(10800L);
        assertThat(slot.getPrice()).isEqualTo(40000L);
    }

    @Test
    void defaultSlot1ShouldBeTwelveHoursFortyFiveThousand() {
        RoomTime slot = RoomTime.getDefaultTimeSlots()[1];

        assertThat(slot.getTimeSeconds()).isEqualTo(43200L);
        assertThat(slot.getPrice()).isEqualTo(45000L);
    }

    @Test
    void defaultSlot2ShouldBeTwentyFourHoursEightyEightThousand() {
        RoomTime slot = RoomTime.getDefaultTimeSlots()[2];

        assertThat(slot.getTimeSeconds()).isEqualTo(86400L);
        assertThat(slot.getPrice()).isEqualTo(88000L);
    }

    // ========== Immutability Helpers ==========

    @Test
    void withPriceShouldCreateNewInstanceWithUpdatedPrice() {
        RoomTime original = new RoomTime(50000, 7200);

        RoomTime updated = original.withPrice(60000);

        assertThat(updated.getPrice()).isEqualTo(60000);
        assertThat(updated.getTimeSeconds()).isEqualTo(7200);
        assertThat(original.getPrice()).isEqualTo(50000);
    }

    @Test
    void withTimeSecondsShouldCreateNewInstanceWithUpdatedDuration() {
        RoomTime original = new RoomTime(50000, 7200);

        RoomTime updated = original.withTimeSeconds(10800);

        assertThat(updated.getPrice()).isEqualTo(50000);
        assertThat(updated.getTimeSeconds()).isEqualTo(10800);
        assertThat(original.getTimeSeconds()).isEqualTo(7200);
    }

    // ========== Time Conventions ==========

    @Test
    void shouldReturnExpectedTimeConventions() {
        char[] conventions = RoomTime.getTimeConventions();

        assertThat(conventions).containsExactly('s', 'm', 'h');
    }
}
