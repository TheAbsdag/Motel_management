package controller.sub;

import model.json.CurrencyConfig;
import model.modelManagers.MotelManagement;
import view.CurrencyConfigurationView;
import view.helpers.DialogHelper;

public class CurrencyConfigurationController {

    private final MotelManagement motelManager;
    private final CurrencyConfigurationView view;
    private final Runnable onBack;
    private final Runnable saveMainFiles;
    private final Runnable saveBackupFiles;

    public CurrencyConfigurationController(MotelManagement motelManager,
                                           CurrencyConfigurationView view,
                                           Runnable onBack,
                                           Runnable saveMainFiles,
                                           Runnable saveBackupFiles) {
        this.motelManager = motelManager;
        this.view = view;
        this.onBack = onBack;
        this.saveMainFiles = saveMainFiles;
        this.saveBackupFiles = saveBackupFiles;
    }

    public void initListeners() {
        view.onBackButton(() -> onBackPressed());
        view.onSaveButton(() -> onSave());
    }

    public void populateView() {
        CurrencyConfig current = motelManager.getProgramConfig().getCurrencyConfig();
        view.populate(current);
    }

    private void onSave() {
        String symbol = view.getSymbol();
        if (symbol.isEmpty()) {
            DialogHelper.showInfoMessage("El simbolo de moneda no puede estar vacio", "ERROR");
            return;
        }

        boolean confirm = DialogHelper.confirmDialog(
                "Guardar configuracion de moneda?", "CONFIRMAR GUARDAR");
        if (!confirm) return;

        CurrencyConfig cfg = view.toConfig();
        motelManager.saveCurrencyConfiguration(cfg);
        saveMainFiles.run();
        saveBackupFiles.run();
        view.clearDirty();
        DialogHelper.showInfoMessage("Configuracion de moneda guardada exitosamente", "GUARDADO");
        onBack.run();
    }

    private void onBackPressed() {
        if (view.isDirty()) {
            boolean discard = DialogHelper.confirmDialog(
                    "Hay cambios sin guardar en la configuracion de moneda. Perdera los cambios?",
                    "CAMBIOS SIN GUARDAR");
            if (!discard) return;
            populateView();
        }
        onBack.run();
    }

    public void configureForFirstBoot(Runnable onCompleted) {
        view.setBackEnabled(false);
        view.clearDirty();
        view.removeSaveListeners();
        view.onSaveButton(() -> {
            String symbol = view.getSymbol();
            if (symbol.isEmpty()) {
                DialogHelper.showInfoMessage("El simbolo de moneda no puede estar vacio", "ERROR");
                return;
            }
            CurrencyConfig cfg = view.toConfig();
            motelManager.saveCurrencyConfiguration(cfg);
            saveMainFiles.run();
            saveBackupFiles.run();
            view.clearDirty();
            DialogHelper.showInfoMessage("Configuracion de moneda guardada exitosamente", "GUARDADO");
            onCompleted.run();
        });
    }
}
