package controller.sub;

import java.util.List;
import java.util.Map;
import model.ProgramConfig;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import model.modelManagers.EmailConfigurationService;
import model.modelManagers.MotelManagement;
import view.SellingView;
import view.UserGUI;
import view.helpers.DialogHelper;
import view.helpers.InputParser;

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
    private final Runnable saveMainFiles;
    private final Runnable saveBackupFilesTransaction;
    private boolean isListAdjusting = false;

    public SellingController(MotelManagement motelManager, SellingView sellingView, UserGUI userInterface,
                             Runnable saveMainFiles, Runnable saveBackupFilesTransaction) {
        this.motelManager = motelManager;
        this.sellingView = sellingView;
        this.userInterface = userInterface;
        this.saveMainFiles = saveMainFiles;
        this.saveBackupFilesTransaction = saveBackupFilesTransaction;
    }

    /** Registers action listeners for the selling view. */
    public void initListeners() {
        sellingView.onBackButton(this::backFromSelling);
        sellingView.onItemDeleteButton(this::removeItemFromRegisterList);
        sellingView.onAddItemButton(this::addItemToRegisterList);
        sellingView.onAddQuantityButton(() -> updateItemSaleAmount(1));
        sellingView.onRemoveQuantityButton(() -> updateItemSaleAmount(-1));
        sellingView.onFinishSaleButton(this::finishSale);
        sellingView.onUpSellingListButton(() -> scrollSelectedTable(-1));
        sellingView.onDownSellingListButton(() -> scrollSelectedTable(1));
        sellingView.onCourtesySaleButton(this::addCourtesyItemToRegister);

        sellingView.onItemTableSelection(event -> {
            if (!event.getValueIsAdjusting() && !isListAdjusting) {
                isListAdjusting = true;
                sellingView.setQuantityText("1");
                sellingView.setAddItemEnabled(true);
                sellingView.setItemDeleteEnabled(false);
                sellingView.clearSellingTableSelection();
                isListAdjusting = false;
            }
        });
        sellingView.onSellingTableSelection(event -> {
            if (!event.getValueIsAdjusting() && !isListAdjusting) {
                isListAdjusting = true;
                sellingView.setItemDeleteEnabled(true);
                sellingView.setAddItemEnabled(false);
                sellingView.clearItemTableSelection();
                isListAdjusting = false;
            }
        });
    }

    // ========== Sale Flow ==========

    public void roomSale(boolean receptionSale) {
        if (receptionSale) {
            motelManager.setCurrentFloorRoom(-1, -1, -1);
            sellingView.setCourtesySaleVisible(true);
        } else {
            sellingView.setCourtesySaleVisible(false);
        }
        motelManager.restartSaleManager();
        String roomString = motelManager.getRoom(
                motelManager.getCurrentTowerViewed(),
                motelManager.getCurrentFloorViewed(),
                motelManager.getCurrentRoomViewed()
        ).getRoomString();
        sellingView.setSellingToText("VENDIENDO A: " + roomString);
        userInterface.setSellingView();
        sellingView.setAddItemEnabled(false);
        sellingView.setItemDeleteEnabled(false);
        sellingView.setFinishSaleEnabled(false);
        sellingView.updateItemListed(motelManager.getInventoryItemDataList());
        sellingView.updateSellingListed(motelManager.getSellingItemDataList());
    }

    public void addItemToRegisterList() {
        InventoryItemData itemSelected = sellingView.getCurrentSelectedItemListed(
                sellingView.getSelectedItemRow());
        long quantity = InputParser.parseLongSafe(sellingView.getQuantityText(), 1L);
        if (quantity == 0L) {
            quantity = 1L;
            sellingView.setQuantityText("1");
        }
        long itemID = itemSelected.itemID();
        sellingView.setFinishSaleEnabled(true);
        motelManager.addItemToSelling(itemID, quantity, false);
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();
        sellingView.setTotalPriceText(String.valueOf(totalPrice));
        sellingView.updateSellingListed(motelManager.getSellingItemDataList());
        sellingView.clearItemTableSelection();
        sellingView.clearSellingTableSelection();
        sellingView.setAddItemEnabled(false);
    }

    public void removeItemFromRegisterList() {
        SellingItemData itemSelected = sellingView.getCurrentSelectedSellingListed(
                sellingView.getSelectedSellingRow());
        long itemID = itemSelected.itemID();
        motelManager.removeItemToSelling(itemID);
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();
        sellingView.setTotalPriceText(String.valueOf(totalPrice));
        sellingView.updateSellingListed(motelManager.getSellingItemDataList());
    }

    public void updateItemSaleAmount(int delta) {
        int newValue = (int) InputParser.parseLongSafe(sellingView.getQuantityText()) + delta;
        if (newValue < 0) newValue = 0;
        sellingView.setQuantityText(String.valueOf(newValue));
    }

    public void addCourtesyItemToRegister() {
        InventoryItemData itemSelected = sellingView.getCurrentSelectedItemListed(
                sellingView.getSelectedItemRow());
        long quantity = InputParser.parseLongSafe(sellingView.getQuantityText(), 1L);
        if (quantity == 0L) {
            quantity = 1L;
            sellingView.setQuantityText("1");
        }
        long itemID = itemSelected.itemID();
        sellingView.setFinishSaleEnabled(true);
        motelManager.addItemToSelling(itemID, quantity, true);
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();
        sellingView.setTotalPriceText(String.valueOf(totalPrice));
        sellingView.updateSellingListed(motelManager.getSellingItemDataList());
        sellingView.clearItemTableSelection();
        sellingView.clearSellingTableSelection();
        sellingView.setAddItemEnabled(false);
    }

    public void finishSale() {
        boolean print = sellingView.isPrintSelected();
        if (!print) {
            boolean noPrintingConfirmation = DialogHelper.confirmPrinting();
            if (noPrintingConfirmation) {
                motelManager.roomSaleFinished(false);
                userInterface.setFloorView();
                saveMainFiles.run();
                saveBackupFilesTransaction.run();
                attemptSaleEmail();
            }
        } else {
            motelManager.roomSaleFinished(true);
            userInterface.setFloorView();
            saveMainFiles.run();
            saveBackupFilesTransaction.run();
            attemptSaleEmail();
        }
    }

    private void attemptSaleEmail() {
        EmailConfigurationService emailSvc = motelManager.getEmailConfigurationService();
        if (!emailSvc.isEmailEnabled() || !emailSvc.validateCaseConfig(1)) return;
        ProgramConfig cfg = motelManager.getProgramConfig();
        Map<String, String> placeholders = Map.of(
                "{motelName}", cfg.getMotelName(),
                "{motelAddress}", cfg.getMotelAddress(),
                "{motelID}", cfg.getMotelID(),
                "{totalPrice}", String.valueOf(motelManager.getCurrentTotalPriceSellingList()),
                "{date}", java.time.LocalDate.now().toString());
        boolean sent = emailSvc.sendCaseEmail(1, placeholders, List.of());
        if (!sent) {
            DialogHelper.showInfoMessage(
                    "Error al enviar correo de venta", "CORREO");
        }
    }

    public void backFromSelling() {
        motelManager.restartSaleManager();
        userInterface.setFloorView();
    }

    // ========== Table Scrolling (Touch-Friendly) ==========

    private void scrollSelectedTable(int direction) {
        sellingView.scrollSelectedTable(direction);
    }
}
