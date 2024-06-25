/*
 * Created by JFormDesigner on Sat Jun 08 11:35:34 COT 2024
 */
package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
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
public class InventoryManagementView extends JPanel {

    /**
     * @return the upButton
     */
    public JButton getUpButton() {
        return upButton;
    }

    /**
     * @return the downButton
     */
    public JButton getDownButton() {
        return downButton;
    }

    /**
     * @return the inventoryTable
     */
    public JTable getInventoryTable() {
        return inventoryTable;
    }

    public InventoryManagementView() {
        this.cellFont = new Font("Segoe UI", Font.BOLD, 18);
        initComponents();
        initCustomTable();
    }

    //Management for the internal table to shjow the inventory details
    private JTable inventoryTable;
    private InventoryTableModel tableModel;
    private final Font cellFont;

    //Method to update the data for the inventory
    public void updateInventory(JSONObject inventoryData) {
        try {
            tableModel.updateData(inventoryData.getJSONArray("inventoryItems"));
        } catch (JSONException ex) {
            System.out.println("No inventory for GUI to show");
        }
        inventoryTable.repaint();
    }

    private void initCustomTable() {
        tableModel = new InventoryTableModel();
        inventoryTable = new JTable(tableModel);
        //Applying the custom rendererers
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

    public JSONObject getCurrentSelectedItem(int rowSelected) {
        return tableModel.inventoryItems.getJSONObject(rowSelected);
    }

    /*
    Exclusive class for this view, since it's the only one that can see the view itself
     */
    private class InventoryTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Imagen", "Nombre", "Cantidad", "Precio"};
        private JSONArray inventoryItems;

        public InventoryTableModel() {
            this.inventoryItems = new JSONArray();

        }

        public void updateData(JSONArray data) {
            inventoryItems.clear();
            inventoryItems = data;
            getInventoryTable().repaint();
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
                    // case 0:
                    //     return item.getString("imagePath");
                    case 1:
                        return item.getString("itemName");
                    case 2:
                        return item.getInt("quantity");
                    case 3:
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
            if (columnIndex == 0) {
                return ImageIcon.class;
            }
            return super.getColumnClass(columnIndex);
        }
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
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
	    // columns
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[146,grow,fill]" +
	    "[112,grow,fill]",
	    // rows
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]"));

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
	    inventoryPanel.setLayout(new MigLayout(
		"fill,hidemode 3",
		// columns
		"[grow,shrink 0,fill]",
		// rows
		"[grow,shrink 0]"));
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
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
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

    /**
     * @return the deleteItemButton
     */
    public JButton getDeleteItemButton() {
        return deleteItemButton;
    }

    /**
     * @return the newitemButton
     */
    public JButton getNewitemButton() {
        return newitemButton;
    }

    /**
     * @return the informativeEditLabel
     */
    public JLabel getInformativeEditLabel() {
        return informativeEditLabel;
    }

    /**
     * @return the inventoryPanel
     */
    public JPanel getInventoryPanel() {
        return inventoryPanel;
    }

    /**
     * @return the nameLabel
     */
    public JLabel getNameLabel() {
        return nameLabel;
    }

    /**
     * @return the nameTextField
     */
    public JTextField getNameTextField() {
        return nameTextField;
    }

    /**
     * @return the quantityLabel
     */
    public JLabel getQuantityLabel() {
        return quantityLabel;
    }

    /**
     * @return the quantityTextField
     */
    public JTextField getQuantityTextField() {
        return quantityTextField;
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
     * @return the priceLabel
     */
    public JLabel getPriceLabel() {
        return priceLabel;
    }

    /**
     * @return the priceTextField
     */
    public JTextField getPriceTextField() {
        return priceTextField;
    }

    /**
     * @return the removeSmallPriceButton
     */
    public JButton getRemoveSmallPriceButton() {
        return removeSmallPriceButton;
    }

    /**
     * @return the addSmallAmountButton
     */
    public JButton getAddSmallPriceButton() {
        return addSmallPriceButton;
    }

    /**
     * @return the removeBigPriceButton
     */
    public JButton getRemoveBigPriceButton() {
        return removeBigPriceButton;
    }

    /**
     * @return the addBigPriceButton
     */
    public JButton getAddBigPriceButton() {
        return addBigPriceButton;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }

    /**
     * @return the timeLabel
     */
    public JLabel getTimeLabel() {
        return timeLabel;
    }

    /**
     * @return the dateLabel
     */
    public JLabel getDateLabel() {
        return dateLabel;
    }

    /**
     * @return the saveButton
     */
    public JButton getSaveButton() {
        return saveButton;
    }
}
