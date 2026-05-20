/*
 * Created by JFormDesigner on Thu May 14 15:18:05 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author SECC
 */
public class FloorConfigurationView extends JPanel {
    private CardLayout cardLayout;
    
    public FloorConfigurationView() {
        initCustomComponentes();
	initComponents();
        
    }
    
    private void initCustomComponentes() {
        cardLayout = new CardLayout();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	floorConfigurationTitleLabel = new JLabel();
	towerSelectedInformativeLabel = new JLabel();
	towerSelectedLabel = new JLabel();
	towerSelectionInformativeLabel = new JLabel();
	deleteTowerButton = new JButton();
	towerListLeftButton = new JButton();
	towerScrollPane = new JScrollPane();
	towerList = new JList();
	towerListRightButton = new JButton();
	newTowerButton = new JButton();
	floorSelectedInformativeLabel = new JLabel();
	floorSelectedLabel = new JLabel();
	deleteFloorButton = new JButton();
	containerPanel = new JPanel();
	floorListUpButton = new JButton();
	floorScrollPane = new JScrollPane();
	floorList = new JList();
	floorListDownButton = new JButton();
	backButton = new JButton();
	newRoomButton = new JButton();
	saveButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "fill,hidemode 3",
	    // columns
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]" +
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- floorConfigurationTitleLabel ----
	floorConfigurationTitleLabel.setText("CONFIGURACION HABITACIONES MOTEL");
	floorConfigurationTitleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(floorConfigurationTitleLabel, "cell 0 0 10 1");

	//---- towerSelectedInformativeLabel ----
	towerSelectedInformativeLabel.setText("TORRE");
	towerSelectedInformativeLabel.setHorizontalAlignment(SwingConstants.LEFT);
	towerSelectedInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(towerSelectedInformativeLabel, "cell 0 1 2 1");

	//---- towerSelectedLabel ----
	towerSelectedLabel.setText("X");
	towerSelectedLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(towerSelectedLabel, "cell 0 1 2 1");

	//---- towerSelectionInformativeLabel ----
	towerSelectionInformativeLabel.setText("SELECCIONE TORRE");
	towerSelectionInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	towerSelectionInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(towerSelectionInformativeLabel, "cell 2 1 8 1");

	//---- deleteTowerButton ----
	deleteTowerButton.setText("ELIMINAR");
	deleteTowerButton.setBackground(new Color(0xff6666));
	deleteTowerButton.setForeground(Color.black);
	deleteTowerButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(deleteTowerButton, "cell 0 2 2 1,growy");
	add(towerListLeftButton, "cell 3 2,growy");

	//======== towerScrollPane ========
	{

	    //---- towerList ----
	    towerList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	    towerList.setVisibleRowCount(1);
	    towerScrollPane.setViewportView(towerList);
	}
	add(towerScrollPane, "cell 4 2 4 1,growy");
	add(towerListRightButton, "cell 8 2,growy");

	//---- newTowerButton ----
	newTowerButton.setText("A\u00d1ADIR TORRE");
	newTowerButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(newTowerButton, "cell 9 2,growy");

	//---- floorSelectedInformativeLabel ----
	floorSelectedInformativeLabel.setText("PISO");
	floorSelectedInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(floorSelectedInformativeLabel, "cell 0 3 2 1");

	//---- floorSelectedLabel ----
	floorSelectedLabel.setText("X");
	floorSelectedLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
	add(floorSelectedLabel, "cell 0 3 2 1");

	//---- deleteFloorButton ----
	deleteFloorButton.setText("ELIMINAR");
	deleteFloorButton.setBackground(new Color(0xff6666));
	deleteFloorButton.setForeground(Color.black);
	deleteFloorButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(deleteFloorButton, "cell 0 4 2 1,growy");

	//======== containerPanel ========
	{
	    containerPanel.setForeground(new Color(0x999999));
	    containerPanel.setBackground(new Color(0x999999));
	    containerPanel.setLayout(null);
	    containerPanel.setLayout(cardLayout);
	}
	add(containerPanel, "cell 2 3 8 7,grow");
	add(floorListUpButton, "cell 0 5,growy");

	//======== floorScrollPane ========
	{

	    //---- floorList ----
	    floorList.setVisibleRowCount(6);
	    floorScrollPane.setViewportView(floorList);
	}
	add(floorScrollPane, "cell 0 6 1 3,growy");
	add(floorListDownButton, "cell 0 9,growy");

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(backButton, "cell 0 10,growy");

	//---- newRoomButton ----
	newRoomButton.setText("NUEVA HABITACION");
	newRoomButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(newRoomButton, "cell 4 10 4 1,growy");

	//---- saveButton ----
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(saveButton, "cell 9 10,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel floorConfigurationTitleLabel;
    private JLabel towerSelectedInformativeLabel;
    private JLabel towerSelectedLabel;
    private JLabel towerSelectionInformativeLabel;
    private JButton deleteTowerButton;
    private JButton towerListLeftButton;
    private JScrollPane towerScrollPane;
    private JList towerList;
    private JButton towerListRightButton;
    private JButton newTowerButton;
    private JLabel floorSelectedInformativeLabel;
    private JLabel floorSelectedLabel;
    private JButton deleteFloorButton;
    private JPanel containerPanel;
    private JButton floorListUpButton;
    private JScrollPane floorScrollPane;
    private JList floorList;
    private JButton floorListDownButton;
    private JButton backButton;
    private JButton newRoomButton;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    public JButton getBackButton() {
        return backButton;
    }

}
