/*
 * Created by JFormDesigner on Sat Jun 08 11:15:03 COT 2024
 */
package view;

import java.awt.*;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import net.miginfocom.swing.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import view.customListRenderes.CustomCellRenderer;
import view.customListRenderes.CustomHeaderRenderer;

/**
 * @author Santiago
 */
public class TurnManagerView extends JPanel {

    private JTable turnDetailsTable;
    private TurnDetailsTableModel turnDetailsTableModel;
    private final Font cellFont;
    private final NumberFormat numberFormat;
    private JTable summarizedTurnTable;
    private SummarizedTurnTableModel summarizedTurnTableModel;

    public TurnManagerView() {
        numberFormat = NumberFormat.getNumberInstance(Locale.US);
        this.cellFont = new Font("Segoe UI", Font.BOLD, 16);
        initComponents();
        initCustomTable();
        initCustomComponents();
    }

    public void setTurnDetailsData(JSONObject turnDetails) {
        try {
            turnDetailsTableModel.updateData(turnDetails.getJSONArray("turnActivity"));
            totalRoomsLabel.setText(numberFormat.format(turnDetails.getLong("totalRooms")));
            totalItemsLabel.setText(numberFormat.format(turnDetails.getLong("totalItems")));
            totalSalesLabel.setText(numberFormat.format(turnDetails.getLong("totalSales")));
        } catch (JSONException ex) {
            System.out.println("No turn for GUI to show");
            totalRoomsLabel.setText(numberFormat.format(0));
            totalItemsLabel.setText(numberFormat.format(0));
            totalSalesLabel.setText(numberFormat.format(0));
            turnDetailsTableModel.updateData(null);
            this.backButton.setEnabled(true);

        }
        turnDetailsTable.repaint();
    }

    public void updateSummarizedTurnData(JSONObject summarizedTurn) {
        summarizedTurnTableModel.updateData(summarizedTurn);
    }

    private void initCustomTable() {
        turnDetailsTableModel = new TurnDetailsTableModel();
        turnDetailsTable = new JTable(turnDetailsTableModel);
        TableColumnModel columnModel = turnDetailsTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(new CustomCellRenderer(cellFont));
            columnModel.getColumn(i).setHeaderRenderer(new CustomHeaderRenderer(cellFont));
        }

        turnDetailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(turnDetailsTable);
        turnDetailsTable.getTableHeader().setReorderingAllowed(false);
        turnDetailsPanel.add(scrollPane, "cell 0 0, grow");

        //intialize for the summarized table
        summarizedTurnTableModel = new SummarizedTurnTableModel();
        summarizedTurnTable = new JTable(summarizedTurnTableModel);
        TableColumnModel summarizedColumnModel = getSummarizedTurnTable().getColumnModel();
        for (int i = 0; i < summarizedColumnModel.getColumnCount(); i++) {
            summarizedColumnModel.getColumn(i).setCellRenderer(new CustomCellRenderer(cellFont));
            summarizedColumnModel.getColumn(i).setHeaderRenderer(new CustomHeaderRenderer(cellFont));
        }

        getSummarizedTurnTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane summarizedScrollPane = new JScrollPane(getSummarizedTurnTable());
        getSummarizedTurnTable().getTableHeader().setReorderingAllowed(false);
        summarizedTurnInfoPanel.add(summarizedScrollPane, "cell 0 0, grow");
    }

    public JSONObject getCurrentSelectedItem(int selectedRow) {
        return turnDetailsTableModel.filteredTurnDetails.get(selectedRow);
    }

    private void initCustomComponents() {
        summarizedPopup.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private class TurnDetailsTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Habitacion", "Tiempo", "Accion", "Valor"};
        private ArrayList<JSONObject> filteredTurnDetails;

        public TurnDetailsTableModel() {
            this.filteredTurnDetails = new ArrayList<>();
        }

        public void updateData(JSONArray data) {
            filteredTurnDetails.clear();
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    try {
                        JSONObject item = data.getJSONObject(i);
                        String changeType = item.getString("changeType");
                        if (changeType.equals("sale")) {
                            JSONArray registerArray = item.getJSONArray("register");
                            String roomSoldTo = item.getString("roomSoldTo");
                            String changeDate = item.getString("changeDate");
                            for (int registerItem = 0; registerItem < registerArray.length(); registerItem++) {
                                JSONObject currentItem = new JSONObject(registerArray.getJSONObject(registerItem).toString());
                                currentItem.put("roomSoldTo", roomSoldTo);
                                currentItem.put("changeType", "sale");
                                currentItem.put("changeDate", changeDate);
                                filteredTurnDetails.add(currentItem);
                            }
                        } else if (changeType.equals("room") && item.getInt("roomStatus") == 3) {
                            filteredTurnDetails.add(item);
                        } else if (changeType.equals("roomSwap")) {
                            filteredTurnDetails.add(item);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return filteredTurnDetails.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                JSONObject item = filteredTurnDetails.get(rowIndex);
                String changeType = item.getString("changeType");
                ZonedDateTime changeDate = ZonedDateTime.parse(item.getString("changeDate"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
                String formattedDate = changeDate.format(formatter);

                switch (columnIndex) {
                    case 0:
                        if (changeType.equals("sale")) {
                            return item.getString("roomSoldTo");
                        } else if (changeType.equals("room") && item.getInt("roomStatus") == 3) {
                            return item.getString("roomString");
                        } else if (changeType.equals("roomSwap")) {
                            return item.getString("originalRoom");
                        } else {
                            return "";
                        }
                    case 1: // Time
                        return formattedDate;
                    case 2: // Action
                        if (changeType.equals("sale")) {
                            return item.getLong("quantity") + " de " + item.getString("itemName");
                        } else if (changeType.equals("room") && item.getInt("roomStatus") == 3) {
                            if (item.getInt("servicedExtension") == 0) {
                                return "Alquiler " + item.getInt("service");
                            } else {
                                return "Alquiler " + item.getInt("servicedExtension");
                            }
                        } else if (changeType.equals("roomSwap")) {
                            return "Cambio de habitacion a: " + item.getString("swapedRoom");
                        } else {
                            return "";
                        }
                    case 3: // Value
                        if (changeType.equals("sale")) {
                            return item.getInt("price");
                        } else if (changeType.equals("room") && item.getInt("roomStatus") == 3) {
                            return item.getInt("price");
                        } else {
                            return "";
                        }
                    default:
                        return null;
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    }

    private class SummarizedTurnTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Cantidad", "Concepto", "Precio"};
        private ArrayList<JSONObject> summarizedTurnDetails;

        public SummarizedTurnTableModel() {
            this.summarizedTurnDetails = new ArrayList<>();
        }

        public void updateData(JSONObject data) {
            summarizedTurnDetails.clear();
            if (data != null) {
                JSONArray summaryArray = data.getJSONArray("turnSummary");
                for (int i = 0; i < summaryArray.length(); i++) {
                    JSONObject summaryObject = summaryArray.getJSONObject(i);
                    summarizedTurnDetails.add(summaryObject);
                }
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            // The number of rows is the size of the summarizedTurnDetails list
            return summarizedTurnDetails != null ? summarizedTurnDetails.size() : 0;
        }

        @Override
        public int getColumnCount() {
            // We have 3 columns: Cantidad, Concepto, Precio
            return columnNames.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            // Get the JSONObject for the current row
            JSONObject summaryObject = summarizedTurnDetails.get(rowIndex);

            try {
                switch (columnIndex) {
                    case 0:
                        // "Cantidad" column: either room or item quantity
                        return summaryObject.getInt("quantity");
                    case 1:
                        // "Concepto" column: Either "Alquiler" + service or item name
                        String change = summaryObject.getString("summaryType");
                        if ("room".equals(change)) {
                            int service = summaryObject.getInt("service");
                            return "Alquiler " + service;
                        } else if ("item".equals(change)) {
                            return summaryObject.getString("itemName");
                        }
                        return ""; // Default case if unknown
                    case 2:
                        // "Precio" column: Price formatted with the number format
                        long price = summaryObject.getLong("price");
                        return numberFormat.format(price);
                    default:
                        return "";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	deleteActionButton = new JButton();
	upButton = new JButton();
	downButton = new JButton();
	summarizedTurnButton = new JButton();
	turnDetailsPanel = new JPanel();
	timeLabel = new JLabel();
	dateLabel = new JLabel();
	totalRoomsInformativeLabel = new JLabel();
	totalRoomsLabel = new JLabel();
	totalItemsInformativeLabel = new JLabel();
	totalItemsLabel = new JLabel();
	totalSalesInformativeLabel = new JLabel();
	totalSalesLabel = new JLabel();
	noPrintCheckBox = new JCheckBox();
	summarizedPrintCheckBox = new JCheckBox();
	detailedPrintCheckBox = new JCheckBox();
	backButton = new JButton();
	printButton = new JButton();
	endTurnButton = new JButton();
	summarizedPopup = new JFrame();
	summarizedTurnLabel = new JLabel();
	summarizedTurnInfoPanel = new JPanel();
	backFromSummarizedTurn = new JButton();

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
	    "[grow,fill]",
	    // rows
	    "[77]" +
	    "[grow]" +
	    "[]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[25]" +
	    "[grow]"));

	//---- deleteActionButton ----
	deleteActionButton.setText("ELIMINAR");
	deleteActionButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(deleteActionButton, "cell 0 0,growy");

	//---- upButton ----
	upButton.setIcon(new ImageIcon(getClass().getResource("/up.png")));
	add(upButton, "cell 1 0,growy");

	//---- downButton ----
	downButton.setIcon(new ImageIcon(getClass().getResource("/down.png")));
	add(downButton, "cell 2 0,growy");

	//---- summarizedTurnButton ----
	summarizedTurnButton.setText("RESUMIDO TURNO");
	summarizedTurnButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(summarizedTurnButton, "cell 3 0 2 1,grow");

	//======== turnDetailsPanel ========
	{
	    turnDetailsPanel.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[grow,fill]"));
	}
	add(turnDetailsPanel, "cell 0 1 5 7,growy");

	//---- timeLabel ----
	timeLabel.setText("time");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 36));
	timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(timeLabel, "cell 5 0 2 1,dock center");

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(dateLabel, "cell 5 1 2 1,dock center");

	//---- totalRoomsInformativeLabel ----
	totalRoomsInformativeLabel.setText("HABITACIONES:");
	totalRoomsInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRoomsInformativeLabel, "cell 5 2");

	//---- totalRoomsLabel ----
	totalRoomsLabel.setText("text");
	totalRoomsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRoomsLabel, "cell 6 2");

	//---- totalItemsInformativeLabel ----
	totalItemsInformativeLabel.setText("PRODUCTOS");
	totalItemsInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalItemsInformativeLabel, "cell 5 3");

	//---- totalItemsLabel ----
	totalItemsLabel.setText("text");
	totalItemsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalItemsLabel, "cell 6 3");

	//---- totalSalesInformativeLabel ----
	totalSalesInformativeLabel.setText("TOTAL");
	totalSalesInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesInformativeLabel, "cell 5 4");

	//---- totalSalesLabel ----
	totalSalesLabel.setText("text");
	totalSalesLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesLabel, "cell 6 4");

	//---- noPrintCheckBox ----
	noPrintCheckBox.setText("NO IMPRIMIR");
	noPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(noPrintCheckBox, "cell 5 5 2 1,growy");

	//---- summarizedPrintCheckBox ----
	summarizedPrintCheckBox.setText("RESUMIDO");
	summarizedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(summarizedPrintCheckBox, "cell 5 6 2 1,growy");

	//---- detailedPrintCheckBox ----
	detailedPrintCheckBox.setText("DETALLADO");
	detailedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(detailedPrintCheckBox, "cell 5 7 2 1,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(backButton, "cell 0 8,growy");

	//---- printButton ----
	printButton.setText("IMPRIMIR");
	printButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(printButton, "cell 3 8 2 1,growy");

	//---- endTurnButton ----
	endTurnButton.setText("FIN TURNO");
	endTurnButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(endTurnButton, "cell 5 8 2 1,growy");

	//======== summarizedPopup ========
	{
	    summarizedPopup.setAlwaysOnTop(true);
	    Container summarizedPopupContentPane = summarizedPopup.getContentPane();
	    summarizedPopupContentPane.setLayout(new MigLayout(
		"fill,hidemode 3",
		// columns
		"[fill]",
		// rows
		"[0]" +
		"[384]" +
		"[]"));
	    summarizedPopup.setExtendedState(JFrame.MAXIMIZED_BOTH);

	    //---- summarizedTurnLabel ----
	    summarizedTurnLabel.setText("RESUMEN TURNO:");
	    summarizedTurnLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    summarizedTurnLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    summarizedPopupContentPane.add(summarizedTurnLabel, "cell 0 0");

	    //======== summarizedTurnInfoPanel ========
	    {
		summarizedTurnInfoPanel.setLayout(new MigLayout(
		    "fill,hidemode 3",
		    // columns
		    "[fill]",
		    // rows
		    "[]"));
	    }
	    summarizedPopupContentPane.add(summarizedTurnInfoPanel, "cell 0 1,growy");

	    //---- backFromSummarizedTurn ----
	    backFromSummarizedTurn.setText("VOLVER");
	    backFromSummarizedTurn.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	    summarizedPopupContentPane.add(backFromSummarizedTurn, "cell 0 2,growy");
	    summarizedPopup.pack();
	    summarizedPopup.setLocationRelativeTo(summarizedPopup.getOwner());
	}
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JButton deleteActionButton;
    private JButton upButton;
    private JButton downButton;
    private JButton summarizedTurnButton;
    private JPanel turnDetailsPanel;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JLabel totalRoomsInformativeLabel;
    private JLabel totalRoomsLabel;
    private JLabel totalItemsInformativeLabel;
    private JLabel totalItemsLabel;
    private JLabel totalSalesInformativeLabel;
    private JLabel totalSalesLabel;
    private JCheckBox noPrintCheckBox;
    private JCheckBox summarizedPrintCheckBox;
    private JCheckBox detailedPrintCheckBox;
    private JButton backButton;
    private JButton printButton;
    private JButton endTurnButton;
    private JFrame summarizedPopup;
    private JLabel summarizedTurnLabel;
    private JPanel summarizedTurnInfoPanel;
    private JButton backFromSummarizedTurn;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the turnDetailsPanel
     */
    public JPanel getTurnDetailsPanel() {
        return turnDetailsPanel;
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
     * @return the noPrintCheckBox
     */
    public JCheckBox getNoPrintCheckBox() {
        return noPrintCheckBox;
    }

    /**
     * @return the summarizedPrintCheckBox
     */
    public JCheckBox getSummarizedPrintCheckBox() {
        return summarizedPrintCheckBox;
    }

    /**
     * @return the detailedPrintCheckBox
     */
    public JCheckBox getDetailedPrintCheckBox() {
        return detailedPrintCheckBox;
    }

    /**
     * @return the printButton
     */
    public JButton getPrintButton() {
        return printButton;
    }

    /**
     * @return the endTurnButton
     */
    public JButton getEndTurnButton() {
        return endTurnButton;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }

    /**
     * @return the turnDetailsTable
     */
    public JTable getTurnDetailsTable() {
        return turnDetailsTable;
    }

    /**
     * @return the deleteActionButton
     */
    public JButton getDeleteActionButton() {
        return deleteActionButton;
    }

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
     * @return the summarizedTurnButton
     */
    public JButton getSummarizedTurnButton() {
        return summarizedTurnButton;
    }

    /**
     * @return the summarizedTurnTable
     */
    public JTable getSummarizedTurnTable() {
        return summarizedTurnTable;
    }

    /**
     * @return the summarizedPopup
     */
    public JFrame getSummarizedPopup() {
        return summarizedPopup;
    }

    /**
     * @return the summarizedTurnLabel
     */
    public JLabel getSummarizedTurnLabel() {
        return summarizedTurnLabel;
    }

    /**
     * @return the backFromSummarizedTurn
     */
    public JButton getBackFromSummarizedTurn() {
        return backFromSummarizedTurn;
    }
    
}
