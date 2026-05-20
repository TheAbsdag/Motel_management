package controller.sub;

import java.util.List;
import javax.swing.DefaultListModel;
import model.modelManagers.MotelManagement;
import view.AppOptionsView;
import view.PrinterConfigurationView;

/**
 * Controls the application options hub view and printer configuration.
 *
 * <p>Handles:
 * <ul>
 *   <li>Navigation from the options hub to sub-configuration views</li>
 *   <li>Displaying available printer services</li>
 *   <li>Confirming and setting the active printer</li>
 * </ul>
 */
public class AppOptionsController {

    private final MotelManagement motelManager;
    private final AppOptionsView appOptionsView;
    private final PrinterConfigurationView printerView;
    private final Runnable onBack;
    private final Runnable onShowPrinter;
    private final Runnable onShowMotelData;
    private final Runnable onShowTimeConfig;
    private final Runnable onShowFloorConfig;
    private final Runnable onShowDataSaving;
    private final Runnable onShowOptions;

    /**
     * @param motelManager    the model
     * @param appOptionsView  the options hub view panel
     * @param printerView     the printer configuration view panel
     * @param onBack          callback to return to management options view
     * @param onShowPrinter   callback to navigate to printer config view
     * @param onShowMotelData callback to navigate to motel data config view
     * @param onShowTimeConfig callback to navigate to time config view
     * @param onShowFloorConfig callback to navigate to floor config view
     * @param onShowDataSaving callback to navigate to data saving config view
     * @param onShowOptions   callback to return to the options hub
     */
    public AppOptionsController(MotelManagement motelManager,
                                AppOptionsView appOptionsView,
                                PrinterConfigurationView printerView,
                                Runnable onBack,
                                Runnable onShowPrinter,
                                Runnable onShowMotelData,
                                Runnable onShowTimeConfig,
                                Runnable onShowFloorConfig,
                                Runnable onShowDataSaving,
                                Runnable onShowOptions) {
        this.motelManager = motelManager;
        this.appOptionsView = appOptionsView;
        this.printerView = printerView;
        this.onBack = onBack;
        this.onShowPrinter = onShowPrinter;
        this.onShowMotelData = onShowMotelData;
        this.onShowTimeConfig = onShowTimeConfig;
        this.onShowFloorConfig = onShowFloorConfig;
        this.onShowDataSaving = onShowDataSaving;
        this.onShowOptions = onShowOptions;
    }

    /** Registers action listeners for the app options views. */
    public void initListeners() {
        // Options hub navigation
        appOptionsView.getBackButton().addActionListener(e -> onBack.run());
        appOptionsView.getPrinterOptionsButton().addActionListener(e -> {
            showPrinterOptions();
            onShowPrinter.run();
        });
        appOptionsView.getDataConfigurationButton().addActionListener(e -> onShowMotelData.run());
        appOptionsView.getDateAndTimeConfigurationButton().addActionListener(e -> onShowTimeConfig.run());
        appOptionsView.getFloorConfigurationButton().addActionListener(e -> onShowFloorConfig.run());
        appOptionsView.getSaveConfigurationButton().addActionListener(e -> onShowDataSaving.run());

        // Sub-config view back buttons → return to options hub
        printerView.getBackButton().addActionListener(e -> onShowOptions.run());

        // Printer selection
        printerView.getConfirmPrinterButton().addActionListener(e -> confirmPrinter());
        printerView.getPrinterList().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedIndex = printerView.getPrinterList().getSelectedIndex();
                if (selectedIndex != -1) {
                    String printerName = motelManager.getPrinterLists().get(selectedIndex);
                    printerView.getSelectedPrinterLabel().setText(printerName);
                    printerView.getConfirmPrinterButton().setEnabled(true);
                }
            }
        });
    }

    /** Opens the options hub. */
    public void showOptions() {
        onShowOptions.run();
    }

    /** Populates the printer configuration view with current printer data. */
    public void showPrinterOptions() {
        printerView.getConfirmPrinterButton().setEnabled(false);
        printerView.getPrinterUsedLabel().setText(motelManager.getCurrentPrinterName());
        List<String> printerNames = motelManager.getPrinterLists();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String name : printerNames) {
            model.addElement(name);
        }
        printerView.getPrinterList().setModel(model);
    }

    /** Confirms the selected printer as the active print service. */
    public void confirmPrinter() {
        int selectedIndex = printerView.getPrinterList().getSelectedIndex();
        if (selectedIndex != -1) {
            String printerName = motelManager.getPrinterLists().get(selectedIndex);
            motelManager.setPrinter(printerName);
            motelManager.savePrinterConfiguration(printerName);
            printerView.getPrinterUsedLabel().setText(motelManager.getCurrentPrinterName());
        }
    }
}
