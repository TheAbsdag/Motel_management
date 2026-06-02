package view;

import view.helpers.NumericDocumentFilter;
import view.helpers.FocusHighlighter;
import view.helpers.PriceAdjustmentHelper;
import view.helpers.TextPromptHelper;
import view.helpers.TableScroller;
import view.helpers.TouchScrollHandler;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.*;
import model.dto.InventoryItemData;
import model.json.CurrencyConfig;
import view.customListRenderes.*;
import view.interfaces.TimeLabelInterface;

/**
 * Inventory management view — displays and edits inventory items.
 * Uses typed {@link InventoryItemData} DTOs instead of raw JSON.
 */
public class InventoryManagementView extends JPanel implements TimeLabelInterface {

    private CurrencyConfig currencyConfig = CurrencyConfig.defaultConfig();

    public void setCurrencyConfig(CurrencyConfig cfg) {
        this.currencyConfig = cfg != null ? cfg : CurrencyConfig.defaultConfig();
    }

    public InventoryManagementView() {
        this.cellFont = new Font("Segoe UI", Font.BOLD, 18);
        initComponents();
        initCustomTable();
        FocusHighlighter.applyToAll(this);
        TextPromptHelper.install(nameTextField, "Ingrese el nombre del producto");
    }

    private JTable inventoryTable;
    private InventoryTableModel tableModel;
    private final Font cellFont;

    /**
     * Updates the inventory table with a typed list of DTOs.
     */
    public void updateInventory(List<InventoryItemData> items) {
        tableModel.updateData(items);
        inventoryTable.repaint();
    }

    private void initCustomTable() {
        tableModel = new InventoryTableModel();
        inventoryTable = new JTable(tableModel);
        TableColumnModel columnModel = inventoryTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(new CustomCellRenderer(cellFont));
            columnModel.getColumn(i).setHeaderRenderer(new CustomHeaderRenderer(cellFont));
        }
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        inventoryPanel.add(scrollPane, "cell 0 0, grow");
        TouchScrollHandler.attach(scrollPane);
    }

    /**
     * Returns the currently selected item as a typed DTO.
     */
    public InventoryItemData getCurrentSelectedItem(int rowSelected) {
        return tableModel.items.get(rowSelected);
    }

    private class InventoryTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Nombre", "Cantidad", "Precio"};
        private List<InventoryItemData> items;

        InventoryTableModel() {
            this.items = new ArrayList<>();
        }

        void updateData(List<InventoryItemData> data) {
            this.items = data;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return items.size(); }

        @Override
        public int getColumnCount() { return columnNames.length; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            InventoryItemData item = items.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.name();
                case 1 -> item.quantity();
                case 2 -> item.price();
                default -> null;
            };
        }

        @Override
        public String getColumnName(int column) { return columnNames[column]; }
    }

    // ===== JFormDesigner generated code (unchanged) =====

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        deleteItemButton = new JButton();
        newitemButton = new JButton();
        informativeEditLabel = new JLabel();
        inventoryPanel = new JPanel();
        nameLabel = new JLabel();
        nameTextField = new JTextField();
        quantityLabel = new JLabel();
        quantityTextField = new JTextField();
         ((AbstractDocument) quantityTextField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        addQuantityButton = new JButton();
        removeQuantityButton = new JButton();
        priceLabel = new JLabel();
        priceTextField = new JTextField();
         ((AbstractDocument) priceTextField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        upButton = new JButton();
        removeSmallPriceButton = new JButton();
        addSmallPriceButton = new JButton();
        downButton = new JButton();
        removeBigPriceButton = new JButton();
        addBigPriceButton = new JButton();
        backButton = new JButton();
        timeLabel = new JLabel();
        dateLabel = new JLabel();
        saveButton = new JButton();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            "[grow,fill][grow,fill][grow,fill][grow,fill][grow,fill][grow,fill][grow,fill][146,grow,fill][112,grow,fill]",
            "[grow][grow][grow][grow][grow][grow][grow]"));

        //---- deleteItemButton ----
        deleteItemButton.setText("BORRAR");
        deleteItemButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        add(deleteItemButton, "cell 0 0,growy");

        //---- newitemButton ----
        newitemButton.setText("NUEVO");
        newitemButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        add(newitemButton, "cell 4 0,growy");

        //---- informativeEditLabel ----
        informativeEditLabel.setText(" ");
        informativeEditLabel.setHorizontalAlignment(SwingConstants.CENTER);
        informativeEditLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        add(informativeEditLabel, "cell 5 0 4 1,grow");

        //======== inventoryPanel ========
        {
            inventoryPanel.setBorder(new LineBorder(Color.darkGray, 4));
            inventoryPanel.setLayout(new MigLayout("fill,hidemode 3", "[grow,shrink 0,fill]", "[grow,shrink 0]"));
        }
        add(inventoryPanel, "cell 0 1 5 5,grow");

        //---- nameLabel ----
        nameLabel.setText("Nombre");
        nameLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        add(nameLabel, "cell 5 1");

        //---- nameTextField ----
        nameTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(nameTextField, "cell 6 1 3 1,growy");

        //---- quantityLabel ----
        quantityLabel.setText("Cantidad:");
        quantityLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        add(quantityLabel, "cell 5 2");

        //---- quantityTextField ----
        quantityTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        add(quantityTextField, "cell 6 2,growy");

        //---- addQuantityButton ----
        addQuantityButton.setText("+");
        addQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        add(addQuantityButton, "cell 7 2,growy");

        //---- removeQuantityButton ----
        removeQuantityButton.setText("-");
        removeQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        add(removeQuantityButton, "cell 8 2,growy");

        //---- priceLabel ----
        priceLabel.setText("Precio");
        priceLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        add(priceLabel, "cell 5 3");

        //---- priceTextField ----
        priceTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
        add(priceTextField, "cell 6 3 2 1,growy");

        //---- upButton ----
        upButton.setIcon(new ImageIcon(getClass().getResource("/images/up.png")));
        add(upButton, "cell 5 4,growy");

        //---- removeSmallPriceButton ----
        removeSmallPriceButton.setText("-100");
        removeSmallPriceButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
        add(removeSmallPriceButton, "cell 6 4,growy");

        //---- addSmallPriceButton ----
        addSmallPriceButton.setText("+100");
        addSmallPriceButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
        add(addSmallPriceButton, "cell 7 4,growy");

        //---- downButton ----
        downButton.setIcon(new ImageIcon(getClass().getResource("/images/down.png")));
        add(downButton, "cell 5 5,growy");

        //---- removeBigPriceButton ----
        removeBigPriceButton.setText("-1000");
        removeBigPriceButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
        add(removeBigPriceButton, "cell 6 5,growy");

        //---- addBigPriceButton ----
        addBigPriceButton.setText("+1000");
        addBigPriceButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
        add(addBigPriceButton, "cell 7 5,growy");

        //---- backButton ----
        backButton.setText("VOLVER");
        backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        add(backButton, "cell 0 6,growy");

        //---- timeLabel ----
        timeLabel.setText("text");
        timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(timeLabel, "cell 2 6 3 1,growy");

        //---- dateLabel ----
        dateLabel.setText("text");
        dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(dateLabel, "cell 5 6 3 1");

        //---- saveButton ----
        saveButton.setText("GUARDAR");
        saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
        add(saveButton, "cell 8 6,growy");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JButton deleteItemButton;
    private JButton newitemButton;
    private JLabel informativeEditLabel;
    private JPanel inventoryPanel;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JLabel quantityLabel;
    private JTextField quantityTextField;
    private JButton addQuantityButton;
    private JButton removeQuantityButton;
    private JLabel priceLabel;
    private JTextField priceTextField;
    private JButton upButton;
    private JButton removeSmallPriceButton;
    private JButton addSmallPriceButton;
    private JButton downButton;
    private JButton removeBigPriceButton;
    private JButton addBigPriceButton;
    private JButton backButton;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    // ========== Encapsulated API ==========

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }

    // -- Button listeners --
    public void onNewItem(Runnable action) { newitemButton.addActionListener(e -> action.run()); }
    public void onDeleteItem(Runnable action) { deleteItemButton.addActionListener(e -> action.run()); }
    public void onAddQuantity(Runnable action) { addQuantityButton.addActionListener(e -> action.run()); }
    public void onRemoveQuantity(Runnable action) { removeQuantityButton.addActionListener(e -> action.run()); }
    public void onRemoveSmallPrice(Runnable action) { removeSmallPriceButton.addActionListener(e -> action.run()); }
    public void onAddSmallPrice(Runnable action) { addSmallPriceButton.addActionListener(e -> action.run()); }
    public void onRemoveBigPrice(Runnable action) { removeBigPriceButton.addActionListener(e -> action.run()); }
    public void onAddBigPrice(Runnable action) { addBigPriceButton.addActionListener(e -> action.run()); }
    public void onSaveButton(Runnable action) { saveButton.addActionListener(e -> action.run()); }
    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }
    public void onUpButton(Runnable action) { upButton.addActionListener(e -> action.run()); }
    public void onDownButton(Runnable action) { downButton.addActionListener(e -> action.run()); }

    // -- Button enable/disable --
    public void setDeleteEnabled(boolean e) { deleteItemButton.setEnabled(e); }
    public void setAddQuantityEnabled(boolean e) { addQuantityButton.setEnabled(e); }
    public void setRemoveQuantityEnabled(boolean e) { removeQuantityButton.setEnabled(e); }
    public void setRemoveSmallPriceEnabled(boolean e) { removeSmallPriceButton.setEnabled(e); }
    public void setAddSmallPriceEnabled(boolean e) { addSmallPriceButton.setEnabled(e); }
    public void setRemoveBigPriceEnabled(boolean e) { removeBigPriceButton.setEnabled(e); }
    public void setAddBigPriceEnabled(boolean e) { addBigPriceButton.setEnabled(e); }
    public void setSaveEnabled(boolean e) { saveButton.setEnabled(e); }

    // -- Text fields --
    public String getNameText() { return nameTextField.getText(); }
    public void setNameText(String text) { nameTextField.setText(text); }
    public void setNameEnabled(boolean e) { nameTextField.setEnabled(e); }
    public boolean isNameEnabled() { return nameTextField.isEnabled(); }
    public void onNameTextChanged(javax.swing.event.DocumentListener listener) {
        nameTextField.getDocument().addDocumentListener(listener);
    }
    public String getQuantityText() { return quantityTextField.getText(); }
    public void setQuantityText(String text) { quantityTextField.setText(text); }
    public void setQuantityEnabled(boolean e) { quantityTextField.setEnabled(e); }
    public String getPriceText() { return priceTextField.getText(); }
    public void setPriceText(String text) { priceTextField.setText(text); }
    public void setPriceEnabled(boolean e) { priceTextField.setEnabled(e); }

    // -- Labels --
    public void setEditInfoText(String text) { informativeEditLabel.setText(text); }

    // -- Table --
    public int getSelectedInventoryRow() { return inventoryTable.getSelectedRow(); }
    public void clearInventorySelection() { inventoryTable.clearSelection(); }
    public void onInventorySelection(javax.swing.event.ListSelectionListener listener) {
        inventoryTable.getSelectionModel().addListSelectionListener(listener);
    }

    // -- Table scrolling --

    public void scrollInventoryTable(int direction) {
        TableScroller.scroll(inventoryTable, direction);
    }

    // -- Quantity & price adjustments --

    public void adjustQuantity(long delta) {
        PriceAdjustmentHelper.adjust(quantityTextField, delta);
    }

    public void adjustPrice(long delta) {
        PriceAdjustmentHelper.adjust(priceTextField, delta);
    }
}
