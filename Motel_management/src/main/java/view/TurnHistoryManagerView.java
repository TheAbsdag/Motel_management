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
import model.dto.TurnHistoryData;
import view.customListRenderes.CustomCellRenderer;
import view.customListRenderes.CustomHeaderRenderer;

/**
 * @author Santiago
 */
public class TurnHistoryManagerView extends JPanel {

    private JTable turnDetailsTable;
    private TurnDetailsTableModel turnDetailsTableModel;
    private final Font cellFont;
    private final NumberFormat numberFormat;

    public TurnHistoryManagerView() {
        numberFormat = NumberFormat.getNumberInstance(Locale.US);
        this.cellFont = new Font("Segoe UI", Font.BOLD, 16);
        initComponents();
        initCustomTable();
    }

    public void setTurnDetailsData(TurnHistoryData turnDetails) {
        turnDetailsTableModel.updateData(turnDetails.getActivities());
        turnStartLabel.setText(turnDetails.getStartString());
        turnEndLabel.setText(turnDetails.getEndString());
        totalRoomsLabel.setText(numberFormat.format(turnDetails.getTotalRooms()));
        totalItemsLabel.setText(numberFormat.format(turnDetails.getTotalItems()));
        totalSalesLabel.setText(numberFormat.format(turnDetails.getTotalSales()));
        turnNumberLabel.setText(String.valueOf(turnDetails.getTurnNumber()));
        totalRefundLabel.setText(numberFormat.format(turnDetails.getTotalRefunds()));
        totalSpendingLabel.setText(numberFormat.format(turnDetails.getTotalSpending()));
        totalTurnLabel.setText(numberFormat.format(turnDetails.getTotalTurn()));
        totalTransferLabel.setText(numberFormat.format(turnDetails.getTotalBankTransfers()));
        totalDepositLabel.setText(numberFormat.format(turnDetails.getTotalDeposits()));
        totalNetLabel.setText(numberFormat.format(turnDetails.getTotalNet()));
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
        JScrollPane scrollPane = new JScrollPane(turnDetailsTable);
        turnDetailsTable.getTableHeader().setReorderingAllowed(false);

        turnDetailsPanel.add(scrollPane, "cell 0 0, grow");
        TouchScrollHandler.attach(scrollPane);
    }

    private class TurnDetailsTableModel extends AbstractTableModel {

        private final String[] columnNames = {"#", "Habitacion", "Tiempo", "Accion", "Valor"};
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
	totalRoomsLabel.setText("text");
	totalRoomsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRoomsLabel, "cell 6 3");

	//---- totalItemsInformativeLabel ----
	totalItemsInformativeLabel.setText("PRODUCTOS");
	totalItemsInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalItemsInformativeLabel, "cell 5 4");

	//---- totalItemsLabel ----
	totalItemsLabel.setText("text");
	totalItemsLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalItemsLabel, "cell 6 4");

	//---- totalSalesInformativeLabel ----
	totalSalesInformativeLabel.setText("PRODUCIDO");
	totalSalesInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesInformativeLabel, "cell 5 5");

	//---- totalSalesLabel ----
	totalSalesLabel.setText("text");
	totalSalesLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSalesLabel, "cell 6 5");

	//---- totalRefundInformativeLabel ----
	totalRefundInformativeLabel.setText("REEMBOLSO");
	totalRefundInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRefundInformativeLabel, "cell 5 6");

	//---- totalRefundLabel ----
	totalRefundLabel.setText("text");
	totalRefundLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalRefundLabel, "cell 6 6");

	//---- totalSpendingInformativeLabel ----
	totalSpendingInformativeLabel.setText("GASTOS");
	totalSpendingInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSpendingInformativeLabel, "cell 5 7");

	//---- totalSpendingLabel ----
	totalSpendingLabel.setText("text");
	totalSpendingLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalSpendingLabel, "cell 6 7");

	//---- totalTurnInformativeLabel ----
	totalTurnInformativeLabel.setText("TOTAL TURNO");
	totalTurnInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTurnInformativeLabel, "cell 5 8");

	//---- totalTurnLabel ----
	totalTurnLabel.setText("text");
	totalTurnLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTurnLabel, "cell 6 8");

	//---- totalTransferInformativeLabel ----
	totalTransferInformativeLabel.setText("TOTAL TRANSFERENCIA");
	totalTransferInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTransferInformativeLabel, "cell 5 9");

	//---- totalTransferLabel ----
	totalTransferLabel.setText("text");
	totalTransferLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalTransferLabel, "cell 6 9");

	//---- totalDepositInformativeLabel ----
	totalDepositInformativeLabel.setText("TOTAL DEPOSITO");
	totalDepositInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalDepositInformativeLabel, "cell 5 10");

	//---- totalDepositLabel ----
	totalDepositLabel.setText("text");
	totalDepositLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalDepositLabel, "cell 6 10");

	//---- totalNetInformativeLabel ----
	totalNetInformativeLabel.setText("TOTAL NETO");
	totalNetInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(totalNetInformativeLabel, "cell 5 11");

	//---- totalNetLabel ----
	totalNetLabel.setText("text");
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

    /**
     * @return the turnDetailsPanel
     */
    public JPanel getTurnDetailsPanel() {
        return turnDetailsPanel;
    }

    /**
     * @return the turnNumberInformativeLabel
     */
    public JLabel getTurnNumberInformativeLabel() {
        return turnNumberInformativeLabel;
    }

    /**
     * @return the turnNumberLabel
     */
    public JLabel getTurnNumberLabel() {
        return turnNumberLabel;
    }

    /**
     * @return the turnStartInformativeLabel
     */
    public JLabel getTurnStartInformativeLabel() {
        return turnStartInformativeLabel;
    }

    /**
     * @return the turnStartLabel
     */
    public JLabel getTurnStartLabel() {
        return turnStartLabel;
    }

    /**
     * @return the turnEndInformativeLabel
     */
    public JLabel getTurnEndInformativeLabel() {
        return turnEndInformativeLabel;
    }

    /**
     * @return the turnEndLabel
     */
    public JLabel getTurnEndLabel() {
        return turnEndLabel;
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
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }

    /**
     * @return the printButton
     */
    public JButton getPrintButton() {
        return printButton;
    }

    /**
     * @return the totalRoomsInformativeLabel
     */
    public JLabel getTotalRoomsInformativeLabel() {
        return totalRoomsInformativeLabel;
    }

    /**
     * @return the totalRoomsLabel
     */
    public JLabel getTotalRoomsLabel() {
        return totalRoomsLabel;
    }

    /**
     * @return the totalItemsInformativeLabel
     */
    public JLabel getTotalItemsInformativeLabel() {
        return totalItemsInformativeLabel;
    }

    /**
     * @return the totalItemsLabel
     */
    public JLabel getTotalItemsLabel() {
        return totalItemsLabel;
    }

    /**
     * @return the totalSalesInformativeLabel
     */
    public JLabel getTotalSalesInformativeLabel() {
        return totalSalesInformativeLabel;
    }

    /**
     * @return the totalSalesLabel
     */
    public JLabel getTotalSalesLabel() {
        return totalSalesLabel;
    }

    /**
     * @return the totalRefundInformativeLabel
     */
    public JLabel getTotalRefundInformativeLabel() {
        return totalRefundInformativeLabel;
    }

    /**
     * @return the totalRefundLabel
     */
    public JLabel getTotalRefundLabel() {
        return totalRefundLabel;
    }

    /**
     * @return the totalSpendingInformativeLabel
     */
    public JLabel getTotalSpendingInformativeLabel() {
        return totalSpendingInformativeLabel;
    }

    /**
     * @return the totalSpendingLabel
     */
    public JLabel getTotalSpendingLabel() {
        return totalSpendingLabel;
    }

    /**
     * @return the totalTurnInformativeLabel
     */
    public JLabel getTotalTurnInformativeLabel() {
        return totalTurnInformativeLabel;
    }

    /**
     * @return the totalTurnLabel
     */
    public JLabel getTotalTurnLabel() {
        return totalTurnLabel;
    }

    /**
     * @return the totalTransferInformativeLabel
     */
    public JLabel getTotalTransferInformativeLabel() {
        return totalTransferInformativeLabel;
    }

    /**
     * @return the totalTransferLabel
     */
    public JLabel getTotalTransferLabel() {
        return totalTransferLabel;
    }

    /**
     * @return the totalDepositInformativeLabel
     */
    public JLabel getTotalDepositInformativeLabel() {
        return totalDepositInformativeLabel;
    }

    /**
     * @return the totalDepositLabel
     */
    public JLabel getTotalDepositLabel() {
        return totalDepositLabel;
    }

    /**
     * @return the totalNetInformativeLabel
     */
    public JLabel getTotalNetInformativeLabel() {
        return totalNetInformativeLabel;
    }

    /**
     * @return the totalNetLabel
     */
    public JLabel getTotalNetLabel() {
        return totalNetLabel;
    }

}
