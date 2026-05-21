/*
 * Created by JFormDesigner on Sat Jun 08 11:15:03 COT 2024
 */
package view;

import view.helpers.TouchScrollHandler;
import java.awt.*;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import net.miginfocom.swing.*;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import view.customListRenderes.CustomCellRenderer;
import view.customListRenderes.CustomHeaderRenderer;
import view.interfaces.TimeLabelInterface;

/**
 * @author Santiago
 */
public class TurnManagerView extends JPanel implements TimeLabelInterface {

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

    public void setTurnDetailsData(List<TurnActivityData> activities,
                                    long totalRooms, long totalItems, long totalSales,
                                    long totalRefunds, long totalSpending, long totalTurnVal,
                                    long totalBankTransfers, long totalDeposits, long totalNet) {
        turnDetailsTableModel.updateData(activities);
        totalRoomsLabel.setText(numberFormat.format(totalRooms));
        totalItemsLabel.setText(numberFormat.format(totalItems));
        totalSalesLabel.setText(numberFormat.format(totalSales));
        totalRefundLabel.setText(numberFormat.format(totalRefunds));
        totalSpendingLabel.setText(numberFormat.format(totalSpending));
        totalTurnLabel.setText(numberFormat.format(totalTurnVal));
        totalTransferLabel.setText(numberFormat.format(totalBankTransfers));
        totalDepositLabel.setText(numberFormat.format(totalDeposits));
        totalNetLabel.setText(numberFormat.format(totalNet));
        turnDetailsTable.repaint();
    }

    public void updateSummarizedTurnData(List<TurnSummaryItemData> summaryItems) {
        summarizedTurnTableModel.updateData(summaryItems);
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
        columnModel.getColumn(0).setPreferredWidth(45);
        columnModel.getColumn(0).setMaxWidth(55);
        columnModel.getColumn(5).setPreferredWidth(45);
        columnModel.getColumn(5).setMaxWidth(55);
        JScrollPane scrollPane = new JScrollPane(turnDetailsTable);
        turnDetailsTable.getTableHeader().setReorderingAllowed(false);
        turnDetailsPanel.add(scrollPane, "cell 0 0, grow");
        TouchScrollHandler.attach(scrollPane);

        //intialize for the summarized table
        summarizedTurnTableModel = new SummarizedTurnTableModel();
        summarizedTurnTable = new JTable(summarizedTurnTableModel);
        TableColumnModel summarizedColumnModel = summarizedTurnTable.getColumnModel();
        for (int i = 0; i < summarizedColumnModel.getColumnCount(); i++) {
            summarizedColumnModel.getColumn(i).setCellRenderer(new CustomCellRenderer(cellFont));
            summarizedColumnModel.getColumn(i).setHeaderRenderer(new CustomHeaderRenderer(cellFont));
        }

        summarizedTurnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane summarizedScrollPane = new JScrollPane(summarizedTurnTable);
        summarizedTurnTable.getTableHeader().setReorderingAllowed(false);
        summarizedTurnInfoPanel.add(summarizedScrollPane, "cell 0 0, grow");
        TouchScrollHandler.attach(summarizedScrollPane);
    }

    public TurnActivityData getCurrentSelectedItem(int selectedRow) {
        return turnDetailsTableModel.filteredTurnDetails.get(selectedRow);
    }

    private void initCustomComponents() {
        summarizedPopup.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private class TurnDetailsTableModel extends AbstractTableModel {

        private final String[] columnNames = {"#", "Habitacion", "Tiempo", "Accion", "Valor", "Dev."};
        private List<TurnActivityData> filteredTurnDetails;

        public TurnDetailsTableModel() {
            this.filteredTurnDetails = new ArrayList<>();
        }

        public void updateData(List<TurnActivityData> data) {
            filteredTurnDetails = data;
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
            TurnActivityData item = filteredTurnDetails.get(rowIndex);
            String changeType = item.getChangeType();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
            String formattedDate = item.getChangeDate().format(formatter);

            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> {
                    if ("sale".equals(changeType)) yield item.getRoomSoldTo();
                    else if ("room".equals(changeType)) yield item.getRoomString();
                    else if ("roomSwap".equals(changeType)) yield item.getOriginalRoom();
                    else if ("refund".equals(changeType)) yield item.getRoomString();
                    else if ("spending".equals(changeType)) yield "N/A";
                    else if ("extraChange".equals(changeType)) yield "N/A";
                    else yield "";
                }
                case 2 -> formattedDate;
                case 3 -> {
                    if ("sale".equals(changeType)) yield item.getQuantity() + " de " + item.getItemName();
                    else if ("room".equals(changeType)) yield "Alquiler " + item.getEffectiveService();
                    else if ("roomSwap".equals(changeType)) yield "Cambio de habitacion a: " + item.getSwappedRoom();
                    else if ("refund".equals(changeType)) {
                        if ("saleRefund".equals(item.getRefundType()))
                            yield "Reembolso " + item.getQuantity() + " de " + item.getItemName();
                        else if ("roomRefund".equals(item.getRefundType()))
                            yield "Reembolso habitacion " + item.getRoomString();
                        else yield "Reembolso";
                    }
                    else if ("spending".equals(changeType)) yield "Gasto: " + item.getDescription();
                    else if ("extraChange".equals(changeType)) {
                        if ("bankTransfer".equals(item.getExtraType())) yield "Transferencia: " + item.getDescription();
                        else if ("safeDeposit".equals(item.getExtraType())) yield "Deposito: " + item.getDescription();
                        else yield item.getExtraType();
                    }
                    else yield "";
                }
                case 4 -> {
                    if ("sale".equals(changeType) || "room".equals(changeType)
                            || "refund".equals(changeType) || "spending".equals(changeType)
                            || "extraChange".equals(changeType)) yield item.getPrice();
                    else yield "";
                }
                case 5 -> ("sale".equals(changeType) || "room".equals(changeType)) && item.isRefunded() ? "Si" : "";
                default -> null;
            };
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Integer.class : String.class;
        }
    }

    private class SummarizedTurnTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Cantidad", "Concepto", "Precio"};
        private List<TurnSummaryItemData> summarizedTurnDetails;

        public SummarizedTurnTableModel() {
            this.summarizedTurnDetails = new ArrayList<>();
        }

        public void updateData(List<TurnSummaryItemData> data) {
            summarizedTurnDetails = data;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return summarizedTurnDetails != null ? summarizedTurnDetails.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TurnSummaryItemData item = summarizedTurnDetails.get(rowIndex);

            return switch (columnIndex) {
                case 0 -> item.quantity();
                case 1 -> item.displayConcept();
                case 2 -> numberFormat.format(item.price());
                default -> "";
            };
        }
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	refundButton = new JButton();
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
	totalRefundInformativeLabel = new JLabel();
	totalRefundLabel = new JLabel();
	totalSpendingInformativeLabel = new JLabel();
	totalSpendingLabel = new JLabel();
	totalTurnInformativeLabel = new JLabel();
	totalTurnLabel = new JLabel();
	totalTransferInformativeLabel = new JLabel();
	totalTransferLabel = new JLabel();
	totalDepositInformativeLabel = new JLabel();
	totalDepositLabel = new JLabel();
	totalNetInformativeLabel = new JLabel();
	totalNetLabel = new JLabel();
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
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[77]" +
	    "[grow]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[grow]" +
	    "[grow]" +
	    "[25]" +
	    "[grow]"));

	//---- refundButton ----
	refundButton.setText("REEMBOLSO");
	refundButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(refundButton, "cell 0 0,growy");

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
	add(turnDetailsPanel, "cell 0 1 5 13,growy");

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
	totalSalesInformativeLabel.setText("TOTAL VENTAS");
	totalSalesInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesInformativeLabel, "cell 5 4");

	//---- totalSalesLabel ----
	totalSalesLabel.setText("text");
	totalSalesLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesLabel, "cell 6 4");

	//---- totalRefundInformativeLabel ----
	totalRefundInformativeLabel.setText("REEMBOLSO");
	totalRefundInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRefundInformativeLabel, "cell 5 5");

	//---- totalRefundLabel ----
	totalRefundLabel.setText("text");
	totalRefundLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRefundLabel, "cell 6 5");

	//---- totalSpendingInformativeLabel ----
	totalSpendingInformativeLabel.setText("GASTOS");
	totalSpendingInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSpendingInformativeLabel, "cell 5 6");

	//---- totalSpendingLabel ----
	totalSpendingLabel.setText("text");
	totalSpendingLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSpendingLabel, "cell 6 6");

	//---- totalTurnInformativeLabel ----
	totalTurnInformativeLabel.setText("TOTAL TURNO");
	totalTurnInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTurnInformativeLabel, "cell 5 7");

	//---- totalTurnLabel ----
	totalTurnLabel.setText("text");
	totalTurnLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTurnLabel, "cell 6 7");

	//---- totalTransferInformativeLabel ----
	totalTransferInformativeLabel.setText("TOTAL TRANSFERENCIA");
	totalTransferInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTransferInformativeLabel, "cell 5 8");

	//---- totalTransferLabel ----
	totalTransferLabel.setText("text");
	totalTransferLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTransferLabel, "cell 6 8");

	//---- totalDepositInformativeLabel ----
	totalDepositInformativeLabel.setText("TOTAL DEPOSITO");
	totalDepositInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalDepositInformativeLabel, "cell 5 9");

	//---- totalDepositLabel ----
	totalDepositLabel.setText("text");
	totalDepositLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalDepositLabel, "cell 6 9");

	//---- totalNetInformativeLabel ----
	totalNetInformativeLabel.setText("TOTAL NETO");
	totalNetInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalNetInformativeLabel, "cell 5 10");

	//---- totalNetLabel ----
	totalNetLabel.setText("text");
	totalNetLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalNetLabel, "cell 6 10");

	//---- noPrintCheckBox ----
	noPrintCheckBox.setText("NO IMPRIMIR");
	noPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(noPrintCheckBox, "cell 5 11 2 1,growy");

	//---- summarizedPrintCheckBox ----
	summarizedPrintCheckBox.setText("RESUMIDO");
	summarizedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(summarizedPrintCheckBox, "cell 5 12 2 1,growy");

	//---- detailedPrintCheckBox ----
	detailedPrintCheckBox.setText("DETALLADO");
	detailedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(detailedPrintCheckBox, "cell 5 13 2 1,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(backButton, "cell 0 14,growy");

	//---- printButton ----
	printButton.setText("IMPRIMIR PARCIAL");
	printButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(printButton, "cell 2 14 3 1,growy");

	//---- endTurnButton ----
	endTurnButton.setText("FIN TURNO");
	endTurnButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(endTurnButton, "cell 5 14 2 1,growy");

	//======== summarizedPopup ========
	{
	    summarizedPopup.setAlwaysOnTop(true);
	    var summarizedPopupContentPane = summarizedPopup.getContentPane();
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
    private JButton refundButton;
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
    private JLabel totalRefundInformativeLabel;
    private JLabel totalRefundLabel;
    private JLabel totalSpendingInformativeLabel;
    private JLabel totalSpendingLabel;
    private JLabel totalTurnInformativeLabel;
    private JLabel totalTurnLabel;
    private JLabel totalTransferInformativeLabel;
    private JLabel totalTransferLabel;
    private JLabel totalDepositInformativeLabel;
    private JLabel totalDepositLabel;
    private JLabel totalNetInformativeLabel;
    private JLabel totalNetLabel;
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

    public JLabel getTimeLabel() {
        return timeLabel;
    }

    public JLabel getDateLabel() {
        return dateLabel;
    }

    public JCheckBox getNoPrintCheckBox() {
        return noPrintCheckBox;
    }

    public JCheckBox getSummarizedPrintCheckBox() {
        return summarizedPrintCheckBox;
    }

    public JCheckBox getDetailedPrintCheckBox() {
        return detailedPrintCheckBox;
    }

    public JButton getPrintButton() {
        return printButton;
    }

    public JButton getEndTurnButton() {
        return endTurnButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JTable getTurnDetailsTable() {
        return turnDetailsTable;
    }

    public JButton getRefundButton() {
        return refundButton;
    }

    public JButton getUpButton() {
        return upButton;
    }

    public JButton getDownButton() {
        return downButton;
    }

    public JButton getSummarizedTurnButton() {
        return summarizedTurnButton;
    }

    public JFrame getSummarizedPopup() {
        return summarizedPopup;
    }

    public JButton getBackFromSummarizedTurn() {
        return backFromSummarizedTurn;
    }
}
