package view.helpers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Attaches drag-to-scroll with momentum to JavaFX TableViews embedded in JFXPanel.
 * JFXPanel does not forward JavaFX GestureEvents (touch scroll), so this bridges
 * mouse drag events (which AWT forwards) into proportional scroll bar movement.
 */
public final class TableViewTouchHelper {

    private static final double SCROLL_SENSITIVITY = 1.8;
    private static final double MOMENTUM_DECAY = 0.92;
    private static final double MOMENTUM_THRESHOLD = 0.5;
    private static final double VELOCITY_THRESHOLD = 1.0;

    private TableViewTouchHelper() { }

    /**
     * Enables drag-to-scroll with momentum on the given TableView.
     * Call once per table after it has been added to a Scene.
     */
    public static void enableDragScroll(TableView<?> table) {
        ScrollBar[] scrollBar = {null};
        double[] lastY = {0};
        double[] velocity = {0};
        Timeline[] momentumAnim = {null};

        table.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (scrollBar[0] == null) scrollBar[0] = findVScrollBar(table);
            lastY[0] = e.getSceneY();
            velocity[0] = 0;
            if (momentumAnim[0] != null) {
                momentumAnim[0].stop();
                momentumAnim[0] = null;
            }
        });

        table.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (scrollBar[0] == null) return;
            double sceneY = e.getSceneY();
            double delta = lastY[0] - sceneY;
            lastY[0] = sceneY;

            double range = scrollBar[0].getMax() - scrollBar[0].getMin();
            double viewH = table.getHeight();
            if (range <= 0 || viewH <= 0) return;

            double fraction = delta / viewH;
            double valueDelta = fraction * range * SCROLL_SENSITIVITY;
            double newVal = scrollBar[0].getValue() + valueDelta;
            scrollBar[0].setValue(clamp(newVal, scrollBar[0].getMin(), scrollBar[0].getMax()));

            velocity[0] = valueDelta;
        });

        table.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (scrollBar[0] == null || Math.abs(velocity[0]) < VELOCITY_THRESHOLD) return;
            momentumAnim[0] = new Timeline(new KeyFrame(Duration.millis(16), ev -> {
                double cur = scrollBar[0].getValue();
                double next = cur - velocity[0];
                velocity[0] *= MOMENTUM_DECAY;
                if (Math.abs(velocity[0]) < MOMENTUM_THRESHOLD
                        || next <= scrollBar[0].getMin()
                        || next >= scrollBar[0].getMax()) {
                    momentumAnim[0].stop();
                    momentumAnim[0] = null;
                    return;
                }
                scrollBar[0].setValue(clamp(next, scrollBar[0].getMin(), scrollBar[0].getMax()));
            }));
            momentumAnim[0].setCycleCount(Animation.INDEFINITE);
            momentumAnim[0].play();
        });
    }

    /**
     * Scrolls the table by one row in the given direction and selects the target row.
     * @param direction 1 for down, -1 for up
     */
    public static void scrollByOneRow(TableView<?> table, int size, int direction) {
        if (table == null || size == 0) return;
        int idx = table.getSelectionModel().getSelectedIndex();
        if (idx < 0) idx = 0;
        int target = idx + direction;
        if (target >= 0 && target < size) {
            table.scrollTo(target);
            table.getSelectionModel().select(target);
        }
    }

    private static ScrollBar findVScrollBar(TableView<?> table) {
        for (Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar bar && bar.getOrientation() == Orientation.VERTICAL) {
                return bar;
            }
        }
        return null;
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
