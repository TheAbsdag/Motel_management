package view.customListRenderes;

import view.EmailCaseConfigurationView.CheckableItem;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer<CheckableItem> {

    public CheckboxListCellRenderer() {
	setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(
	    JList<? extends CheckableItem> list,
	    CheckableItem value,
	    int index,
	    boolean isSelected,
	    boolean cellHasFocus) {

	setSelected(value != null && value.isSelected());
	setText(value != null ? value.toString() : "");
	setFont(list.getFont());

	if (isSelected) {
	    setBackground(list.getSelectionBackground());
	    setForeground(list.getSelectionForeground());
	} else {
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	}

	setEnabled(list.isEnabled());
	return this;
    }
}
