package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FloorConfigurationViewTest {

    private FloorConfigurationView view;

    @BeforeEach
    void setUp() {
        view = new FloorConfigurationView();
    }

    // --- callbacks ---

    /**
     * Verifies that clicking the backButton fires the callback registered via
     * {@link FloorConfigurationView#onBackButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The back button action listener is not wired to the onBackButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the saveButton fires the callback registered via
     * {@link FloorConfigurationView#onSaveButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The save button action listener is not wired to the onSaveButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenSaveButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onSaveButton(() -> invoked.set(true));
        clickButton(view, "saveButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the deleteTowerButton fires the callback registered via
     * {@link FloorConfigurationView#onDeleteTower(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The delete-tower button action listener is not wired to the onDeleteTower callback.
     */
    @Test
    void shouldInvokeCallbackWhenDeleteTowerClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDeleteTower(() -> invoked.set(true));
        clickButton(view, "deleteTowerButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the newTowerButton fires the callback registered via
     * {@link FloorConfigurationView#onNewTower(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The new-tower button action listener is not wired to the onNewTower callback.
     */
    @Test
    void shouldInvokeCallbackWhenNewTowerClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onNewTower(() -> invoked.set(true));
        clickButton(view, "newTowerButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the deleteFloorButton fires the callback registered via
     * {@link FloorConfigurationView#onDeleteFloor(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The delete-floor button action listener is not wired to the onDeleteFloor callback.
     */
    @Test
    void shouldInvokeCallbackWhenDeleteFloorClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDeleteFloor(() -> invoked.set(true));
        clickButton(view, "deleteFloorButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the newFloorButton fires the callback registered via
     * {@link FloorConfigurationView#onNewFloor(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The new-floor button action listener is not wired to the onNewFloor callback.
     */
    @Test
    void shouldInvokeCallbackWhenNewFloorClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onNewFloor(() -> invoked.set(true));
        clickButton(view, "newFloorButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the newRoomButton fires the callback registered via
     * {@link FloorConfigurationView#onNewRoomButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The new-room button action listener is not wired to the onNewRoomButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenNewRoomClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onNewRoomButton(() -> invoked.set(true));
        clickButton(view, "newRoomButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the towerListLeftButton fires the callback registered via
     * {@link FloorConfigurationView#onTowerListLeft(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The tower-list-left button action listener is not wired to the onTowerListLeft callback.
     */
    @Test
    void shouldInvokeCallbackWhenTowerListLeftClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onTowerListLeft(() -> invoked.set(true));
        clickButton(view, "towerListLeftButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the towerListRightButton fires the callback registered via
     * {@link FloorConfigurationView#onTowerListRight(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The tower-list-right button action listener is not wired to the onTowerListRight callback.
     */
    @Test
    void shouldInvokeCallbackWhenTowerListRightClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onTowerListRight(() -> invoked.set(true));
        clickButton(view, "towerListRightButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the floorListUpButton fires the callback registered via
     * {@link FloorConfigurationView#onFloorListUp(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The floor-list-up button action listener is not wired to the onFloorListUp callback.
     */
    @Test
    void shouldInvokeCallbackWhenFloorListUpClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onFloorListUp(() -> invoked.set(true));
        clickButton(view, "floorListUpButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the floorListDownButton fires the callback registered via
     * {@link FloorConfigurationView#onFloorListDown(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The floor-list-down button action listener is not wired to the onFloorListDown callback.
     */
    @Test
    void shouldInvokeCallbackWhenFloorListDownClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onFloorListDown(() -> invoked.set(true));
        clickButton(view, "floorListDownButton");
        assertThat(invoked).isTrue();
    }

    // --- state ---

    /**
     * Verifies the dirty-state tracking lifecycle via {@link FloorConfigurationView#isDirty()},
     * {@link FloorConfigurationView#markDirty()}, and {@link FloorConfigurationView#clearDirty()}.
     * Expected: isDirty() returns false initially, true after markDirty(), and false after clearDirty().
     * Failure: The dirty state flag is not correctly managed, which could cause unsaved configuration
     *          changes to be lost or prompt the user incorrectly.
     */
    @Test
    void shouldTrackDirtyState() {
        assertThat(view.isDirty()).isFalse();
        view.markDirty();
        assertThat(view.isDirty()).isTrue();
        view.clearDirty();
        assertThat(view.isDirty()).isFalse();
    }

    /**
     * Verifies that tower items can be set and selected via
     * {@link FloorConfigurationView#setTowerItems(String[])} and {@link FloorConfigurationView#selectTower(int)}.
     * Expected: After setting 2 towers, getTowerCount() returns 2. After selecting tower index 1,
     *           getSelectedTowerIndex() returns 1.
     * Failure: The tower list component is not updated or the selection index is not reflected correctly.
     */
    @Test
    void shouldSetAndSelectTowerItems() {
        view.setTowerItems(new String[]{"Torre 1", "Torre 2"});
        assertThat(view.getTowerCount()).isEqualTo(2);
        view.selectTower(1);
        assertThat(view.getSelectedTowerIndex()).isEqualTo(1);
    }

    /**
     * Verifies that floor items can be set and selected via
     * {@link FloorConfigurationView#setFloorItems(String[])} and {@link FloorConfigurationView#selectFloor(int)}.
     * Expected: After setting 3 floors, getFloorCount() returns 3. After selecting floor index 0,
     *           getSelectedFloorIndex() returns 0.
     * Failure: The floor list component is not updated or the selection index is not reflected correctly.
     */
    @Test
    void shouldSetAndSelectFloorItems() {
        view.setFloorItems(new String[]{"Piso 1", "Piso 2", "Piso 3"});
        assertThat(view.getFloorCount()).isEqualTo(3);
        view.selectFloor(0);
        assertThat(view.getSelectedFloorIndex()).isEqualTo(0);
    }

    /**
     * Verifies that a freshly constructed FloorConfigurationView defaults to tower index 0 and floor index 0.
     * Expected: getCurrentTowerIndex() and getCurrentFloorIndex() both return 0.
     * Failure: The initial state indices are not zero, which could cause out-of-bounds access on empty data sets.
     */
    @Test
    void shouldDefaultToTowerAndFloorZero() {
        assertThat(view.getCurrentTowerIndex()).isEqualTo(0);
        assertThat(view.getCurrentFloorIndex()).isEqualTo(0);
    }

    /**
     * Verifies that {@link FloorConfigurationView#createRoomButtons(int[][])} initializes room buttons
     * from a 2D room-count array without throwing exceptions.
     * Expected: After createRoomButtons with a single tower having 3 rooms, getCurrentTowerIndex() returns 0
     *           and the method completes successfully.
     * Failure: The room button creation fails, indicating a bug in the dynamic button generation logic
     *          or an array index mismatch.
     */
    @Test
    void shouldCreateRoomButtons() {
        view.createRoomButtons(new int[][]{{3}});
        assertThat(view.getCurrentTowerIndex()).isEqualTo(0);
    }

    // --- helpers ---

    private static void clickButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }
}
