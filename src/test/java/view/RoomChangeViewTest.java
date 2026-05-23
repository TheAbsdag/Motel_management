package view;

import java.awt.Component;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomChangeViewTest {

    private RoomChangeView view;

    @BeforeEach
    void setUp() {
        view = new RoomChangeView(new NavigationState());
        view.createButtonsForTowers(new int[][]{{3}});
    }

    /**
     * Verifies that clicking the backButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the back button.
     * Failure: The back button's action listener is not wired to the onBackButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the confirmButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the confirm button.
     * Failure: The confirm button's action listener is not wired to the onConfirmButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenConfirmButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onConfirmButton(() -> invoked.set(true));
        clickButton(view, "confirmButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the floor-up button fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the up button.
     * Failure: The up button's action listener is not wired to the onFloorUp callback.
     */
    @Test
    void shouldInvokeCallbackWhenFloorUpClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onFloorUp(() -> invoked.set(true));
        clickButton(view, "upButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the floor-down button fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the down button.
     * Failure: The down button's action listener is not wired to the onFloorDown callback.
     */
    @Test
    void shouldInvokeCallbackWhenFloorDownClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onFloorDown(() -> invoked.set(true));
        clickButton(view, "downButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the previous-tower button fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the previous tower button.
     * Failure: The previous tower button's action listener is not wired to the onPreviousTower callback.
     */
    @Test
    void shouldInvokeCallbackWhenPreviousTowerClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onPreviousTower(() -> invoked.set(true));
        clickButton(view, "previousTowerButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the next-tower button fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the next tower button.
     * Failure: The next tower button's action listener is not wired to the onNextTower callback.
     */
    @Test
    void shouldInvokeCallbackWhenNextTowerClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onNextTower(() -> invoked.set(true));
        clickButton(view, "nextTowerButton");
        assertThat(invoked).isTrue();
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
     * Verifies that the selected room label can be set and matches the displayed text.
     * Expected: setSelectedLabel("T1-101") causes the label text to be "T1-101".
     * Failure: The selected label text is not being set or the label field name has changed.
     */
    @Test
    void shouldSetSelectedLabel() throws Exception {
        view.setSelectedLabel("T1-101");
        assertThat(textOf(view, "selectedLabel")).isEqualTo("T1-101");
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
        assertThat(view.getCurrentFloorIndex()).isEqualTo(0);
    }

    /**
     * Verifies that room button count for a floor is greater than zero after tower creation.
     * Expected: getRoomCount(0, 0) returns a value greater than 0.
     * Failure: Room buttons were not created for the tower or the count method returns zero.
     */
    @Test
    void shouldGetRoomCount() {
        assertThat(view.getRoomCount(0, 0)).isGreaterThan(0);
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
