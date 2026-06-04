package controller.sub;

import view.UserGUI;
import view.ViewCard;

/**
 * Controls navigation between management views and the main floor view.
 *
 * <p>Routes to specific management sub-views via Runnable callbacks
 * provided by the main Controller to avoid circular dependencies.
 * Spending register, extra changes, and room summary are handled
 * directly by this controller since they have no external dependencies.
 */
public class ManagementController {

    private final UserGUI userInterface;
    private final Runnable onTurnSelected;
    private final Runnable onInventorySelected;
    private final Runnable onHistorySelected;
    private final Runnable onAppOptionsSelected;

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
        var sel = userInterface.getManagementSelection();
        sel.onBackButton(this::showFloorView);
        sel.onTurnButton(onTurnSelected);
        sel.onInventoryButton(onInventorySelected);
        sel.onHistoryButton(onHistorySelected);
        sel.onAppOptionsButton(onAppOptionsSelected);
        sel.onRoomSummaryButton(this::showRoomSummary);
        sel.onExtraChangesButton(this::showExtraChanges);
        sel.onRegisterSpendingButton(this::showSpendingRegister);

        // Spending register view listeners
        userInterface.getSpendingRegisterView().onCancellationButton(this::showManagementOptions);

        // Extra turn changes view listeners (mutual exclusion handled inside the view)
        userInterface.getExtraTurnChangesView().onBackButton(this::showManagementOptions);

        // Room summary view listeners
        userInterface.getRoomSummaryView().onBackButton(this::showManagementOptions);
    }

    /** Opens the management options menu. */
    public void showManagementOptions() {
        userInterface.setView(ViewCard.MANAGEMENT_SELECT_VIEW);
    }

    /** Returns to the floor view. */
    public void showFloorView() {
        userInterface.setView(ViewCard.FLOOR_VIEW);
    }

    // ========== Spending Register ==========

    /** Opens the spending register view. */
    public void showSpendingRegister() {
        userInterface.getSpendingRegisterView().clearFields();
        userInterface.setView(ViewCard.SPENDING_REGISTER_VIEW);
    }

    // ========== Extra Changes (Bank Transfer / Safe Deposit) ==========

    /** Opens the extra turn changes view. */
    public void showExtraChanges() {
        userInterface.getExtraTurnChangesView().clearFields();
        userInterface.setView(ViewCard.EXTRA_TURN_CHANGES_VIEW);
    }

    // ========== Room Summary ==========

    /** Opens the room summary dashboard. */
    public void showRoomSummary() {
        userInterface.setView(ViewCard.ROOM_SUMMARY_VIEW);
    }
}
