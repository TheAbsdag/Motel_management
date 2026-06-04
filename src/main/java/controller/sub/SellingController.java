package controller.sub;

import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.ProgramConfig;
import model.Room;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import model.json.CurrencyConfig;
import model.modelManagers.EmailConfigurationService;
import model.modelManagers.MotelManagement;
import view.SellingView;
import view.UserGUI;
import view.ViewCard;
import view.helpers.CurrencyFormatter;
import view.helpers.DialogHelper;
import view.helpers.FormatHelper;
import view.helpers.InputParser;
import view.helpers.TimeFormatter;

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
        sellingView.onUpSellingListButton(() -> sellingView.scrollSelectedTable(-1));
        sellingView.onDownSellingListButton(() -> sellingView.scrollSelectedTable(1));
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
        userInterface.setView(ViewCard.SELLING_VIEW);
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
        Room roomSoldTo = motelManager.getRoomForSale();
        String roomString = roomSoldTo != null ? roomSoldTo.getRoomString() : "";
        List<SellingItemData> items = motelManager.getSellingItemDataList();
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();

        boolean print = sellingView.isPrintSelected();
        if (!print) {
            boolean noPrintingConfirmation = DialogHelper.confirmPrinting();
            if (noPrintingConfirmation) {
                motelManager.roomSaleFinished(false);
                userInterface.setView(ViewCard.FLOOR_VIEW);
                saveMainFiles.run();
                saveBackupFilesTransaction.run();
                attemptSaleEmail(items, roomString, totalPrice);
            }
        } else {
            motelManager.roomSaleFinished(true);
            userInterface.setView(ViewCard.FLOOR_VIEW);
            saveMainFiles.run();
            saveBackupFilesTransaction.run();
            attemptSaleEmail(items, roomString, totalPrice);
        }
    }

    private void attemptSaleEmail(List<SellingItemData> items, String roomString, long totalPrice) {
        EmailConfigurationService emailSvc = motelManager.getEmailConfigurationService();
        if (!emailSvc.isEmailEnabled() || !emailSvc.validateCaseConfig(1)) return;
        ProgramConfig cfg = motelManager.getProgramConfig();
        int consecutive = motelManager.getTurnService().getConsecutiveTransaction();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        CurrencyConfig currency = cfg.getCurrencyConfig();
        String formattedTotal = CurrencyFormatter.format(totalPrice, currency);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{motelName}", cfg.getMotelName());
        placeholders.put("{motelAddress}", cfg.getMotelAddress());
        placeholders.put("{motelID}", cfg.getMotelID());
        placeholders.put("{totalPrice}", formattedTotal);
        placeholders.put("{roomString}", roomString);
        placeholders.put("{consecutiveTrans}", String.valueOf(consecutive));
        placeholders.put("{date}", TimeFormatter.formatEmailDatetime(now));
        placeholders.put("{hourService}", now.format(DateTimeFormatter.ofPattern("hh:mm a")));
        placeholders.put("{dateService}", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        placeholders.put("{register}", buildSaleRegisterHtml(items, currency, formattedTotal));
        List<Path> attachments = EmailController.resolveCaseAttachments(emailSvc, 1, consecutive);
        EmailController.sendEmailAsync(1, placeholders, attachments, emailSvc);
    }

    private static String buildSaleRegisterHtml(List<SellingItemData> items, CurrencyConfig currency, String totalPrice) {
        if (items == null || items.isEmpty()) return "<p>Sin artículos</p>";
        StringBuilder table = new StringBuilder();
        table.append("<table border='1' cellpadding='4' cellspacing='0' style='border-collapse:collapse;font-family:Segoe UI,sans-serif;font-size:12px;'>");
        table.append("<thead><tr style='background:#f0f0f0;'><th>Cant</th><th>Artículo</th><th>Precio</th></tr></thead><tbody>");
        for (SellingItemData item : items) {
            table.append("<tr>");
            table.append("<td>").append(item.quantity()).append("</td>");
            table.append("<td>").append(FormatHelper.escapeHtml(item.itemName())).append("</td>");
            table.append("<td>").append(CurrencyFormatter.format(item.price(), currency)).append("</td>");
            table.append("</tr>");
        }
        table.append("</tbody></table>");
        table.append("<p><strong>Total: ").append(totalPrice).append("</strong></p>");
        return table.toString();
    }

    
    public void backFromSelling() {
        motelManager.restartSaleManager();
        userInterface.setView(ViewCard.FLOOR_VIEW);
    }

}
