package view.helpers;

import java.awt.EventQueue;
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

    private static volatile boolean scrolling;

    /** True while a touch-drag scroll is in progress. */
    public static boolean isScrolling() {
        return scrolling;
    }

    private final JScrollPane scrollPane;
    private final JViewport viewport;
    private Point startViewPos;
    private Point startScreenPoint;
    private boolean dragging;
    private static final int DRAG_THRESHOLD = 8;

    // Coalescing: setViewPosition() triggers a repaint.  Touch screens
    // fire mouseDragged at 60-120 Hz; repainting on every event floods
    // the EDT and causes choppy scrolling.  We queue at most one
    // deferred setViewPosition per event-dispatch cycle.
    private int targetX, targetY;
    private int lastAppliedX = Integer.MIN_VALUE;
    private int lastAppliedY = Integer.MIN_VALUE;
    private boolean pending;
    private boolean fightingBack;

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
            viewport.getView().addMouseWheelListener(handler);
        }

        // Also listen on the viewport for clicks in exposed areas
        // (e.g. below the last row or to the right of the last column).
        viewport.addMouseListener(handler);
        viewport.addMouseMotionListener(handler);
        viewport.addMouseWheelListener(handler);

        // Counter native OS scroll injection during touch-drag.
        // Windows converts edge-drags into WM_VSCROLL messages that
        // go directly to the native scrollbar peer, bypassing Java
        // mouse events and pulling the viewport away from our handler.
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (handler.dragging && !handler.fightingBack) {
                handler.fightingBack = true;
                try {
                    scrollPane.getVerticalScrollBar().setValue(handler.lastAppliedY);
                } finally {
                    handler.fightingBack = false;
                }
            }
        });
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(e -> {
            if (handler.dragging && !handler.fightingBack) {
                handler.fightingBack = true;
                try {
                    scrollPane.getHorizontalScrollBar().setValue(handler.lastAppliedX);
                } finally {
                    handler.fightingBack = false;
                }
            }
        });
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // While a drag is in progress the finger may stray onto the
        // viewport's exposed area (below the last row).  That would
        // fire a second mousePressed whose coordinates reset the
        // reference point and produce a "scroll-wheel" runaway effect.
        if (dragging) {
            return;
        }
        scrolling = true;
        startScreenPoint = e.getLocationOnScreen();
        startViewPos = viewport.getViewPosition();
        lastAppliedX = startViewPos.x;
        lastAppliedY = startViewPos.y;
        dragging = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startScreenPoint == null || startViewPos == null) {
            return;
        }

        Point screenPoint = e.getLocationOnScreen();
        int deltaX = startScreenPoint.x - screenPoint.x;
        int deltaY = startScreenPoint.y - screenPoint.y;

        if (!dragging && (Math.abs(deltaX) > DRAG_THRESHOLD
                || Math.abs(deltaY) > DRAG_THRESHOLD)) {
            dragging = true;
        }

        if (!dragging) {
            return;
        }

        int viewW = viewport.getView().getWidth();
        int viewH = viewport.getView().getHeight();
        int vpW = viewport.getWidth();
        int vpH = viewport.getHeight();
        int maxX = viewW - vpW;
        int maxY = viewH - vpH;

        int rawTargetX = startViewPos.x + deltaX;
        int rawTargetY = startViewPos.y + deltaY;
        targetX = Math.max(0, Math.min(rawTargetX, maxX));
        targetY = Math.max(0, Math.min(rawTargetY, maxY));

        // If the target hasn't moved since we last applied it, don't
        // queue another repaint.  This cuts off the flood when the
        // finger is past the scrollable bounds.
        if (targetX == lastAppliedX && targetY == lastAppliedY) {
            return;
        }

        if (!pending) {
            pending = true;
            EventQueue.invokeLater(this::applyScroll);
        }
    }

    private void applyScroll() {
        pending = false;
        viewport.setViewPosition(new Point(targetX, targetY));
        lastAppliedX = targetX;
        lastAppliedY = targetY;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        boolean wasDragging = dragging;
        scrolling = false;
        dragging = false;
        startScreenPoint = null;
        startViewPos = null;
        // Row heights were suppressed during drag (CustomCellRenderer
        // checks isScrolling()).  Force a repaint so newly-visible rows
        // get correct heights now that scrolling has ended.
        if (wasDragging && viewport.getView() != null) {
            viewport.getView().revalidate();
            viewport.getView().repaint();
        }
    }
}
