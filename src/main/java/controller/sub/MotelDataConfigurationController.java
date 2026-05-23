package controller.sub;

import model.modelManagers.MotelManagement;
import view.MotelDataConfigurationView;
import view.helpers.DialogHelper;

/**
 * Controls the motel data configuration view.
 *
 * <p>Handles:
 * <ul>
 *   <li>Populating the view from the application properties model</li>
 *   <li>Validating and saving motel name, NIT, and address</li>
 *   <li>Keeping printer receipt variables in sync with motel data</li>
 *   <li>Dirty tracking with confirmation dialogs for unsaved changes</li>
 * </ul>
 */
public class MotelDataConfigurationController {

    private final MotelManagement motelManager;
    private final MotelDataConfigurationView view;
    private final Runnable onBack;
    private final Runnable saveMainFiles;
    private final Runnable saveBackupFiles;

    public MotelDataConfigurationController(MotelManagement motelManager,
                                            MotelDataConfigurationView view,
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
        view.getBackButton().addActionListener(e -> onBackPressed());
        view.getSaveButton().addActionListener(e -> onSave());
    }

    public void populateView() {
        view.getNameTextField().setText(motelManager.getProgramConfig().getMotelName());
        view.getIdTextField().setText(motelManager.getProgramConfig().getMotelID());
        view.getAddressTextField().setText(motelManager.getProgramConfig().getMotelAddress());
        view.getConescutiveTransactionLabel()
                .setText(String.valueOf(motelManager.getProgramConfig().getConsecutiveTransaction()));
        view.clearDirty();
    }

    private void onSave() {
        String name = view.getNameTextField().getText().trim();
        String id = view.getIdTextField().getText().trim();
        String address = view.getAddressTextField().getText().trim();

        if (name.isEmpty() || id.isEmpty() || address.isEmpty()) {
            DialogHelper.showInfoMessage("Todos los campos (Nombre, NIT, Direccion) deben estar llenos", "ERROR");
            return;
        }

        boolean confirm = DialogHelper.confirmDialog(
                "Guardar cambios en los datos del motel?", "CONFIRMAR GUARDAR");
        if (!confirm) {
            return;
        }

        motelManager.saveMotelDataConfiguration(name, address, id);
        saveMainFiles.run();
        saveBackupFiles.run();
        view.clearDirty();
        DialogHelper.showInfoMessage("Datos del motel guardados exitosamente", "GUARDADO");
        onBack.run();
    }

    private void onBackPressed() {
        if (view.isDirty()) {
            boolean discard = DialogHelper.confirmDialog(
                    "Hay cambios sin guardar en los datos del motel. Perdera los cambios?",
                    "CAMBIOS SIN GUARDAR");
            if (!discard) {
                return;
            }
            populateView();
        }
        onBack.run();
    }
}
