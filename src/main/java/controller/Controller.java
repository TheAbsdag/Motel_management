package controller;

import controller.sub.AppOptionsController;
import controller.sub.CurrencyConfigurationController;
import controller.sub.EmailController;
import controller.sub.FloorConfigurationController;
import controller.sub.FloorController;
import controller.sub.HistoryController;
import controller.sub.InventoryController;
import controller.sub.ManagementController;
import controller.sub.MotelDataConfigurationController;
import controller.sub.RoomController;
import controller.sub.SellingController;
import controller.sub.TurnController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import model.modelManagers.MotelManagement;
import model.RoomStatus;
import model.turn.ExtraChangeType;
import view.UserGUI;
import view.helpers.DialogHelper;
import view.helpers.InputParser;

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

    private static final int CLOCK_UPDATE_INTERVAL_MS = 250;
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
    private final FloorConfigurationController floorConfigurationController;
    private final MotelDataConfigurationController motelDataConfigController;
    private final EmailController emailController;
    private CurrencyConfigurationController currencyConfigurationController;

    // Timers
    private Timer timerForTimeUpdates;
    private Timer timerForBackupFiles;
    private Timer timerForAutomaticFloorChange;
    private Timer timerForOvertimeWarning;

    /**
     * Creates the Controller and all sub-controllers.
     * Sub-controllers are wired with callbacks to avoid circular dependencies.
     * Callbacks stored but never invoked during construction -- safe to pass {@code this}.
     */
    @SuppressWarnings("this-escape")
    public Controller(MotelManagement motelManager, UserGUI userInterface) {
        this.motelManager = motelManager;
        this.userInterface = userInterface;

        // Create sub-controllers (order matters for callback references)
        floorController = new FloorController(motelManager, userInterface.getFloorView());
        sellingController = new SellingController(motelManager, userInterface.getSellingView(), userInterface,
                this::saveMainFiles, () -> saveBackupFiles("transaction"));
        inventoryController = new InventoryController(motelManager, userInterface.getInventoryView(),
                this::showManagementSelection, this::saveMainFiles, () -> saveBackupFiles("roomSwap"));
        appOptionsController = new AppOptionsController(motelManager,
                userInterface.getAppOptions(),
                userInterface.getPrinterConfigView(),
                this::showManagementSelection,
                this::openPrinterConfig,
                this::openMotelDataConfig,
                this::openTimeConfig,
                this::openFloorConfig,
                this::openDataSavingConfig,
                this::openExportConfig,
                this::openCurrencyConfig,
                this::openAppOptionsHub);
        historyController = new HistoryController(motelManager, userInterface.getHistoryView(),
                this::showManagementSelection);
        turnController = new TurnController(motelManager, userInterface.getTurnManagerView(), userInterface,
                this::showManagementSelection, this::saveMainFiles, () -> saveBackupFiles("transaction"));
        roomController = new RoomController(motelManager, userInterface.getFloorView(),
                userInterface.getRoomView(), userInterface.getRoomChangeView(), userInterface,
                () -> sellingController.roomSale(false), this::saveMainFiles, () -> saveBackupFiles("roomSwap"));
        managementController = new ManagementController(userInterface,
                this::openTurnManagement,
                this::openInventoryView,
                this::openHistoryView,
                this::openAppOptionsView
        );
        floorConfigurationController = new FloorConfigurationController(motelManager,
                userInterface.getFloorConfigView(),
                userInterface.getRoomConfigView(),
                this::saveMainFiles,
                this::rebuildFloorView,
                this::openAppOptionsHub,
                this::showRoomConfigCard,
                this::showFloorConfigCard);
        motelDataConfigController = new MotelDataConfigurationController(motelManager,
                userInterface.getMotelDataConfigView(),
                this::openAppOptionsHub,
                this::saveMainFiles,
                () -> saveBackupFiles("motelDataConfig"));

        emailController = new EmailController(
                userInterface.getEmailConfigView(),
                userInterface.getExportConfigView(),
                userInterface,
                this::openExportConfig,
                motelManager.getEmailConfigurationService());

        currencyConfigurationController = new CurrencyConfigurationController(
                motelManager,
                userInterface.getCurrencyConfigurationView(),
                () -> userInterface.setAppOptionsView(),
                this::saveMainFiles,
                () -> saveBackupFiles("CURRENCY_CONFIG_SAVE"));

        // Wire sub-config view back buttons → return to options hub
        userInterface.getDataSavingConfigView().onBackButton(this::openAppOptionsHub);
        userInterface.getTimeConfigView().onBackButton(this::openAppOptionsHub);
        userInterface.getExportConfigView().onBackButton(this::openAppOptionsHub);

        // Wire spending and extra changes confirmation actions
        userInterface.getSpendingRegisterView().onConfirmationButton(this::registerSpending);
        userInterface.getExtraTurnChangesView().onConfirmationButton(this::registerExtraChanges);
    }

    /**
     * Initializes the application: loads data, sets up views and listeners, starts timers.
     * Called once at startup from {@code App.main()}.
     */
    public void start() {
        motelManager.prepareProgramData();

        if (motelManager.isFirstBoot()) {
            boolean accept = DialogHelper.confirmDialog(
                    "No se ha encontrado informacion del programa, por favor continue con la configuracion",
                    "CONFIGURACION INICIAL");
            if (!accept) {
                System.exit(0);
                return;
            }
            motelManager.initializeDefaultConfiguration();
            int[][] roomsArray = motelManager.getRoomsArray();
            userInterface.setupFloors(roomsArray);
            setupListeners();
            startTimers();
            startFirstBootConfiguration();
            return;
        }

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
                DialogHelper.showInfoMessage(message, "CONFIGURACION IMPRESORA");
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
    private void setupListeners() {
        // Have each sub-controller register its own listeners
        floorController.initListeners();
        roomController.initListeners();
        sellingController.initListeners();
        turnController.initListeners();
        historyController.initListeners();
        inventoryController.initListeners();
        managementController.initListeners();
        appOptionsController.initListeners();
        floorConfigurationController.initListeners();
        motelDataConfigController.initListeners();
        emailController.initListeners();
        currencyConfigurationController.initListeners();

        // Floor view management button → management menu
        userInterface.getFloorView().onManagementOptions(this::showManagementSelection);

        // Floor view reception sell button → reception sale
        userInterface.getFloorView().onReceptionSell(() -> sellingController.roomSale(true));
    }

    // ========== View Navigation ==========

    /** Shows the floor perspective. */
    public void showFloorPerspective() {
        userInterface.setFloorView();
    }

    /**
     * Rebuilds the main floor view's room buttons to match the current room grid.
     * Must be called after structural config changes (add/remove towers, floors, rooms)
     * to keep the floor view in sync.
     */
    public void rebuildFloorView() {
        int[][] roomsArray = motelManager.getRoomsArray();
        userInterface.setupFloors(roomsArray);
        roomController.wireRoomGridListeners();
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
    }

    private void openAppOptionsHub() {
        userInterface.setAppOptionsView();
    }

    private void openPrinterConfig() {
        userInterface.setPrinterConfigView();
    }

    private void openMotelDataConfig() {
        motelDataConfigController.populateView();
        userInterface.setMotelDataConfigView();
    }

    private void openTimeConfig() {
        userInterface.setTimeConfigView();
    }

    private void openFloorConfig() {
        floorConfigurationController.populateView();
        userInterface.setFloorConfigView();
    }

    private void showRoomConfigCard() {
        userInterface.setRoomConfigView();
    }

    private void showFloorConfigCard() {
        userInterface.setFloorConfigView();
    }

    private void openDataSavingConfig() {
        userInterface.setDataSavingConfigView();
    }

    private void openExportConfig() {
        userInterface.setExportConfigView();
    }

    private void openCurrencyConfig() {
        currencyConfigurationController.populateView();
        userInterface.setCurrencyConfigurationView();
    }

    // ========== Spending / Extra Changes ==========

    private void registerSpending() {
        String conceptSpending = userInterface.getSpendingRegisterView().getDescriptionText();
        long value = InputParser.parseLongSafe(userInterface.getSpendingRegisterView().getValueText());
        if (value == 0L && !conceptSpending.isEmpty()) {
            DialogHelper.showInfoMessage("El valor ingresado debe ser distinto de cero", "ERROR");
            return;
        }
        if (value != 0L && !conceptSpending.isEmpty()) {
            motelManager.addSpendingTransaction(conceptSpending, value);
            saveMainFiles();
            saveBackupFiles("transaction");
            showFloorPerspective();
        }
    }

    private void registerExtraChanges() {
        String conceptSpending = userInterface.getExtraTurnChangesView().getDescriptionText();
        long value = InputParser.parseLongSafe(userInterface.getExtraTurnChangesView().getValueText());
        ExtraChangeType type = userInterface.getExtraTurnChangesView().isBankTransferSelected()
                ? ExtraChangeType.BANK_TRANSFER : ExtraChangeType.SAFE_DEPOSIT;
        motelManager.addExtraChangeTransaction(conceptSpending, value, type);
        saveMainFiles();
        saveBackupFiles("transaction");
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
        var floorView = userInterface.getFloorView();
        if (motelManager.getOvertimeList().isEmpty()) {
            floorView.setWarningVisible(false);
        } else {
            floorView.setWarningVisible(!floorView.isWarningVisible());
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

    // ========== First-Boot Configuration Flow ==========

    /** Kicks off the first-boot configuration wizard with motel data. */
    private void startFirstBootConfiguration() {
        motelDataConfigController.configureForFirstBoot(this::onFirstBootMotelDataDone);
        userInterface.setMotelDataConfigView();
    }

    /** Called after motel data is saved. Proceeds to currency configuration. */
    private void onFirstBootMotelDataDone() {
        DialogHelper.showInfoMessage("Configure la moneda del motel", "CONFIGURACION INICIAL");
        currencyConfigurationController.populateView();
        currencyConfigurationController.configureForFirstBoot(this::onFirstBootCurrencyDone);
        userInterface.setCurrencyConfigurationView();
    }

    /** Called after currency is saved. Proceeds to floor/room configuration. */
    private void onFirstBootCurrencyDone() {
        DialogHelper.showInfoMessage("Configure las habitaciones del motel", "CONFIGURACION INICIAL");
        floorConfigurationController.configureForFirstBoot(this::onFirstBootFloorConfigDone);
        floorConfigurationController.populateView();
        userInterface.setFloorConfigView();
    }

    /** Called after floor/room configuration is saved. Proceeds to printer selection. */
    private void onFirstBootFloorConfigDone() {
        DialogHelper.showInfoMessage("Seleccione la impresora de recibos, ajuste la impresion",
                "CONFIGURACION INICIAL");
        appOptionsController.showPrinterOptions();
        userInterface.getPrinterConfigView().setFirstBootConfirmAction(() -> {
            appOptionsController.confirmPrinter();
            onFirstBootPrinterDone();
        });
        userInterface.setPrinterConfigView();
    }

    /** Called after printer is confirmed. Completes the first-boot flow. */
    private void onFirstBootPrinterDone() {
        DialogHelper.showInfoMessage(
                "Programa configurado, ahora continue seleccionando turno y usando el programa",
                "CONFIGURACION COMPLETA");
        userInterface.setTurnSelect();
    }
}
