package controller;

import controller.sub.AppOptionsController;
import controller.sub.FloorController;
import controller.sub.HistoryController;
import controller.sub.InventoryController;
import controller.sub.ManagementController;
import controller.sub.RoomController;
import controller.sub.SellingController;
import controller.sub.TurnController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import model.modelManagers.MotelManagement;
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
 * <p><b>Threading:</b> File I/O (saves and backups) is dispatched to a
 * single-threaded background executor so the EDT is never blocked on disk writes.
 *
 * @author Santiago
 */
public class Controller {

    private static final int CLOCK_UPDATE_INTERVAL_MS = 80;
    private static final int BACKUP_SAVE_INTERVAL_MS = 300_000;      // 5 minutes
    private static final int FLOOR_ROTATION_INTERVAL_MS = 1_200_000;  // 20 minutes
    private static final int OVERTIME_WARNING_INTERVAL_MS = 1_000;    // 1 second

    private final MotelManagement motelManager;
    private final UserGUI userInterface;

    // Single-thread executor for all file I/O — preserves write order, never blocks the EDT
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "FileSaveWorker");
        t.setDaemon(true);
        return t;
    });

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
                this::saveMainFiles, this::saveBackupFilesTransaction);
        inventoryController = new InventoryController(motelManager, userInterface.getInventoryView(),
                this::showManagementSelection, this::saveMainFiles, this::saveBackupFilesRoomSwap);
        appOptionsController = new AppOptionsController(motelManager, userInterface.getAppOptions(),
                this::showManagementSelection);
        historyController = new HistoryController(motelManager, userInterface.getHistoryView(),
                this::showManagementSelection);
        turnController = new TurnController(motelManager, userInterface.getTurnManagerView(), userInterface,
                this::showManagementSelection, this::saveMainFiles, this::saveBackupFilesTransaction);
        roomController = new RoomController(motelManager, userInterface.getFloorView(),
                userInterface.getRoomView(), userInterface.getRoomChangeView(), userInterface,
                () -> sellingController.roomSale(false), this::saveMainFiles, this::saveBackupFilesRoomSwap);
        managementController = new ManagementController(userInterface,
                this::openTurnManagement,
                this::openInventoryView,
                this::openHistoryView,
                this::openAppOptionsView
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
            userInterface.setTurnSelect();
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

    private void openTurnManagement() {
        turnController.showTurnManagement();
    }

    private void openInventoryView() {
        inventoryController.openView();
        userInterface.setInventoryView();
    }

    private void openHistoryView() {
        historyController.openView();
        userInterface.setHistoryView();
    }

    private void openAppOptionsView() {
        appOptionsController.showOptions();
        userInterface.setAppOptionsView();
    }

    // ========== Spending / Extra Changes ==========

    private void registerSpending() {
        String conceptSpending = userInterface.getSpendingRegisterView().getDescriptionChangeText().getText();
        long value;
        try {
            value = Long.parseLong(userInterface.getSpendingRegisterView().getValueTextField().getText());
        } catch (NumberFormatException ex) {
            userInterface.showInfoMessage("El valor ingresado no es un numero valido", "ERROR");
            return;
        }
        if (value != 0L && !conceptSpending.isEmpty()) {
            motelManager.addSpendingTransaction(conceptSpending, value);
            saveMainFiles();
            saveBackupFilesTransaction();
            showFloorPerspective();
        }
    }

    private void registerExtraChanges() {
        String conceptSpending = userInterface.getExtraTurnChangesView().getDescriptionText().getText();
        long value;
        try {
            value = Long.parseLong(userInterface.getExtraTurnChangesView().getValueTextField().getText());
        } catch (NumberFormatException ex) {
            userInterface.showInfoMessage("El valor ingresado no es un numero valido", "ERROR");
            return;
        }
        String type = userInterface.getExtraTurnChangesView().getBankTransferBox().isSelected()
                ? "bankTransfer" : "safeDeposit";
        motelManager.addExtraChangeTransaction(conceptSpending, value, type);
        saveMainFiles();
        saveBackupFilesTransaction();
        showFloorPerspective();
    }

    // ========== Timer Management ==========

    private void startTimers() {
        timerForTimeUpdates = new Timer(CLOCK_UPDATE_INTERVAL_MS, e -> updateTime());
        timerForBackupFiles = new Timer(BACKUP_SAVE_INTERVAL_MS, e -> saveBackupFiles("backup"));
        timerForAutomaticFloorChange = new Timer(FLOOR_ROTATION_INTERVAL_MS, e -> automaticRotation());
        timerForOvertimeWarning = new Timer(OVERTIME_WARNING_INTERVAL_MS, e -> updateOvertimeWarning());
        timerForTimeUpdates.start();
        timerForBackupFiles.start();
        timerForAutomaticFloorChange.start();
        timerForOvertimeWarning.start();
    }

    /**
     * Called every ~80ms by the time update timer.
     * Updates only the time/date display and the currently visible view panel.
     * The room grid on the floor view is NOT rebuilt here — it updates via
     * the {@link #updateRoomButtonsSelective()} call for the visible floor only.
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
        var warningLabel = userInterface.getFloorView().getWarningIconLabel();
        if (motelManager.getOvertimeList().isEmpty()) {
            warningLabel.setVisible(false);
        } else {
            warningLabel.setVisible(!warningLabel.isVisible());
        }
        userInterface.getFloorView().updateWarnings(motelManager.getOvertimeList());
    }

    /**
     * Automatic rotation: cycles through floors, and when all floors of the current
     * tower have been shown, advances to the next tower (if multiple towers exist).
     */
    private void automaticRotation() {
        floorController.automaticFloorChange();
        // After floor change, check if we should also advance the tower
        int[][] roomArray = motelManager.getRoomsArray();
        if (roomArray.length > 1) {
            // Tower rotation is handled by FloorController via automaticTowerRotation
            floorController.automaticTowerRotation();
        }
    }

    // ========== File Persistence (Background Threads) ==========

    /**
     * Saves the main data files on a background thread so the EDT is never blocked.
     */
    public void saveMainFiles() {
        saveExecutor.submit(() -> {
            try {
                motelManager.saveFilesForMainService();
            } catch (Exception e) {
                System.err.println("Error saving main files: " + e.getMessage());
            }
        });
    }

    /**
     * Saves backup copies of all data files on a background thread.
     *
     * @param saveType label for the backup (e.g. "backup", "operation")
     */
    public void saveBackupFiles(String saveType) {
        saveExecutor.submit(() -> {
            try {
                motelManager.saveFilesForBackup(saveType);
            } catch (Exception e) {
                System.err.println("Error saving backup files: " + e.getMessage());
            }
        });
    }

    public void saveBackupFilesTransaction() {
        saveExecutor.submit(() -> {
            try {
                motelManager.saveFilesForBackup("transaction");
            } catch (Exception e) {
                System.err.println("Error saving backup files (transaction): " + e.getMessage());
            }
        });
    }

    /**
     * Triggers a backup save with the "roomSwap" label.
     * Called after room operations (booking, extensions, room swaps).
     * Runs on a background thread.
     */
    public void saveBackupFilesRoomSwap() {
        saveExecutor.submit(() -> {
            try {
                motelManager.saveFilesForBackup("roomSwap");
            } catch (Exception e) {
                System.err.println("Error saving backup files (roomSwap): " + e.getMessage());
            }
        });
    }
}
