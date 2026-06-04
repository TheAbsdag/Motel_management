package view.helpers;

import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import view.customListRenderes.CustomCellRenderer;
import view.customListRenderes.CustomHeaderRenderer;

/**
 * Standard table initialization utility.
 * Consolidates the repeated SINGLE_SELECTION + custom renderers + header setup
 * + reordering disable + TouchScrollHandler pattern used across the view layer.
 */
public final class TableSetupHelper {

    private TableSetupHelper() { }

    /**
     * Configures a table with standard MOTEL look and feel:
     * single-selection mode, custom cell/header renderers, reordering disabled.
     *
     * @param table   the table to configure
     * @param cellFont font for cell and header renderers
     */
    public static void configure(JTable table, Font cellFont) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(new CustomCellRenderer(cellFont));
            columnModel.getColumn(i).setHeaderRenderer(new CustomHeaderRenderer(cellFont));
        }
        table.getTableHeader().setReorderingAllowed(false);
    }

    /**
     * Wraps the table in a scroll pane and attaches TouchScrollHandler.
     *
     * @param table the table to wrap
     * @return the configured JScrollPane
     */
    public static JScrollPane wrapInScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        TouchScrollHandler.attach(scrollPane);
        return scrollPane;
    }
}