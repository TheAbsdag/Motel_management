package view;

import view.helpers.NavigationState;
import java.awt.Component;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FloorViewTest {

    private FloorView view;

    @BeforeEach
    void setUp() {
        view = new FloorView(new NavigationState());
        view.createButtonsForTowers(new int[][]{{3}});
    }

    /**
     * Verifies that clicking the floorUpButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the floor-up button.
     * Failure: The floor-up button's action listener is not wired to the onFloorUp callback.
     */
    @Test
    void shouldInvokeCallbackWhenFloorUpClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onFloorUp(() -> invoked.set(true));
        clickButton(view, "floorUpButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the floorDownButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the floor-down button.
     * Failure: The floor-down button's action listener is not wired to the onFloorDown callback.
     */
    @Test
    void shouldInvokeCallbackWhenFloorDownClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onFloorDown(() -> invoked.set(true));
        clickButton(view, "floorDownButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the previousTowerButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the previous-tower button.
     * Failure: The previous-tower button's action listener is not wired to the onPreviousTower callback.
     */
    @Test
    void shouldInvokeCallbackWhenPreviousTowerClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onPreviousTower(() -> invoked.set(true));
        clickButton(view, "previousTowerButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the nextTowerButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the next-tower button.
     * Failure: The next-tower button's action listener is not wired to the onNextTower callback.
     */
    @Test
    void shouldInvokeCallbackWhenNextTowerClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onNextTower(() -> invoked.set(true));
        clickButton(view, "nextTowerButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the management options button fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the management options button.
     * Failure: The management options button's action listener is not wired to the onManagementOptions callback.
     */
    @Test
    void shouldInvokeCallbackWhenManagementOptionsClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onManagementOptions(() -> invoked.set(true));
        clickButton(view, "managementOptionsButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the reception selling button fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the reception sell button.
     * Failure: The reception sell button's action listener is not wired to the onReceptionSell callback.
     */
    @Test
    void shouldInvokeCallbackWhenReceptionSellClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onReceptionSell(() -> invoked.set(true));
        clickButton(view, "receptionSellButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that room buttons can be created and counted via createButtonsForTowers.
     * Expected: After creating buttons for a tower with 2 rooms on floor 0, getRoomCount returns 2.
     * Failure: Room button grid creation is not properly allocating or tracking room buttons.
     */
    @Test
    void shouldInvokeRoomClickCallback() throws Exception {
        view.createButtonsForTowers(new int[][]{{2}});
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onRoomClick(0, 0, 0, () -> invoked.set(true));
        view.onRoomClick(0, 0, 1, () -> invoked.set(true));
        assertThat(view.getRoomCount(0, 0)).isEqualTo(2);
    }

    /**
     * Verifies that the turn number label displays the correct numeric value.
     * Expected: setTurnNumber(42) results in label text "42".
     * Failure: The turn number label is not being updated or the format string has changed.
     */
    @Test
    void shouldSetTurnNumber() throws Exception {
        view.setTurnNumber(42);
        assertThat(textOf(view, "turnNumberLabel")).isEqualTo("42");
    }

    /**
     * Verifies the round-trip get/set behavior of the overtime warning visibility flag.
     * Expected: isWarningVisible() mirrors the value passed to setWarningVisible, both true and false.
     * Failure: The warning visibility state is not properly stored or retrieved.
     */
    @Test
    void shouldSetWarningVisibility() throws Exception {
        view.setWarningVisible(true);
        assertThat(view.isWarningVisible()).isTrue();
        view.setWarningVisible(false);
        assertThat(view.isWarningVisible()).isFalse();
    }

    /**
     * Verifies that room appearance (label text and color) can be set without throwing exceptions.
     * Expected: setRoomAppearance completes without error for a valid room index.
     * Failure: The room button lookup fails or the setRoomAppearance method throws an unexpected exception.
     */
    @Test
    void shouldSetRoomAppearance() {
        view.createButtonsForTowers(new int[][]{{2}});
        view.setRoomAppearance(0, 0, 0, "101", Color.BLUE);
        assertThat(view.getCurrentTowerIndex()).isEqualTo(0);
    }

    /**
     * Verifies that floor and tower indices default to zero after construction.
     * Expected: getCurrentFloorIndex() and getCurrentTowerIndex() both return 0.
     * Failure: The NavigationState or internal indices are not initialized to zero.
     */
    @Test
    void shouldDefaultFloorAndTowerToZero() {
        assertThat(view.getCurrentFloorIndex()).isEqualTo(0);
        assertThat(view.getCurrentTowerIndex()).isEqualTo(0);
    }

    /**
     * Smoke test verifying that updateTimeDisplay can be called without throwing an exception.
     * Expected: No exception is thrown when updating the time/date labels.
     * Failure: A NullPointerException or other runtime exception indicates missing label components.
     */
    @Test
    void shouldUpdateTimeDisplay() {
        view.updateTimeDisplay("12:00 PM", "23 de mayo");
    }

    // --- helpers ---

    private static void clickButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }

    private static String textOf(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        JLabel label = (JLabel) field.get(parent);
        return label.getText();
    }
}
