package controller.sub;

import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.modelManagers.ISellingService;
import model.dto.InventoryItemData;
import view.InventoryManagementView;
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

    private final ISellingService sellingService;
    private final InventoryManagementView inventoryView;
    private final Runnable onBack;
    private final Runnable saveMainFiles;
    private final Runnable saveBackupFilesRoomSwap;
    private boolean isListAdjusting = false;

    public InventoryController(ISellingService sellingService, InventoryManagementView inventoryView,
                               Runnable onBack, Runnable saveMainFiles, Runnable saveBackupFilesRoomSwap) {
        this.sellingService = sellingService;
        this.inventoryView = inventoryView;
        this.onBack = onBack;
        this.saveMainFiles = saveMainFiles;
        this.saveBackupFilesRoomSwap = saveBackupFilesRoomSwap;
    }

    /** Registers action listeners for the inventory view. */
    public void initListeners() {
        inventoryView.onNewItem(this::newItem);
        inventoryView.onDeleteItem(this::deleteItem);
        inventoryView.onAddQuantity(() -> changeQuantity(1));
        inventoryView.onRemoveQuantity(() -> changeQuantity(-1));
        inventoryView.onRemoveSmallPrice(() -> modifyPrice(-100));
        inventoryView.onAddSmallPrice(() -> modifyPrice(100));
        inventoryView.onRemoveBigPrice(() -> modifyPrice(-1000));
        inventoryView.onAddBigPrice(() -> modifyPrice(1000));
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
                    InventoryItemData selectedItem = inventoryView.getCurrentSelectedItem(selectedRow);
                    inventoryView.setEditInfoText("MODIFICANDO");
                    inventoryView.setNameText(selectedItem.name());
                    inventoryView.setQuantityText(String.valueOf(selectedItem.quantity()));
                    inventoryView.setPriceText(String.valueOf(selectedItem.price()));
                    setModificators(true);
                    isListAdjusting = false;
                }
            }
        });
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
            sellingService.deleteItemFromInventory(selectedItem.itemID());
        }
        updateViewData();
        setModificators(false);
    }

    public void changeQuantity(int delta) {
        inventoryView.adjustQuantity(delta);
    }

    public void modifyPrice(int delta) {
        inventoryView.adjustPrice(delta);
    }

    public void saveItem() {
        int rowSelected = inventoryView.getSelectedInventoryRow();
        String newName = inventoryView.getNameText();
        long newQuantity = InputParser.parseLongSafe(inventoryView.getQuantityText());
        long newPrice = InputParser.parseLongSafe(inventoryView.getPriceText());

        if (rowSelected != -1) {
            InventoryItemData selectedItem = inventoryView.getCurrentSelectedItem(rowSelected);
            InventoryItemData updatedItem = new InventoryItemData(
                    selectedItem.itemID(), newName, newPrice, newQuantity);
            sellingService.saveItemInformation(updatedItem);
        } else {
            sellingService.newItemCreated(newName, newPrice, newQuantity);
        }
        setModificators(false);
        inventoryView.clearInventorySelection();
        saveMainFiles.run();
        saveBackupFilesRoomSwap.run();
    }

    public void setModificators(boolean enable) {
        if (!enable) {
            inventoryView.setEditInfoText("SELECCIONE O CREE OBJETO");
            inventoryView.setNameText("");
            inventoryView.setQuantityText("0");
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
        inventoryView.updateInventory(sellingService.getInventoryItemDataList());
    }

}
