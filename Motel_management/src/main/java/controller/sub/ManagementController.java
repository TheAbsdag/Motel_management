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
 */
public class ManagementController {

    private final UserGUI userInterface;
    private final Runnable onTurnSelected;
    private final Runnable onInventorySelected;
    private final Runnable onHistorySelected;
    private final Runnable onAppOptionsSelected;
    private final Runnable onSpendingRegisterSelected;
    private final Runnable onExtraChangesSelected;
    private final Runnable onRoomSummarySelected;

    /**
     * @param userInterface              the main window for view switching
     * @param onTurnSelected             callback to open turn management
     * @param onInventorySelected        callback to open inventory management
     * @param onHistorySelected          callback to open history view
     * @param onAppOptionsSelected       callback to open app options
     * @param onSpendingRegisterSelected callback to open spending register
     * @param onExtraChangesSelected     callback to open extra changes (bank transfer/safe deposit)
     * @param onRoomSummarySelected      callback to open room summary dashboard
     */
    public ManagementController(UserGUI userInterface,
                                Runnable onTurnSelected,
                                Runnable onInventorySelected,
                                Runnable onHistorySelected,
                                Runnable onAppOptionsSelected,
                                Runnable onSpendingRegisterSelected,
                                Runnable onExtraChangesSelected,
                                Runnable onRoomSummarySelected) {
        this.userInterface = userInterface;
        this.onTurnSelected = onTurnSelected;
        this.onInventorySelected = onInventorySelected;
        this.onHistorySelected = onHistorySelected;
        this.onAppOptionsSelected = onAppOptionsSelected;
        this.onSpendingRegisterSelected = onSpendingRegisterSelected;
        this.onExtraChangesSelected = onExtraChangesSelected;
        this.onRoomSummarySelected = onRoomSummarySelected;
    }

    /** Registers action listeners for the management select view. */
    public void initListeners() {
        userInterface.getManagementSelection().getBackButton().addActionListener(e -> showFloorView());
        userInterface.getManagementSelection().getTurnButton().addActionListener(e -> onTurnSelected.run());
        userInterface.getManagementSelection().getInventoryButton().addActionListener(e -> onInventorySelected.run());
        userInterface.getManagementSelection().getHistoryButton().addActionListener(e -> onHistorySelected.run());
        userInterface.getManagementSelection().getAppOptionsButton().addActionListener(e -> onAppOptionsSelected.run());
        userInterface.getManagementSelection().getRoomSummaryButton().addActionListener(e -> onRoomSummarySelected.run());
        userInterface.getManagementSelection().getExtraChangesButton().addActionListener(e -> onExtraChangesSelected.run());
        userInterface.getManagementSelection().getRegisterSpendingButton().addActionListener(e -> onSpendingRegisterSelected.run());

        // Spending register view listeners
        userInterface.getSpendingRegisterView().getCancellationButton().addActionListener(e -> showManagementOptions());
        userInterface.getSpendingRegisterView().getConfirmationButton().addActionListener(e -> registerSpending());

        // Extra turn changes view listeners
        userInterface.getExtraTurnChangesView().getBackButton().addActionListener(e -> showManagementOptions());
        userInterface.getExtraTurnChangesView().getConfirmationButton().addActionListener(e -> registerExtraChanges());
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
        userInterface.getSpendingRegisterView().getValueTextField().setText("0");
        userInterface.getSpendingRegisterView().getDescriptionChangeText().setText("");
        userInterface.setSpendingRegisterView();
    }

    /** Called when the user confirms a spending registration. */
    public void registerSpending() {
        // This method is wired via callbacks in Controller to access motelManager
    }

    // ========== Extra Changes (Bank Transfer / Safe Deposit) ==========

    /** Opens the extra turn changes view. */
    public void showExtraChanges() {
        userInterface.getExtraTurnChangesView().getDescriptionText().setText("");
        userInterface.getExtraTurnChangesView().getValueTextField().setText("0");
        userInterface.getExtraTurnChangesView().getConfirmationButton().setEnabled(false);
        userInterface.getExtraTurnChangesView().getBankTransferBox().setSelected(false);
        userInterface.getExtraTurnChangesView().getSaveDespositBox().setSelected(false);
        userInterface.setExtraTurnChangesView();
    }

    /** Called when the user confirms an extra change registration. */
    public void registerExtraChanges() {
        // This method is wired via callbacks in Controller to access motelManager
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
