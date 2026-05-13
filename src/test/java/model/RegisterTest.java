package model;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Register} — inventory CRUD and selling cart management.
 */
class RegisterTest {

    private Register register;

    @BeforeEach
    void setUp() {
        register = new Register();
    }

    // ========== Inventory Item Creation ==========

    @Test
    void shouldCreateItemAndBeRetrievable() {
        register.createNewItem("Coca-Cola", 2500, 50);

        Item item = register.getItemFromItemID(0);
        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("Coca-Cola");
        assertThat(item.getPrice()).isEqualTo(2500);
        assertThat(item.getQuantity()).isEqualTo(50);
        assertThat(item.getItemID()).isZero();
    }

    @Test
    void shouldAutoGenerateUniqueIdWhenCreatingItems() {
        register.createNewItem("Item A", 1000, 10);
        register.createNewItem("Item B", 2000, 20);
        register.createNewItem("Item C", 3000, 30);

        assertThat(register.getItemFromItemID(0).getName()).isEqualTo("Item A");
        assertThat(register.getItemFromItemID(1).getName()).isEqualTo("Item B");
        assertThat(register.getItemFromItemID(2).getName()).isEqualTo("Item C");
    }

    @Test
    void shouldSkipExistingIdWhenCreating() {
        register.createNewItem("Item A", 1000, 10);
        // createItem with specific ID that already exists should get next available
        register.createItem("Item B", 2000, 20, 0);

        assertThat(register.getItemFromItemID(0).getName()).isEqualTo("Item A");
        assertThat(register.getItemFromItemID(1).getName()).isEqualTo("Item B");
    }

    @Test
    void shouldReturnNullForNonExistentItem() {
        assertThat(register.getItemFromItemID(999)).isNull();
    }

    // ========== Inventory Item Deletion ==========

    @Test
    void shouldDeleteItemById() {
        register.createNewItem("ToDelete", 500, 5);

        register.deleteItemById(0);

        assertThat(register.getItemFromItemID(0)).isNull();
    }

    // ========== Inventory Item Update ==========

    @Test
    void shouldUpdateExistingItem() {
        register.createNewItem("Original", 100, 10);
        Item updated = new Item("Updated", 200, 20, 0);

        register.saveItemInformation(updated);

        Item retrieved = register.getItemFromItemID(0);
        assertThat(retrieved.getName()).isEqualTo("Updated");
        assertThat(retrieved.getPrice()).isEqualTo(200);
        assertThat(retrieved.getQuantity()).isEqualTo(20);
    }

    // ========== Selling List ==========

    @Test
    void shouldAddItemToSellingList() {
        register.createNewItem("Coca-Cola", 2500, 50);

        register.addItemToList(register.getItemFromItemID(0), 5);

        List<model.dto.SellingItemData> sellingList = register.getSellingItemDataList();
        assertThat(sellingList).hasSize(1);
        assertThat(sellingList.get(0).itemName()).isEqualTo("Coca-Cola");
        assertThat(sellingList.get(0).quantity()).isEqualTo(5);
        assertThat(sellingList.get(0).price()).isEqualTo(12500); // 5 * 2500
        assertThat(sellingList.get(0).isCourtesy()).isFalse();
    }

    @Test
    void shouldAccumulateQuantityWhenAddingSameItemTwice() {
        register.createNewItem("Coca-Cola", 2500, 50);

        register.addItemToList(register.getItemFromItemID(0), 3);
        register.addItemToList(register.getItemFromItemID(0), 2);

        List<model.dto.SellingItemData> sellingList = register.getSellingItemDataList();
        assertThat(sellingList).hasSize(1);
        assertThat(sellingList.get(0).quantity()).isEqualTo(5);
        assertThat(sellingList.get(0).price()).isEqualTo(12500); // 5 * 2500
    }

    @Test
    void shouldPreserveItemOrderWhenUpdatingQuantity() {
        register.createNewItem("Item A", 1000, 10);
        register.createNewItem("Item B", 2000, 10);

        register.addItemToList(register.getItemFromItemID(0), 1);
        register.addItemToList(register.getItemFromItemID(1), 1);
        register.addItemToList(register.getItemFromItemID(0), 1); // update Item A in place

        List<model.dto.SellingItemData> sellingList = register.getSellingItemDataList();
        assertThat(sellingList).hasSize(2);
        assertThat(sellingList.get(0).itemName()).isEqualTo("Item A"); // stays at position 0
        assertThat(sellingList.get(1).itemName()).isEqualTo("Item B"); // stays at position 1
    }

    @Test
    void shouldAddCourtesyItemWithZeroPrice() {
        register.createNewItem("Cortesia", 5000, 100);

        register.addCourtesyItemToList(register.getItemFromItemID(0), 2);

        List<model.dto.SellingItemData> sellingList = register.getSellingItemDataList();
        assertThat(sellingList).hasSize(1);
        assertThat(sellingList.get(0).isCourtesy()).isTrue();
        assertThat(sellingList.get(0).price()).isZero();
        assertThat(sellingList.get(0).quantity()).isEqualTo(2);
    }

    @Test
    void shouldRemoveItemFromSellingList() {
        register.createNewItem("Coca-Cola", 2500, 50);
        register.addItemToList(register.getItemFromItemID(0), 5);

        register.removeFromList(register.getItemFromItemID(0));

        assertThat(register.getSellingItemDataList()).isEmpty();
    }

    @Test
    void shouldCalculateTotalPrice() {
        register.createNewItem("Item A", 1000, 10);
        register.createNewItem("Item B", 500, 10);

        register.addItemToList(register.getItemFromItemID(0), 2);  // 2000
        register.addItemToList(register.getItemFromItemID(1), 4);  // 2000

        assertThat(register.getTotalPriceRegisterList()).isEqualTo(4000);
    }

    @Test
    void shouldClearSellingList() {
        register.createNewItem("Item A", 1000, 10);
        register.addItemToList(register.getItemFromItemID(0), 5);

        register.newSellingList();

        assertThat(register.getSellingItemDataList()).isEmpty();
    }

    // ========== DTO Conversion ==========

    @Test
    void shouldConvertInventoryToDtoList() {
        register.createNewItem("Item A", 1000, 10);
        register.createNewItem("Item B", 2000, 5);

        List<model.dto.InventoryItemData> items = register.getInventoryItemDataList();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).name()).isEqualTo("Item A");
        assertThat(items.get(0).price()).isEqualTo(1000);
        assertThat(items.get(0).quantity()).isEqualTo(10);
    }

    // ========== Inventory Persistence Data ==========

    @Test
    void shouldGenerateInventoryJsonData() {
        register.createNewItem("Test", 500, 3);

        JSONObject data = register.getInventoryData();

        assertThat(data.has("inventoryItems")).isTrue();
        assertThat(data.getJSONArray("inventoryItems")).hasSize(1);
    }
}
