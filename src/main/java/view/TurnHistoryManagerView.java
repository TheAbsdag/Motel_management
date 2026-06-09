/*
 * Created by JFormDesigner on Sat Jun 08 11:15:03 COT 2024
 */
package view;

import view.helpers.TouchScrollHandler;
import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import net.miginfocom.swing.*;
import model.dto.TurnActivityData;
import model.dto.TurnHistoryData;
import model.json.CurrencyConfig;
import model.turn.ActivityType;
import model.turn.ExtraChangeType;
import model.turn.RefundType;
import view.customListRenderes.CustomCellRenderer;
import view.customListRenderes.CustomHeaderRenderer;
import view.helpers.CurrencyFormatter;
import view.helpers.TimeFormatter;

/**
 * @author Santiago
 */
public class TurnHistoryManagerView extends JPanel {

    private JTable turnDetailsTable;
    private TurnDetailsTableModel turnDetailsTableModel;
    private final Font cellFont;
    private CurrencyConfig currencyConfig = CurrencyConfig.defaultConfig();

    public void setCurrencyConfig(CurrencyConfig cfg) {
        this.currencyConfig = cfg != null ? cfg : CurrencyConfig.defaultConfig();
    }

    public TurnHistoryManagerView() {
        this.cellFont = new Font("Segoe UI", Font.BOLD, 16);
        initComponents();
        initCustomTable();
    }

    public void setTurnDetailsData(TurnHistoryData turnDetails) {
        turnDetailsTableModel.updateData(turnDetails.getActivities());
        turnStartLabel.setText(turnDetails.getStartString());
        turnEndLabel.setText(turnDetails.getEndString());
        totalRoomsLabel.setText(CurrencyFormatter.format(turnDetails.getTotalRooms(), currencyConfig));
        totalItemsLabel.setText(CurrencyFormatter.format(turnDetails.getTotalItems(), currencyConfig));
        totalSalesLabel.setText(CurrencyFormatter.format(turnDetails.getTotalSales(), currencyConfig));
        turnNumberLabel.setText(String.valueOf(turnDetails.getTurnNumber()));
        totalRefundLabel.setText(CurrencyFormatter.format(turnDetails.getTotalRefunds(), currencyConfig));
        totalSpendingLabel.setText(CurrencyFormatter.format(turnDetails.getTotalSpending(), currencyConfig));
        totalTurnLabel.setText(CurrencyFormatter.format(turnDetails.getTotalTurn(), currencyConfig));
        totalTransferLabel.setText(CurrencyFormatter.format(turnDetails.getTotalBankTransfers(), currencyConfig));
        totalDepositLabel.setText(CurrencyFormatter.format(turnDetails.getTotalDeposits(), currencyConfig));
        totalNetLabel.setText(CurrencyFormatter.format(turnDetails.getTotalNet(), currencyConfig));
        turnDetailsTable.repaint();
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
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

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	turnDetailsPanel = new JPanel();
	turnNumberInformativeLabel = new JLabel();
	turnNumberLabel = new JLabel();
	turnStartInformativeLabel = new JLabel();
	turnStartLabel = new JLabel();
	turnEndInformativeLabel = new JLabel();
	turnEndLabel = new JLabel();
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

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[grow,fill]" +
	    "[grow,shrink 0,fill]" +
	    "[grow,shrink 0,fill]" +
	    "[grow,fill]" +
	    "[grow,shrink 0,fill]" +
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[grow,shrink 0]" +
	    "[grow,shrink 0]" +
	    "[grow,shrink 0]" +
	    "[grow,shrink 0]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow,shrink 0]" +
	    "[grow,shrink 0]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[grow,shrink 0]" +
	    "[grow,shrink 0]" +
	    "[63,grow,shrink 0]" +
	    "[grow,shrink 0]"));

	//======== turnDetailsPanel ========
	{
	    turnDetailsPanel.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[grow,fill]"));
	}
	add(turnDetailsPanel, "cell 0 0 5 15,growy");

	//---- turnNumberInformativeLabel ----
	turnNumberInformativeLabel.setText("TURNO:");
	turnNumberInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(turnNumberInformativeLabel, "cell 5 0");

	//---- turnNumberLabel ----
	turnNumberLabel.setText("N");
	turnNumberLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnNumberLabel, "cell 6 0");

	//---- turnStartInformativeLabel ----
	turnStartInformativeLabel.setText("Inicio:");
	turnStartInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnStartInformativeLabel, "cell 5 1");

	//---- turnStartLabel ----
	turnStartLabel.setText("XXXX-XX-XX - XX:XX");
	turnStartLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnStartLabel, "cell 6 1");

	//---- turnEndInformativeLabel ----
	turnEndInformativeLabel.setText("Final");
	turnEndInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnEndInformativeLabel, "cell 5 2");

	//---- turnEndLabel ----
	turnEndLabel.setText("XXXX-XX-XX - XX:XX");
	turnEndLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(turnEndLabel, "cell 6 2");

	//---- totalRoomsInformativeLabel ----
	totalRoomsInformativeLabel.setText("HABITACIONES:");
	totalRoomsInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRoomsInformativeLabel, "cell 5 3");

	//---- totalRoomsLabel ----
	totalRoomsLabel.setText("0");
	totalRoomsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRoomsLabel, "cell 6 3");

	//---- totalItemsInformativeLabel ----
	totalItemsInformativeLabel.setText("PRODUCTOS");
	totalItemsInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalItemsInformativeLabel, "cell 5 4");

	//---- totalItemsLabel ----
	totalItemsLabel.setText("0");
	totalItemsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalItemsLabel, "cell 6 4");

	//---- totalSalesInformativeLabel ----
	totalSalesInformativeLabel.setText("PRODUCIDO");
	totalSalesInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesInformativeLabel, "cell 5 5");

	//---- totalSalesLabel ----
	totalSalesLabel.setText("0");
	totalSalesLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesLabel, "cell 6 5");

	//---- totalRefundInformativeLabel ----
	totalRefundInformativeLabel.setText("REEMBOLSO");
	totalRefundInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRefundInformativeLabel, "cell 5 6");

	//---- totalRefundLabel ----
	totalRefundLabel.setText("0");
	totalRefundLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRefundLabel, "cell 6 6");

	//---- totalSpendingInformativeLabel ----
	totalSpendingInformativeLabel.setText("GASTOS");
	totalSpendingInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSpendingInformativeLabel, "cell 5 7");

	//---- totalSpendingLabel ----
	totalSpendingLabel.setText("0");
	totalSpendingLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSpendingLabel, "cell 6 7");

	//---- totalTurnInformativeLabel ----
	totalTurnInformativeLabel.setText("TOTAL TURNO");
	totalTurnInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTurnInformativeLabel, "cell 5 8");

	//---- totalTurnLabel ----
	totalTurnLabel.setText("0");
	totalTurnLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTurnLabel, "cell 6 8");

	//---- totalTransferInformativeLabel ----
	totalTransferInformativeLabel.setText("TOTAL TRANSFERENCIA");
	totalTransferInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTransferInformativeLabel, "cell 5 9");

	//---- totalTransferLabel ----
	totalTransferLabel.setText("0");
	totalTransferLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTransferLabel, "cell 6 9");

	//---- totalDepositInformativeLabel ----
	totalDepositInformativeLabel.setText("TOTAL DEPOSITO");
	totalDepositInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalDepositInformativeLabel, "cell 5 10");

	//---- totalDepositLabel ----
	totalDepositLabel.setText("0");
	totalDepositLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalDepositLabel, "cell 6 10");

	//---- totalNetInformativeLabel ----
	totalNetInformativeLabel.setText("TOTAL NETO");
	totalNetInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalNetInformativeLabel, "cell 5 11");

	//---- totalNetLabel ----
	totalNetLabel.setText("0");
	totalNetLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalNetLabel, "cell 6 11");

	//---- noPrintCheckBox ----
	noPrintCheckBox.setText("NO IMPRIMIR");
	noPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(noPrintCheckBox, "cell 5 12 2 1,growy");

	//---- summarizedPrintCheckBox ----
	summarizedPrintCheckBox.setText("RESUMIDO");
	summarizedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(summarizedPrintCheckBox, "cell 5 13 2 1,growy");

	//---- detailedPrintCheckBox ----
	detailedPrintCheckBox.setText("DETALLADO");
	detailedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(detailedPrintCheckBox, "cell 5 14 2 1,growy");

	//---- backButton ----
	backButton.setText("CERRAR");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(backButton, "cell 0 15,growy");

	//---- printButton ----
	printButton.setText("IMPRIMIR");
	printButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(printButton, "cell 5 15 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel turnDetailsPanel;
    private JLabel turnNumberInformativeLabel;
    private JLabel turnNumberLabel;
    private JLabel turnStartInformativeLabel;
    private JLabel turnStartLabel;
    private JLabel turnEndInformativeLabel;
    private JLabel turnEndLabel;
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
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }
    public void onPrintButton(Runnable action) { printButton.addActionListener(e -> action.run()); }

    public void setPrintEnabled(boolean e) { printButton.setEnabled(e); }

    public boolean isNoPrintSelected() { return noPrintCheckBox.isSelected(); }
    public boolean isSummarizedPrintSelected() { return summarizedPrintCheckBox.isSelected(); }
    public boolean isDetailedPrintSelected() { return detailedPrintCheckBox.isSelected(); }
    public void setNoPrintSelected(boolean b) { noPrintCheckBox.setSelected(b); }
    public void setSummarizedPrintSelected(boolean b) { summarizedPrintCheckBox.setSelected(b); }
    public void setDetailedPrintSelected(boolean b) { detailedPrintCheckBox.setSelected(b); }
    public void setupPrintCheckboxes() {
        var listener = view.helpers.PrintCheckboxHelper.createPrintCheckboxListener(
                noPrintCheckBox, summarizedPrintCheckBox, detailedPrintCheckBox,
                printButton, null);
        noPrintCheckBox.addItemListener(listener);
        summarizedPrintCheckBox.addItemListener(listener);
        detailedPrintCheckBox.addItemListener(listener);
    }
}
