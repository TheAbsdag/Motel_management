/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view.customListRenderes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Santiago
 */
public class CustomHeaderRenderer extends JLabel implements TableCellRenderer{

        private final Font headerFont;

        public CustomHeaderRenderer(Font font) {
            headerFont = font;
            setOpaque(true);
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            setFont(headerFont);
            setBackground(Color.LIGHT_GRAY); // Customize header background if needed
            return this;
        }
    }
