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

        // Bump the table's default row height so newly-exposed rows
        // enter at a reasonable height even during scroll, when
        // per-row setRowHeight is suppressed.
        if (table.getRowHeight() < prefH) {
            table.setRowHeight(prefH);
        }

        // Skip per-row row-height changes during touch-drag scrolling.
        // setRowHeight(row) triggers resizeAndRepaint() which revalidates
        // the scroll-pane layout and shifts the viewport position,
        // fighting the user's finger and causing choppiness.
        if (scrolling) {
            return;
        }
        if (currentRowHeight < prefH) {
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
}
