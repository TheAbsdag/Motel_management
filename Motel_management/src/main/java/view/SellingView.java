/*
 * Created by JFormDesigner on Fri Jun 07 00:47:34 COT 2024
 */
package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import view.customListRenderes.*;

/**
 * @author Santiago
 */
public class SellingView extends JPanel {

    private JTable itemTable;
    private JTable sellingTable;

    private ItemTableModel itemTableModel;
    private SellingTableModel sellingTableModel;
    private final Font cellFont;

    public SellingView() {
        this.cellFont = new Font("Segoe UI", Font.BOLD, 18);
        initComponents();
        initCustomTable();
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
    }

    public JSONObject getCurrentSelectedItemListed(int rowSelected) {
        return itemTableModel.inventoryItems.getJSONObject(rowSelected);
    }

    public JSONObject getCurrentSelectedSellingListed(int rowSelected) {
        return sellingTableModel.sellingItems.getJSONObject(rowSelected);
    }

    public void updateItemListed(JSONObject inventoryData) {
        try {
            itemTableModel.updateData(inventoryData.getJSONArray("inventoryItems"));
        } catch (JSONException ex) {
            System.out.println("No inventory for GUI to show");
        }
        itemTable.repaint();
    }

    public void updateSellingListed(JSONArray sellingList) {
        try {
            sellingTableModel.updateData(sellingList);
        } catch (JSONException ex) {
            System.out.println("No selling for GUI to show");
        }
        sellingTable.repaint();
    }

    //Custom tablemodel class for showing registerlist
    private class SellingTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Nombre", "Cantidad", "Precio"};
        private JSONArray sellingItems;

        public SellingTableModel() {
            this.sellingItems = new JSONArray();
        }

        public void updateData(JSONArray data) {
            //sellingItems.clear();
            sellingItems = data;
            getSellingTable().repaint();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return sellingItems.length();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                JSONObject item = sellingItems.getJSONObject(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return item.getString("itemName");
                    case 1:
                        return item.getInt("quantity");
                    case 2:
                        return item.getDouble("price");
                    default:
                        return null;
                }
            } catch (JSONException ex) {
                return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return Integer.class;
                case 2:
                    return Double.class;
                default:
                    return super.getColumnClass(columnIndex);
            }
        }
    }

    //Custom tableModel class for showing items
    private class ItemTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Nombre", "Precio"};
        private JSONArray inventoryItems;

        public ItemTableModel() {
            this.inventoryItems = new JSONArray();
        }

        public void updateData(JSONArray data) {
            inventoryItems.clear();
            inventoryItems = data;
            getItemTable().repaint();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return inventoryItems.length();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                JSONObject item = inventoryItems.getJSONObject(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return item.getString("itemName");
                    case 1:
                        return item.getDouble("price");
                    default:
                        return null;
                }
            } catch (JSONException ex) {
                return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return Double.class;
                default:
                    return super.getColumnClass(columnIndex);
            }
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
	    "hidemode 3",
	    // columns
	    "[175,fill]" +
	    "[106,grow,fill]" +
	    "[118,fill]" +
	    "[117,fill]" +
	    "[112,fill]" +
	    "[136,fill]" +
	    "[139,grow,fill]",
	    // rows
	    "[]" +
	    "[33]" +
	    "[51]" +
	    "[80]" +
	    "[70]" +
	    "[72]" +
	    "[51]" +
	    "[77]" +
	    "[146]"));

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	dateLabel.setForeground(Color.black);
	add(dateLabel, "cell 4 0 3 1");

	//---- upSellingListButton ----
	upSellingListButton.setIcon(new ImageIcon(getClass().getResource("/up.png")));
	add(upSellingListButton, "cell 1 0 1 2,growy");

	//---- downSellingListButton ----
	downSellingListButton.setIcon(new ImageIcon(getClass().getResource("/down.png")));
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
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[389,grow,shrink 0,fill]"));
	}
	add(itemListPanel, "cell 0 2 3 6");

	//======== registerListPanel ========
	{
	    registerListPanel.setBackground(new Color(0xccebc7));
	    registerListPanel.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[261,fill]"));
	}
	add(registerListPanel, "cell 4 2 3 4");

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
    public JLabel getDateLabel() {
        return dateLabel;
    }

    /**
     * @return the timeLabel
     */
    public JLabel getTimeLabel() {
        return timeLabel;
    }

    /**
     * @return the itemListPanel
     */
    public JPanel getItemListPanel() {
        return itemListPanel;
    }

    /**
     * @return the registerListPanel
     */
    public JPanel getRegisterListPanel() {
        return registerListPanel;
    }

    /**
     * @return the quantityTextField
     */
    public JTextField getQuantityTextField() {
        return quantityTextField;
    }

    /**
     * @return the itemDeleteButton
     */
    public JButton getItemDeleteButton() {
        return itemDeleteButton;
    }

    /**
     * @return the printingCheckBox
     */
    public JCheckBox getPrintingCheckBox() {
        return printingCheckBox;
    }

    /**
     * @return the sellingToLabel
     */
    public JLabel getSellingToLabel() {
        return sellingToLabel;
    }

    /**
     * @return the finishSaleButton
     */
    public JButton getFinishSaleButton() {
        return finishSaleButton;
    }

    /**
     * @return the addQuantityButton
     */
    public JButton getAddQuantityButton() {
        return addQuantityButton;
    }

    /**
     * @return the removeQuantityButton
     */
    public JButton getRemoveQuantityButton() {
        return removeQuantityButton;
    }

    /**
     * @return the addItemButton
     */
    public JButton getAddItemButton() {
        return addItemButton;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }

    /**
     * @return the itemTable
     */
    public JTable getItemTable() {
        return itemTable;
    }

    /**
     * @return the sellingTable
     */
    public JTable getSellingTable() {
        return sellingTable;
    }

    /**
     * @return the totalPriceInformativeLabel
     */
    public JLabel getTotalPriceInformativeLabel() {
        return totalPriceInformativeLabel;
    }

    /**
     * @return the totalPriceLabel
     */
    public JLabel getTotalPriceLabel() {
        return totalPriceLabel;
    }

    /**
     * @return the upSellingListButton
     */
    public JButton getUpSellingListButton() {
        return upSellingListButton;
    }

    /**
     * @return the downSellingListButton
     */
    public JButton getDownSellingListButton() {
        return downSellingListButton;
    }

    /**
     * @return the courtesySaleButton
     */
    public JButton getCourtesySaleButton() {
        return courtesySaleButton;
    }

}
