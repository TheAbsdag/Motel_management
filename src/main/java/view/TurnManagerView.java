/*
 * Created by JFormDesigner on Sat Jun 08 11:15:03 COT 2024
 */
package view;

import view.helpers.TouchScrollHandler;
import java.awt.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import net.miginfocom.swing.*;
import model.dto.TurnActivityData;
import model.dto.TurnSummaryItemData;
import model.json.CurrencyConfig;
import model.turn.ActivityType;
import model.turn.ExtraChangeType;
import model.turn.RefundType;
import view.customListRenderes.CustomCellRenderer;
import view.customListRenderes.CustomHeaderRenderer;
import view.helpers.CurrencyFormatter;
import view.helpers.TimeFormatter;
import view.interfaces.TimeLabelInterface;

/**
 * @author Santiago
 */
public class TurnManagerView extends JPanel implements TimeLabelInterface {

    private JTable turnDetailsTable;
    private TurnDetailsTableModel turnDetailsTableModel;
    private final Font cellFont;
    private CurrencyConfig currencyConfig = CurrencyConfig.defaultConfig();
    private JTable summarizedTurnTable;
    private SummarizedTurnTableModel summarizedTurnTableModel;

    public void setCurrencyConfig(CurrencyConfig cfg) {
        this.currencyConfig = cfg != null ? cfg : CurrencyConfig.defaultConfig();
    }

    public TurnManagerView() {
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
        totalRoomsLabel.setText(CurrencyFormatter.format(totalRooms, currencyConfig));
        totalItemsLabel.setText(CurrencyFormatter.format(totalItems, currencyConfig));
        totalSalesLabel.setText(CurrencyFormatter.format(totalSales, currencyConfig));
        totalRefundLabel.setText(CurrencyFormatter.format(totalRefunds, currencyConfig));
        totalSpendingLabel.setText(CurrencyFormatter.format(totalSpending, currencyConfig));
        totalTurnLabel.setText(CurrencyFormatter.format(totalTurnVal, currencyConfig));
        totalTransferLabel.setText(CurrencyFormatter.format(totalBankTransfers, currencyConfig));
        totalDepositLabel.setText(CurrencyFormatter.format(totalDeposits, currencyConfig));
        totalNetLabel.setText(CurrencyFormatter.format(totalNet, currencyConfig));
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
            ActivityType changeType = item.getChangeType();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a").withZone(ZoneId.of("America/Bogota"));
            String formattedDate = item.getChangeDate().format(formatter);

            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> {
                    if (ActivityType.SALE == changeType) yield item.getRoomSoldTo();
                    else if (ActivityType.ROOM == changeType) yield item.getRoomString();
                    else if (ActivityType.ROOM_SWAP == changeType) yield item.getOriginalRoom();
                    else if (ActivityType.REFUND == changeType) yield item.getRoomString();
                    else if (ActivityType.SPENDING == changeType) yield "N/A";
                    else if (ActivityType.EXTRA_CHANGE == changeType) yield "N/A";
                    else yield "";
                }
                case 2 -> formattedDate;
                case 3 -> {
                    if (ActivityType.SALE == changeType) yield item.getQuantity() + " de " + item.getItemName();
                    else if (ActivityType.ROOM == changeType) yield "Alquiler " + TimeFormatter.formatDuration(item.getEffectiveServiceDuration());
                    else if (ActivityType.ROOM_SWAP == changeType) yield "Cambio de habitacion a: " + item.getSwappedRoom();
                    else if (ActivityType.REFUND == changeType) {
                        if (RefundType.SALE_REFUND == item.getRefundType())
                            yield "Reembolso " + item.getQuantity() + " de " + item.getItemName();
                        else if (RefundType.ROOM_REFUND == item.getRefundType())
                            yield "Reembolso habitacion " + item.getRoomString();
                        else yield "Reembolso";
                    }
                    else if (ActivityType.SPENDING == changeType) yield "Gasto: " + item.getDescription();
                    else if (ActivityType.EXTRA_CHANGE == changeType) {
                        if (ExtraChangeType.BANK_TRANSFER == item.getExtraType()) yield "Transferencia: " + item.getDescription();
                        else if (ExtraChangeType.SAFE_DEPOSIT == item.getExtraType()) yield "Deposito: " + item.getDescription();
                        else yield "";
                    }
                    else yield "";
                }
                case 4 -> {
                    if (ActivityType.SALE == changeType || ActivityType.ROOM == changeType
                            || ActivityType.REFUND == changeType || ActivityType.SPENDING == changeType
                            || ActivityType.EXTRA_CHANGE == changeType) yield item.getPrice();
                    else yield "";
                }
                case 5 -> (ActivityType.SALE == changeType || ActivityType.ROOM == changeType) && item.isRefunded() ? "Si" : "";
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
                case 2 -> CurrencyFormatter.format(item.price(), currencyConfig);
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
	upButton.setIcon(new ImageIcon(getClass().getResource("/images/up.png")));
	add(upButton, "cell 1 0,growy");

	//---- downButton ----
	downButton.setIcon(new ImageIcon(getClass().getResource("/images/down.png")));
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
	totalRoomsLabel.setText("0");
	totalRoomsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRoomsLabel, "cell 6 2");

	//---- totalItemsInformativeLabel ----
	totalItemsInformativeLabel.setText("PRODUCTOS");
	totalItemsInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalItemsInformativeLabel, "cell 5 3");

	//---- totalItemsLabel ----
	totalItemsLabel.setText("0");
	totalItemsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalItemsLabel, "cell 6 3");

	//---- totalSalesInformativeLabel ----
	totalSalesInformativeLabel.setText("TOTAL VENTAS");
	totalSalesInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesInformativeLabel, "cell 5 4");

	//---- totalSalesLabel ----
	totalSalesLabel.setText("0");
	totalSalesLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesLabel, "cell 6 4");

	//---- totalRefundInformativeLabel ----
	totalRefundInformativeLabel.setText("REEMBOLSO");
	totalRefundInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRefundInformativeLabel, "cell 5 5");

	//---- totalRefundLabel ----
	totalRefundLabel.setText("0");
	totalRefundLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRefundLabel, "cell 6 5");

	//---- totalSpendingInformativeLabel ----
	totalSpendingInformativeLabel.setText("GASTOS");
	totalSpendingInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSpendingInformativeLabel, "cell 5 6");

	//---- totalSpendingLabel ----
	totalSpendingLabel.setText("0");
	totalSpendingLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSpendingLabel, "cell 6 6");

	//---- totalTurnInformativeLabel ----
	totalTurnInformativeLabel.setText("TOTAL TURNO");
	totalTurnInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTurnInformativeLabel, "cell 5 7");

	//---- totalTurnLabel ----
	totalTurnLabel.setText("0");
	totalTurnLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTurnLabel, "cell 6 7");

	//---- totalTransferInformativeLabel ----
	totalTransferInformativeLabel.setText("TOTAL TRANSFERENCIA");
	totalTransferInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTransferInformativeLabel, "cell 5 8");

	//---- totalTransferLabel ----
	totalTransferLabel.setText("0");
	totalTransferLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTransferLabel, "cell 6 8");

	//---- totalDepositInformativeLabel ----
	totalDepositInformativeLabel.setText("TOTAL DEPOSITO");
	totalDepositInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalDepositInformativeLabel, "cell 5 9");

	//---- totalDepositLabel ----
	totalDepositLabel.setText("0");
	totalDepositLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalDepositLabel, "cell 6 9");

	//---- totalNetInformativeLabel ----
	totalNetInformativeLabel.setText("TOTAL NETO");
	totalNetInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalNetInformativeLabel, "cell 5 10");

	//---- totalNetLabel ----
	totalNetLabel.setText("0");
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

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }

    // -- Buttons --
    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }
    public void onPrintButton(Runnable action) { printButton.addActionListener(e -> action.run()); }
    public void onEndTurnButton(Runnable action) { endTurnButton.addActionListener(e -> action.run()); }
    public void onRefundButton(Runnable action) { refundButton.addActionListener(e -> action.run()); }
    public void onSummarizedTurn(Runnable action) { summarizedTurnButton.addActionListener(e -> action.run()); }
    public void onBackFromSummarizedTurn(Runnable action) { backFromSummarizedTurn.addActionListener(e -> action.run()); }
    public void onUpButton(Runnable action) { upButton.addActionListener(e -> action.run()); }
    public void onDownButton(Runnable action) { downButton.addActionListener(e -> action.run()); }

    // -- Enable/disable --
    public void setBackEnabled(boolean e) { backButton.setEnabled(e); }
    public void setPrintEnabled(boolean e) { printButton.setEnabled(e); }
    public void setEndTurnEnabled(boolean e) { endTurnButton.setEnabled(e); }
    public void setRefundEnabled(boolean e) { refundButton.setEnabled(e); }

    // -- Checkboxes --
    public boolean isNoPrintSelected() { return noPrintCheckBox.isSelected(); }
    public boolean isSummarizedPrintSelected() { return summarizedPrintCheckBox.isSelected(); }
    public boolean isDetailedPrintSelected() { return detailedPrintCheckBox.isSelected(); }
    public void setNoPrintSelected(boolean b) { noPrintCheckBox.setSelected(b); }
    public void setSummarizedPrintSelected(boolean b) { summarizedPrintCheckBox.setSelected(b); }
    public void setDetailedPrintSelected(boolean b) { detailedPrintCheckBox.setSelected(b); }
    public void setupPrintCheckboxes() {
        var listener = view.helpers.PrintCheckboxHelper.createPrintCheckboxListener(
                noPrintCheckBox, summarizedPrintCheckBox, detailedPrintCheckBox,
                printButton, endTurnButton);
        noPrintCheckBox.addItemListener(listener);
        summarizedPrintCheckBox.addItemListener(listener);
        detailedPrintCheckBox.addItemListener(listener);
    }
    public void scrollTurnDetailsTable(int direction) {
        view.helpers.TableScroller.scroll(turnDetailsTable, direction);
    }

    // -- Table --
    public int getSelectedDetailRow() { return turnDetailsTable.getSelectedRow(); }
    public void onTurnDetailsSelection(javax.swing.event.ListSelectionListener listener) {
        turnDetailsTable.getSelectionModel().addListSelectionListener(listener);
    }

    // -- Popup --
    public void showSummarizedPopup(boolean visible) { summarizedPopup.setVisible(visible); }
}
