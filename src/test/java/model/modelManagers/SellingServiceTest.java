package model.modelManagers;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import model.Item;
import model.Register;
import model.dto.InventoryItemData;
import model.turn.InventoryMovement;
import model.turn.InventorySnapshotItem;
import model.turn.MovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SellingServiceTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final ZonedDateTime NOW = ZonedDateTime.of(2026, 11, 17, 20, 30, 0, 0, BOGOTA);

    private Register register;
    private SellingService service;

    @BeforeEach
    void setUp() {
        register = new Register();
        service = new SellingService(register);
        register.createNewItem("Cerveza", 8000, 4, 2);
    }

    @Test
    void shouldBuildAnIncreaseMovementWithCost() {
        InventoryMovement movement = service.applyInventoryChange(
                new InventoryItemData(0, "Cerveza", 8000, 10, 2), "COMPRA", 30_000L, NOW);

        assertThat(movement).isNotNull();
        assertThat(movement.movementType()).isEqualTo(MovementType.INCREASE);
        assertThat(movement.quantity()).isEqualTo(6);
        assertThat(movement.cost()).isEqualTo(30_000L);
        assertThat(movement.concept()).isEqualTo("COMPRA");
        assertThat(movement.itemName()).isEqualTo("Cerveza");
        assertThat(register.getItemFromItemID(0).getQuantity()).isEqualTo(10);
    }

    @Test
    void shouldBuildConsumptionWithZeroCost() {
        InventoryMovement movement = service.applyInventoryChange(
                new InventoryItemData(0, "Cerveza", 8000, 2, 2), "MERMA", 99_000L, NOW);

        assertThat(movement).isNotNull();
        assertThat(movement.movementType()).isEqualTo(MovementType.CONSUMPTION);
        assertThat(movement.quantity()).isEqualTo(2);
        assertThat(movement.cost()).isZero();
    }

    @Test
    void shouldReturnNullWhenQuantityDoesNotChange() {
        assertThat(service.applyInventoryChange(
                new InventoryItemData(0, "Cerveza", 9000, 4, 5), null, 0L, NOW)).isNull();
        assertThat(register.getItemFromItemID(0).getPrice()).isEqualTo(9000);
        assertThat(register.getItemFromItemID(0).getMinimum()).isEqualTo(5);
    }

    @Test
    void shouldRejectAQuantityChangeWithoutConcept() {
        assertThatThrownBy(() -> service.applyInventoryChange(
                new InventoryItemData(0, "Cerveza", 8000, 10, 2), "  ", 0L, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldSnapshotTheInventoryWithMinimums() {
        List<InventorySnapshotItem> snapshot = service.inventorySnapshot();

        assertThat(snapshot).containsExactly(new InventorySnapshotItem(0, "Cerveza", 4, 2));
    }

    @Test
    void shouldCreateAnItemWithItsCreationMovement() {
        InventoryMovement movement = service.newItemCreated("Ron", 20_000, 5, 1, NOW);

        assertThat(movement).isNotNull();
        assertThat(movement.movementType()).isEqualTo(MovementType.INCREASE);
        assertThat(movement.quantity()).isEqualTo(5);
        assertThat(movement.concept()).isEqualTo("CREACIÓN DE PRODUCTO");
        assertThat(register.getItemFromItemID(1).getMinimum()).isEqualTo(1);
    }

    @Test
    void shouldRecordAWriteOffWhenDeletingWithStock() {
        InventoryMovement movement = service.deleteItemWithMovement(0, NOW);

        assertThat(movement).isNotNull();
        assertThat(movement.movementType()).isEqualTo(MovementType.CONSUMPTION);
        assertThat(movement.quantity()).isEqualTo(4);
        assertThat(movement.concept()).isEqualTo("BAJA DE PRODUCTO");
        assertThat(register.getItemFromItemID(0)).isNull();
    }

    @Test
    void shouldNotRecordAnythingWhenDeletingAnUnknownItem() {
        assertThat(service.deleteItemWithMovement(99, NOW)).isNull();
    }

    @Test
    void shouldKeepTheMinimumWhenSavingAnItem() {
        Item item = register.getItemFromItemID(0);
        assertThat(service.saveItemInformation(new InventoryItemData(0, item.getName(), 9000, 12, 3))).isTrue();

        assertThat(register.getItemFromItemID(0).getPrice()).isEqualTo(9000);
        assertThat(register.getItemFromItemID(0).getQuantity()).isEqualTo(12);
        assertThat(register.getItemFromItemID(0).getMinimum()).isEqualTo(3);
    }
}
