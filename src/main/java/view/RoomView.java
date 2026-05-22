/*
 * Created by JFormDesigner on Sat Jun 08 00:25:29 COT 2024
 */

package view;

import view.helpers.NumericDocumentFilter;
import view.helpers.FocusHighlighter;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.*;
import view.interfaces.TimeLabelInterface;

/**
 * @author Santiago
 */
public class RoomView extends JPanel implements TimeLabelInterface {
    public RoomView() {
	initComponents();
        FocusHighlighter.applyToAll(this);
    }

    private void initComponents() {
	// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
	// Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
	roomChangeButton = new JButton();
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
	detailedSelectedTimeLabel = new JLabel();
	booking3HoursButton = new JButton();
	booking12HoursButton = new JButton();
	booking24HoursButton = new JButton();
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
	    "fill,hidemode 3",
	    // columns
	    "[145,grow,fill]" +
	    "[78,grow,fill]" +
	    "[114,grow,fill]" +
	    "[79,grow]" +
	    "[127,grow,fill]" +
	    "[15,grow,fill]" +
	    "[83,grow,fill]" +
	    "[15,grow,fill]" +
	    "[138,grow,fill]",
	    // rows
	    "[42]" +
	    "[49,grow]" +
	    "[66]" +
	    "[grow]" +
	    "[49]" +
	    "[53,grow]" +
	    "[55,grow]" +
	    "[63,grow]" +
	    "[86,grow]" +
	    "[76,grow]"));

	//---- roomChangeButton ----
	roomChangeButton.setText("CAMBIO DE HABITACION");
	roomChangeButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(roomChangeButton, "cell 0 0 3 1,growy");

	//======== roomStatusBackground ========
	{
	    roomStatusBackground.setLayout(new MigLayout(
		"fillx,hidemode 3",
		// columns
		"[173,grow,fill]" +
		"[62,grow,fill]" +
		"[226,grow,fill]",
		// rows
		"[102,grow]" +
		"[92,grow]" +
		"[76,grow]" +
		"[101,grow]" +
		"[54,grow]"));

	    //---- roomNumber ----
	    roomNumber.setText("000");
	    roomNumber.setFont(new Font("Segoe UI Black", Font.PLAIN, 90));
	    roomNumber.setHorizontalAlignment(SwingConstants.CENTER);
	    roomNumber.setForeground(Color.black);
	    roomStatusBackground.add(roomNumber, "cell 0 0 3 1,growy");

	    //---- statusLabel ----
	    statusLabel.setText("LIMPIEZA");
	    statusLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 70));
	    statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    statusLabel.setBorder(new LineBorder(Color.gray, 7, true));
	    statusLabel.setForeground(Color.black);
	    roomStatusBackground.add(statusLabel, "cell 0 1 3 1,growy");

	    //---- startInformativeLabel ----
	    startInformativeLabel.setText("INICIO");
	    startInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 50));
	    startInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    startInformativeLabel.setForeground(Color.black);
	    roomStatusBackground.add(startInformativeLabel, "cell 0 2");

	    //---- remainingInformativeLabel ----
	    remainingInformativeLabel.setText("RESTANTE");
	    remainingInformativeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 50));
	    remainingInformativeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    remainingInformativeLabel.setForeground(Color.black);
	    roomStatusBackground.add(remainingInformativeLabel, "cell 1 2 2 1,grow");

	    //---- startTimeLabel ----
	    startTimeLabel.setText("00:00");
	    startTimeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	    startTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    startTimeLabel.setForeground(Color.black);
	    roomStatusBackground.add(startTimeLabel, "cell 0 3,growy");

	    //---- remainingTimeLabel ----
	    remainingTimeLabel.setText("00:00");
	    remainingTimeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	    remainingTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    remainingTimeLabel.setForeground(Color.black);
	    roomStatusBackground.add(remainingTimeLabel, "cell 1 3 2 1,growy");

	    //---- startDateLabel ----
	    startDateLabel.setText("date");
	    startDateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	    startDateLabel.setForeground(Color.black);
	    startDateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    roomStatusBackground.add(startDateLabel, "cell 0 4,growy");
	}
	add(roomStatusBackground, "cell 0 1 4 8,growy");

	//---- dateLabel ----
	dateLabel.setText("22 DICIEMBRE 2022");
	dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
	dateLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 24));
	add(dateLabel, "cell 4 0 5 1,growy");

	//---- timeLabel ----
	timeLabel.setText("00:00 AM");
	timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	timeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(timeLabel, "cell 4 1 5 1,growy");

	//---- roomStatusInformative ----
	roomStatusInformative.setText("SOBRETIEMPO");
	roomStatusInformative.setHorizontalAlignment(SwingConstants.CENTER);
	roomStatusInformative.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
	add(roomStatusInformative, "cell 4 2 5 1,growy");

	//---- detailedSelectedTimeLabel ----
	detailedSelectedTimeLabel.setText("TIEMPO SELECCIONADO: XXH:XXM:XXS");
	detailedSelectedTimeLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(detailedSelectedTimeLabel, "cell 4 3 5 1");

	//---- booking3HoursButton ----
	booking3HoursButton.setText("3");
	booking3HoursButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(booking3HoursButton, "cell 4 4,growy");

	//---- booking12HoursButton ----
	booking12HoursButton.setText("12");
	booking12HoursButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(booking12HoursButton, "cell 6 4,growy");

	//---- booking24HoursButton ----
	booking24HoursButton.setText("24");
	booking24HoursButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(booking24HoursButton, "cell 8 4,growy");

	//---- priceTextField ----
	priceTextField.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(priceTextField, "cell 4 5 5 1,growy");

	//---- removeSmallQuantityButton ----
	removeSmallQuantityButton.setText("-100");
	removeSmallQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 42));
	add(removeSmallQuantityButton, "cell 4 6 2 1,growy");

	//---- addSmallQuantityButton ----
	addSmallQuantityButton.setText("+100");
	addSmallQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 42));
	add(addSmallQuantityButton, "cell 7 6 2 1,growy");

	//---- removeBigQuantity ----
	removeBigQuantity.setText("-1000");
	removeBigQuantity.setFont(new Font("Segoe UI Black", Font.PLAIN, 42));
	add(removeBigQuantity, "cell 4 7 2 1,growy");

	//---- addBigQuantityButton ----
	addBigQuantityButton.setText("+1000");
	addBigQuantityButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 42));
	add(addBigQuantityButton, "cell 7 7 2 1,growy");

	//---- printingCheckBox ----
	printingCheckBox.setText("IMPRESION");
	printingCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
	printingCheckBox.setFont(new Font("Segoe UI Black", Font.PLAIN, 48));
	add(printingCheckBox, "cell 4 8 5 1,growy");

	//---- backRoomButton ----
	backRoomButton.setText("VOLVER");
	backRoomButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 36));
	add(backRoomButton, "cell 0 9,growy");

	//---- roomSellingButton ----
	roomSellingButton.setText("VENTA HAB");
	roomSellingButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 20));
	add(roomSellingButton, "cell 1 9 2 1,growy");

	//---- endTimeButton ----
	endTimeButton.setText("TERMINAR");
	endTimeButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 36));
	add(endTimeButton, "cell 3 9,growy");

	//---- addTimeButton ----
	addTimeButton.setText("VENDER");
	addTimeButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 36));
	add(addTimeButton, "cell 4 9 5 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Educational license - Santiago Esteban Castelblanco Castiblanco (saecastelblancoc)
    private JButton roomChangeButton;
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
    private JLabel detailedSelectedTimeLabel;
    private JButton booking3HoursButton;
    private JButton booking12HoursButton;
    private JButton booking24HoursButton;
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
     * @return the detailedSelectedTimeLabel
     */
    public JLabel getDetailedSelectedTimeLabel() {
        return detailedSelectedTimeLabel;
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
    public JButton getBooking12HoursButton() {
        return booking12HoursButton;
    }

    /**
     * @return the booking12HoursButton
     */
    public JButton getBooking24HoursButton() {
        return booking24HoursButton;
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

    /**
     * @return the roomChangeButton
     */
    public JButton getRoomChangeButton() {
        return roomChangeButton;
    }

    // ========== Encapsulated listener registration (replaces raw getter access) ==========

    /** Registers a listener for the back/return button. */
    public void onBackButton(Runnable action) {
        backRoomButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the room selling button. */
    public void onRoomSellingButton(Runnable action) {
        roomSellingButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the end time (check-out) button. */
    public void onEndTimeButton(Runnable action) {
        endTimeButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the add time button. */
    public void onAddTimeButton(Runnable action) {
        addTimeButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for the room change button. */
    public void onRoomChangeButton(Runnable action) {
        roomChangeButton.addActionListener(e -> action.run());
    }

    /** Registers a listener for a booking duration button (0=3h, 1=12h, 2=24h). */
    public void onBookingHourButton(int slot, Runnable action) {
        switch (slot) {
            case 0 -> booking3HoursButton.addActionListener(e -> action.run());
            case 1 -> booking12HoursButton.addActionListener(e -> action.run());
            case 2 -> booking24HoursButton.addActionListener(e -> action.run());
            default -> { /* no-op */ }
        }
    }

    /** Registers listeners for price adjustment buttons. */
    public void onPriceAdjust(int delta, Runnable action) {
        if (delta == 1000) {
            addBigQuantityButton.addActionListener(e -> action.run());
        } else if (delta == -1000) {
            removeBigQuantity.addActionListener(e -> action.run());
        } else if (delta == 100) {
            addSmallQuantityButton.addActionListener(e -> action.run());
        } else if (delta == -100) {
            removeSmallQuantityButton.addActionListener(e -> action.run());
        }
    }

    /** Sets the room display information. */
    public void setRoomInfo(String roomNumberText, String statusText, String roomStatusInfo) {
        roomNumber.setText(roomNumberText);
        statusLabel.setText(statusText);
        roomStatusInformative.setText(roomStatusInfo);
    }

    /** Sets the current booking price in the price field. */
    public void setDisplayPrice(String priceText) {
        priceTextField.setText(priceText);
    }

    /** Returns whether the printing checkbox is selected. */
    public boolean isPrintSelected() {
        return printingCheckBox.isSelected();
    }

    /** Returns the current text in the price field. */
    public String getDisplayPrice() {
        return priceTextField.getText();
    }

    // ========== View state configuration ==========

    /** Sets the room number label. */
    public void setRoomNumber(String text) {
        roomNumber.setText(text);
    }

    /** Sets the status background color. */
    public void setStatusBackground(Color color) {
        roomStatusBackground.setBackground(color);
    }

    /** Sets both status label and informative text. */
    public void setStatusLabels(String status, String informative) {
        statusLabel.setText(status);
        roomStatusInformative.setText(informative);
    }

    /** Shows or hides the start/remaining/date time info section. */
    public void showTimeInfo(boolean start, boolean remaining, boolean date) {
        startInformativeLabel.setVisible(start);
        remainingInformativeLabel.setVisible(remaining);
        remainingTimeLabel.setVisible(remaining);
        startTimeLabel.setVisible(start);
        startDateLabel.setVisible(date);
    }

    /** Shows or hides the price adjustment controls. */
    public void showPriceControls(boolean visible) {
        addBigQuantityButton.setVisible(visible);
        removeBigQuantity.setVisible(visible);
        addSmallQuantityButton.setVisible(visible);
        removeSmallQuantityButton.setVisible(visible);
        priceTextField.setVisible(visible);
        printingCheckBox.setVisible(visible);
    }

    /** Shows or hides the booking duration buttons. */
    public void showBookingButtons(boolean visible) {
        booking3HoursButton.setVisible(visible);
        booking12HoursButton.setVisible(visible);
        booking24HoursButton.setVisible(visible);
    }

    /** Shows or hides the room-specific action buttons. */
    public void showActionButtons(boolean selling, boolean endTime, boolean addTime, boolean change) {
        roomSellingButton.setVisible(selling);
        endTimeButton.setVisible(endTime);
        addTimeButton.setVisible(addTime);
        roomChangeButton.setVisible(change);
    }

    // ========== Booking button configuration ==========

    /** Sets the text on a booking duration button (0=3h, 1=12h, 2=24h). */
    public void setBookingButtonText(int slot, String text) {
        switch (slot) {
            case 0 -> booking3HoursButton.setText(text);
            case 1 -> booking12HoursButton.setText(text);
            case 2 -> booking24HoursButton.setText(text);
            default -> { /* no-op */ }
        }
    }

    /** Resets all booking button backgrounds to white. */
    public void resetBookingHighlights() {
        booking3HoursButton.setBackground(Color.WHITE);
        booking12HoursButton.setBackground(Color.WHITE);
        booking24HoursButton.setBackground(Color.WHITE);
    }

    /** Highlights one booking button by slot (0, 1, 2). */
    public void setBookingButtonHighlight(int slot) {
        Color highlight = new Color(103, 159, 51);
        booking3HoursButton.setBackground(slot == 0 ? highlight : Color.WHITE);
        booking12HoursButton.setBackground(slot == 1 ? highlight : Color.WHITE);
        booking24HoursButton.setBackground(slot == 2 ? highlight : Color.WHITE);
    }

    // ========== Time label updates ==========

    /** Sets the start time label text. */
    public void setStartTimeLabel(String text) {
        startTimeLabel.setText(text);
    }

    /** Sets the remaining time label text. */
    public void setRemainingTimeLabel(String text) {
        remainingTimeLabel.setText(text);
    }

    /** Sets the start date label text. */
    public void setStartDateLabel(String text) {
        startDateLabel.setText(text);
    }

    /** Sets the detailed selected time label. */
    public void setDetailedSelectedTime(String text) {
        detailedSelectedTimeLabel.setText(text);
    }

    /** Applies overtime warning appearance (yellow background + warning text). */
    public void setOvertimeWarning(boolean isOvertime) {
        if (isOvertime) {
            roomStatusBackground.setBackground(new Color(241, 196, 15));
            statusLabel.setText("SOBRETIEMPO");
            roomStatusInformative.setText("SOBRETIEMPO");
        }
    }

    // ========== Add time button ==========

    /** Enables or disables the add time button. */
    public void setAddTimeEnabled(boolean enabled) {
        addTimeButton.setEnabled(enabled);
    }
}
