package view.customListRenderes;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 * This is exclusive for the JList management custom renderes, due that are used multiple times through the program
 * @author Santiago
 */
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
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        adjustRowHeight(table, row, column);
        return this;
    }

    private void adjustRowHeight(JTable table, int row, int column) {
        int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
        setSize(new Dimension(cWidth, 1000));
        int prefH = getPreferredSize().height;
        table.setRowHeight(row, prefH);
    }

}
