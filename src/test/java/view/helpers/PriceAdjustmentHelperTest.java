package view.helpers;

import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceAdjustmentHelperTest {

    /**
     * Verifies that {@link PriceAdjustmentHelper#adjust(JTextField, int)} adds a
     * positive delta to the current numeric value in the text field.
     * Expected: A field containing {@code "1000"} adjusted by +500 becomes {@code "1500"}.
     * Failure: Addition is incorrect (off-by-one, overflow) or the field text is
     *          not updated, breaking the price-increment UI controls.
     */
    @Test
    void shouldAddPositiveDelta() {
        JTextField field = new JTextField("1000");
        PriceAdjustmentHelper.adjust(field, 500);
        assertThat(field.getText()).isEqualTo("1500");
    }

    /**
     * Verifies that {@link PriceAdjustmentHelper#adjust(JTextField, int)}
     * subtracts a negative delta from the current numeric value.
     * Expected: A field containing {@code "1000"} adjusted by -300 becomes {@code "700"}.
     * Failure: Subtraction wraps around, ignores the negative sign, or produces
     *          an incorrect result, corrupting price-decrement functionality.
     */
    @Test
    void shouldSubtractDelta() {
        JTextField field = new JTextField("1000");
        PriceAdjustmentHelper.adjust(field, -300);
        assertThat(field.getText()).isEqualTo("700");
    }

    /**
     * Verifies that {@link PriceAdjustmentHelper#adjust(JTextField, int)} clamps
     * the result to the implicit minimum of zero when no explicit minimum is given.
     * Expected: A field containing {@code "100"} adjusted by -200 becomes {@code "0"}.
     * Failure: The value goes negative, meaning the clamp-to-zero guard is
     *          missing or ineffective, which could display negative prices in the UI.
     */
    @Test
    void shouldClampToMinValueWithDefaultZero() {
        JTextField field = new JTextField("100");
        PriceAdjustmentHelper.adjust(field, -200);
        assertThat(field.getText()).isEqualTo("0");
    }

    /**
     * Verifies that {@link PriceAdjustmentHelper#adjust(JTextField, int, int)}
     * clamps the result to the caller-specified minimum value.
     * Expected: A field containing {@code "100"} adjusted by -200 with min 50
     *           becomes {@code "50"}.
     * Failure: The custom minimum is ignored (value goes to zero or below 50),
     *          meaning the overload delegation or clamping logic is broken.
     */
    @Test
    void shouldClampToGivenMinValue() {
        JTextField field = new JTextField("100");
        PriceAdjustmentHelper.adjust(field, -200, 50);
        assertThat(field.getText()).isEqualTo("50");
    }

    /**
     * Verifies that {@link PriceAdjustmentHelper#adjust(JTextField, int, int)}
     * does not clamp the result when the adjusted value remains above the
     * specified minimum.
     * Expected: A field containing {@code "100"} adjusted by -30 with min 50
     *           becomes {@code "70"}.
     * Failure: The clamp is applied prematurely (value clamped to 50 instead of
     *          70), indicating an incorrect comparison or off-by-one in the guard.
     */
    @Test
    void shouldNotClampWhenAboveMinValue() {
        JTextField field = new JTextField("100");
        PriceAdjustmentHelper.adjust(field, -30, 50);
        assertThat(field.getText()).isEqualTo("70");
    }

    /**
     * Verifies that {@link PriceAdjustmentHelper#adjust(JTextField, int)} returns
     * the correctly adjusted numeric value after the operation.
     * Expected: A field containing {@code "200"} adjusted by +300 returns 500.
     * Failure: The return value does not match the field text, or the return
     *          value is stale/incorrect, breaking callers that rely on the
     *          return value for further computation.
     */
    @Test
    void shouldReturnAdjustedValue() {
        JTextField field = new JTextField("200");
        long result = PriceAdjustmentHelper.adjust(field, 300);
        assertThat(result).isEqualTo(500);
    }

    /**
     * Verifies that {@link PriceAdjustmentHelper#adjust(JTextField, int)} treats
     * an empty text field as zero, applying the delta from that baseline.
     * Expected: An empty field adjusted by +500 becomes {@code "500"}.
     * Failure: An empty field causes a parse error or leaves the field blank,
     *          meaning the helper does not handle the empty-case gracefully.
     */
    @Test
    void shouldHandleEmptyFieldAsZero() {
        JTextField field = new JTextField("");
        PriceAdjustmentHelper.adjust(field, 500);
        assertThat(field.getText()).isEqualTo("500");
    }
}
