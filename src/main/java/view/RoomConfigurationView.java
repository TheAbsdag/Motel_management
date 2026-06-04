/*
 * Created by JFormDesigner on Wed May 20 09:30:58 GMT-05:00 2026
 */

package view;

import java.awt.*;
import javax.swing.*;
import model.Room;
import model.RoomTime;
import model.json.CurrencyConfig;
import net.miginfocom.swing.*;
import view.helpers.CurrencyFormatter;
import view.helpers.TextPromptHelper;
import view.interfaces.DirtyTrackable;

/**
 * @author SECC
 */
public class RoomConfigurationView extends JPanel implements DirtyTrackable {

    private int currentTower;
    private int currentFloor;
    private int currentRoom;
    private int selectedTimeSlot;
    private RoomTime[] timeSlots;
    private boolean hasUnsavedChanges;
    private ButtonGroup unitButtonGroup;

    private CurrencyConfig currencyConfig = CurrencyConfig.defaultConfig();

    public void setCurrencyConfig(CurrencyConfig cfg) {
        this.currencyConfig = cfg != null ? cfg : CurrencyConfig.defaultConfig();
    }

    public RoomConfigurationView() {
        initCustomComponents();
        initComponents();
        TextPromptHelper.install(roomStringLabel, "Nombre de la habitacion");
    }

    private void initCustomComponents() {
        selectedTimeSlot = 0;
        timeSlots = RoomTime.getDefaultTimeSlots();
        hasUnsavedChanges = false;
        unitButtonGroup = new ButtonGroup();
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
	floorLabel = new JLabel();
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
	backButton = new JButton();
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

	//---- floorLabel ----
	floorLabel.setText("X");
	floorLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
	add(floorLabel, "cell 0 3");

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

	//---- backButton ----
	backButton.setText("VOLVER");
	backButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(backButton, "cell 0 7,grow");

	//---- saveButton ----
	saveButton.setText("GUARDAR");
	saveButton.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
	add(saveButton, "cell 5 7 2 1,growy");
	// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on

        unitButtonGroup.add(secondsRadioButton);
        unitButtonGroup.add(minuteRadioButton);
        unitButtonGroup.add(hourRadioButton);

        setupInternalListeners();
    }

    private void setupInternalListeners() {
        firstTimeConfiguration.addActionListener(e -> selectTimeSlot(0));
        secondTimeConfiguration.addActionListener(e -> selectTimeSlot(1));
        thirdTimeConfiguration.addActionListener(e -> selectTimeSlot(2));

        subtractSmallPriceButton.addActionListener(e -> adjustPrice(-100));
        addSmallPriceButton.addActionListener(e -> adjustPrice(100));
        subtractBigPriceButton.addActionListener(e -> adjustPrice(-1000));
        addBigPriceButton.addActionListener(e -> adjustPrice(1000));

        timeDurationTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { onDurationChanged(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { onDurationChanged(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onDurationChanged(); }
        });

        priceTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { markDirty(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { markDirty(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { markDirty(); }
        });

        roomStringLabel.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { markDirty(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { markDirty(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { markDirty(); }
        });

        secondsRadioButton.addActionListener(e -> onUnitChanged());
        minuteRadioButton.addActionListener(e -> onUnitChanged());
        hourRadioButton.addActionListener(e -> onUnitChanged());
    }

    // ========== Data Loading ==========

    public void loadRoom(int tower, int floor, int room, Room roomData) {
        currentTower = tower;
        currentFloor = floor;
        currentRoom = room;

        towerLabel.setText(String.valueOf(tower + 1));
        floorLabel.setText(String.valueOf(floor + 1));
        numberLabel.setText(String.valueOf(room + 1));
        roomStringLabel.setText(roomData.getRoomString());

        RoomTime[] data = roomData.getCustomRoomTimeData();
        timeSlots = new RoomTime[3];
        for (int i = 0; i < 3; i++) {
            timeSlots[i] = data[i];
        }

        updateTimeSlotButtonLabels();
        selectTimeSlot(0);
        clearDirty();
    }

    public void resetView() {
        currentTower = 0;
        currentFloor = 0;
        currentRoom = 0;
        timeSlots = RoomTime.getDefaultTimeSlots();
        towerLabel.setText("X");
        floorLabel.setText("X");
        numberLabel.setText("X");
        roomStringLabel.setText("X-XXX");
        timeDurationTextField.setText("");
        priceTextField.setText("");
        timeReadableLabel.setText("X  SEGUNDOS, X MINUTOS, X HORAS");
        updateTimeSlotButtonLabels();
        clearDirty();
    }

    // ========== Time Slot Selection ==========

    private void selectTimeSlot(int index) {
        if (index < 0 || index >= timeSlots.length) {
            return;
        }
        selectedTimeSlot = index;
        updateSlotButtonHighlights();
        loadTimeSlotIntoFields(timeSlots[index]);
    }

    private void loadTimeSlotIntoFields(RoomTime slot) {
        long seconds = slot.getTimeSeconds();
        if (seconds >= 3600 && seconds % 3600 == 0) {
            timeDurationTextField.setText(String.valueOf(seconds / 3600));
            hourRadioButton.setSelected(true);
        } else if (seconds >= 60 && seconds % 60 == 0) {
            timeDurationTextField.setText(String.valueOf(seconds / 60));
            minuteRadioButton.setSelected(true);
        } else {
            timeDurationTextField.setText(String.valueOf(seconds));
            secondsRadioButton.setSelected(true);
        }
        priceTextField.setText(String.valueOf(slot.getPrice()));
        updateReadableDuration();
    }

    private void updateSlotButtonHighlights() {
        Color selected = new Color(103, 159, 51);
        Color deselected = UIManager.getColor("Button.background");
        firstTimeConfiguration.setBackground(selectedTimeSlot == 0 ? selected : deselected);
        secondTimeConfiguration.setBackground(selectedTimeSlot == 1 ? selected : deselected);
        thirdTimeConfiguration.setBackground(selectedTimeSlot == 2 ? selected : deselected);
    }

    private void updateTimeSlotButtonLabels() {
        if (timeSlots != null && timeSlots.length >= 3) {
        firstTimeConfiguration.setText(formatDuration(timeSlots[0].getTimeSeconds())
                + " / " + CurrencyFormatter.format(timeSlots[0].getPrice(), currencyConfig));
        secondTimeConfiguration.setText(formatDuration(timeSlots[1].getTimeSeconds())
                + " / " + CurrencyFormatter.format(timeSlots[1].getPrice(), currencyConfig));
        thirdTimeConfiguration.setText(formatDuration(timeSlots[2].getTimeSeconds())
                + " / " + CurrencyFormatter.format(timeSlots[2].getPrice(), currencyConfig));
        }
    }

    // ========== Duration Parsing ==========

    private long getCurrentDurationSeconds() {
        try {
            long value = Long.parseLong(timeDurationTextField.getText().trim());
            if (secondsRadioButton.isSelected()) {
                return value;
            } else if (minuteRadioButton.isSelected()) {
                return value * 60;
            } else {
                return value * 3600;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void onDurationChanged() {
        updateReadableDuration();
        markDirty();
    }

    private void onUnitChanged() {
        updateReadableDuration();
        markDirty();
    }

    private void updateReadableDuration() {
        long seconds = getCurrentDurationSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        timeReadableLabel.setText(hours + " HORAS, " + minutes + " MINUTOS, " + secs + " SEGUNDOS");
    }

    private String formatDuration(long seconds) {
        if (seconds >= 3600 && seconds % 3600 == 0) {
            return (seconds / 3600) + "h";
        } else if (seconds >= 60 && seconds % 60 == 0) {
            return (seconds / 60) + "m";
        }
        return seconds + "s";
    }

    // ========== Price Adjustment ==========

    private void adjustPrice(long delta) {
        try {
            long current = Long.parseLong(priceTextField.getText().trim());
            long newPrice = current + delta;
            if (newPrice >= 0) {
                priceTextField.setText(String.valueOf(newPrice));
            }
        } catch (NumberFormatException e) {
            priceTextField.setText(String.valueOf(Math.max(0, delta)));
        }
    }

    // ========== Dirty Tracking ==========

    public void markDirty() {
        hasUnsavedChanges = true;
    }

    public void clearDirty() {
        hasUnsavedChanges = false;
    }

    public boolean isDirty() {
        return hasUnsavedChanges;
    }

    public void applyCurrentFieldsToSlot() {
        long seconds = getCurrentDurationSeconds();
        long price;
        try {
            price = Long.parseLong(priceTextField.getText().trim());
        } catch (NumberFormatException e) {
            price = 0;
        }
        if (seconds > 0 && price > 0) {
            timeSlots[selectedTimeSlot] = new RoomTime(price, seconds);
        }
    }

    // ========== Getters for Modified Data ==========

    public RoomTime[] getModifiedTimeSlots() {
        applyCurrentFieldsToSlot();
        RoomTime[] copy = new RoomTime[3];
        for (int i = 0; i < 3; i++) {
            copy[i] = timeSlots[i];
        }
        return copy;
    }

    public String getModifiedRoomString() {
        return roomStringLabel.getText().trim();
    }

    public int getCurrentTower() { return currentTower; }
    public int getCurrentFloor() { return currentFloor; }
    public int getCurrentRoom() { return currentRoom; }

    // ========== Behavior Methods ==========

    public void onBackButton(Runnable action) {
        backButton.addActionListener(e -> action.run());
    }

    public void onSaveButton(Runnable action) {
        saveButton.addActionListener(e -> action.run());
    }

    public void onDeleteRoom(Runnable action) {
        deleteRoomButton.addActionListener(e -> action.run());
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
    private JLabel floorLabel;
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
    private JButton backButton;
    private JButton saveButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
