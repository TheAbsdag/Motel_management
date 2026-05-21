package controller.sub;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import view.UserGUI;

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
        userInterface.getManagementSelection().getBackButton().addActionListener(e -> showFloorView());
        userInterface.getManagementSelection().getTurnButton().addActionListener(e -> onTurnSelected.run());
        userInterface.getManagementSelection().getInventoryButton().addActionListener(e -> onInventorySelected.run());
        userInterface.getManagementSelection().getHistoryButton().addActionListener(e -> onHistorySelected.run());
        userInterface.getManagementSelection().getAppOptionsButton().addActionListener(e -> onAppOptionsSelected.run());
        userInterface.getManagementSelection().getRoomSummaryButton().addActionListener(e -> showRoomSummary());
        userInterface.getManagementSelection().getExtraChangesButton().addActionListener(e -> showExtraChanges());
        userInterface.getManagementSelection().getRegisterSpendingButton().addActionListener(e -> showSpendingRegister());

        // Spending register view listeners
        userInterface.getSpendingRegisterView().getCancellationButton().addActionListener(e -> showManagementOptions());

        // Extra turn changes view listeners
        userInterface.getExtraTurnChangesView().getBackButton().addActionListener(e -> showManagementOptions());
        userInterface.getExtraTurnChangesView().getBankTransferBox().addItemListener(new CheckBoxListener(userInterface));
        userInterface.getExtraTurnChangesView().getSaveDespositBox().addItemListener(new CheckBoxListener(userInterface));

        // Room summary view listeners
        userInterface.getRoomSummaryView().getBackButton().addActionListener(e -> showManagementOptions());
    }

    /** Opens the management options menu. */
    public void showManagementOptions() {
        userInterface.setManagementSelection();
    }

    /** Returns to the floor view. */
    public void showFloorView() {
        userInterface.setFloorView();
    }

    // ========== Spending Register ==========

    /** Opens the spending register view. */
    public void showSpendingRegister() {
        userInterface.getSpendingRegisterView().clearFields();
        userInterface.setSpendingRegisterView();
    }

    // ========== Extra Changes (Bank Transfer / Safe Deposit) ==========

    /** Opens the extra turn changes view. */
    public void showExtraChanges() {
        userInterface.getExtraTurnChangesView().clearFields();
        userInterface.setExtraTurnChangesView();
    }

    // ========== Room Summary ==========

    /** Opens the room summary dashboard. */
    public void showRoomSummary() {
        userInterface.setRoomSummaryView();
    }

    // ========== Checkbox Listener ==========

    /**
     * Manages mutually exclusive checkboxes for extra turn changes
     * (bank transfer and safe deposit).
     */
    private static class CheckBoxListener implements ItemListener {
        private final UserGUI view;

        CheckBoxListener(UserGUI view) {
            this.view = view;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            JCheckBox selected = (JCheckBox) e.getSource();
            if (selected.isSelected()) {
                view.getExtraTurnChangesView().getConfirmationButton().setEnabled(true);
                if (selected != view.getExtraTurnChangesView().getBankTransferBox()) {
                    view.getExtraTurnChangesView().getBankTransferBox().setSelected(false);
                }
                if (selected != view.getExtraTurnChangesView().getSaveDespositBox()) {
                    view.getExtraTurnChangesView().getSaveDespositBox().setSelected(false);
                }
            }
        }
    }
}
