package controller.sub;

import javax.swing.JTable;
import model.modelManagers.MotelManagement;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import view.SellingView;
import view.UserGUI;

/**
 * Controls the item selling flow (room charges and reception sales).
 *
 * <p>Handles:
 * <ul>
 *   <li>Initiating a sale (from room view or reception)</li>
 *   <li>Adding/removing items to the selling cart</li>
 *   <li>Quantity adjustments and courtesy items</li>
 *   <li>Completing the sale with optional receipt printing</li>
 * </ul>
 */
public class SellingController {

    private final MotelManagement motelManager;
    private final SellingView sellingView;
    private final UserGUI userInterface;
    private final Runnable saveBackupFilesOperation;
    private boolean isListAdjusting = false;

    public SellingController(MotelManagement motelManager, SellingView sellingView, UserGUI userInterface,
                             Runnable saveBackupFilesOperation) {
        this.motelManager = motelManager;
        this.sellingView = sellingView;
        this.userInterface = userInterface;
        this.saveBackupFilesOperation = saveBackupFilesOperation;
    }

    /** Registers action listeners for the selling view. */
    public void initListeners() {
        sellingView.getBackButton().addActionListener(e -> backFromSelling());
        sellingView.getItemDeleteButton().addActionListener(e -> removeItemFromRegisterList());
        sellingView.getAddItemButton().addActionListener(e -> addItemToRegisterList());
        sellingView.getAddQuantityButton().addActionListener(e -> updateItemSaleAmount(1));
        sellingView.getRemoveQuantityButton().addActionListener(e -> updateItemSaleAmount(-1));
        sellingView.getFinishSaleButton().addActionListener(e -> finishSale());
        sellingView.getUpSellingListButton().addActionListener(e -> scrollSelectedTable(-1));
        sellingView.getDownSellingListButton().addActionListener(e -> scrollSelectedTable(1));
        sellingView.getCourtesySaleButton().addActionListener(e -> addCourtesyItemToRegister());

        // Table selection listeners for enabling/disabling add/delete buttons
        sellingView.getItemTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !isListAdjusting) {
                isListAdjusting = true;
                sellingView.getQuantityTextField().setText("1");
                sellingView.getAddItemButton().setEnabled(true);
                sellingView.getItemDeleteButton().setEnabled(false);
                sellingView.getSellingTable().clearSelection();
                isListAdjusting = false;
            }
        });
        sellingView.getSellingTable().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !isListAdjusting) {
                isListAdjusting = true;
                sellingView.getItemDeleteButton().setEnabled(true);
                sellingView.getAddItemButton().setEnabled(false);
                sellingView.getItemTable().clearSelection();
                isListAdjusting = false;
            }
        });
    }

    // ========== Sale Flow ==========

    /**
     * Starts a new sale session.
     *
     * @param receptionSale if true, sale is from reception (no room assignment);
     *                      if false, sale is charged to the currently selected room
     */
    public void roomSale(boolean receptionSale) {
        if (receptionSale) {
            motelManager.setCurrentFloorRoom(-1, -1, -1);
            sellingView.getCourtesySaleButton().setVisible(true);
        } else {
            sellingView.getCourtesySaleButton().setVisible(false);
        }
        motelManager.restartSaleManager();
        String roomString = motelManager.getRoom(
                motelManager.getCurrentTowerViewed(),
                motelManager.getCurrentFloorViewed(),
                motelManager.getCurrentRoomViewed()
        ).getRoomString();
        sellingView.getSellingToLabel().setText("VENDIENDO A: " + roomString);
        userInterface.setSellingView();
        sellingView.getAddItemButton().setEnabled(false);
        sellingView.getItemDeleteButton().setEnabled(false);
        sellingView.getFinishSaleButton().setEnabled(false);
        sellingView.updateItemListed(motelManager.getInventoryItemDataList());
        sellingView.updateSellingListed(motelManager.getSellingItemDataList());
    }

    /** Adds the selected item to the selling cart. */
    public void addItemToRegisterList() {
        InventoryItemData itemSelected = sellingView.getCurrentSelectedItemListed(
                sellingView.getItemTable().getSelectedRow());
        int quantity;
        try {
            quantity = Integer.parseInt(sellingView.getQuantityTextField().getText());
        } catch (NumberFormatException ex) {
            quantity = 1;
            sellingView.getQuantityTextField().setText("1");
        }
        long itemID = itemSelected.itemID();
        sellingView.getFinishSaleButton().setEnabled(true);
        motelManager.addItemToSelling(itemID, quantity, false);
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();
        sellingView.getTotalPriceLabel().setText(String.valueOf(totalPrice));
        sellingView.updateSellingListed(motelManager.getSellingItemDataList());
        sellingView.getItemTable().clearSelection();
        sellingView.getSellingTable().clearSelection();
        sellingView.getAddItemButton().setEnabled(false);
    }

    /** Removes the selected item from the selling cart. */
    public void removeItemFromRegisterList() {
        SellingItemData itemSelected = sellingView.getCurrentSelectedSellingListed(
                sellingView.getSellingTable().getSelectedRow());
        long itemID = itemSelected.itemID();
        motelManager.removeItemToSelling(itemID);
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();
        sellingView.getTotalPriceLabel().setText(String.valueOf(totalPrice));
        sellingView.updateSellingListed(motelManager.getSellingItemDataList());
    }

    /**
     * Adjusts the quantity for the item about to be added.
     *
     * @param delta amount to add or subtract (typically +1 or -1)
     */
    public void updateItemSaleAmount(int delta) {
        int newValue = 0;
        try {
            newValue = Integer.parseInt(sellingView.getQuantityTextField().getText()) + delta;
            if (newValue < 0) newValue = 0;
        } catch (NumberFormatException ex) {
            newValue = 0;
        }
        sellingView.getQuantityTextField().setText(String.valueOf(newValue));
    }

    /** Adds a courtesy (zero-price) item to the selling cart. */
    public void addCourtesyItemToRegister() {
        InventoryItemData itemSelected = sellingView.getCurrentSelectedItemListed(
                sellingView.getItemTable().getSelectedRow());
        int quantity;
        try {
            quantity = Integer.parseInt(sellingView.getQuantityTextField().getText());
        } catch (NumberFormatException ex) {
            quantity = 1;
            sellingView.getQuantityTextField().setText("1");
        }
        long itemID = itemSelected.itemID();
        sellingView.getFinishSaleButton().setEnabled(true);
        motelManager.addItemToSelling(itemID, quantity, true);
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();
        sellingView.getTotalPriceLabel().setText(String.valueOf(totalPrice));
        sellingView.updateSellingListed(motelManager.getSellingItemDataList());
        sellingView.getItemTable().clearSelection();
        sellingView.getSellingTable().clearSelection();
        sellingView.getAddItemButton().setEnabled(false);
    }

    /**
     * Completes the sale, records the transaction, and optionally prints a receipt.
     */
    public void finishSale() {
        boolean print = sellingView.getPrintingCheckBox().isSelected();
        if (!print) {
            boolean noPrintingConfirmation = userInterface.confirmPrinting();
            if (noPrintingConfirmation) {
                motelManager.roomSaleFinished(false);
                userInterface.setFloorView();
                motelManager.saveFilesForMainService();
                saveBackupFilesOperation.run();
            }
        } else {
            motelManager.roomSaleFinished(true);
            userInterface.setFloorView();
            motelManager.saveFilesForMainService();
            saveBackupFilesOperation.run();
        }
    }

    /** Cancels the sale and returns to the floor view. */
    public void backFromSelling() {
        motelManager.restartSaleManager();
        userInterface.setFloorView();
    }

    // ========== Table Scrolling (Touch-Friendly) ==========

    /**
     * Scrolls whichever table currently has a row selected.
     * If the selling table is selected, scrolls that one; otherwise scrolls the item table.
     *
     * @param direction +1 for down, -1 for up
     */
    private void scrollSelectedTable(int direction) {
        JTable table = sellingView.getSellingTable().getSelectedRow() >= 0
                ? sellingView.getSellingTable()
                : sellingView.getItemTable();
        int currentRow = table.getSelectedRow();
        int targetRow = Math.max(0, Math.min(currentRow + direction, table.getRowCount() - 1));
        if (targetRow >= 0) {
            table.setRowSelectionInterval(targetRow, targetRow);
            table.scrollRectToVisible(table.getCellRect(targetRow, 0, true));
        }
    }
}
