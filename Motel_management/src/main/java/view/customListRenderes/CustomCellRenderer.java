package view.customListRenderes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

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

        // Apply background color based on change type
        Object changeType = null;
        if(table.getColumnCount()>2){
            changeType = table.getValueAt(row, 2);
        }
        
        if (changeType != null && changeType.toString().contains("Alquiler")) {
            setBackground(Color.CYAN);
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
        // Adjust row height for wrapping text
        if(column ==1){
            adjustRowHeight(table, row);
        }
        

        return this;
    }

    private void adjustRowHeight(JTable table, int row) {
        int maxRowHeight = 0;
        for (int column = 0; column < table.getColumnCount(); column++) {
            int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
            setSize(new Dimension(cWidth, Short.MAX_VALUE));
            int prefH = getPreferredSize().height;
            maxRowHeight = Math.max(maxRowHeight, prefH);
        }
        if (table.getRowHeight(row) != maxRowHeight) {
            table.setRowHeight(row, maxRowHeight);
        }
    }
}
