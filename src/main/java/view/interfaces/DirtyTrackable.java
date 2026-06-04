package view.interfaces;

/**
 * Interface for views that track unsaved changes.
 * Implementations maintain a dirty flag that is set when the user
 * modifies any field and cleared when changes are saved or discarded.
 */
public interface DirtyTrackable {
    void markDirty();
    void clearDirty();
    boolean isDirty();
}