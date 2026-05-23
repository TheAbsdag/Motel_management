package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SellingViewTest {

    private SellingView view;

    @BeforeEach
    void setUp() {
        view = new SellingView();
    }

    // --- callbacks ---

    /**
     * Verifies that clicking the backButton fires the callback registered via
     * {@link SellingView#onBackButton(Runnable)}.
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
     * Verifies that clicking the itemDeleteButton fires the callback registered via
     * {@link SellingView#onItemDeleteButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The item-delete button action listener is not wired to the onItemDeleteButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenItemDeleteButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onItemDeleteButton(() -> invoked.set(true));
        clickButton(view, "itemDeleteButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the addItemButton fires the callback registered via
     * {@link SellingView#onAddItemButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The add-item button action listener is not wired to the onAddItemButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenAddItemButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onAddItemButton(() -> invoked.set(true));
        clickButton(view, "addItemButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the addQuantityButton fires the callback registered via
     * {@link SellingView#onAddQuantityButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The add-quantity button action listener is not wired to the onAddQuantityButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenAddQuantityButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onAddQuantityButton(() -> invoked.set(true));
        clickButton(view, "addQuantityButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the removeQuantityButton fires the callback registered via
     * {@link SellingView#onRemoveQuantityButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The remove-quantity button action listener is not wired to the onRemoveQuantityButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenRemoveQuantityButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onRemoveQuantityButton(() -> invoked.set(true));
        clickButton(view, "removeQuantityButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the finishSaleButton fires the callback registered via
     * {@link SellingView#onFinishSaleButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The finish-sale button action listener is not wired to the onFinishSaleButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenFinishSaleButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onFinishSaleButton(() -> invoked.set(true));
        clickButton(view, "finishSaleButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the upSellingListButton fires the callback registered via
     * {@link SellingView#onUpSellingListButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The up-selling-list button action listener is not wired to the onUpSellingListButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenUpSellingListButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onUpSellingListButton(() -> invoked.set(true));
        clickButton(view, "upSellingListButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the downSellingListButton fires the callback registered via
     * {@link SellingView#onDownSellingListButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The down-selling-list button action listener is not wired to the onDownSellingListButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenDownSellingListButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onDownSellingListButton(() -> invoked.set(true));
        clickButton(view, "downSellingListButton");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the courtesySaleButton fires the callback registered via
     * {@link SellingView#onCourtesySaleButton(Runnable)}.
     * Expected: The AtomicBoolean is set to true after the button click.
     * Failure: The courtesy-sale button action listener is not wired to the onCourtesySaleButton callback.
     */
    @Test
    void shouldInvokeCallbackWhenCourtesySaleButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onCourtesySaleButton(() -> invoked.set(true));
        clickButton(view, "courtesySaleButton");
        assertThat(invoked).isTrue();
    }

    // --- state ---

    /**
     * Verifies that the quantity text field correctly roundtrips its value through
     * {@link SellingView#getQuantityText()} and {@link SellingView#setQuantityText(String)}.
     * Expected: getQuantityText() returns the exact string previously set by setQuantityText().
     * Failure: The setter or getter is wired to the wrong text field component.
     */
    @Test
    void shouldRoundtripQuantityText() {
        view.setQuantityText("5");
        assertThat(view.getQuantityText()).isEqualTo("5");
    }

    /**
     * Verifies that {@link SellingView#isPrintSelected()} correctly reflects the selected state of
     * the printingCheckBox JCheckBox.
     * Expected: isPrintSelected() returns false initially, and true after the checkbox is selected.
     * Failure: The isPrintSelected() method reads from the wrong checkbox or does not properly
     *          reflect the component's selected state.
     */
    @Test
    void shouldReflectPrintCheckboxState() throws Exception {
        JCheckBox cb = (JCheckBox) getField(view, "printingCheckBox");
        assertThat(view.isPrintSelected()).isFalse();
        cb.setSelected(true);
        assertThat(view.isPrintSelected()).isTrue();
    }

    /**
     * Verifies that {@link SellingView#setTotalPriceText(String)} correctly updates the totalPriceLabel.
     * Expected: The totalPriceLabel displays "5,000" after setTotalPriceText("5,000") is called.
     * Failure: The setTotalPriceText method is not wired to the correct JLabel field (totalPriceLabel).
     */
    @Test
    void shouldSetTotalPriceText() throws Exception {
        view.setTotalPriceText("5,000");
        assertThat(textOf(view, "totalPriceLabel")).isEqualTo("5,000");
    }

    /**
     * Verifies that {@link SellingView#setSellingToText(String)} correctly updates the sellingToLabel.
     * Expected: The sellingToLabel displays "VENDIENDO A: T1-101" after setSellingToText is called.
     * Failure: The setSellingToText method is not wired to the correct JLabel field (sellingToLabel).
     */
    @Test
    void shouldSetSellingToText() throws Exception {
        view.setSellingToText("VENDIENDO A: T1-101");
        assertThat(textOf(view, "sellingToLabel")).isEqualTo("VENDIENDO A: T1-101");
    }

    /**
     * Verifies that {@link SellingView#updateItemListed(List)} populates the itemTable with the
     * provided inventory items.
     * Expected: After calling updateItemListed with one InventoryItemData, the itemTable has exactly 1 row.
     * Failure: The item table model is not updated when updateItemListed is called, or the table
     *          widget (itemTable) field name has changed.
     */
    @Test
    void shouldUpdateItemTable() {
        List<InventoryItemData> items = List.of(
                new InventoryItemData(1, "Coca-Cola", 2500, 50));
        view.updateItemListed(items);
        JTable table = viewGetItemTable();
        assertThat(table.getRowCount()).isEqualTo(1);
    }

    /**
     * Verifies that {@link SellingView#updateSellingListed(List)} populates the sellingTable with the
     * provided selling items.
     * Expected: After calling updateSellingListed with one SellingItemData, the sellingTable has exactly 1 row.
     * Failure: The selling table model is not updated when updateSellingListed is called, or the table
     *          widget (sellingTable) field name has changed.
     */
    @Test
    void shouldUpdateSellingTable() {
        List<SellingItemData> items = List.of(
                new SellingItemData(1, "Coca-Cola", 5, 12500, false));
        view.updateSellingListed(items);
        JTable table = viewGetSellingTable();
        assertThat(table.getRowCount()).isEqualTo(1);
    }

    // --- helpers ---

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

    private static String textOf(SellingView view, String fieldName) throws Exception {
        JLabel label = (JLabel) getField(view, fieldName);
        return label.getText();
    }

    private JTable viewGetItemTable() {
        try {
            return (JTable) getField(view, "itemTable");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JTable viewGetSellingTable() {
        try {
            return (JTable) getField(view, "sellingTable");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
