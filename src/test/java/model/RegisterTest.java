package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import model.json.ObjectMapperFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterTest {

    private Register register;

    @BeforeEach
    void setUp() {
        register = new Register();
    }

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
        register.createItem("Item B", 2000, 20, 0);
        assertThat(register.getItemFromItemID(0).getName()).isEqualTo("Item A");
        assertThat(register.getItemFromItemID(1).getName()).isEqualTo("Item B");
    }

    @Test
    void shouldReturnNullForNonExistentItem() {
        assertThat(register.getItemFromItemID(999)).isNull();
    }

    @Test
    void shouldDeleteItemById() {
        register.createNewItem("ToDelete", 500, 5);
        register.deleteItemById(0);
        assertThat(register.getItemFromItemID(0)).isNull();
    }

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

    @Test
    void shouldAddItemToSellingList() {
        register.createNewItem("Coca-Cola", 2500, 50);
        register.addItemToList(register.getItemFromItemID(0), 5);
        List<model.dto.SellingItemData> sellingList = register.getSellingItemDataList();
        assertThat(sellingList).hasSize(1);
        assertThat(sellingList.get(0).itemName()).isEqualTo("Coca-Cola");
        assertThat(sellingList.get(0).quantity()).isEqualTo(5);
        assertThat(sellingList.get(0).price()).isEqualTo(12500);
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
        assertThat(sellingList.get(0).price()).isEqualTo(12500);
    }

    @Test
    void shouldPreserveItemOrderWhenUpdatingQuantity() {
        register.createNewItem("Item A", 1000, 10);
        register.createNewItem("Item B", 2000, 10);
        register.addItemToList(register.getItemFromItemID(0), 1);
        register.addItemToList(register.getItemFromItemID(1), 1);
        register.addItemToList(register.getItemFromItemID(0), 1);
        List<model.dto.SellingItemData> sellingList = register.getSellingItemDataList();
        assertThat(sellingList).hasSize(2);
        assertThat(sellingList.get(0).itemName()).isEqualTo("Item A");
        assertThat(sellingList.get(1).itemName()).isEqualTo("Item B");
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
        register.addItemToList(register.getItemFromItemID(0), 2);
        register.addItemToList(register.getItemFromItemID(1), 4);
        assertThat(register.getTotalPriceRegisterList()).isEqualTo(4000);
    }

    @Test
    void shouldClearSellingList() {
        register.createNewItem("Item A", 1000, 10);
        register.addItemToList(register.getItemFromItemID(0), 5);
        register.newSellingList();
        assertThat(register.getSellingItemDataList()).isEmpty();
    }

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

    @Test
    void shouldGenerateInventoryJsonData() throws JsonProcessingException {
        register.createNewItem("Test", 500, 3);
        Register.InventoryData data = register.getInventoryData();
        assertThat(data.inventoryItems()).hasSize(1);
    }
}
