package view.helpers;

import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JTextField;
import model.RoomTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the content of the tower pricing dialog of {@link DialogHelper}, which is built
 * without being shown so the fields can be asserted.
 */
class DialogHelperTest {

    private static final RoomTime[] TOWER_DEFAULTS = {
        new RoomTime(40000L, 10800L),
        new RoomTime(45000L, 43200L),
        new RoomTime(88000L, 86400L)
    };

    /**
     * Verifies that a duration taken from the stored slots is shown in the unit its combo
     * box selects, so the dialog opens with the values the tower already has.
     * Expected: 10.800 s, 43.200 s and 86.400 s read as 3, 12 and 24 hours.
     * Failure: the field shows the duration in seconds while the unit says hours.
     */
    @Test
    void shouldShowDurationsInTheSelectedUnit() {
        DialogHelper.TowerPricingFields fields =
                DialogHelper.createTowerPricingFields("TORRE 1", TOWER_DEFAULTS);

        assertThat(fields.durationField(0).getText()).isEqualTo("3");
        assertThat(fields.durationField(1).getText()).isEqualTo("12");
        assertThat(fields.durationField(2).getText()).isEqualTo("24");
    }

    /**
     * Verifies that the prefilled dialog reads back as the slots it was opened with.
     * Expected: the 3 slots, unchanged, with their durations in seconds.
     * Failure: opening and accepting the dialog changes the duration of a slot.
     */
    @Test
    void shouldReadBackTheDurationsItWasOpenedWith() {
        DialogHelper.TowerPricingFields fields =
                DialogHelper.createTowerPricingFields("TORRE 1", TOWER_DEFAULTS);

        RoomTime[] slots = fields.readSlots();

        assertThat(slots).hasSize(3);
        assertThat(slots[0].getTimeSeconds()).isEqualTo(10800L);
        assertThat(slots[1].getTimeSeconds()).isEqualTo(43200L);
        assertThat(slots[2].getTimeSeconds()).isEqualTo(86400L);
        assertThat(slots[0].getPrice()).isEqualTo(40000L);
    }

    /**
     * Verifies that a duration that is not a whole number of hours is shown in the unit
     * that divides it exactly.
     * Expected: 5.400 s as 90 MINUTOS and 45 s as 45 SEGUNDOS.
     * Failure: a duration is truncated to hours, changing what the tower stores.
     */
    @Test
    void shouldShowADurationInTheUnitThatFitsIt() {
        DialogHelper.TowerPricingFields fields = DialogHelper.createTowerPricingFields("TORRE 1",
                new RoomTime[]{new RoomTime(1000L, 5400L), new RoomTime(1000L, 45L), new RoomTime(1000L, 3600L)});

        assertThat(fields.durationField(0).getText()).isEqualTo("90");
        assertThat(fields.durationField(1).getText()).isEqualTo("45");
        assertThat(fields.durationField(2).getText()).isEqualTo("1");
        assertThat(fields.readSlots()[0].getTimeSeconds()).isEqualTo(5400L);
        assertThat(fields.readSlots()[1].getTimeSeconds()).isEqualTo(45L);
        assertThat(fields.readSlots()[2].getTimeSeconds()).isEqualTo(3600L);
    }

    /**
     * Verifies that the dialog starts from the built-in slots when the tower has none.
     * Expected: 3, 12 and 24 hours for 40.000, 45.000 and 88.000.
     * Failure: a tower without a stored default opens with an empty dialog.
     */
    @Test
    void shouldStartFromTheBuiltInSlotsWhenTheTowerHasNone() {
        DialogHelper.TowerPricingFields fields = DialogHelper.createTowerPricingFields("TORRE 2", null);

        assertThat(fields.durationField(0).getText()).isEqualTo("3");
        assertThat(fields.priceField(0).getText()).isEqualTo("40000");
        assertThat(fields.readSlots()).hasSize(3);
    }

    /**
     * Verifies that the adjustment buttons change the value of their own time slot.
     * Expected: +1000 raises the first slot to 41.000 while the second stays at 45.000.
     * Failure: the buttons do not adjust the value, or adjust another slot.
     */
    @Test
    void shouldAdjustThePriceOfTheSlotThatOwnsTheButton() {
        DialogHelper.TowerPricingFields fields =
                DialogHelper.createTowerPricingFields("TORRE 1", TOWER_DEFAULTS);

        fields.priceStepButtons(0)[3].doClick();

        assertThat(fields.priceField(0).getText()).isEqualTo("41000");
        assertThat(fields.priceField(1).getText()).isEqualTo("45000");
        assertThat(fields.readSlots()[0].getPrice()).isEqualTo(41000L);
    }

    /**
     * Verifies that the adjustment buttons offer the same steps as the room configuration
     * screen and stop at zero.
     * Expected: -1000 and -100 below zero leave 0, +100 and +1000 above it add up.
     * Failure: a negative value can be stored in the room pricing.
     */
    @Test
    void shouldOfferTheRoomScreenStepsAndStopAtZero() {
        DialogHelper.TowerPricingFields fields =
                DialogHelper.createTowerPricingFields("TORRE 1", TOWER_DEFAULTS);
        JButton[] buttons = fields.priceStepButtons(0);

        assertThat(buttons).hasSize(4);
        assertThat(buttons[0].getText()).isEqualTo("-1000");
        assertThat(buttons[1].getText()).isEqualTo("-100");
        assertThat(buttons[2].getText()).isEqualTo("+100");
        assertThat(buttons[3].getText()).isEqualTo("+1000");

        buttons[1].doClick();
        buttons[1].doClick();
        buttons[1].doClick();
        assertThat(fields.priceField(0).getText()).isEqualTo("39700");

        fields.priceField(0).setText("50");
        buttons[1].doClick();
        assertThat(fields.priceField(0).getText()).isEqualTo("0");

        buttons[2].doClick();
        buttons[2].doClick();
        assertThat(fields.priceField(0).getText()).isEqualTo("200");
    }

    /**
     * Verifies that an unusable value is reported by the dialog instead of being accepted.
     * Expected: a cleared price yields no slots.
     * Failure: a saved room time slot has a price of 0.
     */
    @Test
    void shouldRejectAnUnusableSlot() {
        DialogHelper.TowerPricingFields fields =
                DialogHelper.createTowerPricingFields("TORRE 1", TOWER_DEFAULTS);

        fields.priceField(1).setText("");

        assertThat(fields.readSlots()).isNull();
    }

    /**
     * Verifies that what the dialog reads back is what a save writes, unit included.
     * Expected: 5 HORAS is written as 18.000 seconds.
     * Failure: the duration entered is stored in the unit it was typed in.
     */
    @Test
    void shouldReadAnEditedDurationInSeconds() {
        DialogHelper.TowerPricingFields fields =
                DialogHelper.createTowerPricingFields("TORRE 1", TOWER_DEFAULTS);

        JTextField duration = fields.durationField(0);
        duration.setText("5");

        assertThat(fields.readSlots()[0].getTimeSeconds()).isEqualTo(5 * 3600L);
        assertThat(InputParser.parseDurationSeconds(duration.getText(), TimeUnit.HOURS)).isEqualTo(18000L);
    }
}
