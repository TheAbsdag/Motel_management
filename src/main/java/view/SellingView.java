/*
 * Created by JFormDesigner on Fri Jun 07 00:47:34 COT 2024
 */
package view;

import view.helpers.NumericDocumentFilter;
import view.helpers.FocusHighlighter;
import view.helpers.TableScroller;
import view.helpers.TouchScrollHandler;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.*;
import model.dto.InventoryItemData;
import model.dto.SellingItemData;
import model.json.CurrencyConfig;
import view.customListRenderes.*;
import view.interfaces.TimeLabelInterface;

/**
 * @author Santiago
 */
public class SellingView extends JPanel implements TimeLabelInterface {

    private JTable itemTable;
    private JTable sellingTable;

    private ItemTableModel itemTableModel;
    private SellingTableModel sellingTableModel;
    private final Font cellFont;

    private CurrencyConfig currencyConfig = CurrencyConfig.defaultConfig();

    public void setCurrencyConfig(CurrencyConfig cfg) {
        this.currencyConfig = cfg != null ? cfg : CurrencyConfig.defaultConfig();
    }

    public SellingView() {
        this.cellFont = new Font("Segoe UI", Font.BOLD, 18);
        initComponents();
        initCustomTable();
        FocusHighlighter.applyToAll(this);
    }

    private void initCustomTable() {
        itemTableModel = new ItemTableModel();
        itemTable = new JTable(itemTableModel);
        TableColumnModel columnModel = itemTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(new CustomCellRenderer(cellFont));
            columnModel.getColumn(i).setHeaderRenderer(new CustomHeaderRenderer(cellFont));
        }
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane itemScrollPane = new JScrollPane(itemTable);
        itemTable.getTableHeader().setReorderingAllowed(false);
        itemListPanel.add(itemScrollPane, "cell 0 0, grow");
        TouchScrollHandler.attach(itemScrollPane);

        sellingTableModel = new SellingTableModel();
        sellingTable = new JTable(sellingTableModel);
        TableColumnModel columnModelSell = sellingTable.getColumnModel();
        for (int i = 0; i < columnModelSell.getColumnCount(); i++) {
            columnModelSell.getColumn(i).setCellRenderer(new CustomCellRenderer(cellFont));
            columnModelSell.getColumn(i).setHeaderRenderer(new CustomHeaderRenderer(cellFont));
        }
        sellingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sellingScrollPane = new JScrollPane(sellingTable);
        sellingTable.getTableHeader().setReorderingAllowed(false);
        registerListPanel.add(sellingScrollPane, "cell 0 0, grow");
        TouchScrollHandler.attach(sellingScrollPane);
    }

    public InventoryItemData getCurrentSelectedItemListed(int rowSelected) {
        return itemTableModel.inventoryItems.get(rowSelected);
    }

    public SellingItemData getCurrentSelectedSellingListed(int rowSelected) {
        return sellingTableModel.sellingItems.get(rowSelected);
    }

    public void updateItemListed(List<InventoryItemData> inventoryItems) {
        itemTableModel.updateData(inventoryItems);
        itemTable.repaint();
    }

    public void updateSellingListed(List<SellingItemData> sellingList) {
        sellingTableModel.updateData(sellingList);
        sellingTable.repaint();
    }

    //Custom tablemodel class for showing registerlist
    private class SellingTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Nombre", "Cantidad", "Precio"};
        private List<SellingItemData> sellingItems;

        public SellingTableModel() {
            this.sellingItems = new ArrayList<>();
        }

        public void updateData(List<SellingItemData> data) {
            sellingItems = data;
            sellingTable.repaint();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return sellingItems.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SellingItemData item = sellingItems.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.itemName();
                case 1 -> item.quantity();
                case 2 -> item.price();
                default -> null;
            };
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> String.class;
                case 1 -> Long.class;
                case 2 -> Long.class;
                default -> super.getColumnClass(columnIndex);
            };
        }
    }

    //Custom tableModel class for showing items
    private class ItemTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Nombre", "Precio"};
        private List<InventoryItemData> inventoryItems;

        public ItemTableModel() {
            this.inventoryItems = new ArrayList<>();
        }

        public void updateData(List<InventoryItemData> data) {
            inventoryItems = data;
            itemTable.repaint();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return inventoryItems.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            InventoryItemData item = inventoryItems.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.name();
                case 1 -> item.price();
                default -> null;
            };
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> String.class;
                case 1 -> Long.class;
                default -> super.getColumnClass(columnIndex);
            };
        }
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	dateLabel = new JLabel();
	upSellingListButton = new JButton();
	downSellingListButton = new JButton();
	courtesySaleButton = new JButton();
	timeLabel = new JLabel();
	itemListPanel = new JPanel();
	registerListPanel = new JPanel();
	quantityTextField = new JTextField();
	 ((AbstractDocument) quantityTextField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
	addQuantityButton = new JButton();
	removeQuantityButton = new JButton();
	totalPriceInformativeLabel = new JLabel();
	totalPriceLabel = new JLabel();
	itemDeleteButton = new JButton();
	addItemButton = new JButton();
	printingCheckBox = new JCheckBox();
	backButton = new JButton();
	sellingToLabel = new JLabel();
	finishSaleButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[175,fill]" +
	    "[106,grow,fill]" +
	    "[118,grow,fill]" +
	    "[117,fill]" +
	    "[112,fill]" +
	    "[136,fill]" +
	    "[139,grow,fill]",
	    // rows
	    "[]" +
	    "[33]" +
	    "[51,grow]" +
	    "[80,grow]" +
	    "[70,grow]" +
	    "[72,grow]" +
	    "[51,grow]" +
	    "[77,grow]" +
	    "[146]"));

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	dateLabel.setForeground(Color.black);
	add(dateLabel, "cell 4 0 3 1");

	//---- upSellingListButton ----
	upSellingListButton.setIcon(new ImageIcon(getClass().getResource("/images/up.png")));
	add(upSellingListButton, "cell 1 0 1 2,growy");

	//---- downSellingListButton ----
	downSellingListButton.setIcon(new ImageIcon(getClass().getResource("/images/down.png")));
	add(downSellingListButton, "cell 2 0 1 2,growy");

	//---- courtesySaleButton ----
	courtesySaleButton.setText("CORTESIA");
	courtesySaleButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(courtesySaleButton, "cell 3 1,growy");

	//---- timeLabel ----
	timeLabel.setText("time:");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
	add(timeLabel, "cell 4 1 3 1");

	//======== itemListPanel ========
	{
	    itemListPanel.setBackground(new Color(0x75c0f1));
	    itemListPanel.setLayout(new MigLayout(
		"fill,hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[389,grow,shrink 0,fill]"));
	}
	add(itemListPanel, "cell 0 2 3 6,grow");

	//======== registerListPanel ========
	{
	    registerListPanel.setBackground(new Color(0xccebc7));
	    registerListPanel.setLayout(new MigLayout(
		"fill,hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[261,fill]"));
	}
	add(registerListPanel, "cell 4 2 3 4,grow");

	//---- quantityTextField ----
	quantityTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	quantityTextField.setText("0");
	quantityTextField.setHorizontalAlignment(SwingConstants.CENTER);
	add(quantityTextField, "cell 3 3");

	//---- addQuantityButton ----
	addQuantityButton.setText("+");
	addQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(addQuantityButton, "cell 3 4,growy");

	//---- removeQuantityButton ----
	removeQuantityButton.setText("-");
	removeQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(removeQuantityButton, "cell 3 5,growy");

	//---- totalPriceInformativeLabel ----
	totalPriceInformativeLabel.setText("TOTAL:");
	totalPriceInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(totalPriceInformativeLabel, "cell 4 6");

	//---- totalPriceLabel ----
	totalPriceLabel.setText("00000");
	totalPriceLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(totalPriceLabel, "cell 5 6");

	//---- itemDeleteButton ----
	itemDeleteButton.setText("BORRAR");
	itemDeleteButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(itemDeleteButton, "cell 6 6");

	//---- addItemButton ----
	addItemButton.setText("A\u00d1ADIR");
	addItemButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(addItemButton, "cell 3 7,growy");

	//---- printingCheckBox ----
	printingCheckBox.setText("IMPRIMIR");
	printingCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(printingCheckBox, "cell 5 7 2 1,grow");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(backButton, "cell 0 8,growy");

	//---- sellingToLabel ----
	sellingToLabel.setText("VENDIENDO A: XXX");
	sellingToLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	sellingToLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(sellingToLabel, "cell 1 8 4 1");

	//---- finishSaleButton ----
	finishSaleButton.setText("COMPLETAR");
	finishSaleButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(finishSaleButton, "cell 5 8 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel dateLabel;
    private JButton upSellingListButton;
    private JButton downSellingListButton;
    private JButton courtesySaleButton;
    private JLabel timeLabel;
    private JPanel itemListPanel;
    private JPanel registerListPanel;
    private JTextField quantityTextField;
    private JButton addQuantityButton;
    private JButton removeQuantityButton;
    private JLabel totalPriceInformativeLabel;
    private JLabel totalPriceLabel;
    private JButton itemDeleteButton;
    private JButton addItemButton;
    private JCheckBox printingCheckBox;
    private JButton backButton;
    private JLabel sellingToLabel;
    private JButton finishSaleButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the dateLabel
     */
    // ========== Encapsulated API ==========

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }

    // -- Buttons --

    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }
    public void onItemDeleteButton(Runnable action) { itemDeleteButton.addActionListener(e -> action.run()); }
    public void onAddItemButton(Runnable action) { addItemButton.addActionListener(e -> action.run()); }
    public void onAddQuantityButton(Runnable action) { addQuantityButton.addActionListener(e -> action.run()); }
    public void onRemoveQuantityButton(Runnable action) { removeQuantityButton.addActionListener(e -> action.run()); }
    public void onFinishSaleButton(Runnable action) { finishSaleButton.addActionListener(e -> action.run()); }
    public void onUpSellingListButton(Runnable action) { upSellingListButton.addActionListener(e -> action.run()); }
    public void onDownSellingListButton(Runnable action) { downSellingListButton.addActionListener(e -> action.run()); }
    public void onCourtesySaleButton(Runnable action) { courtesySaleButton.addActionListener(e -> action.run()); }
    public void setItemDeleteEnabled(boolean e) { itemDeleteButton.setEnabled(e); }
    public void setAddItemEnabled(boolean e) { addItemButton.setEnabled(e); }
    public void setFinishSaleEnabled(boolean e) { finishSaleButton.setEnabled(e); }
    public void setCourtesySaleVisible(boolean v) { courtesySaleButton.setVisible(v); }

    // -- Text fields --

    public void setQuantityText(String text) { quantityTextField.setText(text); }
    public String getQuantityText() { return quantityTextField.getText(); }

    // -- Labels --

    public void setTotalPriceText(String text) { totalPriceLabel.setText(text); }
    public void setSellingToText(String text) { sellingToLabel.setText(text); }

    // -- Checkbox --

    public boolean isPrintSelected() { return printingCheckBox.isSelected(); }

    // -- Tables: item table --

    public int getSelectedItemRow() { return itemTable.getSelectedRow(); }
    public void clearItemTableSelection() { itemTable.clearSelection(); }
    public void onItemTableSelection(javax.swing.event.ListSelectionListener listener) {
        itemTable.getSelectionModel().addListSelectionListener(listener);
    }

    // -- Tables: selling table --

    public int getSelectedSellingRow() { return sellingTable.getSelectedRow(); }
    public void clearSellingTableSelection() { sellingTable.clearSelection(); }
    public void onSellingTableSelection(javax.swing.event.ListSelectionListener listener) {
        sellingTable.getSelectionModel().addListSelectionListener(listener);
    }

    // -- Table scrolling --

    public void scrollItemTable(int direction) {
        TableScroller.scroll(itemTable, direction);
    }

    public void scrollSellingTable(int direction) {
        TableScroller.scroll(sellingTable, direction);
    }

    public void scrollSelectedTable(int direction) {
        JTable table = sellingTable.getSelectedRow() >= 0 ? sellingTable : itemTable;
        TableScroller.scroll(table, direction);
    }
    }

