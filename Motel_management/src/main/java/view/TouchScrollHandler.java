package view;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * Enables drag-to-scroll on any JScrollPane for touch-screen use.
 * Attach via {@link #attach(JScrollPane)}.
 *
 * <p>A short press-without-drag still produces normal clicks (cell selection),
 * while a press-and-drag scrolls the viewport content.</p>
 */
public class TouchScrollHandler extends MouseAdapter {

    private final JScrollPane scrollPane;
    private final JViewport viewport;
    private Point startViewPos;
    private Point startMousePoint;
    private boolean dragging;
    private static final int DRAG_THRESHOLD = 8;

    private TouchScrollHandler(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
        this.viewport = scrollPane.getViewport();
    }

    /**
     * Enables touch-drag scrolling on the given scroll pane.
     */
    public static void attach(JScrollPane scrollPane) {
        TouchScrollHandler handler = new TouchScrollHandler(scrollPane);
        JViewport viewport = scrollPane.getViewport();

        // Listen on the child view (e.g. JTable) since it covers the
        // entire viewport and receives all mouse events.
        if (viewport.getView() != null) {
            viewport.getView().addMouseListener(handler);
            viewport.getView().addMouseMotionListener(handler);
        }

        // Also listen on the viewport for clicks in exposed areas
        // (e.g. below the last row or to the right of the last column).
        viewport.addMouseListener(handler);
        viewport.addMouseMotionListener(handler);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startMousePoint = e.getPoint();
        startViewPos = viewport.getViewPosition();
        dragging = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startMousePoint == null || startViewPos == null) {
            return;
        }

        int deltaX = startMousePoint.x - e.getX();
        int deltaY = startMousePoint.y - e.getY();

        if (!dragging && (Math.abs(deltaX) > DRAG_THRESHOLD
                || Math.abs(deltaY) > DRAG_THRESHOLD)) {
            dragging = true;
        }

        if (dragging) {
            int maxX = viewport.getView().getWidth() - viewport.getWidth();
            int maxY = viewport.getView().getHeight() - viewport.getHeight();

            int newX = Math.max(0, Math.min(startViewPos.x + deltaX, maxX));
            int newY = Math.max(0, Math.min(startViewPos.y + deltaY, maxY));

            viewport.setViewPosition(new Point(newX, newY));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
        startMousePoint = null;
        startViewPos = null;
    }
}
