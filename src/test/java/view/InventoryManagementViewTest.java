package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import model.dto.InventoryItemData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryManagementViewTest {

    private InventoryManagementView view;

    @BeforeEach
    void setUp() {
        view = new InventoryManagementView();
    }

    // --- callbacks ---

    /**
     * Verifies that clicking the backButton fires the callback registered via
     * {@link InventoryManagementView#onBackButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The back button action listener is not wired to the onBackButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onBackButton(() -> invoked.set(true));
        clickButton(view, "backButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the saveButton fires the callback registered via
     * {@link InventoryManagementView#onSaveButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The save button action listener is not wired to the onSaveButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenSaveButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onSaveButton(() -> invoked.set(true));
        clickButton(view, "saveButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the newitemButton fires the callback registered via
     * {@link InventoryManagementView#onNewItem(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The new-item button action listener is not wired to the onNewItem callback.
     */
    @Test
    void shouldInvokeCallbackWhenNewItemClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onNewItem(() -> invoked.set(true));
        clickButton(view, "newitemButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the deleteItemButton fires the callback registered via
     * {@link InventoryManagementView#onDeleteItem(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The delete-item button action listener is not wired to the onDeleteItem callback.
     */
    @Test
    void shouldInvokeCallbackWhenDeleteItemClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDeleteItem(() -> invoked.set(true));
        clickButton(view, "deleteItemButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the addQuantityButton fires the callback registered via
     * {@link InventoryManagementView#onAddQuantity(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The add-quantity button action listener is not wired to the onAddQuantity callback.
     */
    @Test
    void shouldInvokeCallbackWhenAddQuantityClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onAddQuantity(() -> invoked.set(true));
        clickButton(view, "addQuantityButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the removeQuantityButton fires the callback registered via
     * {@link InventoryManagementView#onRemoveQuantity(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The remove-quantity button action listener is not wired to the onRemoveQuantity callback.
     */
    @Test
    void shouldInvokeCallbackWhenRemoveQuantityClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onRemoveQuantity(() -> invoked.set(true));
        clickButton(view, "removeQuantityButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the upButton fires the callback registered via
     * {@link InventoryManagementView#onUpButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The up/navigate-up button action listener is not wired to the onUpButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenUpButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onUpButton(() -> invoked.set(true));
        clickButton(view, "upButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the downButton fires the callback registered via
     * {@link InventoryManagementView#onDownButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The down/navigate-down button action listener is not wired to the onDownButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenDownButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDownButton(() -> invoked.set(true));
        clickButton(view, "downButton");
        assertThat(invoked).isTrue();
    }

    // --- state ---

    /**
     * Verifies that the item name text field correctly roundtrips its value through
     * {@link InventoryManagementView#getNameText()} and {@link InventoryManagementView#setNameText(String)}.
     * Expected: getNameText() returns "Coca-Cola" after setNameText("Coca-Cola") is called.
     * Failure: The setter or getter is wired to the wrong text field component.
     */
    @Test
    void shouldRoundtripNameText() {
        view.setNameText("Coca-Cola");
        assertThat(view.getNameText()).isEqualTo("Coca-Cola");
    }

    /**
     * Verifies that the quantity text field correctly roundtrips its value through
     * {@link InventoryManagementView#getQuantityText()} and {@link InventoryManagementView#setQuantityText(String)}.
     * Expected: getQuantityText() returns "50" after setQuantityText("50") is called.
     * Failure: The setter or getter is wired to the wrong text field component.
     */
    @Test
    void shouldRoundtripQuantityText() {
        view.setQuantityText("50");
        assertThat(view.getQuantityText()).isEqualTo("50");
    }

    /**
     * Verifies that the price text field correctly roundtrips its value through
     * {@link InventoryManagementView#getPriceText()} and {@link InventoryManagementView#setPriceText(String)}.
     * Expected: getPriceText() returns "2500" after setPriceText("2500") is called.
     * Failure: The setter or getter is wired to the wrong text field component.
     */
    @Test
    void shouldRoundtripPriceText() {
        view.setPriceText("2500");
        assertThat(view.getPriceText()).isEqualTo("2500");
    }

    /**
     * Verifies that {@link InventoryManagementView#setEditInfoText(String)} correctly updates the
     * informativeEditLabel JLabel.
     * Expected: The informativeEditLabel displays "Editando: Coca-Cola" after setEditInfoText is called.
     * Failure: The setEditInfoText method is not wired to the correct label field (informativeEditLabel).
     */
    @Test
    void shouldSetEditInfoText() throws Exception {
        view.setEditInfoText("Editando: Coca-Cola");
        assertThat(textOf(view, "informativeEditLabel")).isEqualTo("Editando: Coca-Cola");
    }

    /**
     * Verifies that the button enabled-state setters ({@link InventoryManagementView#setDeleteEnabled(boolean)},
     * {@link InventoryManagementView#setAddQuantityEnabled(boolean)},
     * {@link InventoryManagementView#setRemoveQuantityEnabled(boolean)},
     * {@link InventoryManagementView#setSaveEnabled(boolean)}) complete without throwing exceptions.
     * Expected: All four setEnabled calls complete successfully with false.
     * Failure: Any setter throws an exception, indicating a missing or misnamed button field.
     */
    @Test
    void shouldSetEnabledStates() {
        view.setDeleteEnabled(false);
        view.setAddQuantityEnabled(false);
        view.setRemoveQuantityEnabled(false);
        view.setSaveEnabled(false);
    }

    /**
     * Verifies that {@link InventoryManagementView#adjustQuantity(int)} correctly increments the
     * quantity text field by the given amount.
     * Expected: After setQuantityText("10") and adjustQuantity(5), getQuantityText() returns "15".
     * Failure: The adjustQuantity method does not correctly parse and re-format the quantity value,
     *          or the underlying text field is not updated.
     */
    @Test
    void shouldAdjustQuantity() {
        view.setQuantityText("10");
        view.adjustQuantity(5);
        assertThat(view.getQuantityText()).isEqualTo("15");
    }

    /**
     * Verifies that {@link InventoryManagementView#adjustPrice(int)} correctly increments the
     * price text field by the given amount.
     * Expected: After setPriceText("1000") and adjustPrice(500), getPriceText() returns "1500".
     * Failure: The adjustPrice method does not correctly parse and re-format the price value,
     *          or the underlying text field is not updated.
     */
    @Test
    void shouldAdjustPrice() {
        view.setPriceText("1000");
        view.adjustPrice(500);
        assertThat(view.getPriceText()).isEqualTo("1500");
    }

    /**
     * Verifies that {@link InventoryManagementView#updateInventory(List)} populates the inventoryTable
     * with the provided inventory items.
     * Expected: After calling updateInventory with one InventoryItemData, the inventoryTable has exactly 1 row.
     * Failure: The inventory table model is not updated when updateInventory is called, or the table
     *          widget (inventoryTable) field name has changed.
     */
    @Test
    void shouldUpdateInventoryTable() {
        List<InventoryItemData> items = List.of(
                new InventoryItemData(1, "Coca-Cola", 2500, 50));
        view.updateInventory(items);
        JTable table = viewGetTable();
        assertThat(table.getRowCount()).isEqualTo(1);
    }

    // --- helpers ---

    private JTable viewGetTable() {
        try {
            Field field = view.getClass().getDeclaredField("inventoryTable");
            field.setAccessible(true);
            return (JTable) field.get(view);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void clickButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }

    private static Object getField(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(parent);
    }

    private static String textOf(Component view, String fieldName) throws Exception {
        JLabel label = (JLabel) getField(view, fieldName);
        return label.getText();
    }
}
