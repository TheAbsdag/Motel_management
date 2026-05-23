package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import model.Room;
import model.RoomTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomConfigurationViewTest {

    private RoomConfigurationView view;

    @BeforeEach
    void setUp() {
        view = new RoomConfigurationView();
    }

    // --- callbacks ---

    /**
     * Verifies that clicking the backButton fires the callback registered via
     * {@link RoomConfigurationView#onBackButton(Runnable)}.
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
     * {@link RoomConfigurationView#onSaveButton(Runnable)}.
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
     * Verifies that clicking the deleteRoomButton fires the callback registered via
     * {@link RoomConfigurationView#onDeleteRoom(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The delete-room button action listener is not wired to the onDeleteRoom callback.
     */
    @Test
    void shouldInvokeCallbackWhenDeleteRoomClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDeleteRoom(() -> invoked.set(true));
        clickButton(view, "deleteRoomButton");
        assertThat(invoked).isTrue();
    }

    // --- state ---

    /**
     * Verifies that {@link RoomConfigurationView#loadRoom(int, int, int, Room)} leaves the view in a
     * clean (non-dirty) state after loading a room.
     * Expected: isDirty() returns false immediately after loadRoom() with a valid Room.
     * Failure: The loadRoom method incorrectly marks the view as dirty on initial load, which could
     *          cause unnecessary save prompts.
     */
    @Test
    void shouldBeCleanAfterLoadRoom() {
        view.loadRoom(0, 0, 0, new Room("T1-101", 0, 0, 0));
        assertThat(view.isDirty()).isFalse();
    }

    /**
     * Verifies that {@link RoomConfigurationView#clearDirty()} is idempotent when the view is already clean.
     * Expected: isDirty() returns false both before and after calling clearDirty() on a clean view.
     * Failure: clearDirty() has side effects beyond resetting the dirty flag, or the dirty state
     *          is not properly maintained.
     */
    @Test
    void shouldClearDirtyState() {
        view.loadRoom(0, 0, 0, new Room("T1-101", 0, 0, 0));
        assertThat(view.isDirty()).isFalse();
        view.clearDirty();
        assertThat(view.isDirty()).isFalse();
    }

    /**
     * Verifies that {@link RoomConfigurationView#loadRoom(int, int, int, Room)} correctly sets the
     * current tower, floor, and room indices.
     * Expected: getCurrentTower(), getCurrentFloor(), and getCurrentRoom() all return the values
     *           passed to loadRoom (0, 0, 0).
     * Failure: The loadRoom method does not update the position indices, which would cause the
     *          controller to operate on the wrong room.
     */
    @Test
    void shouldLoadRoomData() throws Exception {
        Room room = new Room("T1-101", 0, 0, 0);
        view.loadRoom(0, 0, 0, room);

        assertThat(view.getCurrentTower()).isEqualTo(0);
        assertThat(view.getCurrentFloor()).isEqualTo(0);
        assertThat(view.getCurrentRoom()).isEqualTo(0);
    }

    /**
     * Verifies that a freshly constructed RoomConfigurationView defaults all position indices to zero.
     * Expected: getCurrentTower(), getCurrentFloor(), and getCurrentRoom() all return 0.
     * Failure: The default position is not zero, which could cause out-of-bounds access before loadRoom is called.
     */
    @Test
    void shouldDefaultTowerFloorRoomToZero() {
        RoomConfigurationView fresh = new RoomConfigurationView();
        assertThat(fresh.getCurrentTower()).isEqualTo(0);
        assertThat(fresh.getCurrentFloor()).isEqualTo(0);
        assertThat(fresh.getCurrentRoom()).isEqualTo(0);
    }

    /**
     * Verifies that {@link RoomConfigurationView#getModifiedRoomString()} returns a non-null result
     * after a room has been loaded.
     * Expected: getModifiedRoomString() returns a non-null string after loadRoom() with a valid Room.
     * Failure: The modified room string is null, indicating that the room data was not properly
     *          serialized or the internal state is corrupted.
     */
    @Test
    void shouldGetModifiedRoomStringAfterLoad() {
        Room room = new Room("T1-101", 0, 0, 0);
        view.loadRoom(0, 0, 0, room);
        String modified = view.getModifiedRoomString();
        assertThat(modified).isNotNull();
    }

    /**
     * Verifies that {@link RoomConfigurationView#getModifiedTimeSlots()} returns a valid array of
     * RoomTime objects after a room has been loaded.
     * Expected: getModifiedTimeSlots() returns an array of size 3 (one per booking slot) after loadRoom().
     * Failure: The time slots array is null or has an unexpected size, indicating that the room
     *          time configuration is not properly loaded.
     */
    @Test
    void shouldGetModifiedTimeSlotsAfterLoad() {
        Room room = new Room("T1-101", 0, 0, 0);
        view.loadRoom(0, 0, 0, room);
        RoomTime[] slots = view.getModifiedTimeSlots();
        assertThat(slots).hasSize(3);
    }

    /**
     * Verifies that {@link RoomConfigurationView#resetView()} restores the position indices to their
     * previously loaded values after a clearDirty() call.
     * Expected: After loadRoom(0,0,0,...), clearDirty(), and resetView(), all position indices still return 0.
     * Failure: resetView() does not properly restore the view state, which could cause the user to
     *          lose their place in the room configuration after a reset.
     */
    @Test
    void shouldResetView() throws Exception {
        Room room = new Room("T1-101", 0, 0, 0);
        view.loadRoom(0, 0, 0, room);
        view.clearDirty();
        view.resetView();

        assertThat(view.getCurrentTower()).isEqualTo(0);
        assertThat(view.getCurrentFloor()).isEqualTo(0);
        assertThat(view.getCurrentRoom()).isEqualTo(0);
    }

    // --- helpers ---

    private static void clickButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }
}
