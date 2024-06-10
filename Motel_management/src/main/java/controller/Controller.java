package controller;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import model.MotelManagement;
import org.json.JSONObject;
import view.UserGUI;

/**
 *
 * @author Santiago
 */
public class Controller {

    private UserGUI userInterface;
    private MotelManagement motelManager;
    private Timer timerForTimeUpdates;
    private Timer timerForBackupFiles;
    private Timer timerForCurrentFile;

    public Controller(MotelManagement motelManager, UserGUI userInterface) {
        this.motelManager = motelManager;
        this.userInterface = userInterface;
    }

    public void start() {
        motelManager.prepareProgramData();
        userInterface.setupFloors(motelManager.getRoomsArray());

        boolean validTurn = motelManager.prepareTurnRegisterData();
        if (validTurn) {
            System.out.println("Valid turn found");
            userInterface.setFloorView();
        } else {
            System.out.println("No previous turn found");
            userInterface.setTurnSelectView();
        }

        setupListeners();
        timerForTimeUpdates = new Timer(80, e -> updateTime());
        timerForBackupFiles = new Timer(15000, e -> System.out.println("backup files"));
        timerForCurrentFile = new Timer(2000, e -> System.out.println("saving main file"));
        startTimers();
    }

    public void setupListeners() {
        //Setting up listeners for each of the view type.
        //Setting TurnSelect Listeners
        userInterface.getTurnSelectView().getTurn1Button().addActionListener(e -> startTurn(1));
        userInterface.getTurnSelectView().getTurn2Button().addActionListener(e -> startTurn(2));
        userInterface.getTurnSelectView().getTurn3Button().addActionListener(e -> startTurn(3));

        //Setting FloorView Listeners
        userInterface.getFloorView().getFloorUpButton().addActionListener(e -> setFloorUp());
        userInterface.getFloorView().getFloorDownButton().addActionListener(e -> setFloorDown());

        int floors[] = motelManager.getRoomsArray();
        for (int floor = 0; floor < floors.length; floor++) {
            for (int room = 0; room < floors[floor]; room++) {
                final int currentFloor = floor;
                final int currentRoom = room;
                //Listeners for each room
                userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).addActionListener(e -> roomSelected(currentFloor, currentRoom));
            }
        }
        userInterface.getFloorView().getManagementOptionsButton().addActionListener(e -> managementOptions());
        userInterface.getFloorView().getReceptionSellButton().addActionListener(e -> roomSale(true));

        //Room listener
        userInterface.getRoomView().getBackRoomButton().addActionListener(e -> showFloorPerspective());
        userInterface.getRoomView().getRoomSellingButton().addActionListener(e -> roomSale(false));
        userInterface.getRoomView().getEndTimeButton().addActionListener(e -> roomTimeEnd());
        userInterface.getRoomView().getAddTimeButton().addActionListener(e -> roomTimeSale());
        //Room booking hours
        userInterface.getRoomView().getBooking12HoursButton().addActionListener(e -> roomTimeModification(12));
        userInterface.getRoomView().getBooking3HoursButton().addActionListener(e -> roomTimeModification(3));
        userInterface.getRoomView().getBooking6HoursButton().addActionListener(e -> roomTimeModification(6));
        //Room price updates
        userInterface.getRoomView().getAddBigQuantityButton().addActionListener(e -> updateRoomPrice(1000));
        userInterface.getRoomView().getRemoveBigQuantity().addActionListener(e -> updateRoomPrice(-1000));
        userInterface.getRoomView().getAddSmallQuantityButton().addActionListener(e -> updateRoomPrice(100));
        userInterface.getRoomView().getRemoveSmallQuantityButton().addActionListener(e -> updateRoomPrice(-100));

        //Setting up selling view listeners
        userInterface.getSellingView().getBackButton().addActionListener(e -> backFromSelling());
        userInterface.getSellingView().getItemDeleteButton().addActionListener(e -> itemRemovedFromRegisterList());
        userInterface.getSellingView().getAddItemButton().addActionListener(e -> addItemToRegisterList());
        userInterface.getSellingView().getAddQuantityButton().addActionListener(e -> updateItemSaleAmount(1));
        userInterface.getSellingView().getRemoveQuantityButton().addActionListener(e -> updateItemSaleAmount(-1));
        userInterface.getSellingView().getFinishSaleButton().addActionListener(e -> finishSale());

        //Setting up management Select view listeners
        userInterface.getManagementSelection().getBackButton().addActionListener(e -> showFloorPerspective());
        userInterface.getManagementSelection().getTurnButton().addActionListener(e -> managementTurnSelected());
        userInterface.getManagementSelection().getInventoryButton().addActionListener(e -> managementInventorySelected());
        userInterface.getManagementSelection().getHistoryButton().addActionListener(e -> managementHistorySelected());

        //Setting up turn management listeners
        userInterface.getTurnManagerView().getBackButton().addActionListener(e -> managementOptions());
        userInterface.getTurnManagerView().getPrintButton().addActionListener(e -> printTurn());
        userInterface.getTurnManagerView().getEndTurnButton().addActionListener(e -> turnEnd());

        userInterface.getTurnManagerView().getNoPrintCheckBox().addItemListener(new CheckBoxItemListener(this.userInterface));
        userInterface.getTurnManagerView().getSummarizedPrintCheckBox().addItemListener(new CheckBoxItemListener(this.userInterface));
        userInterface.getTurnManagerView().getDetailedPrintCheckBox().addItemListener(new CheckBoxItemListener(this.userInterface));

        //Setting up inventory listeners
        userInterface.getInventoryView().getNewitemButton().addActionListener(e -> newInventoryItem());
        userInterface.getInventoryView().getDeleteItemButton().addActionListener(e -> deleteInventoryItem());
        userInterface.getInventoryView().getAddQuantityButton().addActionListener(e -> changeInventoryItemQuantity(1));
        userInterface.getInventoryView().getRemoveQuantityButton().addActionListener(e -> changeInventoryItemQuantity(-1));
        userInterface.getInventoryView().getRemoveBigPriceButton().addActionListener(e -> modifyPriceInventoryItem(-1000));
        userInterface.getInventoryView().getRemoveSmallPriceButton().addActionListener(e -> modifyPriceInventoryItem(-100));
        userInterface.getInventoryView().getAddSmallPriceButton().addActionListener(e -> modifyPriceInventoryItem(100));
        userInterface.getInventoryView().getAddBigPriceButton().addActionListener(e -> modifyPriceInventoryItem(1000));

        userInterface.getInventoryView().getSaveButton().addActionListener(e -> inventoryItemModification());
        userInterface.getInventoryView().getBackButton().addActionListener(e -> managementOptions());

        //Special inventory listener
        userInterface.getInventoryView().getInventoryTable().getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = userInterface.getInventoryView().getInventoryTable().getSelectedRow();
                System.out.println("Row is " + selectedRow);
                if (selectedRow != -1) {
                    JSONObject selectedItem = userInterface.getInventoryView().getCurrentSelectedItem(selectedRow);
                    userInterface.getInventoryView().getInformativeEditLabel().setText("MODIFICANDO");
                    userInterface.getInventoryView().getNameTextField().setText(selectedItem.getString("itemName"));
                    userInterface.getInventoryView().getQuantityTextField().setText(String.valueOf(selectedItem.getInt("quantity")));
                    userInterface.getInventoryView().getPriceTextField().setText(String.valueOf(selectedItem.getLong("price")));
                    setInventoryModificators(true);
                    
                }
            }
        });

        //Setting up history view
        userInterface.getHistoryView().getBackButton().addActionListener(e -> managementOptions());
    }

    private void newInventoryItem() {
        userInterface.getInventoryView().getInventoryTable().clearSelection();
        setInventoryModificators(true);
        userInterface.getInventoryView().getInformativeEditLabel().setText("CREANDO");
        updateInventoryViewData();
        userInterface.getInventoryView().getSaveButton().setEnabled(true);
    }

    private void deleteInventoryItem() {
        int rowSelected = userInterface.getInventoryView().getInventoryTable().getSelectedRow();
        if(rowSelected !=-1){
            JSONObject selectedItem = userInterface.getInventoryView().getCurrentSelectedItem(rowSelected);
            motelManager.deleteItemFromInventory(selectedItem);
        }
        updateInventoryViewData();
        setInventoryModificators(false);
    }

    private void changeInventoryItemQuantity(int i) {
        int currentValue;
        try {
            currentValue = Integer.parseInt(userInterface.getInventoryView().getQuantityTextField().getText()) + i;
        } catch (NumberFormatException ex) {
            currentValue = 0;
        }
        if (currentValue < 0) {
            currentValue = 0;
        }
        userInterface.getInventoryView().getQuantityTextField().setText(String.valueOf(currentValue));
    }

    private void modifyPriceInventoryItem(int i) {
        int currentValue = 0;
        try {
            currentValue = Integer.parseInt(userInterface.getInventoryView().getPriceTextField().getText()) + i;
        } catch (NumberFormatException ex) {
            currentValue = 0;
        }
        if (currentValue < 0) {
            currentValue = 0;
        }
        userInterface.getInventoryView().getPriceTextField().setText(String.valueOf(currentValue));
    }

    private void inventoryItemModification() {
        int rowSelected = userInterface.getInventoryView().getInventoryTable().getSelectedRow();

        String newName = userInterface.getInventoryView().getNameTextField().getText();
        int newQuantity = Integer.parseInt(userInterface.getInventoryView().getQuantityTextField().getText());
        long newPrice = Long.parseLong(userInterface.getInventoryView().getPriceTextField().getText());

        JSONObject newItemInformation = new JSONObject();
        newItemInformation.put("price", newPrice);
        newItemInformation.put("itemName", newName);
        newItemInformation.put("quantity", newQuantity);

        if (rowSelected != -1) {
            JSONObject selectedItem = userInterface.getInventoryView().getCurrentSelectedItem(rowSelected);
            long itemIDOfModification = selectedItem.getLong("itemID");
            newItemInformation.put("itemID", itemIDOfModification);
            motelManager.saveItemInformation(newItemInformation);
        } else {
            motelManager.newItemCreated(newItemInformation);
        }
        setInventoryModificators(false);
        userInterface.getInventoryView().getInventoryTable().clearSelection();
    }

    private void setInventoryModificators(boolean enable) {
        if (!enable) {
            userInterface.getInventoryView().getInformativeEditLabel().setText("SELECCIONE O CREE OBJETO");
            userInterface.getInventoryView().getNameTextField().setText("");
            userInterface.getInventoryView().getQuantityTextField().setText("0");
            userInterface.getInventoryView().getPriceTextField().setText("0");
            updateInventoryViewData();
        }

        userInterface.getInventoryView().getDeleteItemButton().setEnabled(enable);
        userInterface.getInventoryView().getAddQuantityButton().setEnabled(enable);
        userInterface.getInventoryView().getRemoveQuantityButton().setEnabled(enable);
        userInterface.getInventoryView().getRemoveBigPriceButton().setEnabled(enable);
        userInterface.getInventoryView().getRemoveSmallPriceButton().setEnabled(enable);
        userInterface.getInventoryView().getAddSmallPriceButton().setEnabled(enable);
        userInterface.getInventoryView().getAddBigPriceButton().setEnabled(enable);
        userInterface.getInventoryView().getSaveButton().setEnabled(enable);
        userInterface.getInventoryView().getNameTextField().setEnabled(enable);
        userInterface.getInventoryView().getPriceTextField().setEnabled(enable);
        userInterface.getInventoryView().getQuantityTextField().setEnabled(enable);
    }

    private void updateInventoryViewData() {
        userInterface.getInventoryView().updateInventory(motelManager.getInventoryData());
    }

    //Separate class for all item checks
    private class CheckBoxItemListener implements ItemListener {

        private final UserGUI view;

        public CheckBoxItemListener(UserGUI view) {
            this.view = view;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            JCheckBox selectedCheckBox = (JCheckBox) e.getSource();
            if (selectedCheckBox.isSelected()) {

                //Managing for the turn manager
                if (selectedCheckBox == view.getTurnManagerView().getNoPrintCheckBox()
                        || selectedCheckBox == view.getTurnManagerView().getSummarizedPrintCheckBox()
                        || selectedCheckBox == view.getTurnManagerView().getDetailedPrintCheckBox()) {
                    userInterface.getTurnManagerView().getEndTurnButton().setEnabled(true);
                }
                if (selectedCheckBox != view.getTurnManagerView().getNoPrintCheckBox()) {
                    view.getTurnManagerView().getNoPrintCheckBox().setSelected(false);
                }
                if (selectedCheckBox != view.getTurnManagerView().getSummarizedPrintCheckBox()) {
                    view.getTurnManagerView().getSummarizedPrintCheckBox().setSelected(false);
                }
                if (selectedCheckBox != view.getTurnManagerView().getDetailedPrintCheckBox()) {
                    view.getTurnManagerView().getDetailedPrintCheckBox().setSelected(false);
                }
            }
        }

    }

    //Validation for room data
    public void roomSelected(int floor, int room) {
        motelManager.setCurrentFloorRoom(floor, room);
        motelManager.setCurrentServiceDesired(0);
        String roomText = motelManager.getRoom(floor, room).getRoomString();
        int roomStatus = motelManager.getRoom(floor, room).getStatus();

        userInterface.getRoomView().getRoomNumber().setText(roomText);

        switch (roomStatus) {
            case 1 -> {
                //It will setup all required values for the room being free
                userInterface.getRoomView().getRoomStatusBackground().setBackground(new Color(39, 174, 96));
                userInterface.getRoomView().getStatusLabel().setText("LIBRE");

                //hiding all informative labels that are not required
                userInterface.getRoomView().getStartInformativeLabel().setVisible(false);
                userInterface.getRoomView().getRemainingInformativeLabel().setVisible(false);
                userInterface.getRoomView().getRemainingTimeLabel().setVisible(false);
                userInterface.getRoomView().getStartTimeLabel().setVisible(false);
                userInterface.getRoomView().getRoomStatusInformative().setText(" ");
                userInterface.getRoomView().getStartDateLabel().setVisible(false);

                userInterface.getRoomView().getAddBigQuantityButton().setVisible(true);
                userInterface.getRoomView().getRemoveBigQuantity().setVisible(true);
                userInterface.getRoomView().getAddSmallQuantityButton().setVisible(true);
                userInterface.getRoomView().getRemoveSmallQuantityButton().setVisible(true);
                userInterface.getRoomView().getPriceTextField().setVisible(true);
                userInterface.getRoomView().getPrintingCheckBox().setVisible(true);

                userInterface.getRoomView().getPriceTextField().setText("0");

                //Hiding buttons that are not required
                userInterface.getRoomView().getRoomSellingButton().setVisible(false);
                userInterface.getRoomView().getEndTimeButton().setVisible(false);
                userInterface.getRoomView().getAddTimeButton().setEnabled(false);
                userInterface.getRoomView().getBooking12HoursButton().setVisible(true);
                userInterface.getRoomView().getBooking6HoursButton().setVisible(true);
                userInterface.getRoomView().getBooking3HoursButton().setVisible(true);
            }
            case 2 -> {
                userInterface.getRoomView().getRoomStatusBackground().setBackground(new Color(93, 173, 226));
                userInterface.getRoomView().getStatusLabel().setText("LIMPIEZA");

                //hiding all informative labels that are not required
                userInterface.getRoomView().getStartInformativeLabel().setVisible(true);
                userInterface.getRoomView().getRemainingInformativeLabel().setVisible(false);
                userInterface.getRoomView().getRemainingTimeLabel().setVisible(false);
                userInterface.getRoomView().getStartTimeLabel().setVisible(true);
                userInterface.getRoomView().getRoomStatusInformative().setText("LIMPIEZA");
                userInterface.getRoomView().getStartDateLabel().setVisible(true);

                userInterface.getRoomView().getAddBigQuantityButton().setVisible(false);
                userInterface.getRoomView().getRemoveBigQuantity().setVisible(false);
                userInterface.getRoomView().getAddSmallQuantityButton().setVisible(false);
                userInterface.getRoomView().getRemoveSmallQuantityButton().setVisible(false);
                userInterface.getRoomView().getPriceTextField().setVisible(false);
                userInterface.getRoomView().getPrintingCheckBox().setVisible(false);

                //Showing required buttons
                userInterface.getRoomView().getRoomSellingButton().setVisible(false);
                userInterface.getRoomView().getEndTimeButton().setVisible(true);
                userInterface.getRoomView().getBooking12HoursButton().setVisible(false);
                userInterface.getRoomView().getBooking6HoursButton().setVisible(false);
                userInterface.getRoomView().getBooking3HoursButton().setVisible(false);
            }
            case 3 -> {
                userInterface.getRoomView().getRoomStatusBackground().setBackground(new Color(205, 97, 85));
                userInterface.getRoomView().getStatusLabel().setText("SERVICIO");

                //hiding all informative labels that are not required
                userInterface.getRoomView().getStartInformativeLabel().setVisible(true);
                userInterface.getRoomView().getRemainingInformativeLabel().setVisible(true);
                userInterface.getRoomView().getRemainingTimeLabel().setVisible(true);
                userInterface.getRoomView().getStartTimeLabel().setVisible(true);
                userInterface.getRoomView().getRoomStatusInformative().setText("OCUPADA");
                userInterface.getRoomView().getAddBigQuantityButton().setVisible(true);
                userInterface.getRoomView().getRemoveBigQuantity().setVisible(true);
                userInterface.getRoomView().getAddSmallQuantityButton().setVisible(true);
                userInterface.getRoomView().getRemoveSmallQuantityButton().setVisible(true);
                userInterface.getRoomView().getPriceTextField().setVisible(true);
                userInterface.getRoomView().getPrintingCheckBox().setVisible(true);
                userInterface.getRoomView().getStartDateLabel().setVisible(true);

                userInterface.getRoomView().getPriceTextField().setText("0");

                //showing required buttons
                userInterface.getRoomView().getRoomSellingButton().setVisible(true);
                userInterface.getRoomView().getEndTimeButton().setVisible(true);
                userInterface.getRoomView().getAddTimeButton().setEnabled(false);
                userInterface.getRoomView().getBooking12HoursButton().setVisible(true);
                userInterface.getRoomView().getBooking6HoursButton().setVisible(true);
                userInterface.getRoomView().getBooking3HoursButton().setVisible(true);
            }
        }
        userInterface.setRoomView();
    }

    private void roomSale(boolean receptionSale) {
        if (receptionSale) {
            motelManager.setCurrentFloorRoom(-1, -1);
        }
        motelManager.startSaleManager();
        String roomString = motelManager.getRoom(motelManager.getCurrentFloorViewed(), motelManager.getCurrentRoomViewed()).getRoomString();
        userInterface.getSellingView().getSellingToLabel().setText("VENIENDO A: " + roomString);
        userInterface.setSellingView();
    }

    private void backFromSelling() {
        motelManager.stopSaleManager();
        userInterface.setFloorView();
    }

    private void roomTimeSale() {
        motelManager.timeInformationUpdate();
        int roomNumber = motelManager.getCurrentRoomViewed();
        int floorNumber = motelManager.getCurrentFloorViewed();
        int service = motelManager.getCurrentServiceDesired();
        int price = Integer.parseInt(userInterface.getRoomView().getPriceTextField().getText());
        motelManager.registerRoomTimeAdded(floorNumber, roomNumber, service, price);
        userInterface.setFloorView();
    }

    private void updateRoomPrice(long price) {
        long newPrice = Long.parseLong(userInterface.getRoomView().getPriceTextField().getText()) + price;
        userInterface.getRoomView().getPriceTextField().setText(String.valueOf(newPrice));
    }

    private void roomTimeModification(int amount) {
        //Adds the default price for each service
        userInterface.getRoomView().getAddTimeButton().setEnabled(true);
        switch (amount) {
            case 3 -> {
                userInterface.getRoomView().getPriceTextField().setText("30000");
                motelManager.setCurrentServiceDesired(amount);
            }
            case 6 -> {
                userInterface.getRoomView().getPriceTextField().setText("40000");
                motelManager.setCurrentServiceDesired(amount);
            }
            case 12 -> {
                userInterface.getRoomView().getPriceTextField().setText("50000");
                motelManager.setCurrentServiceDesired(amount);
            }
        }
    }

    private void roomTimeEnd() {
        motelManager.timeInformationUpdate();
        int roomNumber = motelManager.getCurrentRoomViewed();
        int floorNumber = motelManager.getCurrentFloorViewed();
        motelManager.registerRoomTimeEnd(floorNumber, roomNumber);
        userInterface.setFloorView();
    }

    public void startTurn(int turnNumber) {
        motelManager.timeInformationUpdate();
        motelManager.setNewTurn(turnNumber);
        userInterface.setFloorView();
    }

    public void updateTime() {
        motelManager.timeInformationUpdate();
        String timeShown = motelManager.getCurrentLocalizedTime();
        String dateShown = motelManager.getCurrentLocalizedDate();
        userInterface.updateDateTime(timeShown, dateShown);
        boolean isFloorShown = userInterface.isFloorShown();
        boolean isRoomShown = userInterface.isRoomShown();
        //We update each button for the current information required.
        if (isFloorShown) {
            int roomArray[] = motelManager.getRoomsArray();
            for (int floor = 0; floor < roomArray.length; floor++) {
                for (int room = 0; room < roomArray[floor]; room++) {
                    int status = motelManager.getRoom(floor, room).getStatus();
                    String roomString = motelManager.getRoom(floor, room).getRoomString();
                    switch (status) {
                        case 1 -> {
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setText("<html><center>" + roomString + "</center></html>");
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setBackground(new Color(39, 174, 96));
                        }
                        case 2 -> {
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setText("<html><center>" + roomString + "</center></html>");
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setBackground(new Color(84, 153, 199));
                        }
                        case 3 -> {
                            String remainingTime = motelManager.getRemainingTimeRoom(floor, room);
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setText("<html><center>" + roomString + "<br>QUEDAN " + remainingTime + "</center></html>");
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setBackground(new Color(231, 76, 60));
                        }
                    }
                }
            }
        }
        if (isRoomShown) {
            int floor = motelManager.getCurrentFloorViewed();
            int room = motelManager.getCurrentRoomViewed();
            int status = motelManager.getRoom(floor, room).getStatus();
            String roomString = motelManager.getRoom(floor, room).getRoomString();
            switch (status) {
                case 2 -> {
                    String startTime = motelManager.getStartTimeRoom(floor, room);
                    userInterface.getRoomView().getStartTimeLabel().setText(startTime);
                }
                case 3 -> {
                    String startTime = motelManager.getStartTimeRoom(floor, room);
                    String startDate = motelManager.getStartDateRoom(floor, room);
                    String remainingTime = motelManager.getRemainingTimeRoom(floor, room);
                    userInterface.getRoomView().getStartTimeLabel().setText(startTime);
                    userInterface.getRoomView().getRemainingTimeLabel().setText(remainingTime);
                    userInterface.getRoomView().getStartDateLabel().setText(startDate);
                }
            }
        }
    }

    private void setFloorUp() {
        int currentFloorGUI = userInterface.getFloorView().getCurrentFloorIndex();
        userInterface.getFloorView().switchFloor(currentFloorGUI + 1);
    }

    private void setFloorDown() {
        int currentFloorGUI = userInterface.getFloorView().getCurrentFloorIndex();
        userInterface.getFloorView().switchFloor(currentFloorGUI - 1);
    }

    private void managementOptions() {
        userInterface.setManagementSelection();
    }

    private void showFloorPerspective() {
        userInterface.setFloorView();
    }

    private void startTimers() {
        timerForTimeUpdates.start();
        timerForBackupFiles.start();
        timerForCurrentFile.start();
    }

    private void itemRemovedFromRegisterList() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void updateItemSaleAmount(int i) {
        int newValue = 0;
        try {
            newValue = Integer.parseInt(userInterface.getSellingView().getQuantityTextField().getText()) + i;
            if (newValue < 0) {
                newValue = 0;
            }
        } catch (NumberFormatException ex) {
            newValue = 0;
        }
        userInterface.getSellingView().getQuantityTextField().setText(String.valueOf(newValue));
    }

    private void addItemToRegisterList() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void finishSale() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void managementTurnSelected() {
        userInterface.getTurnManagerView().getEndTurnButton().setEnabled(false);
        userInterface.setTurnManagerView();
    }

    private void managementInventorySelected() {
        userInterface.setInventoryView();
        setInventoryModificators(false);
    }

    private void managementHistorySelected() {
        userInterface.setHistoryView();
    }

    private void printTurn() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void turnEnd() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
