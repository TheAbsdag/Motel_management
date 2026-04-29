package model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Item} — inventory item data object.
 */
class ItemTest {

    @Test
    void shouldCreateItemWithGivenValues() {
        Item item = new Item("Coca-Cola", 2500, 50, 1);

        assertThat(item.getName()).isEqualTo("Coca-Cola");
        assertThat(item.getPrice()).isEqualTo(2500);
        assertThat(item.getQuantity()).isEqualTo(50);
        assertThat(item.getItemID()).isEqualTo(1);
    }

    @Test
    void shouldDecreaseQuantityWhenSold() {
        Item item = new Item("Agua", 1000, 30, 2);

        item.itemSold(10);

        assertThat(item.getQuantity()).isEqualTo(20);
    }

    @Test
    void shouldIncreaseQuantityWhenRestocked() {
        Item item = new Item("Agua", 1000, 30, 2);

        item.itemAdded(15);

        assertThat(item.getQuantity()).isEqualTo(45);
    }

    @Test
    void shouldHandleMultipleSales() {
        Item item = new Item("Cerveza", 3500, 100, 3);

        item.itemSold(20);
        item.itemSold(30);

        assertThat(item.getQuantity()).isEqualTo(50);
    }

    @Test
    void shouldHandleMultipleRestocks() {
        Item item = new Item("Cerveza", 3500, 10, 3);

        item.itemAdded(5);
        item.itemAdded(5);

        assertThat(item.getQuantity()).isEqualTo(20);
    }
}
