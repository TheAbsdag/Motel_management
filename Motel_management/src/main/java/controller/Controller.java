package controller;

import controller.sub.AppOptionsController;
import controller.sub.FloorController;
import controller.sub.HistoryController;
import controller.sub.InventoryController;
import controller.sub.ManagementController;
import controller.sub.RoomController;
import controller.sub.SellingController;
import controller.sub.TurnController;
import javax.swing.Timer;
import model.MotelManagement;
import model.RoomStatus;
import view.UserGUI;

/**
 * Main application controller — thin orchestrator that creates sub-controllers,
 * manages timers, and delegates domain logic.
 *
 * <p>Specific responsibilities have been extracted to focused sub-controllers:
 * <ul>
 *   <li>{@link FloorController} — floor/tower navigation and room button updates</li>
 *   <li>{@link RoomController} — room selection, booking, time management, room changes</li>
 *   <li>{@link SellingController} — item sales flow, cart management, checkout</li>
 *   <li>{@link TurnController} — turn lifecycle, turn details, printing, reversals</li>
 *   <li>{@link HistoryController} — historical turn viewing and printing</li>
 *   <li>{@link InventoryController} — inventory CRUD operations</li>
 *   <li>{@link ManagementController} — management menu navigation</li>
 *   <li>{@link AppOptionsController} — printer selection and configuration</li>
 * </ul>
 *
 * @author Santiago
 */
public class Controller {

    private static final int CLOCK_UPDATE_INTERVAL_MS = 80;
    private static final int BACKUP_SAVE_INTERVAL_MS = 300_000;      // 5 minutes
    private static final int MAIN_FILE_SAVE_INTERVAL_MS = 20_000;     // 20 seconds
    private static final int FLOOR_ROTATION_INTERVAL_MS = 1_200_000;  // 20 minutes
    private static final int OVERTIME_WARNING_INTERVAL_MS = 1_000;    // 1 second

    private final MotelManagement motelManager;
    private final UserGUI userInterface;

    // Sub-controllers
    private final FloorController floorController;
    private final RoomController roomController;
    private final SellingController sellingController;
    private final TurnController turnController;
    private final HistoryController historyController;
    private final InventoryController inventoryController;
    private final ManagementController managementController;
    private final AppOptionsController appOptionsController;

    // Timers
    private Timer timerForTimeUpdates;
    private Timer timerForBackupFiles;
    private Timer timerForCurrentFile;
    private Timer timerForAutomaticFloorChange;
    private Timer timerForOvertimeWarning;

    /**
     * Creates the Controller and all sub-controllers.
     * Sub-controllers are wired with callbacks to avoid circular dependencies.
     */
    public Controller(MotelManagement motelManager, UserGUI userInterface) {
        this.motelManager = motelManager;
        this.userInterface = userInterface;

        // Create sub-controllers (order matters for callback references)
        floorController = new FloorController(motelManager, userInterface.getFloorView());
        sellingController = new SellingController(motelManager, userInterface.getSellingView(), userInterface,
                this::saveBackupFilesOperation);
        inventoryController = new InventoryController(motelManager, userInterface.getInventoryView(),
                this::showManagementSelection, this::saveBackupFilesOperation);
        appOptionsController = new AppOptionsController(motelManager, userInterface.getAppOptions(),
                this::showManagementSelection);
        historyController = new HistoryController(motelManager, userInterface.getHistoryView(),
                this::showManagementSelection);
        turnController = new TurnController(motelManager, userInterface.getTurnManagerView(), userInterface,
                this::showManagementSelection);
        roomController = new RoomController(motelManager, userInterface.getFloorView(),
                userInterface.getRoomView(), userInterface.getRoomChangeView(), userInterface,
                () -> sellingController.roomSale(false), this::saveBackupFilesOperation);
        managementController = new ManagementController(userInterface,
                () -> { turnController.showTurnManagement(); },
                () -> { inventoryController.openView(); userInterface.setInventoryView(); },
                () -> { historyController.openView(); userInterface.setHistoryView(); },
                () -> { appOptionsController.showOptions(); userInterface.setAppOptionsView(); },
                () -> {
                    userInterface.getSpendingRegisterView().getValueTextField().setText("0");
                    userInterface.getSpendingRegisterView().getDescriptionChangeText().setText("");
                    userInterface.setSpendingRegisterView();
                },
                () -> {
                    userInterface.getExtraTurnChangesView().getDescriptionText().setText("");
                    userInterface.getExtraTurnChangesView().getValueTextField().setText("0");
                    userInterface.getExtraTurnChangesView().getConfirmationButton().setEnabled(false);
                    userInterface.getExtraTurnChangesView().getBankTransferBox().setSelected(false);
                    userInterface.getExtraTurnChangesView().getSaveDespositBox().setSelected(false);
                    userInterface.setExtraTurnChangesView();
                },
                () -> userInterface.setRoomSummaryView()
        );

        // Wire spending and extra changes confirmation actions
        userInterface.getSpendingRegisterView().getConfirmationButton()
                .addActionListener(e -> registerSpending());
        userInterface.getExtraTurnChangesView().getConfirmationButton()
                .addActionListener(e -> registerExtraChanges());
    }

    /**
     * Initializes the application: loads data, sets up views and listeners, starts timers.
     * Called once at startup from {@code App.main()}.
     */
    public void start() {
        motelManager.prepareProgramData();
        int[][] roomsArray = motelManager.getRoomsArray();
        userInterface.setupFloors(roomsArray);

        // Single tower mode: hide tower navigation if only one tower exists
        if (roomsArray.length == 1) {
            userInterface.getRoomChangeView().setSingleTowerMode();
        }

        // Validate saved printer — fall back to first available if missing
        if (!motelManager.isPrinterAvailable()) {
            String originalName = motelManager.getConfiguredPrinterName();
            String fallbackName = motelManager.setFirstAvailablePrinter();
            if (fallbackName != null) {
                motelManager.savePrinterConfiguration(fallbackName);
                String message = "Impresora " + (originalName != null ? originalName : "N/A")
                        + " no encontrada, revise la configuracion, se ha cambiado a impresora "
                        + fallbackName;
                userInterface.showInfoMessage(message, "CONFIGURACION IMPRESORA");
            }
        }

        boolean validTurn = motelManager.prepareTurnRegisterData();
        if (validTurn) {
            System.out.println("Valid turn found");
            userInterface.setFloorView();
        } else {
            System.out.println("No previous turn found");
            userInterface.setTurnSelectView();
        }

        setupListeners();
        startTimers();
    }

    // ========== Listener Setup ==========

    /**
     * Wires all action/list selection listeners by delegating to each sub-controller's
     * initListeners() method, then setting up cross-view listeners.
     */
    public void setupListeners() {
        // Have each sub-controller register its own listeners
        floorController.initListeners();
        roomController.initListeners();
        sellingController.initListeners();
        turnController.initListeners();
        historyController.initListeners();
        inventoryController.initListeners();
        managementController.initListeners();
        appOptionsController.initListeners();

        // Floor view management button → management menu
        userInterface.getFloorView().getManagementOptionsButton()
                .addActionListener(e -> showManagementSelection());

        // Floor view reception sell button → reception sale
        userInterface.getFloorView().getReceptionSellButton()
                .addActionListener(e -> sellingController.roomSale(true));
    }

    // ========== View Navigation ==========

    /** Shows the floor perspective. */
    public void showFloorPerspective() {
        userInterface.setFloorView();
    }

    /** Shows the management options menu. */
    public void showManagementSelection() {
        userInterface.setManagementSelection();
    }

    // ========== Spending / Extra Changes ==========

    private void registerSpending() {
        String conceptSpending = userInterface.getSpendingRegisterView().getDescriptionChangeText().getText();
        long value = Long.parseLong(userInterface.getSpendingRegisterView().getValueTextField().getText());
        if (value != 0L && !conceptSpending.isEmpty()) {
            motelManager.addSpendingTransaction(conceptSpending, value);
            saveMainFiles();
            saveBackupFilesOperation();
            showFloorPerspective();
        }
    }

    private void registerExtraChanges() {
        String conceptSpending = userInterface.getExtraTurnChangesView().getDescriptionText().getText();
        long value = Long.parseLong(userInterface.getExtraTurnChangesView().getValueTextField().getText());
        String type;
        if (userInterface.getExtraTurnChangesView().getBankTransferBox().isSelected()) {
            type = "bankTransfer";
        } else {
            type = "safeDeposit";
        }
        motelManager.addExtraChangeTransaction(conceptSpending, value, type);
        saveMainFiles();
        saveBackupFilesOperation();
        showFloorPerspective();
    }

    // ========== Timer Management ==========

    private void startTimers() {
        timerForTimeUpdates = new Timer(CLOCK_UPDATE_INTERVAL_MS, e -> updateTime());
        timerForBackupFiles = new Timer(BACKUP_SAVE_INTERVAL_MS, e -> saveBackupFiles("backup"));
        timerForCurrentFile = new Timer(MAIN_FILE_SAVE_INTERVAL_MS, e -> motelManager.saveFilesForMainService());
        timerForAutomaticFloorChange = new Timer(FLOOR_ROTATION_INTERVAL_MS, e -> floorController.automaticFloorChange());
        timerForOvertimeWarning = new Timer(OVERTIME_WARNING_INTERVAL_MS, e -> updateOvertimeWarning());
        timerForTimeUpdates.start();
        timerForBackupFiles.start();
        timerForCurrentFile.start();
        timerForAutomaticFloorChange.start();
        timerForOvertimeWarning.start();
    }

    /**
     * Called every ~80ms by the time update timer.
     * Updates the current time/date display and room status visual indicators
     * depending on which view is currently shown.
     */
    public void updateTime() {
        motelManager.timeInformationUpdate();
        String timeShown = motelManager.getCurrentLocalizedTime();
        String dateShown = motelManager.getCurrentLocalizedDate();
        userInterface.updateDateTime(timeShown, dateShown);

        if (userInterface.isFloorShown()) {
            floorController.updateRoomButtons();
        }
        if (userInterface.isRoomShown()) {
            roomController.updateRoomView();
        }
        if (userInterface.isRoomChangeShown()) {
            roomController.updateRoomChangeView();
        }
        if (userInterface.isRoomSummaryShown()) {
            updateRoomSummaryView();
        }
    }

    /**
     * Updates the room summary dashboard with current room states.
     */
    private void updateRoomSummaryView() {
        int[][] roomArray = motelManager.getRoomsArray();
        int[][][] statusData = new int[roomArray.length][][];
        String[][][] stringsData = new String[roomArray.length][][];
        boolean[][][] overtimeData = new boolean[roomArray.length][][];

        for (int tower = 0; tower < roomArray.length; tower++) {
            statusData[tower] = new int[roomArray[tower].length][];
            stringsData[tower] = new String[roomArray[tower].length][];
            overtimeData[tower] = new boolean[roomArray[tower].length][];
            for (int floor = 0; floor < roomArray[tower].length; floor++) {
                statusData[tower][floor] = new int[roomArray[tower][floor]];
                stringsData[tower][floor] = new String[roomArray[tower][floor]];
                overtimeData[tower][floor] = new boolean[roomArray[tower][floor]];
                for (int room = 0; room < roomArray[tower][floor]; room++) {
                    statusData[tower][floor][room] = motelManager.getRoom(tower, floor, room).getStatus().getCode();
                    stringsData[tower][floor][room] = motelManager.getRoom(tower, floor, room).getRoomString();
                    String remainingTime = motelManager.getRemainingTimeRoom(tower, floor, room);
                    overtimeData[tower][floor][room] = remainingTime.contains("-");
                }
            }
        }
        userInterface.getRoomSummaryView().updateRoomSummary(statusData, stringsData, overtimeData);
    }

    /**
     * Updates the overtime warning indicator.
     * Flashes the warning icon when rooms are in overtime.
     */
    private void updateOvertimeWarning() {
        if (motelManager.getOvertimeList().isEmpty()) {
            userInterface.getFloorView().getWarningIconLabel().setVisible(false);
        } else if (userInterface.getFloorView().getWarningIconLabel().isVisible()) {
            userInterface.getFloorView().getWarningIconLabel().setVisible(false);
        } else {
            userInterface.getFloorView().getWarningIconLabel().setVisible(true);
        }

        userInterface.getFloorView().updateWarnings(motelManager.getOvertimeList());
    }

    // ========== File Persistence ==========

    /**
     * Saves the main data files.
     */
    public void saveMainFiles() {
        motelManager.saveFilesForMainService();
    }

    /**
     * Saves backup copies of all data files with a timestamped folder.
     *
     * @param saveType label for the backup (e.g. "backup", "operation")
     */
    public void saveBackupFiles(String saveType) {
        motelManager.saveFilesForBackup(saveType);
    }

    /**
     * Triggers a backup save with the "operation" label.
     * Called after significant user operations (room booking, sales, etc.).
     */
    public void saveBackupFilesOperation() {
        motelManager.saveFilesForBackup("operation");
    }
}
