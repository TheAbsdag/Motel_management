package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomSummaryViewTest {

    private RoomSummaryView view;

    @BeforeEach
    void setUp() {
        view = new RoomSummaryView();
    }

    /**
     * Verifies that clicking the backButton fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on the back button.
     * Failure: The backButton's action listener is not wired to the onBackButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that updateRoomSummary() can be called with valid status, string, and overtime data arrays.
     * Expected: The method completes without exceptions, confirming the room summary grid API is wired.
     * Failure: The room summary grid components are not properly initialized or the array dimensions
     *          are incompatible, causing an IndexOutOfBoundsException or NullPointerException.
     */
    @Test
    void shouldUpdateRoomSummary() {
        int[][][] statusData = new int[1][1][1];
        String[][][] stringsData = new String[1][1][1];
        boolean[][][] overtimeData = new boolean[1][1][1];
        statusData[0][0][0] = 1;
        stringsData[0][0][0] = "T1-101";
        overtimeData[0][0][0] = false;

        view.updateRoomSummary(statusData, stringsData, overtimeData);
    }

    /**
     * Verifies that updateTimeDisplay() can be called without throwing exceptions.
     * Expected: The method completes normally, confirming the time display API is wired.
     * Failure: The time display labels are not properly initialized, causing a NullPointerException.
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
}
