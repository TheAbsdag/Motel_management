package view.interfaces;

/**
 * Interface for view panels that display time and date.
 * Enables polymorphic time-display updates from {@link view.UserGUI}
 * without exposing internal Swing components.
 *
 * <p>Compatible with JFormDesigner — implement this interface
 * on any JPanel that hosts time/date display labels.
 * Implementors own their internal labels and update them
 * in response to this callback.
 */
public interface TimeLabelInterface {

    /**
     * Updates the time and date text displayed on this panel.
     *
     * @param timeText formatted current time (e.g. "03:45 PM")
     * @param dateText formatted current date (e.g. "2 de mayo de 2026")
     */
    void updateTimeDisplay(String timeText, String dateText);
}
