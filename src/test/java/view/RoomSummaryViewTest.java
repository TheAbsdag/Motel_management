package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
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
        view.updateRoomSummary(statusData(1, 1, 1), stringData(1, 1, 1), overtimeData(1, 1, 1));
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

    /**
     * Verifies that a periodic refresh (every clock tick) keeps the same widgets
     * instead of rebuilding the whole grid.
     * Expected: the room label instance and the scroll pane survive the refresh.
     * Failure: the grid is recreated on every update, which resets the scroll
     *          position and breaks touch dragging.
     */
    @Test
    void shouldRefreshInPlaceWhenShapeDoesNotChange() throws Exception {
        int[][][] status = statusData(2, 4, 8);
        String[][][] names = stringData(2, 4, 8);
        boolean[][][] overtime = overtimeData(2, 4, 8);
        view.updateRoomSummary(status, names, overtime);

        JLabel firstRoom = findRoomLabel("1-101");
        JScrollPane scrollPane = scrollPane();

        fillStatus(status, 3);
        view.updateRoomSummary(status, names, overtime);

        assertThat(findRoomLabel("1-101")).isSameAs(firstRoom);
        assertThat(scrollPane()).isSameAs(scrollPane);
    }

    /**
     * Verifies that the scroll position survives the periodic refresh.
     * Expected: the viewport stays where the user left it after updateRoomSummary().
     * Failure: the summary is rebuilt on every tick and the user can never reach
     *          the floors that do not fit on screen.
     */
    @Test
    void shouldKeepScrollPositionWhenRefreshing() throws Exception {
        int[][][] status = statusData(2, 6, 8);
        String[][][] names = stringData(2, 6, 8);
        boolean[][][] overtime = overtimeData(2, 6, 8);
        view.setSize(800, 600);
        view.updateRoomSummary(status, names, overtime);
        layoutAll(view);

        JViewport viewport = scrollPane().getViewport();
        assertThat(viewport.getView().getHeight()).isGreaterThan(viewport.getExtentSize().height);
        viewport.setViewPosition(new Point(0, 40));

        view.updateRoomSummary(status, names, overtime);

        assertThat(viewport.getViewPosition().y).isEqualTo(40);
    }

    /**
     * Verifies that a status change only recolors the existing label.
     * Expected: the same label instance ends up with the occupied background.
     * Failure: the colors are not refreshed in place, so the summary shows stale states.
     */
    @Test
    void shouldRecolorRoomWhenStatusChanges() throws Exception {
        int[][][] status = statusData(1, 1, 1);
        String[][][] names = stringData(1, 1, 1);
        boolean[][][] overtime = overtimeData(1, 1, 1);
        status[0][0][0] = 1;
        view.updateRoomSummary(status, names, overtime);
        JLabel room = findRoomLabel("1-101");
        assertThat(room.getBackground()).isEqualTo(new Color(30, 132, 73));

        status[0][0][0] = 3;
        view.updateRoomSummary(status, names, overtime);

        assertThat(findRoomLabel("1-101")).isSameAs(room);
        assertThat(room.getBackground()).isEqualTo(new Color(192, 57, 43));
    }

    /**
     * Verifies the status counts shown in the legend next to the colors.
     * Expected: each count matches the number of rooms in that state, with
     *           overtime counted inside occupied rooms.
     * Failure: the legend is missing or the counts do not match the grid.
     */
    @Test
    void shouldCountRoomsByStatusInLegend() throws Exception {
        int[][][] status = statusData(1, 1, 4);
        String[][][] names = stringData(1, 1, 4);
        boolean[][][] overtime = overtimeData(1, 1, 4);
        status[0][0][0] = 1;
        status[0][0][1] = 2;
        status[0][0][2] = 3;
        status[0][0][3] = 3;
        overtime[0][0][3] = true;

        view.updateRoomSummary(status, names, overtime);

        String legend = legendText();
        assertThat(legend).contains("LIBRES 1", "LIMPIEZA 1", "OCUPADAS 2", "SOBRETIEMPO 1");
    }

    /**
     * Verifies that every room fits inside its tower card at a low resolution.
     * Expected: all chips are visible and at least 30 px wide with no horizontal
     *           clipping inside the tower, and an eight-room floor wraps into
     *           four balanced columns.
     * Failure: the chips overflow the card (the old fixed 50 px columns), so the
     *          last rooms of each floor are hidden.
     */
    @Test
    void shouldLayOutEveryRoomInsideItsTowerAtLowResolution() throws Exception {
        view.setSize(800, 600);
        view.updateRoomSummary(statusData(2, 4, 8), stringData(2, 4, 8), overtimeData(2, 4, 8));
        layoutAll(view);

        JPanel summary = summaryPanel();
        assertThat(summary.getComponentCount()).isEqualTo(2);

        int laidOutRooms = 0;
        List<Integer> columns = new ArrayList<>();
        for (Component card : summary.getComponents()) {
            assertThat(card).isInstanceOf(JPanel.class);
            Container cardPanel = (Container) card;
            for (Component child : allChildren(cardPanel)) {
                if (child instanceof JLabel label && isRoomLabel(label)) {
                    laidOutRooms++;
                    Rectangle bounds = SwingUtilities.convertRectangle(label.getParent(), label.getBounds(), cardPanel);
                    assertThat(bounds.width).isGreaterThanOrEqualTo(30);
                    assertThat(bounds.x).isGreaterThanOrEqualTo(0);
                    assertThat(bounds.x + bounds.width).isLessThanOrEqualTo(cardPanel.getWidth());
                    assertThat(bounds.y + bounds.height).isLessThanOrEqualTo(cardPanel.getHeight());
                }
                if (child instanceof JPanel panel && panel.getLayout() instanceof GridLayout grid
                        && panel.getComponentCount() > 0) {
                    columns.add(grid.getColumns());
                }
            }
        }
        assertThat(laidOutRooms).isEqualTo(64);
        assertThat(columns).isNotEmpty().allMatch(c -> c == 4);
    }

    // ========== Helpers ==========

    private static int[][][] statusData(int towers, int floors, int rooms) {
        return new int[towers][floors][rooms];
    }

    private static String[][][] stringData(int towers, int floors, int rooms) {
        String[][][] data = new String[towers][floors][rooms];
        for (int tower = 0; tower < towers; tower++) {
            for (int floor = 0; floor < floors; floor++) {
                for (int room = 0; room < rooms; room++) {
                    data[tower][floor][room] = (tower + 1) + "-" + ((floor + 1) * 100 + room + 1);
                }
            }
        }
        return data;
    }

    private static boolean[][][] overtimeData(int towers, int floors, int rooms) {
        return new boolean[towers][floors][rooms];
    }

    private static void fillStatus(int[][][] status, int code) {
        for (int tower = 0; tower < status.length; tower++) {
            for (int floor = 0; floor < status[tower].length; floor++) {
                for (int room = 0; room < status[tower][floor].length; room++) {
                    status[tower][floor][room] = code;
                }
            }
        }
    }

    private JLabel findRoomLabel(String roomString) throws Exception {
        for (Component card : summaryPanel().getComponents()) {
            for (Component child : allChildren((Container) card)) {
                if (child instanceof JLabel label && roomString.equals(label.getText())) {
                    return label;
                }
            }
        }
        throw new AssertionError("Room label not found: " + roomString);
    }

    private static boolean isRoomLabel(JLabel label) {
        return label.getText() != null && label.getText().matches("\\d+-\\d+");
    }

    private static List<Component> allChildren(Container root) {
        List<Component> children = new ArrayList<>();
        for (Component child : root.getComponents()) {
            children.add(child);
            if (child instanceof Container container) {
                children.addAll(allChildren(container));
            }
        }
        return children;
    }

    private JScrollPane scrollPane() throws Exception {
        Field field = RoomSummaryView.class.getDeclaredField("scrollPane");
        field.setAccessible(true);
        return (JScrollPane) field.get(view);
    }

    private JPanel summaryPanel() throws Exception {
        return (JPanel) scrollPane().getViewport().getView();
    }

    private String legendText() throws Exception {
        Field field = RoomSummaryView.class.getDeclaredField("legendLabel");
        field.setAccessible(true);
        return ((JLabel) field.get(view)).getText();
    }

    private static void clickButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }

    /** Lays out the view tree offscreen (the usual validate() needs a display). */
    private static void layoutAll(Container container) {
        container.doLayout();
        if (container instanceof JScrollPane scrollPane) {
            scrollPane.getViewport().doLayout();
            if (scrollPane.getViewport().getView() instanceof Container content) {
                layoutAll(content);
            }
        }
        for (Component child : container.getComponents()) {
            if (child instanceof Container childContainer && !(container instanceof JScrollPane)) {
                layoutAll(childContainer);
            }
        }
    }
}
