package controller.sub;

import java.awt.Color;
import javax.swing.JButton;
import model.MotelManagement;
import model.Room;
import model.RoomStatus;
import view.FloorView;
import view.RoomChangeView;
import view.RoomView;
import view.UserGUI;

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
    private final Runnable saveBackupFilesOperation;
    private boolean isListAdjusting = false;

    /**
     * @param motelManager              the model
     * @param floorView                 the floor view (for room button listener setup)
     * @param roomView                  the room detail view
     * @param roomChangeView            the room change view
     * @param userInterface             the main window for view switching
     * @param onRoomSale                callback to start a room charge sale
     * @param saveBackupFilesOperation  callback to save backup files after an operation
     */
    public RoomController(MotelManagement motelManager, FloorView floorView,
                          RoomView roomView, RoomChangeView roomChangeView,
                          UserGUI userInterface, Runnable onRoomSale,
                          Runnable saveBackupFilesOperation) {
        this.motelManager = motelManager;
        this.floorView = floorView;
        this.roomView = roomView;
        this.roomChangeView = roomChangeView;
        this.userInterface = userInterface;
        this.onRoomSale = onRoomSale;
        this.saveBackupFilesOperation = saveBackupFilesOperation;
    }

    /** Registers action listeners for room view and room change view buttons. */
    public void initListeners() {
        // Room view buttons
        roomView.getBackRoomButton().addActionListener(e -> showFloorPerspective());
        roomView.getRoomSellingButton().addActionListener(e -> onRoomSale.run());
        roomView.getEndTimeButton().addActionListener(e -> roomTimeEnd());
        roomView.getAddTimeButton().addActionListener(e -> roomTimeSale());
        roomView.getRoomChangeButton().addActionListener(e -> roomReassignment());

        // Room booking hour buttons
        roomView.getBooking24HoursButton().addActionListener(e -> roomTimeModification(24));
        roomView.getBooking3HoursButton().addActionListener(e -> roomTimeModification(3));
        roomView.getBooking12HoursButton().addActionListener(e -> roomTimeModification(12));

        // Room price adjustment buttons
        roomView.getAddBigQuantityButton().addActionListener(e -> updateRoomPrice(1000));
        roomView.getRemoveBigQuantity().addActionListener(e -> updateRoomPrice(-1000));
        roomView.getAddSmallQuantityButton().addActionListener(e -> updateRoomPrice(100));
        roomView.getRemoveSmallQuantityButton().addActionListener(e -> updateRoomPrice(-100));

        // Room change view buttons
        roomChangeView.getUpButton().addActionListener(e -> roomChangeViewFloorChange(1));
        roomChangeView.getDownButton().addActionListener(e -> roomChangeViewFloorChange(-1));
        roomChangeView.getBackButton().addActionListener(e -> showFloorPerspective());
        roomChangeView.getConfirmButton().addActionListener(e -> changeRoomTime());

        // Cross-domain: room buttons on FloorView → this controller
        int towers[][] = motelManager.getRoomsArray();
        for (int tower = 0; tower < towers.length; tower++) {
            for (int floor = 0; floor < towers[tower].length; floor++) {
                for (int room = 0; room < towers[tower][floor]; room++) {
                    final int currentTower = tower;
                    final int currentFloor = floor;
                    final int currentRoom = room;
                    floorView.getRoomButtonGridByTower().get(tower).get(floor).get(room)
                        .addActionListener(e -> roomSelected(currentTower, currentFloor, currentRoom));
                    roomChangeView.getRoomButtonGridByTower().get(tower).get(floor).get(room)
                        .addActionListener(e -> roomChangeSelected(currentTower, currentFloor, currentRoom));
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

        roomView.getRoomNumber().setText(roomText);
        roomView.getBooking12HoursButton().setBackground(Color.WHITE);
        roomView.getBooking24HoursButton().setBackground(Color.WHITE);
        roomView.getBooking3HoursButton().setBackground(Color.WHITE);

        switch (roomStatus) {
            case FREE:
                // Room is free — show booking controls
                roomView.getRoomStatusBackground().setBackground(new Color(39, 174, 96));
                roomView.getStatusLabel().setText("LIBRE");
                roomView.getStartInformativeLabel().setVisible(false);
                roomView.getRemainingInformativeLabel().setVisible(false);
                roomView.getRemainingTimeLabel().setVisible(false);
                roomView.getStartTimeLabel().setVisible(false);
                roomView.getRoomStatusInformative().setText(" ");
                roomView.getStartDateLabel().setVisible(false);

                roomView.getAddBigQuantityButton().setVisible(true);
                roomView.getRemoveBigQuantity().setVisible(true);
                roomView.getAddSmallQuantityButton().setVisible(true);
                roomView.getRemoveSmallQuantityButton().setVisible(true);
                roomView.getPriceTextField().setVisible(true);
                roomView.getPrintingCheckBox().setVisible(true);
                roomView.getAddTimeButton().setVisible(true);
                roomView.getPriceTextField().setText("0");

                roomView.getRoomSellingButton().setVisible(false);
                roomView.getEndTimeButton().setVisible(false);
                roomView.getAddTimeButton().setEnabled(false);
                roomView.getBooking12HoursButton().setVisible(true);
                roomView.getBooking24HoursButton().setVisible(true);
                roomView.getBooking3HoursButton().setVisible(true);
                roomView.getRoomChangeButton().setVisible(false);
                break;

            case CLEANING:
                // Room is being cleaned — show cleaning info
                roomView.getRoomStatusBackground().setBackground(new Color(93, 173, 226));
                roomView.getStatusLabel().setText("LIMPIEZA");
                roomView.getStartInformativeLabel().setVisible(true);
                roomView.getRemainingInformativeLabel().setVisible(false);
                roomView.getRemainingTimeLabel().setVisible(false);
                roomView.getStartTimeLabel().setVisible(true);
                roomView.getRoomStatusInformative().setText("LIMPIEZA");
                roomView.getStartDateLabel().setVisible(true);

                roomView.getAddBigQuantityButton().setVisible(false);
                roomView.getRemoveBigQuantity().setVisible(false);
                roomView.getAddSmallQuantityButton().setVisible(false);
                roomView.getRemoveSmallQuantityButton().setVisible(false);
                roomView.getPriceTextField().setVisible(false);
                roomView.getPrintingCheckBox().setVisible(false);

                roomView.getRoomSellingButton().setVisible(false);
                roomView.getEndTimeButton().setVisible(true);
                roomView.getAddTimeButton().setVisible(false);
                roomView.getBooking12HoursButton().setVisible(false);
                roomView.getBooking24HoursButton().setVisible(false);
                roomView.getBooking3HoursButton().setVisible(false);
                roomView.getRoomChangeButton().setVisible(false);
                break;

            case OCCUPIED:
                // Room is occupied — show booking info and sale/end controls
                roomView.getRoomStatusBackground().setBackground(new Color(205, 97, 85));
                roomView.getStatusLabel().setText("SERVICIO");
                roomView.getRoomStatusInformative().setText("OCUPADA");
                roomView.getStartInformativeLabel().setVisible(true);
                roomView.getRemainingInformativeLabel().setVisible(true);
                roomView.getRemainingTimeLabel().setVisible(true);
                roomView.getStartTimeLabel().setVisible(true);
                roomView.getAddBigQuantityButton().setVisible(true);
                roomView.getRemoveBigQuantity().setVisible(true);
                roomView.getAddSmallQuantityButton().setVisible(true);
                roomView.getRemoveSmallQuantityButton().setVisible(true);
                roomView.getPriceTextField().setVisible(true);
                roomView.getPrintingCheckBox().setVisible(true);
                roomView.getStartDateLabel().setVisible(true);
                roomView.getAddTimeButton().setVisible(true);
                roomView.getPriceTextField().setText("0");

                roomView.getRoomSellingButton().setVisible(true);
                roomView.getEndTimeButton().setVisible(true);
                roomView.getAddTimeButton().setEnabled(false);
                roomView.getBooking12HoursButton().setVisible(true);
                roomView.getBooking24HoursButton().setVisible(true);
                roomView.getBooking3HoursButton().setVisible(true);
                roomView.getRoomChangeButton().setVisible(true);
                break;
        }
        userInterface.setRoomView();
    }

    // ========== Service / Price Management ==========

    /**
     * Sets the desired service duration and its default price.
     * Highlights the selected booking button.
     *
     * @param amount service hours (3, 12, or 24)
     */
    public void roomTimeModification(int amount) {
        roomView.getAddTimeButton().setEnabled(true);
        switch (amount) {
            case 3 -> {
                roomView.getPriceTextField().setText("30000");
                roomView.getBooking3HoursButton().setBackground(new Color(103, 159, 51));
                roomView.getBooking12HoursButton().setBackground(Color.WHITE);
                roomView.getBooking24HoursButton().setBackground(Color.WHITE);
                motelManager.setCurrentServiceDesired(amount);
            }
            case 12 -> {
                roomView.getPriceTextField().setText("35000");
                roomView.getBooking3HoursButton().setBackground(Color.WHITE);
                roomView.getBooking12HoursButton().setBackground(new Color(103, 159, 51));
                roomView.getBooking24HoursButton().setBackground(Color.WHITE);
                motelManager.setCurrentServiceDesired(amount);
            }
            case 24 -> {
                roomView.getPriceTextField().setText("88000");
                roomView.getBooking3HoursButton().setBackground(Color.WHITE);
                roomView.getBooking12HoursButton().setBackground(Color.WHITE);
                roomView.getBooking24HoursButton().setBackground(new Color(103, 159, 51));
                motelManager.setCurrentServiceDesired(amount);
            }
        }
    }

    /**
     * Adjusts the displayed room price by the given delta.
     */
    public void updateRoomPrice(long delta) {
        long currentPrice;
        try {
            currentPrice = Long.parseLong(roomView.getPriceTextField().getText());
        } catch (NumberFormatException ex) {
            currentPrice = 0;
        }
        long newPrice = currentPrice + delta;
        if (newPrice > 0) {
            roomView.getPriceTextField().setText(String.valueOf(newPrice));
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
        int service = motelManager.getCurrentServiceDesired();
        int price;
        try {
            price = Integer.parseInt(roomView.getPriceTextField().getText());
        } catch (NumberFormatException ex) {
            price = 0;
        }
        boolean print = roomView.getPrintingCheckBox().isSelected();

        if (!print) {
            boolean noPrintingConfirmation = userInterface.confirmPrinting();
            if (noPrintingConfirmation) {
                motelManager.registerRoomTimeAdded(towerNumber, floorNumber, roomNumber, service, price, false);
                userInterface.setFloorView();
                motelManager.saveFilesForMainService();
                saveBackupFilesOperation.run();
            }
        } else {
            motelManager.registerRoomTimeAdded(towerNumber, floorNumber, roomNumber, service, price, true);
            userInterface.setFloorView();
            motelManager.saveFilesForMainService();
            saveBackupFilesOperation.run();
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
        userInterface.setFloorView();
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
            roomChangeView.getSelectedLabel().setText("NO DISPONIBLE");
        } else {
            roomChangeView.getSelectedLabel().setText(roomString);
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
                roomView.getStartTimeLabel().setText(startTime);
                roomView.getStartDateLabel().setText(startDateClean);
            }
            case OCCUPIED -> {
                String startTimeRoom = motelManager.getStartTimeRoom(tower, floor, room);
                String startDate = motelManager.getStartDateRoom(tower, floor, room);
                String remainingTime = motelManager.getRemainingTimeRoom(tower, floor, room);
                if (remainingTime.contains("-")) {
                    roomView.getRoomStatusBackground().setBackground(new Color(241, 196, 15));
                    roomView.getStatusLabel().setText("SOBRETIEMPO");
                    roomView.getRoomStatusInformative().setText("SOBRETIEMPO");
                }
                roomView.getStartTimeLabel().setText(startTimeRoom);
                roomView.getRemainingTimeLabel().setText(remainingTime);
                roomView.getStartDateLabel().setText(startDate);
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
                    JButton roomButton = roomChangeView.getRoomButtonGridByTower().get(tower).get(floor).get(room);
                    switch (status) {
                        case FREE -> {
                            roomButton.setText("<html><center>" + roomString + "</center></html>");
                            roomButton.setBackground(new Color(39, 174, 96));
                        }
                        case CLEANING -> {
                            roomButton.setText("<html><center>" + roomString + "</center></html>");
                            roomButton.setBackground(new Color(84, 153, 199));
                        }
                        case OCCUPIED -> {
                            roomButton.setText("<html><center>NO DISPONIBLE</center></html>");
                            roomButton.setBackground(new Color(231, 76, 60));
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
