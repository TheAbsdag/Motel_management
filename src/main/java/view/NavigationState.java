package view;

/**
 * Shared navigation state for tower/floor position across FloorView and RoomChangeView.
 * Eliminates duplicated index tracking that previously existed in both views independently.
 */
public class NavigationState {

    private int currentTowerIndex;
    private int currentFloorIndex;

    public NavigationState() {
        this.currentTowerIndex = 0;
        this.currentFloorIndex = 0;
    }

    public int getCurrentTowerIndex() { return currentTowerIndex; }
    public void setCurrentTowerIndex(int index) { this.currentTowerIndex = index; }

    public int getCurrentFloorIndex() { return currentFloorIndex; }
    public void setCurrentFloorIndex(int index) { this.currentFloorIndex = index; }
}
