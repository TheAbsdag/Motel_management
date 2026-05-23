package controller.sub;

import java.awt.Color;
import model.modelManagers.MotelManagement;
import model.RoomStatus;
import view.FloorView;

/**
 * Controls the floor/tower navigation and automatic floor rotation.
 *
 * <p>Handles:
 * <ul>
 *   <li>Floor up/down navigation</li>
 *   <li>Tower previous/next switching</li>
 *   <li>Automatic floor cycling (screensaver-like rotation)</li>
 *   <li>Real-time room button color/status updates</li>
 * </ul>
 */
public class FloorController {

    private final MotelManagement motelManager;
    private final FloorView floorView;
    private boolean goingUp = false;

    public FloorController(MotelManagement motelManager, FloorView floorView) {
        this.motelManager = motelManager;
        this.floorView = floorView;
    }

    /** Registers action listeners for floor/tower navigation buttons. */
    public void initListeners() {
        floorView.onFloorUp(() -> changeFloor(1));
        floorView.onFloorDown(() -> changeFloor(-1));
        floorView.onPreviousTower(() -> changeTower(-1));
        floorView.onNextTower(() -> changeTower(1));
    }

    /**
     * Changes the currently viewed floor by the given direction (+1 up, -1 down).
     * Delegates to FloorView which manages the card layout internally.
     */
    public void changeFloor(int direction) {
        int currentFloorGUI = floorView.getCurrentFloorIndex();
        floorView.switchFloor(currentFloorGUI + direction);
    }

    /**
     * Changes the currently viewed tower by the given direction (+1 next, -1 previous).
     */
    public void changeTower(int direction) {
        int currentTowerGUI = floorView.getCurrentTowerIndex();
        floorView.switchTower(currentTowerGUI + direction);
    }

    /**
     * Cycles through floors automatically (screensaver-like rotation).
     * Direction reverses when reaching the top or bottom floor.
     */
    public void automaticFloorChange() {
        int[][] roomArray = motelManager.getRoomsArray();
        if (roomArray.length == 0 || roomArray[0].length == 0) {
            return;
        }
        int maxFloor = roomArray[0].length;
        if (goingUp) {
            if (floorView.getCurrentFloorIndex() < maxFloor - 1) {
                changeFloor(1);
            } else {
                goingUp = false;
            }
        } else {
            if (floorView.getCurrentFloorIndex() != 0) {
                changeFloor(-1);
            } else {
                goingUp = true;
            }
        }
    }

    /**
     * Automatically advances to the next tower. Only meaningful when multiple
     * towers exist. Called alongside {@link #automaticFloorChange()} so the
     * display cycles through all towers over time.
     */
    public void automaticTowerRotation() {
        int towerCount = motelManager.getRoomsArray().length;
        if (towerCount <= 1) {
            return;
        }
        int currentTower = floorView.getCurrentTowerIndex();
        int nextTower = (currentTower + 1) % towerCount;
        floorView.switchTower(nextTower);
    }

    /**
     * Updates all room buttons on the floor view with current status colors and text,
     * and refreshes the turn number label.
     * Called periodically from the main timer loop.
     *
     * <p>Color legend:
     * <ul>
     *   <li>Green ({@link Color#GREEN}) — FREE</li>
     *   <li>Blue — CLEANING</li>
     *   <li>Red — OCCUPIED (with remaining time)</li>
     *   <li>Yellow — OCCUPIED in overtime</li>
     * </ul>
     */
    public void updateRoomButtons() {
        floorView.setTurnNumber(motelManager.getTurnNumber());
        int roomArray[][] = motelManager.getRoomsArray();
        for (int tower = 0; tower < roomArray.length; tower++) {
            for (int floor = 0; floor < roomArray[tower].length; floor++) {
                for (int room = 0; room < roomArray[tower][floor]; room++) {
                    RoomStatus status = motelManager.getRoom(tower, floor, room).getStatus();
                    String roomString = motelManager.getRoom(tower, floor, room).getRoomString();

                    switch (status) {
                        case FREE:
                            floorView.setRoomAppearance(tower, floor, room,
                                    "<html><center>" + roomString + "</center></html>",
                                    new Color(39, 174, 96));
                            motelManager.removeFromOvertimeList(roomString);
                            break;
                        case CLEANING:
                            floorView.setRoomAppearance(tower, floor, room,
                                    "<html><center>" + roomString + "</center></html>",
                                    new Color(84, 153, 199));
                            motelManager.removeFromOvertimeList(roomString);
                            break;
                        case OCCUPIED:
                            String remainingTime = motelManager.getRemainingTimeRoom(tower, floor, room);
                            if (remainingTime.contains("-")) {
                                floorView.setRoomAppearance(tower, floor, room,
                                        "<html><center>" + roomString + "<br>SOBRETIEMPO</center></html>",
                                        new Color(241, 196, 15));
                                motelManager.addToOvertimeList(roomString);
                            } else {
                                floorView.setRoomAppearance(tower, floor, room,
                                        "<html><center>" + roomString + "<br>QUEDAN " + remainingTime + "</center></html>",
                                        new Color(231, 76, 60));
                                motelManager.removeFromOvertimeList(roomString);
                            }
                            break;
                    }
                }
            }
        }
    }
}
