package view;

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
import view.customListRenderes.*;

/**
 * Inventory management view — displays and edits inventory items.
 * Uses typed {@link InventoryItemData} DTOs instead of raw JSON.
 */
public class InventoryManagementView extends JPanel {

    public JButton getUpButton() { return upButton; }
    public JButton getDownButton() { return downButton; }
    public JTable getInventoryTable() { return inventoryTable; }

    public InventoryManagementView() {
        this.cellFont = new Font("Segoe UI", Font.BOLD, 18);
        initComponents();
        initCustomTable();
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
        upButton.setIcon(new ImageIcon(getClass().getResource("/up.png")));
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
        downButton.setIcon(new ImageIcon(getClass().getResource("/down.png")));
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

    // Getters for JFormDesigner components
    public JButton getDeleteItemButton() { return deleteItemButton; }
    public JButton getNewitemButton() { return newitemButton; }
    public JLabel getInformativeEditLabel() { return informativeEditLabel; }
    public JTextField getNameTextField() { return nameTextField; }
    public JTextField getQuantityTextField() { return quantityTextField; }
    public JButton getAddQuantityButton() { return addQuantityButton; }
    public JButton getRemoveQuantityButton() { return removeQuantityButton; }
    public JTextField getPriceTextField() { return priceTextField; }
    public JButton getRemoveSmallPriceButton() { return removeSmallPriceButton; }
    public JButton getAddSmallPriceButton() { return addSmallPriceButton; }
    public JButton getRemoveBigPriceButton() { return removeBigPriceButton; }
    public JButton getAddBigPriceButton() { return addBigPriceButton; }
    public JButton getBackButton() { return backButton; }
    public JLabel getTimeLabel() { return timeLabel; }
    public JLabel getDateLabel() { return dateLabel; }
    public JButton getSaveButton() { return saveButton; }
}
