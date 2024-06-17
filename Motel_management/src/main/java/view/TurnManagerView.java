/*
 * Created by JFormDesigner on Sat Jun 08 11:15:03 COT 2024
 */

package view;

import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import net.miginfocom.swing.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import view.customListRenderes.CustomCellRenderer;
import view.customListRenderes.CustomHeaderRenderer;

/**
 * @author Santiago
 */
public class TurnManagerView extends JPanel {
    
    private JTable turnDetailsTable;
    private TurnDetailsTableModel turnDetailsTableModel;
    private final Font cellFont;

    public TurnManagerView(){
        this.cellFont = new Font("Segoe UI", Font.BOLD, 16);
        initComponents();
        initCustomTable();
    }
    
    public void setTurnDetailsData(JSONObject turnDetails){
        try{
            turnDetailsTableModel.updateData(turnDetails.getJSONArray("turnActivity"));
        }catch(JSONException ex){
            System.out.println("No turn for GUI to show");
            this.backButton.setEnabled(true);
        }
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
        JScrollPane scrollPane = new JScrollPane(turnDetailsTable);
        turnDetailsTable.getTableHeader().setReorderingAllowed(false);
        
        turnDetailsPanel.add(scrollPane, "cell 0 0, grow");
    }

    private class TurnDetailsTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Time", "Action", "Value"};
    private ArrayList<JSONObject> filteredTurnDetails;

    public TurnDetailsTableModel() {
        this.filteredTurnDetails = new ArrayList<>();
    }

    public void updateData(JSONArray data) {
        filteredTurnDetails.clear();
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject item = data.getJSONObject(i);
                String changeType = item.getString("changeType");
                if (changeType.equals("sale") || (changeType.equals("room") && item.getInt("roomStatus") == 3)) {
                    filteredTurnDetails.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        try {
            JSONObject item = filteredTurnDetails.get(rowIndex);
            String changeType = item.getString("changeType");
            ZonedDateTime changeDate = ZonedDateTime.parse(item.getString("changeDate"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
            String formattedDate = changeDate.format(formatter);

            switch (columnIndex) {
                case 0: // Time
                    return formattedDate;
                case 1: // Action
                    if (changeType.equals("sale")) {
                        return "Vendidos objetos a: " + item.getString("roomSoldTo");
                    } else if (changeType.equals("room") && item.getInt("roomStatus") == 3) {
                        return "Habitacion " + item.getString("roomString") + " alquilada";
                    } else {
                        return "";
                    }
                case 2: // Value
                    if (changeType.equals("sale")) {
                        JSONArray register = item.getJSONArray("register");
                        int totalValue = 0;
                        for (int i = 0; i < register.length(); i++) {
                            JSONObject registerItem = register.getJSONObject(i);
                            totalValue += registerItem.getInt("price");
                        }
                        return totalValue;
                    } else if (changeType.equals("room") && item.getInt("roomStatus") == 3) {
                        return item.getInt("price");
                    } else {
                        return "";
                    }
                default:
                    return null;
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
}

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	turnDetailsPanel = new JPanel();
	timeLabel = new JLabel();
	dateLabel = new JLabel();
	noPrintCheckBox = new JCheckBox();
	summarizedPrintCheckBox = new JCheckBox();
	detailedPrintCheckBox = new JCheckBox();
	printButton = new JButton();
	backButton = new JButton();
	endTurnButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "hidemode 3",
	    // columns
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]",
	    // rows
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[25]" +
	    "[grow]"));

	//======== turnDetailsPanel ========
	{
	    turnDetailsPanel.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[grow,fill]"));
	}
	add(turnDetailsPanel, "cell 0 0 5 6,growy");

	//---- timeLabel ----
	timeLabel.setText("time");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 36));
	timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(timeLabel, "cell 5 0 2 1,dock center");

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	add(dateLabel, "cell 5 1 2 1,dock center");

	//---- noPrintCheckBox ----
	noPrintCheckBox.setText("NO IMPRIMIR");
	noPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(noPrintCheckBox, "cell 5 2 2 1,growy");

	//---- summarizedPrintCheckBox ----
	summarizedPrintCheckBox.setText("RESUMIDO");
	summarizedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(summarizedPrintCheckBox, "cell 5 3 2 1,growy");

	//---- detailedPrintCheckBox ----
	detailedPrintCheckBox.setText("DETALLADO");
	detailedPrintCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(detailedPrintCheckBox, "cell 5 4 2 1,growy");

	//---- printButton ----
	printButton.setText("IMPRIMIR");
	printButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(printButton, "cell 5 5 2 1,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(backButton, "cell 0 7,growy");

	//---- endTurnButton ----
	endTurnButton.setText("FIN TURNO");
	endTurnButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 28));
	add(endTurnButton, "cell 5 7 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel turnDetailsPanel;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JCheckBox noPrintCheckBox;
    private JCheckBox summarizedPrintCheckBox;
    private JCheckBox detailedPrintCheckBox;
    private JButton printButton;
    private JButton backButton;
    private JButton endTurnButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the turnDetailsPanel
     */
    public JPanel getTurnDetailsPanel() {
        return turnDetailsPanel;
    }

    /**
     * @return the timeLabel
     */
    public JLabel getTimeLabel() {
        return timeLabel;
    }

    /**
     * @return the dateLabel
     */
    public JLabel getDateLabel() {
        return dateLabel;
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
     * @return the printButton
     */
    public JButton getPrintButton() {
        return printButton;
    }
    /**
     * @return the endTurnButton
     */
    public JButton getEndTurnButton() {
        return endTurnButton;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
    }
}
