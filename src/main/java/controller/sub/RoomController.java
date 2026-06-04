package controller.sub;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import model.ProgramConfig;
import model.Room;
import model.RoomStatus;
import model.RoomTime;
import model.modelManagers.EmailConfigurationService;
import model.modelManagers.MotelManagement;
import view.FloorView;
import view.RoomChangeView;
import view.RoomView;
import view.UserGUI;
import view.helpers.DialogHelper;
import view.helpers.InputParser;
import view.helpers.TimeFormatter;

/**
 * Controls room-level operations: selection, booking, time extension, room changes.
 *
 * <p>Handles:
 * <ul>
 *   <li>Room selection from floor view and its status-based UI setup</li>
 *   <li>Service duration selection (3h, 12h, 24h) and price management</li>
 *   <li>Room booking (time sale), time extension, and check-out</li>
 *   <li>Room reassignment (move guest to another room)</li>
 *   <li>Real-time room status and time updates in RoomView and RoomChangeView</li>
 * </ul>
 */
public class RoomController {

    private final MotelManagement motelManager;
    private final FloorView floorView;
    private final RoomView roomView;
    private final RoomChangeView roomChangeView;
    private final UserGUI userInterface;
    private final Runnable onRoomSale;
    private final Runnable saveMainFiles;
    private final Runnable saveBackupFilesRoomSwap;
    private boolean isListAdjusting = false;

    /**
     * @param motelManager              the model
     * @param floorView                 the floor view (for room button listener setup)
     * @param roomView                  the room detail view
     * @param roomChangeView            the room change view
     * @param userInterface             the main window for view switching
     * @param onRoomSale                callback to start a room charge sale
     * @param saveMainFiles             callback to save main data files
     * @param saveBackupFilesRoomSwap   callback to save backup files as room operation
     */
    public RoomController(MotelManagement motelManager, FloorView floorView,
                          RoomView roomView, RoomChangeView roomChangeView,
                          UserGUI userInterface, Runnable onRoomSale,
                          Runnable saveMainFiles, Runnable saveBackupFilesRoomSwap) {
        this.motelManager = motelManager;
        this.floorView = floorView;
        this.roomView = roomView;
        this.roomChangeView = roomChangeView;
        this.userInterface = userInterface;
        this.onRoomSale = onRoomSale;
        this.saveMainFiles = saveMainFiles;
        this.saveBackupFilesRoomSwap = saveBackupFilesRoomSwap;
    }

    /** Registers action listeners for room view and room change view buttons. */
    public void initListeners() {
        // Room view buttons — using encapsulated listener registration
        roomView.onBackButton(this::showFloorPerspective);
        roomView.onRoomSellingButton(onRoomSale);
        roomView.onEndTimeButton(this::roomTimeEnd);
        roomView.onAddTimeButton(this::roomTimeSale);
        roomView.onRoomChangeButton(this::roomReassignment);

        // Room booking hour buttons (slot indices 0=3h, 1=12h, 2=24h)
        roomView.onBookingHourButton(0, () -> roomTimeModification(0));
        roomView.onBookingHourButton(1, () -> roomTimeModification(1));
        roomView.onBookingHourButton(2, () -> roomTimeModification(2));

        // Room price adjustment buttons
        roomView.onPriceAdjust(1000, () -> updateRoomPrice(1000));
        roomView.onPriceAdjust(-1000, () -> updateRoomPrice(-1000));
        roomView.onPriceAdjust(100, () -> updateRoomPrice(100));
        roomView.onPriceAdjust(-100, () -> updateRoomPrice(-100));

        // Room change view buttons
        roomChangeView.onFloorUp(() -> roomChangeViewFloorChange(1));
        roomChangeView.onFloorDown(() -> roomChangeViewFloorChange(-1));
        roomChangeView.onBackButton(this::showFloorPerspective);
        roomChangeView.onConfirmButton(this::changeRoomTime);

        // Cross-domain: room buttons on FloorView → this controller
        wireRoomGridListeners();
    }

    /**
     * Wires action listeners to all room buttons on FloorView and RoomChangeView.
     * Must be called after the button grids are rebuilt (e.g., after config changes).
     */
    public void wireRoomGridListeners() {
        int towers[][] = motelManager.getRoomsArray();
        for (int tower = 0; tower < towers.length; tower++) {
            for (int floor = 0; floor < towers[tower].length; floor++) {
                for (int room = 0; room < towers[tower][floor]; room++) {
                    final int currentTower = tower;
                    final int currentFloor = floor;
                    final int currentRoom = room;
                    floorView.onRoomClick(tower, floor, room,
                        () -> roomSelected(currentTower, currentFloor, currentRoom));
                    roomChangeView.onRoomClick(tower, floor, room,
                        () -> roomChangeSelected(currentTower, currentFloor, currentRoom));
                }
            }
        }
    }

    // ========== Room Selection & Display ==========

    /**
     * Handles room selection from the floor view.
     * Sets up the RoomView UI based on the room's current status.
     *
     * @param tower tower index
     * @param floor floor index
     * @param room  room index
     */
    public void roomSelected(int tower, int floor, int room) {
        // Store selection in model for cross-controller access (e.g., SellingController)
        motelManager.setCurrentFloorRoom(tower, floor, room);
        motelManager.setCurrentServiceDesired(0);
        Room targetRoom = motelManager.getRoom(tower, floor, room);
        String roomText = targetRoom.getRoomString();
        RoomStatus roomStatus = targetRoom.getStatus();

        roomView.setRoomNumber(roomText);
        roomView.resetBookingHighlights();

        RoomTime[] timeData = targetRoom.getCustomRoomTimeData();
        roomView.setBookingButtonText(0, formatTimeText(timeData[0]));
        roomView.setBookingButtonText(1, formatTimeText(timeData[1]));
        roomView.setBookingButtonText(2, formatTimeText(timeData[2]));
        roomView.setDetailedSelectedTime("--");

        switch (roomStatus) {
            case FREE:
                roomView.setStatusBackground(new Color(39, 174, 96));
                roomView.setStatusLabels("LIBRE", " ");
                roomView.showTimeInfo(false, false, false);
                roomView.showPriceControls(true);
                roomView.setDisplayPrice("0");
                roomView.showActionButtons(false, false, true, false);
                roomView.setAddTimeEnabled(false);
                roomView.showBookingButtons(true);
                break;

            case CLEANING:
                roomView.setStatusBackground(new Color(93, 173, 226));
                roomView.setStatusLabels("LIMPIEZA", "LIMPIEZA");
                roomView.showTimeInfo(true, false, true);
                roomView.showPriceControls(false);
                roomView.showActionButtons(false, true, false, false);
                roomView.showBookingButtons(false);
                break;

            case OCCUPIED:
                roomView.setStatusBackground(new Color(205, 97, 85));
                roomView.setStatusLabels("SERVICIO", "OCUPADA");
                roomView.showTimeInfo(true, true, true);
                roomView.showPriceControls(true);
                roomView.setDisplayPrice("0");
                roomView.showActionButtons(true, true, true, true);
                roomView.setAddTimeEnabled(false);
                roomView.showBookingButtons(true);
                break;
        }
        userInterface.setRoomView();
    }

    // ========== Service / Price Management ==========

    /**
     * Sets the desired service duration and price from the room's custom time data.
     * Highlights the selected booking button and updates the time label.
     *
     * @param slotIndex 0, 1, or 2 (the pricing tier)
     */
    public void roomTimeModification(int slotIndex) {
        int tower = motelManager.getCurrentTowerViewed();
        int floor = motelManager.getCurrentFloorViewed();
        int room = motelManager.getCurrentRoomViewed();
        Room targetRoom = motelManager.getRoom(tower, floor, room);
        RoomTime[] timeData = targetRoom.getCustomRoomTimeData();
        if (slotIndex < 0 || slotIndex >= timeData.length) return;

        RoomTime slot = timeData[slotIndex];
        long price = slot.getPrice();
        long seconds = slot.getTimeSeconds();

        roomView.setDisplayPrice(String.valueOf(price));
        roomView.setAddTimeEnabled(true);
        motelManager.setCurrentServiceDesired(seconds);

        roomView.setBookingButtonHighlight(slotIndex);

        roomView.setDetailedSelectedTime(formatTimeText(slot));
    }

    /**
     * Formats a duration (from RoomTime) as a human-readable string via {@link TimeFormatter#formatDuration}.
     * @return e.g. "12h" for whole hours, "45min" for less than an hour
     */
    private static String formatTimeText(RoomTime rt) {
        return TimeFormatter.formatDuration(rt.getTimeSeconds());
    }

    /**
     * Adjusts the displayed room price by the given delta.
     */
    public void updateRoomPrice(long delta) {
        long currentPrice = InputParser.parseLongSafe(roomView.getDisplayPrice());
        long newPrice = currentPrice + delta;
        if (newPrice > 0) {
            roomView.setDisplayPrice(String.valueOf(newPrice));
        }
    }

    // ========== Booking Operations ==========

    /**
     * Completes a room booking or time extension sale.
     * Saves the booking, optionally prints a receipt, and returns to floor view.
     */
    public void roomTimeSale() {
        motelManager.timeInformationUpdate();
        int towerNumber = motelManager.getCurrentTowerViewed();
        int roomNumber = motelManager.getCurrentRoomViewed();
        int floorNumber = motelManager.getCurrentFloorViewed();
        long serviceDuration = motelManager.getCurrentServiceDesired();
        long price = InputParser.parseLongSafe(roomView.getDisplayPrice());
        boolean print = roomView.isPrintSelected();

        if (!print) {
            boolean noPrintingConfirmation = DialogHelper.confirmPrinting();
            if (noPrintingConfirmation) {
                motelManager.registerRoomTimeAdded(towerNumber, floorNumber, roomNumber, serviceDuration, price, false);
                userInterface.setFloorView();
                saveMainFiles.run();
                saveBackupFilesRoomSwap.run();
            }
        } else {
            motelManager.registerRoomTimeAdded(towerNumber, floorNumber, roomNumber, serviceDuration, price, true);
            userInterface.setFloorView();
            saveMainFiles.run();
            saveBackupFilesRoomSwap.run();
        }
    }

    /**
     * Ends the current room service (check-out).
     * Sets room to cleaning status or frees it if already cleaning.
     */
    public void roomTimeEnd() {
        motelManager.timeInformationUpdate();
        int towerNumber = motelManager.getCurrentTowerViewed();
        int roomNumber = motelManager.getCurrentRoomViewed();
        int floorNumber = motelManager.getCurrentFloorViewed();
        motelManager.registerRoomTimeEnd(towerNumber, floorNumber, roomNumber);
        saveMainFiles.run();
        saveBackupFilesRoomSwap.run();
        attemptRoomEmail(towerNumber, floorNumber, roomNumber);
        userInterface.setFloorView();
    }

    private void attemptRoomEmail(int tower, int floor, int room) {
        EmailConfigurationService emailSvc = motelManager.getEmailConfigurationService();
        if (!emailSvc.isEmailEnabled() || !emailSvc.validateCaseConfig(0)) return;
        Room roomObj = motelManager.getRoom(tower, floor, room);
        if (roomObj == null) return;
        ProgramConfig cfg = motelManager.getProgramConfig();
        Map<String, String> placeholders = java.util.Map.of(
                "{motelName}", cfg.getMotelName(),
                "{motelAddress}", cfg.getMotelAddress(),
                "{motelID}", cfg.getMotelID(),
                "{roomString}", roomObj.getRoomString(),
                "{towerNumber}", String.valueOf(tower),
                "{floorNumber}", String.valueOf(floor),
                "{date}", java.time.LocalDate.now().toString());
        EmailController.sendEmailAsync(0, placeholders, List.of(), emailSvc);
    }

    // ========== Room Reassignment ==========

    /** Opens the room change view for the currently selected room. */
    public void roomReassignment() {
        userInterface.setRoomChangeView();
    }

    /**
     * Handles room selection in the room change view.
     * Validates the target room is available.
     */
    public void roomChangeSelected(int currentTower, int currentFloor, int currentRoom) {
        String roomString = motelManager.getRoom(currentTower, currentFloor, currentRoom).getRoomString();
        RoomStatus status = motelManager.getRoom(currentTower, currentFloor, currentRoom).getStatus();
        motelManager.setDesiredRoomChange(currentTower, currentFloor, currentRoom);
        if (status == RoomStatus.OCCUPIED) {
            roomChangeView.setSelectedLabel("NO DISPONIBLE");
        } else {
            roomChangeView.setSelectedLabel(roomString);
        }
    }

    /** Changes the floor being viewed in the room change view. */
    public void roomChangeViewFloorChange(int direction) {
        int currentFloor = roomChangeView.getCurrentFloorIndex();
        roomChangeView.switchFloor(currentFloor + direction);
    }

    /**
     * Confirms the room change operation.
     * Moves the guest from the current room to the selected target room.
     */
    public void changeRoomTime() {
        motelManager.timeInformationUpdate();
        boolean validReturn = motelManager.changeRoomTimeToAnother();
        if (validReturn) {
            saveMainFiles.run();
            saveBackupFilesRoomSwap.run();
            userInterface.setFloorView();
        }
    }

    // ========== Timer Updates ==========

    /**
     * Updates time/status information on RoomView.
     * Called periodically from the main timer loop.
     */
    public void updateRoomView() {
        int floor = motelManager.getCurrentFloorViewed();
        int room = motelManager.getCurrentRoomViewed();
        int tower = motelManager.getCurrentTowerViewed();
        RoomStatus status = motelManager.getRoom(tower, floor, room).getStatus();
        switch (status) {
            case CLEANING -> {
                String startTime = motelManager.getStartTimeRoom(tower, floor, room);
                String startDateClean = motelManager.getStartDateRoom(tower, floor, room);
                roomView.setStartTimeLabel(startTime);
                roomView.setStartDateLabel(startDateClean);
            }
            case OCCUPIED -> {
                String startTimeRoom = motelManager.getStartTimeRoom(tower, floor, room);
                String startDate = motelManager.getStartDateRoom(tower, floor, room);
                String remainingTime = motelManager.getRemainingTimeRoom(tower, floor, room);
                if (remainingTime.contains("-")) {
                    roomView.setOvertimeWarning(true);
                }
                roomView.setStartTimeLabel(startTimeRoom);
                roomView.setRemainingTimeLabel(remainingTime);
                roomView.setStartDateLabel(startDate);
            }
        }
    }

    /**
     * Updates room buttons on the RoomChangeView with current status.
     * Called periodically from the main timer loop.
     */
    public void updateRoomChangeView() {
        int roomArray[][] = motelManager.getRoomsArray();
        for (int tower = 0; tower < roomArray.length; tower++) {
            for (int floor = 0; floor < roomArray[tower].length; floor++) {
                for (int room = 0; room < roomArray[tower][floor]; room++) {
                    RoomStatus status = motelManager.getRoom(tower, floor, room).getStatus();
                    String roomString = motelManager.getRoom(tower, floor, room).getRoomString();
                    switch (status) {
                        case FREE -> {
                            roomChangeView.setRoomAppearance(tower, floor, room,
                                    "<html><center>" + roomString + "</center></html>",
                                    new Color(39, 174, 96));
                        }
                        case CLEANING -> {
                            roomChangeView.setRoomAppearance(tower, floor, room,
                                    "<html><center>" + roomString + "</center></html>",
                                    new Color(84, 153, 199));
                        }
                        case OCCUPIED -> {
                            roomChangeView.setRoomAppearance(tower, floor, room,
                                    "<html><center>NO DISPONIBLE</center></html>",
                                    new Color(231, 76, 60));
                        }
                    }
                }
            }
        }
    }

    // ========== Navigation Helpers ==========

    private void showFloorPerspective() {
        userInterface.setFloorView();
    }
}
