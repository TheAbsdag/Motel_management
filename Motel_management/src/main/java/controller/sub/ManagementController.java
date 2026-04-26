package controller.sub;

import view.UserGUI;

/**
 * Controls navigation between management views and the main floor view.
 *
 * <p>Routes to specific management sub-views via Runnable callbacks
 * provided by the main Controller to avoid circular dependencies.
 */
public class ManagementController {

    private final UserGUI userInterface;
    private final Runnable onTurnSelected;
    private final Runnable onInventorySelected;
    private final Runnable onHistorySelected;
    private final Runnable onAppOptionsSelected;

    /**
     * @param userInterface       the main window for view switching
     * @param onTurnSelected      callback to open turn management
     * @param onInventorySelected callback to open inventory management
     * @param onHistorySelected   callback to open history view
     * @param onAppOptionsSelected callback to open app options
     */
    public ManagementController(UserGUI userInterface,
                                Runnable onTurnSelected,
                                Runnable onInventorySelected,
                                Runnable onHistorySelected,
                                Runnable onAppOptionsSelected) {
        this.userInterface = userInterface;
        this.onTurnSelected = onTurnSelected;
        this.onInventorySelected = onInventorySelected;
        this.onHistorySelected = onHistorySelected;
        this.onAppOptionsSelected = onAppOptionsSelected;
    }

    /** Registers action listeners for the management select view. */
    public void initListeners() {
        userInterface.getManagementSelection().getBackButton().addActionListener(e -> showFloorView());
        userInterface.getManagementSelection().getTurnButton().addActionListener(e -> onTurnSelected.run());
        userInterface.getManagementSelection().getInventoryButton().addActionListener(e -> onInventorySelected.run());
        userInterface.getManagementSelection().getHistoryButton().addActionListener(e -> onHistorySelected.run());
        userInterface.getManagementSelection().getAppOptionsButton().addActionListener(e -> onAppOptionsSelected.run());
    }

    /** Opens the management options menu. */
    public void showManagementOptions() {
        userInterface.setManagementSelection();
    }

    /** Returns to the floor view. */
    public void showFloorView() {
        userInterface.setFloorView();
    }
}
