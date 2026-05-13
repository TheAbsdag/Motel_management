package controller.sub;

import java.util.List;
import javax.swing.DefaultListModel;
import model.modelManagers.MotelManagement;
import view.AppOptionsView;

/**
 * Controls the application options view (printer selection).
 *
 * <p>Handles:
 * <ul>
 *   <li>Displaying available printer services</li>
 *   <li>Confirming and setting the active printer</li>
 * </ul>
 */
public class AppOptionsController {

    private final MotelManagement motelManager;
    private final AppOptionsView appOptionsView;
    private final Runnable onBack;

    /**
     * @param motelManager   the model
     * @param appOptionsView the app options view panel
     * @param onBack         callback to return to management options view
     */
    public AppOptionsController(MotelManagement motelManager, AppOptionsView appOptionsView, Runnable onBack) {
        this.motelManager = motelManager;
        this.appOptionsView = appOptionsView;
        this.onBack = onBack;
    }

    /** Registers action listeners for the app options view. */
    public void initListeners() {
        appOptionsView.getBackButton().addActionListener(e -> onBack.run());
        appOptionsView.getConfirmPrinterButton().addActionListener(e -> confirmPrinter());
        appOptionsView.getPrinterList().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedIndex = appOptionsView.getPrinterList().getSelectedIndex();
                if (selectedIndex != -1) {
                    String printerName = motelManager.getPrinterLists().get(selectedIndex);
                    appOptionsView.getSelectedPrinterLabel().setText(printerName);
                    appOptionsView.getConfirmPrinterButton().setEnabled(true);
                }
            }
        });
    }

    /** Opens the app options view with the current printer information shown. */
    public void showOptions() {
        appOptionsView.getConfirmPrinterButton().setEnabled(false);
        appOptionsView.getPrinterUsedLabel().setText(motelManager.getCurrentPrinterName());
        List<String> printerNames = motelManager.getPrinterLists();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String name : printerNames) {
            model.addElement(name);
        }
        appOptionsView.getPrinterList().setModel(model);
    }

    /** Confirms the selected printer as the active print service. */
    public void confirmPrinter() {
        int selectedIndex = appOptionsView.getPrinterList().getSelectedIndex();
        if (selectedIndex != -1) {
            String printerName = motelManager.getPrinterLists().get(selectedIndex);
            motelManager.setPrinter(printerName);
            motelManager.savePrinterConfiguration(printerName);
            appOptionsView.getPrinterUsedLabel().setText(motelManager.getCurrentPrinterName());
        }
    }
}
