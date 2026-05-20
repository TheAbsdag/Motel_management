/*
 * Created by JFormDesigner on Wed May 20 09:30:58 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author SECC
 */
public class RoomConfigurationView extends JPanel {
    public RoomConfigurationView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	roomConfigurationTitleLabel = new JLabel();
	roomDataInformativeLabel = new JLabel();
	deleteRoomButton = new JButton();
	towerInformativeLabel = new JLabel();
	towerLabel = new JLabel();
	timeConfigurationInformativeLabel = new JLabel();
	firstTimeConfiguration = new JButton();
	secondTimeConfiguration = new JButton();
	thirdTimeConfiguration = new JButton();
	floorInformativeLabel = new JLabel();
	label8 = new JLabel();
	timeDurationInformativeLAbel = new JLabel();
	timeDurationTextField = new JTextField();
	secondsRadioButton = new JRadioButton();
	priceInformativeLabel = new JLabel();
	priceTextField = new JTextField();
	numberInformativeLabel = new JLabel();
	numberLabel = new JLabel();
	timeReadableInformativeLabel = new JLabel();
	minuteRadioButton = new JRadioButton();
	subtractSmallPriceButton = new JButton();
	addSmallPriceButton = new JButton();
	roomStringInformativeLabel = new JLabel();
	roomStringLabel = new JTextField();
	timeReadableLabel = new JLabel();
	hourRadioButton = new JRadioButton();
	subtractBigPriceButton = new JButton();
	addBigPriceButton = new JButton();
	button1 = new JButton();
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
	    "[fill]",
	    // rows
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]" +
	    "[]"));

	//---- roomConfigurationTitleLabel ----
	roomConfigurationTitleLabel.setText("CONFIGURACION HABITACION");
	roomConfigurationTitleLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(roomConfigurationTitleLabel, "cell 0 0 7 1");

	//---- roomDataInformativeLabel ----
	roomDataInformativeLabel.setText("DATOS HABITACION:");
	roomDataInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(roomDataInformativeLabel, "cell 0 1");

	//---- deleteRoomButton ----
	deleteRoomButton.setText("ELIMINAR HABITACION");
	deleteRoomButton.setBackground(new Color(0xff6666));
	deleteRoomButton.setForeground(Color.black);
	deleteRoomButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(deleteRoomButton, "cell 2 1,growy");

	//---- towerInformativeLabel ----
	towerInformativeLabel.setText("TORRE:");
	towerInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(towerInformativeLabel, "cell 0 2");

	//---- towerLabel ----
	towerLabel.setText("X");
	towerLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(towerLabel, "cell 0 2");

	//---- timeConfigurationInformativeLabel ----
	timeConfigurationInformativeLabel.setText("CONFIGURACION TIEMPOS");
	timeConfigurationInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(timeConfigurationInformativeLabel, "cell 2 2");

	//---- firstTimeConfiguration ----
	firstTimeConfiguration.setText("x");
	firstTimeConfiguration.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(firstTimeConfiguration, "cell 3 2,growy");

	//---- secondTimeConfiguration ----
	secondTimeConfiguration.setText("x");
	secondTimeConfiguration.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(secondTimeConfiguration, "cell 4 2,growy");

	//---- thirdTimeConfiguration ----
	thirdTimeConfiguration.setText("x");
	thirdTimeConfiguration.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(thirdTimeConfiguration, "cell 5 2,growy");

	//---- floorInformativeLabel ----
	floorInformativeLabel.setText("PISO:");
	floorInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(floorInformativeLabel, "cell 0 3");

	//---- label8 ----
	label8.setText("X");
	label8.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(label8, "cell 0 3");

	//---- timeDurationInformativeLAbel ----
	timeDurationInformativeLAbel.setText("DURACION");
	timeDurationInformativeLAbel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(timeDurationInformativeLAbel, "cell 2 3");
	add(timeDurationTextField, "cell 2 3");

	//---- secondsRadioButton ----
	secondsRadioButton.setText("SEGUNDOS");
	secondsRadioButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(secondsRadioButton, "cell 3 3");

	//---- priceInformativeLabel ----
	priceInformativeLabel.setText("VALOR DURACION");
	priceInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(priceInformativeLabel, "cell 4 3");
	add(priceTextField, "cell 5 3,growy");

	//---- numberInformativeLabel ----
	numberInformativeLabel.setText("NUMERO:");
	numberInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(numberInformativeLabel, "cell 0 4");

	//---- numberLabel ----
	numberLabel.setText("X");
	numberLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(numberLabel, "cell 0 4");

	//---- timeReadableInformativeLabel ----
	timeReadableInformativeLabel.setText("DURACION ACTUAL ES:");
	timeReadableInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(timeReadableInformativeLabel, "cell 2 4");

	//---- minuteRadioButton ----
	minuteRadioButton.setText("MINUTOS");
	minuteRadioButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(minuteRadioButton, "cell 3 4");

	//---- subtractSmallPriceButton ----
	subtractSmallPriceButton.setText("-100");
	subtractSmallPriceButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(subtractSmallPriceButton, "cell 5 4,growy");

	//---- addSmallPriceButton ----
	addSmallPriceButton.setText("+100");
	addSmallPriceButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(addSmallPriceButton, "cell 6 4,growy");

	//---- roomStringInformativeLabel ----
	roomStringInformativeLabel.setText("NOMBRE:");
	roomStringInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(roomStringInformativeLabel, "cell 0 5");

	//---- roomStringLabel ----
	roomStringLabel.setText("X-XXX");
	roomStringLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(roomStringLabel, "cell 0 5,growy");

	//---- timeReadableLabel ----
	timeReadableLabel.setText("X  SEGUNDOS, X MINUTOS, X HORAS");
	timeReadableLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(timeReadableLabel, "cell 2 5");

	//---- hourRadioButton ----
	hourRadioButton.setText("HORAS");
	hourRadioButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(hourRadioButton, "cell 3 5");

	//---- subtractBigPriceButton ----
	subtractBigPriceButton.setText("-1000");
	subtractBigPriceButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(subtractBigPriceButton, "cell 5 5,growy");

	//---- addBigPriceButton ----
	addBigPriceButton.setText("+1000");
	addBigPriceButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(addBigPriceButton, "cell 6 5,growy");

	//---- button1 ----
	button1.setText("VOLVER");
	button1.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(button1, "cell 0 7,grow");

	//---- saveButton ----
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(saveButton, "cell 5 7 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JLabel roomConfigurationTitleLabel;
    private JLabel roomDataInformativeLabel;
    private JButton deleteRoomButton;
    private JLabel towerInformativeLabel;
    private JLabel towerLabel;
    private JLabel timeConfigurationInformativeLabel;
    private JButton firstTimeConfiguration;
    private JButton secondTimeConfiguration;
    private JButton thirdTimeConfiguration;
    private JLabel floorInformativeLabel;
    private JLabel label8;
    private JLabel timeDurationInformativeLAbel;
    private JTextField timeDurationTextField;
    private JRadioButton secondsRadioButton;
    private JLabel priceInformativeLabel;
    private JTextField priceTextField;
    private JLabel numberInformativeLabel;
    private JLabel numberLabel;
    private JLabel timeReadableInformativeLabel;
    private JRadioButton minuteRadioButton;
    private JButton subtractSmallPriceButton;
    private JButton addSmallPriceButton;
    private JLabel roomStringInformativeLabel;
    private JTextField roomStringLabel;
    private JLabel timeReadableLabel;
    private JRadioButton hourRadioButton;
    private JButton subtractBigPriceButton;
    private JButton addBigPriceButton;
    private JButton button1;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
