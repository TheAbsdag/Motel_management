package view.interfaces;

import javax.swing.JLabel;

/**
 * Interface for view panels that have time and date labels.
 * Enables polymorphic access from {@link UserGUI} without
 * the previous {@code instanceof} chain.
 *
 * <p>Compatible with JFormDesigner — implement this interface
 * on any JPanel that exposes time/date display labels.
 */
public interface TimeLabelInterface {

    /** Returns the label used to display the current time (e.g. "03:45 PM"). */
    JLabel getTimeLabel();

    /** Returns the label used to display the current date (e.g. "2 de mayo de 2026"). */
    JLabel getDateLabel();
}
