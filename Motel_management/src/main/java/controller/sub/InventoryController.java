package controller.sub;

import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.MotelManagement;
import model.dto.InventoryItemData;
import view.InventoryManagementView;

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
    private final Runnable saveBackupFilesOperation;
    private boolean isListAdjusting = false;

    public InventoryController(MotelManagement motelManager, InventoryManagementView inventoryView,
                               Runnable onBack, Runnable saveBackupFilesOperation) {
        this.motelManager = motelManager;
        this.inventoryView = inventoryView;
        this.onBack = onBack;
        this.saveBackupFilesOperation = saveBackupFilesOperation;
    }

    /** Registers action listeners for the inventory view. */
    public void initListeners() {
        inventoryView.getNewitemButton().addActionListener(e -> newItem());
        inventoryView.getDeleteItemButton().addActionListener(e -> deleteItem());
        inventoryView.getAddQuantityButton().addActionListener(e -> changeQuantity(1));
        inventoryView.getRemoveQuantityButton().addActionListener(e -> changeQuantity(-1));
        inventoryView.getRemoveSmallPriceButton().addActionListener(e -> modifyPrice(-100));
        inventoryView.getAddSmallPriceButton().addActionListener(e -> modifyPrice(100));
        inventoryView.getRemoveBigPriceButton().addActionListener(e -> modifyPrice(-1000));
        inventoryView.getAddBigPriceButton().addActionListener(e -> modifyPrice(1000));
        inventoryView.getSaveButton().addActionListener(e -> saveItem());
        inventoryView.getBackButton().addActionListener(e -> onBack.run());
        inventoryView.getUpButton().addActionListener(e ->
                ControllerUtils.scrollTable(inventoryView.getInventoryTable(), -1));
        inventoryView.getDownButton().addActionListener(e ->
                ControllerUtils.scrollTable(inventoryView.getInventoryTable(), 1));

        // Name field validation: enable save only when name is non-empty
        inventoryView.getNameTextField().getDocument().addDocumentListener(new DocumentListener() {
            private void update() { updateSaveButtonState(); }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        // Table selection listener for loading selected item into edit fields
        inventoryView.getInventoryTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = inventoryView.getInventoryTable().getSelectedRow();
                if (selectedRow != -1 && !isListAdjusting) {
                    isListAdjusting = true;
                    InventoryItemData selectedItem = inventoryView.getCurrentSelectedItem(selectedRow);
                    inventoryView.getInformativeEditLabel().setText("MODIFICANDO");
                    inventoryView.getNameTextField().setText(selectedItem.name());
                    inventoryView.getQuantityTextField().setText(String.valueOf(selectedItem.quantity()));
                    inventoryView.getPriceTextField().setText(String.valueOf(selectedItem.price()));
                    setModificators(true);
                    isListAdjusting = false;
                }
            }
        });
    }

    /** Prepares the view for creating a new inventory item. */
    public void newItem() {
        inventoryView.getInventoryTable().clearSelection();
        setModificators(true);
        inventoryView.getInformativeEditLabel().setText("CREANDO");
        updateViewData();
        inventoryView.getSaveButton().setEnabled(true);
    }

    /** Deletes the currently selected inventory item. */
    public void deleteItem() {
        int rowSelected = inventoryView.getInventoryTable().getSelectedRow();
        if (rowSelected != -1) {
            InventoryItemData selectedItem = inventoryView.getCurrentSelectedItem(rowSelected);
            motelManager.deleteItemFromInventory(selectedItem.itemID());
        }
        updateViewData();
        setModificators(false);
    }

    /**
     * Adjusts the quantity field by the given delta.
     *
     * @param delta typically +1 or -1
     */
    public void changeQuantity(int delta) {
        int currentValue;
        try {
            currentValue = Integer.parseInt(inventoryView.getQuantityTextField().getText()) + delta;
        } catch (NumberFormatException ex) {
            currentValue = 0;
        }
        if (currentValue < 0) currentValue = 0;
        inventoryView.getQuantityTextField().setText(String.valueOf(currentValue));
    }

    /**
     * Adjusts the price field by the given delta.
     *
     * @param delta typically +/- 100 or +/- 1000
     */
    public void modifyPrice(int delta) {
        int currentValue = 0;
        try {
            currentValue = Integer.parseInt(inventoryView.getPriceTextField().getText()) + delta;
        } catch (NumberFormatException ex) {
            currentValue = 0;
        }
        if (currentValue < 0) currentValue = 0;
        inventoryView.getPriceTextField().setText(String.valueOf(currentValue));
    }

    /**
     * Saves the current item (new or modified) to the inventory.
     */
    public void saveItem() {
        int rowSelected = inventoryView.getInventoryTable().getSelectedRow();
        String newName = inventoryView.getNameTextField().getText();
        int newQuantity;
        long newPrice;
        try {
            newQuantity = Integer.parseInt(inventoryView.getQuantityTextField().getText());
        } catch (NumberFormatException ex) {
            newQuantity = 0;
        }
        try {
            newPrice = Long.parseLong(inventoryView.getPriceTextField().getText());
        } catch (NumberFormatException ex) {
            newPrice = 0;
        }

        if (rowSelected != -1) {
            InventoryItemData selectedItem = inventoryView.getCurrentSelectedItem(rowSelected);
            InventoryItemData updatedItem = new InventoryItemData(
                    selectedItem.itemID(), newName, newPrice, newQuantity);
            motelManager.saveItemInformation(updatedItem);
        } else {
            motelManager.newItemCreated(newName, newPrice, newQuantity);
        }
        setModificators(false);
        inventoryView.getInventoryTable().clearSelection();
        motelManager.saveFilesForMainService();
        saveBackupFilesOperation.run();
    }

    /**
     * Enables or disables the inventory modification controls.
     */
    public void setModificators(boolean enable) {
        if (!enable) {
            inventoryView.getInformativeEditLabel().setText("SELECCIONE O CREE OBJETO");
            inventoryView.getNameTextField().setText("");
            inventoryView.getQuantityTextField().setText("0");
            inventoryView.getPriceTextField().setText("0");
            updateViewData();
        }
        inventoryView.getDeleteItemButton().setEnabled(enable);
        inventoryView.getAddQuantityButton().setEnabled(enable);
        inventoryView.getRemoveQuantityButton().setEnabled(enable);
        inventoryView.getRemoveSmallPriceButton().setEnabled(enable);
        inventoryView.getAddSmallPriceButton().setEnabled(enable);
        inventoryView.getRemoveBigPriceButton().setEnabled(enable);
        inventoryView.getAddBigPriceButton().setEnabled(enable);
        inventoryView.getSaveButton().setEnabled(enable && !inventoryView.getNameTextField().getText().trim().isEmpty());
        inventoryView.getNameTextField().setEnabled(enable);
        inventoryView.getPriceTextField().setEnabled(enable);
        inventoryView.getQuantityTextField().setEnabled(enable);
    }

    /** Enables the save button only when the name field is non-empty. */
    private void updateSaveButtonState() {
        String name = inventoryView.getNameTextField().getText().trim();
        inventoryView.getSaveButton().setEnabled(!name.isEmpty()
                && inventoryView.getNameTextField().isEnabled());
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
