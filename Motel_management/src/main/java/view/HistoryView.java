/*
 * Created by JFormDesigner on Sat Jun 08 21:33:40 COT 2024
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
public class HistoryView extends JPanel {

    /**
     * @return the upButton
     */
    public JButton getUpButton() {
        return upButton;
    }

    /**
     * @return the downButton
     */
    public JButton getDownButton() {
        return downButton;
    }

    /**
     * @return the turnHistoryTable
     */
    public JTable getTurnHistoryTable() {
        return turnHistoryTable;
    }

    private JTable turnHistoryTable;
    private TunHistoryTableModel turnHistoryTableModel;

    private final Font cellFont;

    public HistoryView() {
        this.cellFont = new Font("Segoe UI", Font.BOLD, 16);
        initComponents();
        initCustomTable();
        initCustomComponents();
    }

    public void setTurnHistoryDetails(JSONArray historyList) {
        turnHistoryTableModel.updateData(historyList);
        turnHistoryTable.repaint();
    }

    private void initCustomTable() {
        turnHistoryTableModel = new TunHistoryTableModel();
        turnHistoryTable = new JTable(turnHistoryTableModel);
        TableColumnModel columnModel = turnHistoryTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(new CustomCellRenderer(cellFont));
            columnModel.getColumn(i).setHeaderRenderer(new CustomHeaderRenderer(cellFont));
        }
        turnHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(turnHistoryTable);
        turnHistoryTable.getTableHeader().setReorderingAllowed(false);
        
        turnSelectionPanel.add(scrollPane, "cell 0 0, grow");
    }

    private void initCustomComponents() {
        popupTurn.setSize(1024, 768);
        popupTurn.setExtendedState(JFrame.MAXIMIZED_BOTH);
        popupTurn.setResizable(false);
    }

    public class TunHistoryTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Inicio", "Fin", "Num Turno"};
        private ArrayList<JSONObject> filteredTurnHistory;

        public TunHistoryTableModel() {
            this.filteredTurnHistory = new ArrayList<>();
        }

        public void updateData(JSONArray data) {
            filteredTurnHistory.clear();
            for (int i = 0; i < data.length(); i++) {
                try {
                    JSONObject item = data.getJSONObject(i);
                    filteredTurnHistory.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return filteredTurnHistory.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                JSONObject item = filteredTurnHistory.get(rowIndex);
                ZonedDateTime turnStart = ZonedDateTime.parse(item.getString("turnStart"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
                String formattedDate = turnStart.format(formatter);

                switch (columnIndex) {
                    case 0: // Inicio
                        return formattedDate;
                    case 1: // Fin
                        // Assuming there is a turnEnd in the JSON, if not, this needs to be adjusted
                        ZonedDateTime turnEnd = ZonedDateTime.parse(item.getString("turnEnd"));
                        return turnEnd.format(formatter);
                    case 2: // Num Turno
                        return item.getInt("turnNumber");
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
            if (columnIndex == 2) {
                return Integer.class;
            } else {
                return String.class;
            }
        }
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	turnSelectionPanel = new JPanel();
	turnDateInformativeLabel = new JLabel();
	turnDateLabel = new JLabel();
	startDateInformativeLabel = new JLabel();
	turnStartLabel = new JLabel();
	turnEndInformativeLabel = new JLabel();
	turnEndLabel = new JLabel();
	durationInformativeLabel = new JLabel();
	durationLabel = new JLabel();
	backButton = new JButton();
	timeLabel = new JLabel();
	upButton = new JButton();
	downButton = new JButton();
	turnDetailsButton = new JButton();
	dateLabel = new JLabel();
	popupTurn = new JFrame();
	turnDetailsView = new TurnHistoryManagerView();

	//======== this ========
	setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[fill]" +
	    "[0,grow,shrink 0,fill]" +
	    "[grow,fill]" +
	    "[grow,fill]" +
	    "[183,fill]",
	    // rows
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[grow]" +
	    "[16:n,grow]" +
	    "[32]" +
	    "[grow]"));

	//======== turnSelectionPanel ========
	{
	    turnSelectionPanel.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[grow]"));
	}
	add(turnSelectionPanel, "cell 0 0 5 8,grow");

	//---- turnDateInformativeLabel ----
	turnDateInformativeLabel.setText("FECHA:");
	turnDateInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(turnDateInformativeLabel, "cell 5 0");

	//---- turnDateLabel ----
	turnDateLabel.setText("XXXX-XX-XX ");
	turnDateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(turnDateLabel, "cell 5 1");

	//---- startDateInformativeLabel ----
	startDateInformativeLabel.setText("INICIO");
	startDateInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(startDateInformativeLabel, "cell 5 2");

	//---- turnStartLabel ----
	turnStartLabel.setText("XXXX-XX-XX - XX:XX AM/PM");
	turnStartLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(turnStartLabel, "cell 5 3");

	//---- turnEndInformativeLabel ----
	turnEndInformativeLabel.setText("FINAL");
	turnEndInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(turnEndInformativeLabel, "cell 5 4");

	//---- turnEndLabel ----
	turnEndLabel.setText("XXXX-XX-XX - XX:XX AM/PM");
	turnEndLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(turnEndLabel, "cell 5 5");

	//---- durationInformativeLabel ----
	durationInformativeLabel.setText("DURACION");
	durationInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(durationInformativeLabel, "cell 5 6");

	//---- durationLabel ----
	durationLabel.setText("N");
	durationLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(durationLabel, "cell 5 7");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(backButton, "cell 0 8 1 2,growy");

	//---- timeLabel ----
	timeLabel.setText("time");
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 26));
	add(timeLabel, "cell 1 8 2 1");

	//---- upButton ----
	upButton.setIcon(new ImageIcon(getClass().getResource("/up.png")));
	add(upButton, "cell 3 8,growy");

	//---- downButton ----
	downButton.setIcon(new ImageIcon(getClass().getResource("/down.png")));
	add(downButton, "cell 4 8,growy");

	//---- turnDetailsButton ----
	turnDetailsButton.setText("DETALLES");
	turnDetailsButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(turnDetailsButton, "cell 5 8 1 2,growy");

	//---- dateLabel ----
	dateLabel.setText("date");
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(dateLabel, "cell 1 9 2 1");

	//======== popupTurn ========
	{
	    popupTurn.setAlwaysOnTop(true);
	    Container popupTurnContentPane = popupTurn.getContentPane();
	    popupTurnContentPane.setLayout(new MigLayout(
		"fill,hidemode 3",
		// columns
		"[grow,fill]",
		// rows
		"[grow]"));
	    popupTurnContentPane.add(turnDetailsView, "cell 0 0,grow");
	    popupTurn.pack();
	    popupTurn.setLocationRelativeTo(popupTurn.getOwner());
	}
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel turnSelectionPanel;
    private JLabel turnDateInformativeLabel;
    private JLabel turnDateLabel;
    private JLabel startDateInformativeLabel;
    private JLabel turnStartLabel;
    private JLabel turnEndInformativeLabel;
    private JLabel turnEndLabel;
    private JLabel durationInformativeLabel;
    private JLabel durationLabel;
    private JButton backButton;
    private JLabel timeLabel;
    private JButton upButton;
    private JButton downButton;
    private JButton turnDetailsButton;
    private JLabel dateLabel;
    private JFrame popupTurn;
    private TurnHistoryManagerView turnDetailsView;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the turnSelectionPanel
     */
    public JPanel getTurnSelectionPanel() {
        return turnSelectionPanel;
    }

    /**
     * @return the turnDateInformativeLabel
     */
    public JLabel getTurnDateInformativeLabel() {
        return turnDateInformativeLabel;
    }

    /**
     * @return the turnDateLabel
     */
    public JLabel getTurnDateLabel() {
        return turnDateLabel;
    }

    /**
     * @return the startDateInformativeLabel
     */
    public JLabel getStartDateInformativeLabel() {
        return startDateInformativeLabel;
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
     * @return the durationInformativeLabel
     */
    public JLabel getDurationInformativeLabel() {
        return durationInformativeLabel;
    }

    /**
     * @return the durationLabel
     */
    public JLabel getDurationLabel() {
        return durationLabel;
    }

    /**
     * @return the backButton
     */
    public JButton getBackButton() {
        return backButton;
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
     * @return the turnDetailsButton
     */
    public JButton getTurnDetailsButton() {
        return turnDetailsButton;
    }

    /**
     * @return the popupTurn
     */
    public JFrame getPopupTurn() {
        return popupTurn;
    }

    /**
     * @return the turnDetailsView
     */
    public TurnHistoryManagerView getTurnDetailsView() {
        return turnDetailsView;
    }
}
