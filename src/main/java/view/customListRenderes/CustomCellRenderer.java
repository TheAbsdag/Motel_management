package view.customListRenderes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import view.helpers.TouchScrollHandler;

public class CustomCellRenderer extends JTextArea implements TableCellRenderer {

    private final Font cellFont;

    public CustomCellRenderer(Font font) {
        cellFont = font;
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        setFont(cellFont);

        // Determine background color from the "Accion" (column 3) and "Valor" (column 4) columns
        Object changeType = null;
        long changeValue = 0L;

        if (table.getColumnCount() > 3) {
            changeType = table.getValueAt(row, 3);
        }

        if (table.getColumnCount() > 4) {
            try {
                changeValue = Long.parseLong(table.getValueAt(row, 4).toString());
            } catch (NumberFormatException ex) {
                changeValue = 0L;
            }
        }

        if (changeType != null && changeType.toString().contains("Alquiler")) {
            // Room booking rows: alternating blue/cyan
            if (row % 2 == 0) {
                setBackground(new Color(120, 207, 214));
            } else {
                setBackground(new Color(105, 235, 245));
            }
        } else if (changeValue < 0L) {
            // Negative-value rows (refunds, spending, extra changes): alternating red
            if (row % 2 == 0) {
                setBackground(new Color(227, 136, 136));
            } else {
                setBackground(new Color(228, 107, 107));
            }
        } else {
            setBackground(table.getBackground());
        }

        // Handle selection colors
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setForeground(table.getForeground());
        }
        // Adjust row height for wrapping text in this cell only.
        // Each column's renderer call adjusts for its own content;
        // across all columns the row ends up at the max height.
        adjustRowHeight(table, row, column);

        return this;
    }

    private void adjustRowHeight(JTable table, int row, int column) {
        boolean scrolling = TouchScrollHandler.isScrolling();
        int currentRowHeight = table.getRowHeight(row);
        int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
        setSize(new Dimension(cWidth, Short.MAX_VALUE));
        int prefH = getPreferredSize().height;

        if (currentRowHeight >= prefH) {
            return;
        }

        if (scrolling) {
            // During touch-drag scrolling avoid deferred setRowHeight which
            // triggers resizeAndRepaint and fights the user's finger.
            // Instead set the per-row height inline for rows that are still
            // at the default (uncustomized) height.  This fires at most once
            // per row so the repaint cost is bounded.
            table.setRowHeight(row, prefH);
            return;
        }

        final int r = row;
        final int h = prefH;
        final JTable t = table;
        SwingUtilities.invokeLater(() -> {
            if (t.getRowHeight(r) < h) {
                t.setRowHeight(r, h);
            }
        });
    }
}
