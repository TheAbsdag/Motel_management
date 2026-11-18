package view;

import view.helpers.TableScroller;
import view.helpers.TouchScrollHandler;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import net.miginfocom.swing.*;
import model.dto.TurnInventoryRowData;
import view.customListRenderes.CustomCellRenderer;
import view.customListRenderes.CustomHeaderRenderer;
import view.interfaces.TimeLabelInterface;

/**
 * Inventory movement summary of the active turn: what each product received, what
 * came in, what was sold, what was given as courtesy and what is left.
 */
public class TurnInventoryView extends JPanel implements TimeLabelInterface {

    private final Font cellFont;
    private JTable inventoryTable;
    private TurnInventoryTableModel tableModel;

    public TurnInventoryView() {
        this.cellFont = new Font("Segoe UI", Font.BOLD, 16);
        initComponents();
        initCustomTable();
    }

    /**
     * Replaces the table contents with the rows of the current turn.
     *
     * @param rows movement summary rows, already ordered by product name
     */
    public void updateInventoryRows(List<TurnInventoryRowData> rows) {
        tableModel.updateData(rows);
        inventoryTable.repaint();
    }

    /**
     * Shows the number of the turn whose inventory is displayed.
     *
     * @param turnNumber current turn number
     */
    public void setTurnNumber(long turnNumber) {
        turnLabel.setText("TURNO #" + turnNumber);
    }

    private void initCustomTable() {
        tableModel = new TurnInventoryTableModel();
        inventoryTable = new JTable(tableModel);
        TableColumnModel columnModel = inventoryTable.getColumnModel();
        int[] preferredWidths = {230, 95, 95, 130, 95, 100, 170};
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(new CustomCellRenderer(cellFont));
            columnModel.getColumn(i).setHeaderRenderer(new CustomHeaderRenderer(cellFont));
            columnModel.getColumn(i).setPreferredWidth(preferredWidths[i]);
        }
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        inventoryPanel.add(scrollPane, "cell 0 0, grow");
        TouchScrollHandler.attach(scrollPane);
    }

    private class TurnInventoryTableModel extends AbstractTableModel {

        private final String[] columnNames = {
            "Artículo", "Recibido", "Entrada", "Total Ingresado",
            "Ventas", "Cortesías", "Existencias actuales"
        };
        private List<TurnInventoryRowData> rows = new ArrayList<>();

        void updateData(List<TurnInventoryRowData> data) {
            rows = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TurnInventoryRowData row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.itemName();
                case 1 -> row.received();
                case 2 -> row.increase();
                case 3 -> row.totalIn();
                case 4 -> row.sold();
                case 5 -> row.courtesy();
                case 6 -> row.current();
                default -> null;
            };
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? String.class : Long.class;
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        titleLabel = new JLabel();
        turnLabel = new JLabel();
        timeLabel = new JLabel();
        dateLabel = new JLabel();
        inventoryPanel = new JPanel();
        hintLabel = new JLabel();
        backButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();

        //======== this ========
        setLayout(new MigLayout(
            "fill,hidemode 3",
            // columns
            "[grow,fill]" +
            "[grow,fill]" +
            "[grow,fill]" +
            "[grow,fill]" +
            "[grow,fill]",
            // rows
            "[77]" +
            "[grow]" +
            "[60]"));

        //---- titleLabel ----
        titleLabel.setText("INVENTARIO DEL TURNO");
        titleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 36));
        add(titleLabel, "cell 0 0 2 1");

        //---- turnLabel ----
        turnLabel.setText("TURNO #");
        turnLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        add(turnLabel, "cell 2 0");

        //---- timeLabel ----
        timeLabel.setText("time");
        timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(timeLabel, "cell 3 0");

        //---- dateLabel ----
        dateLabel.setText("date");
        dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(dateLabel, "cell 4 0");

        //======== inventoryPanel ========
        {
            inventoryPanel.setLayout(new MigLayout(
                "fill,hidemode 3",
                // columns
                "[grow,fill]",
                // rows
                "[grow,fill]"));
        }
        add(inventoryPanel, "cell 0 1 5 1,grow");

        //---- backButton ----
        backButton.setText("VOLVER");
        backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        add(backButton, "cell 0 2,growy");

        //---- upButton ----
        upButton.setIcon(new ImageIcon(getClass().getResource("/images/up.png")));
        add(upButton, "cell 1 2,growy");

        //---- downButton ----
        downButton.setIcon(new ImageIcon(getClass().getResource("/images/down.png")));
        add(downButton, "cell 2 2,growy");

        //---- hintLabel ----
        hintLabel.setText("Recibido = existencia al iniciar el turno");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(hintLabel, "cell 3 2 2 1");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JLabel titleLabel;
    private JLabel turnLabel;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JPanel inventoryPanel;
    private JLabel hintLabel;
    private JButton backButton;
    private JButton upButton;
    private JButton downButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    @Override
    public void updateTimeDisplay(String timeText, String dateText) {
        timeLabel.setText(timeText);
        dateLabel.setText(dateText);
    }

    // -- Button listeners --
    public void onBackButton(Runnable action) { backButton.addActionListener(e -> action.run()); }
    public void onUpButton(Runnable action) { upButton.addActionListener(e -> action.run()); }
    public void onDownButton(Runnable action) { downButton.addActionListener(e -> action.run()); }

    // -- Table scrolling --
    public void scrollInventoryTable(int direction) {
        TableScroller.scroll(inventoryTable, direction);
    }
}
