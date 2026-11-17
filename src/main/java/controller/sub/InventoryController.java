package controller.sub;

import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.dto.InventoryItemData;
import model.modelManagers.MotelManagement;
import view.InventoryManagementView;
import view.helpers.DialogHelper;
import view.helpers.InputParser;

/**
 * Controls inventory management operations (CRUD, quantity/price adjustments).
 *
 * <p>Handles:
 * <ul>
 *   <li>Creating new inventory items</li>
 *   <li>Deleting existing items</li>
 *   <li>Modifying item name, quantity, and price</li>
 *   <li>Saving item changes</li>
 *   <li>Table navigation (touch-friendly scrolling)</li>
 * </ul>
 */
public class InventoryController {

    private final MotelManagement motelManager;
    private final InventoryManagementView inventoryView;
    private final Runnable onBack;
    private final Runnable saveMainFiles;
    private final Runnable saveBackupFiles;
    private boolean isListAdjusting = false;

    public InventoryController(MotelManagement motelManager, InventoryManagementView inventoryView,
                               Runnable onBack, Runnable saveMainFiles, Runnable saveBackupFiles) {
        this.motelManager = motelManager;
        this.inventoryView = inventoryView;
        this.onBack = onBack;
        this.saveMainFiles = saveMainFiles;
        this.saveBackupFiles = saveBackupFiles;
    }

    /** Registers action listeners for the inventory view. */
    public void initListeners() {
        inventoryView.onNewItem(this::newItem);
        inventoryView.onDeleteItem(this::deleteItem);
        inventoryView.onAddQuantity(() -> inventoryView.adjustQuantity(1));
        inventoryView.onRemoveQuantity(() -> inventoryView.adjustQuantity(-1));
        inventoryView.onRemoveSmallPrice(() -> inventoryView.adjustPrice(-100));
        inventoryView.onAddSmallPrice(() -> inventoryView.adjustPrice(100));
        inventoryView.onRemoveBigPrice(() -> inventoryView.adjustPrice(-1000));
        inventoryView.onAddBigPrice(() -> inventoryView.adjustPrice(1000));
        inventoryView.onSaveButton(this::saveItem);
        inventoryView.onBackButton(onBack);
        inventoryView.onUpButton(() -> inventoryView.scrollInventoryTable(-1));
        inventoryView.onDownButton(() -> inventoryView.scrollInventoryTable(1));

        inventoryView.onNameTextChanged(new DocumentListener() {
            private void update() { updateSaveButtonState(); }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        inventoryView.onInventorySelection(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = inventoryView.getSelectedInventoryRow();
                if (selectedRow != -1 && !isListAdjusting) {
                    isListAdjusting = true;
                    loadSelectedItem(inventoryView.getCurrentSelectedItem(selectedRow));
                    setModificators(true);
                    isListAdjusting = false;
                }
            }
        });
    }

    private void loadSelectedItem(InventoryItemData selectedItem) {
        inventoryView.setEditInfoText("MODIFICANDO");
        inventoryView.setNameText(selectedItem.name());
        inventoryView.setQuantityText(String.valueOf(selectedItem.quantity()));
        inventoryView.setMinimumText(String.valueOf(selectedItem.minimum()));
        inventoryView.setPriceText(String.valueOf(selectedItem.price()));
    }

    /** Prepares the view for creating a new inventory item. */
    public void newItem() {
        inventoryView.clearInventorySelection();
        setModificators(true);
        inventoryView.setEditInfoText("CREANDO");
        updateViewData();
        inventoryView.setSaveEnabled(true);
    }

    /** Deletes the currently selected inventory item. */
    public void deleteItem() {
        int rowSelected = inventoryView.getSelectedInventoryRow();
        if (rowSelected != -1) {
            InventoryItemData selectedItem = inventoryView.getCurrentSelectedItem(rowSelected);
            if (selectedItem.quantity() != 0 && !DialogHelper.confirmDialog(
                    "El producto " + selectedItem.name() + " tiene " + selectedItem.quantity()
                            + " unidades. Se registrará una baja de inventario. ¿Eliminarlo?",
                    "ELIMINAR PRODUCTO")) {
                return;
            }
            motelManager.deleteItemFromInventory(selectedItem.itemID());
        }
        updateViewData();
        setModificators(false);
    }

    public void saveItem() {
        int rowSelected = inventoryView.getSelectedInventoryRow();
        String newName = inventoryView.getNameText();
        long newQuantity = InputParser.parseLongSafe(inventoryView.getQuantityText());
        long newPrice = InputParser.parseLongSafe(inventoryView.getPriceText());
        long newMinimum = InputParser.parseLongSafe(inventoryView.getMinimumText());

        if (rowSelected != -1) {
            InventoryItemData selectedItem = inventoryView.getCurrentSelectedItem(rowSelected);
            String concept = null;
            long cost = 0L;
            if (newQuantity != selectedItem.quantity()) {
                DialogHelper.InventoryMovementInput input = DialogHelper.showInventoryMovementDialog(
                        selectedItem.name(), selectedItem.quantity(), newQuantity);
                if (input == null) {
                    loadSelectedItem(selectedItem);
                    return;
                }
                concept = input.concept();
                cost = input.cost();
            }
            InventoryItemData updatedItem = new InventoryItemData(
                    selectedItem.itemID(), newName, newPrice, newQuantity, newMinimum);
            motelManager.saveInventoryItem(updatedItem, concept, cost);
        } else {
            motelManager.newItemCreated(newName, newPrice, newQuantity, newMinimum);
        }
        setModificators(false);
        inventoryView.clearInventorySelection();
        saveMainFiles.run();
        saveBackupFiles.run();
    }

    public void setModificators(boolean enable) {
        if (!enable) {
            inventoryView.setEditInfoText("SELECCIONE O CREE OBJETO");
            inventoryView.setNameText("");
            inventoryView.setQuantityText("0");
            inventoryView.setMinimumText("0");
            inventoryView.setPriceText("0");
            updateViewData();
        }
        inventoryView.setDeleteEnabled(enable);
        inventoryView.setAddQuantityEnabled(enable);
        inventoryView.setRemoveQuantityEnabled(enable);
        inventoryView.setRemoveSmallPriceEnabled(enable);
        inventoryView.setAddSmallPriceEnabled(enable);
        inventoryView.setRemoveBigPriceEnabled(enable);
        inventoryView.setAddBigPriceEnabled(enable);
        inventoryView.setSaveEnabled(enable && !inventoryView.getNameText().trim().isEmpty());
        inventoryView.setNameEnabled(enable);
        inventoryView.setPriceEnabled(enable);
        inventoryView.setQuantityEnabled(enable);
        inventoryView.setMinimumEnabled(enable);
    }

    private void updateSaveButtonState() {
        String name = inventoryView.getNameText().trim();
        inventoryView.setSaveEnabled(!name.isEmpty()
                && inventoryView.isNameEnabled());
    }

    /** Opens the inventory management view with modificators disabled. */
    public void openView() {
        setModificators(false);
    }

    /** Refreshes the inventory table data. */
    public void updateViewData() {
        inventoryView.updateInventory(motelManager.getInventoryItemDataList());
    }

}
