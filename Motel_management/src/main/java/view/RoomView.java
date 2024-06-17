/*
 * Created by JFormDesigner on Sat Jun 08 00:25:29 COT 2024
 */

package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.*;

/**
 * @author Santiago
 */
public class RoomView extends JPanel {
    public RoomView() {
	initComponents();
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	roomStatusBackground = new JPanel();
	roomNumber = new JLabel();
	statusLabel = new JLabel();
	startInformativeLabel = new JLabel();
	remainingInformativeLabel = new JLabel();
	startTimeLabel = new JLabel();
	remainingTimeLabel = new JLabel();
	startDateLabel = new JLabel();
	dateLabel = new JLabel();
	timeLabel = new JLabel();
	roomStatusInformative = new JLabel();
	booking3HoursButton = new JButton();
	booking6HoursButton = new JButton();
	booking12HoursButton = new JButton();
	priceTextField = new JTextField();
	 ((AbstractDocument) priceTextField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
	removeSmallQuantityButton = new JButton();
	addSmallQuantityButton = new JButton();
	removeBigQuantity = new JButton();
	addBigQuantityButton = new JButton();
	printingCheckBox = new JCheckBox();
	backRoomButton = new JButton();
	roomSellingButton = new JButton();
	endTimeButton = new JButton();
	addTimeButton = new JButton();

	//======== this ========
	setLayout(new MigLayout(
	    "hidemode 3",
	    // columns
	    "[145,grow,fill]" +
	    "[78,grow,shrink 0,fill]" +
	    "[147,grow,shrink 0,fill]" +
	    "[79,grow,fill]" +
	    "[145,grow,shrink 0,fill]" +
	    "[127,grow,fill]" +
	    "[15,fill]" +
	    "[83,grow,fill]" +
	    "[15,fill]" +
	    "[138,grow,fill]",
	    // rows
	    "[42]" +
	    "[49,grow]" +
	    "[66]" +
	    "[49]" +
	    "[53,grow]" +
	    "[55,grow]" +
	    "[63,grow]" +
	    "[86,grow]" +
	    "[76,grow]"));

	//======== roomStatusBackground ========
	{
	    roomStatusBackground.setLayout(new MigLayout(
		"hidemode 3",
		// columns
		"[65,grow,fill]" +
		"[261,grow,fill]" +
		"[68,grow,fill]" +
		"[226,grow,fill]" +
		"[97,grow,fill]",
		// rows
		"[102,grow]" +
		"[92,grow]" +
		"[10,grow]" +
		"[76,grow]" +
		"[101,grow]" +
		"[54]"));

	    //---- roomNumber ----
	    roomNumber.setText("000");
	    roomNumber.setFont(new Font("Segoe UI Black", Font.PLAIN, 90));
	    roomNumber.setHorizontalAlignment(SwingConstants.CENTER);
	    roomNumber.setForeground(Color.black);
	    roomStatusBackground.add(roomNumber, "cell 2 0,growy");

	    //---- statusLabel ----
	    statusLabel.setText("LIMPIEZA");
	    statusLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 90));
	    statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    statusLabel.setBorder(new LineBorder(Color.gray, 7, true));
	    statusLabel.setForeground(Color.black);
	    roomStatusBackground.add(statusLabel, "cell 1 1 3 1,growy");

	    //---- startInformativeLabel ----
	    startInformativeLabel.setText("INICIO");
	    startInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 70));
	    startInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    startInformativeLabel.setForeground(Color.black);
	    roomStatusBackground.add(startInformativeLabel, "cell 0 3 2 1");

	    //---- remainingInformativeLabel ----
	    remainingInformativeLabel.setText("RESTANTE");
	    remainingInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 70));
	    remainingInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    remainingInformativeLabel.setForeground(Color.black);
	    roomStatusBackground.add(remainingInformativeLabel, "cell 3 3 2 1,grow");

	    //---- startTimeLabel ----
	    startTimeLabel.setText("00:00");
	    startTimeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	    startTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    startTimeLabel.setForeground(Color.black);
	    roomStatusBackground.add(startTimeLabel, "cell 0 4 2 1,growy");

	    //---- remainingTimeLabel ----
	    remainingTimeLabel.setText("00:00");
	    remainingTimeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	    remainingTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    remainingTimeLabel.setForeground(Color.black);
	    roomStatusBackground.add(remainingTimeLabel, "cell 3 4 2 1,growy");

	    //---- startDateLabel ----
	    startDateLabel.setText("date");
	    startDateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    startDateLabel.setForeground(Color.black);
	    startDateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    roomStatusBackground.add(startDateLabel, "cell 0 5 2 1,growy");
	}
	add(roomStatusBackground, "cell 0 0 5 8");

	//---- dateLabel ----
	dateLabel.setText("22 DICIEMBRE 2022");
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(dateLabel, "cell 5 0 5 1,growy");

	//---- timeLabel ----
	timeLabel.setText("00:00 AM");
	timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 56));
	add(timeLabel, "cell 5 1 5 1,growy");

	//---- roomStatusInformative ----
	roomStatusInformative.setText("SOBRETIEMPO");
	roomStatusInformative.setHorizontalAlignment(SwingConstants.CENTER);
	roomStatusInformative.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(roomStatusInformative, "cell 5 2 5 1,growy");

	//---- booking3HoursButton ----
	booking3HoursButton.setText("3");
	booking3HoursButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(booking3HoursButton, "cell 5 3,growy");

	//---- booking6HoursButton ----
	booking6HoursButton.setText("6");
	booking6HoursButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(booking6HoursButton, "cell 7 3,growy");

	//---- booking12HoursButton ----
	booking12HoursButton.setText("12");
	booking12HoursButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(booking12HoursButton, "cell 9 3,growy");

	//---- priceTextField ----
	priceTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(priceTextField, "cell 5 4 5 1,growy");

	//---- removeSmallQuantityButton ----
	removeSmallQuantityButton.setText("-100");
	removeSmallQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(removeSmallQuantityButton, "cell 5 5 2 1,growy");

	//---- addSmallQuantityButton ----
	addSmallQuantityButton.setText("100");
	addSmallQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(addSmallQuantityButton, "cell 8 5 2 1,growy");

	//---- removeBigQuantity ----
	removeBigQuantity.setText("-1000");
	removeBigQuantity.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(removeBigQuantity, "cell 5 6 2 1,growy");

	//---- addBigQuantityButton ----
	addBigQuantityButton.setText("1000");
	addBigQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(addBigQuantityButton, "cell 8 6 2 1,growy");

	//---- printingCheckBox ----
	printingCheckBox.setText("IMPRESION");
	printingCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
	printingCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(printingCheckBox, "cell 5 7 5 1,growy");

	//---- backRoomButton ----
	backRoomButton.setText("VOLVER");
	backRoomButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(backRoomButton, "cell 0 8,growy");

	//---- roomSellingButton ----
	roomSellingButton.setText("VENTA HABITACION");
	roomSellingButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(roomSellingButton, "cell 1 8 2 1,growy");

	//---- endTimeButton ----
	endTimeButton.setText("TERMINAR");
	endTimeButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(endTimeButton, "cell 3 8 2 1,growy");

	//---- addTimeButton ----
	addTimeButton.setText("VENDER");
	addTimeButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(addTimeButton, "cell 5 8 5 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JPanel roomStatusBackground;
    private JLabel roomNumber;
    private JLabel statusLabel;
    private JLabel startInformativeLabel;
    private JLabel remainingInformativeLabel;
    private JLabel startTimeLabel;
    private JLabel remainingTimeLabel;
    private JLabel startDateLabel;
    private JLabel dateLabel;
    private JLabel timeLabel;
    private JLabel roomStatusInformative;
    private JButton booking3HoursButton;
    private JButton booking6HoursButton;
    private JButton booking12HoursButton;
    private JTextField priceTextField;
    private JButton removeSmallQuantityButton;
    private JButton addSmallQuantityButton;
    private JButton removeBigQuantity;
    private JButton addBigQuantityButton;
    private JCheckBox printingCheckBox;
    private JButton backRoomButton;
    private JButton roomSellingButton;
    private JButton endTimeButton;
    private JButton addTimeButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    /**
     * @return the roomStatusBackground
     */
    public JPanel getRoomStatusBackground() {
        return roomStatusBackground;
    }

    /**
     * @return the roomNumber
     */
    public JLabel getRoomNumber() {
        return roomNumber;
    }

    /**
     * @return the statusLabel
     */
    public JLabel getStatusLabel() {
        return statusLabel;
    }

    /**
     * @return the startInformativeLabel
     */
    public JLabel getStartInformativeLabel() {
        return startInformativeLabel;
    }

    /**
     * @return the remainingInformativeLabel
     */
    public JLabel getRemainingInformativeLabel() {
        return remainingInformativeLabel;
    }

    /**
     * @return the startTimeLabel
     */
    public JLabel getStartTimeLabel() {
        return startTimeLabel;
    }

    /**
     * @return the remainingTimeLabel
     */
    public JLabel getRemainingTimeLabel() {
        return remainingTimeLabel;
    }

    /**
     * @return the dateLabel
     */
    public JLabel getDateLabel() {
        return dateLabel;
    }

    /**
     * @return the timeLabel
     */
    public JLabel getTimeLabel() {
        return timeLabel;
    }

    /**
     * @return the roomStatusInformative
     */
    public JLabel getRoomStatusInformative() {
        return roomStatusInformative;
    }

    /**
     * @return the booking3HoursButton
     */
    public JButton getBooking3HoursButton() {
        return booking3HoursButton;
    }

    /**
     * @return the booking6HoursButton
     */
    public JButton getBooking6HoursButton() {
        return booking6HoursButton;
    }

    /**
     * @return the booking12HoursButton
     */
    public JButton getBooking12HoursButton() {
        return booking12HoursButton;
    }

    /**
     * @return the priceTextField
     */
    public JTextField getPriceTextField() {
        return priceTextField;
    }

    /**
     * @return the removeSmallQuantityButton
     */
    public JButton getRemoveSmallQuantityButton() {
        return removeSmallQuantityButton;
    }

    /**
     * @return the addSmallQuantityButton
     */
    public JButton getAddSmallQuantityButton() {
        return addSmallQuantityButton;
    }

    /**
     * @return the removeBigQuantity
     */
    public JButton getRemoveBigQuantity() {
        return removeBigQuantity;
    }

    /**
     * @return the addBigQuantityButton
     */
    public JButton getAddBigQuantityButton() {
        return addBigQuantityButton;
    }

    /**
     * @return the printingCheckBox
     */
    public JCheckBox getPrintingCheckBox() {
        return printingCheckBox;
    }

    /**
     * @return the backRoomButton
     */
    public JButton getBackRoomButton() {
        return backRoomButton;
    }

    /**
     * @return the roomSellingButton
     */
    public JButton getRoomSellingButton() {
        return roomSellingButton;
    }

    /**
     * @return the endTimeButton
     */
    public JButton getEndTimeButton() {
        return endTimeButton;
    }

    /**
     * @return the addTimeButton
     */
    public JButton getAddTimeButton() {
        return addTimeButton;
    }

    /**
     * @return the startDateLabel
     */
    public JLabel getStartDateLabel() {
        return startDateLabel;
    }
}
