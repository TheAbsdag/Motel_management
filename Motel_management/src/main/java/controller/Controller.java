package controller;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
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
    private boolean isListAdjusting = false;
    private Robot robotSim;

    public Controller(MotelManagement motelManager, UserGUI userInterface) {
        this.motelManager = motelManager;
        this.userInterface = userInterface;
        try {
            robotSim = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
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
        timerForBackupFiles = new Timer(15000, e -> SaveBackupFiles());
        timerForCurrentFile = new Timer(2000, e -> saveMainFiles());
        startTimers();
    }

    public void setupListeners() {
        //Setting up listeners for each of the view type.
        //Setting TurnSelect Listeners
        userInterface.getTurnSelectView().getTurn1Button().addActionListener(e -> startTurn(1));
        userInterface.getTurnSelectView().getTurn2Button().addActionListener(e -> startTurn(2));
        userInterface.getTurnSelectView().getTurn3Button().addActionListener(e -> startTurn(3));

        //Setting FloorView Listeners
        userInterface.getFloorView().getFloorUpButton().addActionListener(e -> changeFloor(1));
        userInterface.getFloorView().getFloorDownButton().addActionListener(e -> changeFloor(-1));

        int floors[] = motelManager.getRoomsArray();
        for (int floor = 0; floor < floors.length; floor++) {
            for (int room = 0; room < floors[floor]; room++) {
                final int currentFloor = floor;
                final int currentRoom = room;
                //Listeners for each room
                userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).addActionListener(e -> roomSelected(currentFloor, currentRoom));
                userInterface.getRoomChangeView().getRoomButtonGrid().get(floor).get(room).addActionListener(e -> roomChangeSelected(currentFloor, currentRoom));
            }
        }
        userInterface.getFloorView().getManagementOptionsButton().addActionListener(e -> managementOptions());
        userInterface.getFloorView().getReceptionSellButton().addActionListener(e -> roomSale(true));

        //Setting up RoomChangeView Listeners
        userInterface.getRoomChangeView().getUpButton().addActionListener(e -> roomChangeViewFloorChange(1));
        userInterface.getRoomChangeView().getDownButton().addActionListener(e -> roomChangeViewFloorChange(-1));
        userInterface.getRoomChangeView().getBackButton().addActionListener(e -> showFloorPerspective());
        userInterface.getRoomChangeView().getConfirmButton().addActionListener(e -> changeRoomTime());

        //Room listener
        userInterface.getRoomView().getBackRoomButton().addActionListener(e -> showFloorPerspective());
        userInterface.getRoomView().getRoomSellingButton().addActionListener(e -> roomSale(false));
        userInterface.getRoomView().getEndTimeButton().addActionListener(e -> roomTimeEnd());
        userInterface.getRoomView().getAddTimeButton().addActionListener(e -> roomTimeSale());
        userInterface.getRoomView().getRoomChangeButton().addActionListener(e -> roomReassigment());
        //Room booking hours
        userInterface.getRoomView().getBooking24HoursButton().addActionListener(e -> roomTimeModification(24));
        userInterface.getRoomView().getBooking3HoursButton().addActionListener(e -> roomTimeModification(3));
        userInterface.getRoomView().getBooking12HoursButton().addActionListener(e -> roomTimeModification(12));
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
        userInterface.getSellingView().getItemTable().getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            if (!event.getValueIsAdjusting() && !isListAdjusting) {
                isListAdjusting = true;
                userInterface.getSellingView().getQuantityTextField().setText("1");
                userInterface.getSellingView().getAddItemButton().setEnabled(true);
                userInterface.getSellingView().getItemDeleteButton().setEnabled(false);
                userInterface.getSellingView().getSellingTable().clearSelection();
                isListAdjusting = false;
            }
        });
        userInterface.getSellingView().getSellingTable().getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            if (!event.getValueIsAdjusting() && !isListAdjusting) {
                isListAdjusting = true;
                userInterface.getSellingView().getItemDeleteButton().setEnabled(true);
                userInterface.getSellingView().getAddItemButton().setEnabled(false);
                userInterface.getSellingView().getItemTable().clearSelection();
                isListAdjusting = false;
            }
        });
        userInterface.getSellingView().getUpSellingListButton().addActionListener(e -> simulateArrowUpSelling());
        userInterface.getSellingView().getDownSellingListButton().addActionListener(e -> simulateArrowDownSelling());
        userInterface.getSellingView().getCourtesySaleButton().addActionListener(e -> addCourtesyItemToRegister());

        //Setting up management Select view listeners
        userInterface.getManagementSelection().getBackButton().addActionListener(e -> showFloorPerspective());
        userInterface.getManagementSelection().getTurnButton().addActionListener(e -> managementTurnSelected());
        userInterface.getManagementSelection().getInventoryButton().addActionListener(e -> managementInventorySelected());
        userInterface.getManagementSelection().getHistoryButton().addActionListener(e -> managementHistorySelected());

        //Setting up turn management listeners
        userInterface.getTurnManagerView().getBackButton().addActionListener(e -> managementOptions());
        userInterface.getTurnManagerView().getPrintButton().addActionListener(e -> printTurn());
        userInterface.getTurnManagerView().getEndTurnButton().addActionListener(e -> turnChange());

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
        userInterface.getInventoryView().getUpButton().addActionListener(e -> simulateArrowUpInventory());
        userInterface.getInventoryView().getDownButton().addActionListener(e -> simulateArrowDownInventory());

        //Special inventory listener
        userInterface.getInventoryView().getInventoryTable().getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = userInterface.getInventoryView().getInventoryTable().getSelectedRow();
                if (selectedRow != -1 && !isListAdjusting) {
                    isListAdjusting = true;
                    JSONObject selectedItem = userInterface.getInventoryView().getCurrentSelectedItem(selectedRow);
                    userInterface.getInventoryView().getInformativeEditLabel().setText("MODIFICANDO");
                    userInterface.getInventoryView().getNameTextField().setText(selectedItem.getString("itemName"));
                    userInterface.getInventoryView().getQuantityTextField().setText(String.valueOf(selectedItem.getInt("quantity")));
                    userInterface.getInventoryView().getPriceTextField().setText(String.valueOf(selectedItem.getLong("price")));
                    setInventoryModificators(true);
                    isListAdjusting = false;
                }
            }
        });

        //Setting up history view
        userInterface.getHistoryView().getBackButton().addActionListener(e -> managementOptions());
        userInterface.getHistoryView().getTurnDetailsButton().addActionListener(e -> turnHistoryDetails());
        userInterface.getHistoryView().getTurnDetailsView().getBackButton().addActionListener(e -> closeHistoryDetails());
        userInterface.getHistoryView().getTurnDetailsView().getNoPrintCheckBox().addItemListener(new CheckBoxItemListener(this.userInterface));
        userInterface.getHistoryView().getTurnDetailsView().getSummarizedPrintCheckBox().addItemListener(new CheckBoxItemListener(this.userInterface));
        userInterface.getHistoryView().getTurnDetailsView().getDetailedPrintCheckBox().addItemListener(new CheckBoxItemListener(this.userInterface));

        //Special history listener
        userInterface.getHistoryView().getTurnHistoryTable().getSelectionModel().addListSelectionListener((ListSelectionEvent event) ->{
            if(!event.getValueIsAdjusting()){
                int selectedRow = userInterface.getHistoryView().getTurnHistoryTable().getSelectedRow();
                if(selectedRow != -1 &&!isListAdjusting){
                    isListAdjusting = true;
                    JSONObject historyBasicInformation = motelManager.getBasicTurnHistoryData(selectedRow);
                    userInterface.getHistoryView().getTurnStartLabel().setText(historyBasicInformation.getString("startString"));
                    userInterface.getHistoryView().getTurnEndLabel().setText(historyBasicInformation.getString("endString"));
                    userInterface.getHistoryView().getTurnDateLabel().setText(historyBasicInformation.getString("startDate"));
                    userInterface.getHistoryView().getDurationLabel().setText(historyBasicInformation.getString("duration"));
                    isListAdjusting = false;
                }
            }
        });
        
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
        if (rowSelected != -1) {
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

    private void saveMainFiles() {
        motelManager.saveFilesForMainService();
    }

    private void SaveBackupFiles() {
        motelManager.saveFilesForBackup();
    }

    private void simulateArrowUpSelling() {
        userInterface.getSellingView().getItemTable().requestFocusInWindow();
        robotSim.keyPress(KeyEvent.VK_UP);
        robotSim.keyRelease(KeyEvent.VK_UP);
    }

    private void simulateArrowDownSelling() {
        userInterface.getSellingView().getItemTable().requestFocusInWindow();
        robotSim.keyPress(KeyEvent.VK_DOWN);
        robotSim.keyRelease(KeyEvent.VK_DOWN);
    }

    private void simulateArrowDownInventory() {
        userInterface.getInventoryView().getInventoryTable().requestFocusInWindow();
        robotSim.keyPress(KeyEvent.VK_DOWN);
        robotSim.keyRelease(KeyEvent.VK_DOWN);
    }

    private void simulateArrowUpInventory() {
        userInterface.getInventoryView().getInventoryTable().requestFocusInWindow();
        robotSim.keyPress(KeyEvent.VK_UP);
        robotSim.keyRelease(KeyEvent.VK_UP);
    }

    private void roomReassigment() {
        userInterface.setRoomChangeView();
    }

    private void roomChangeSelected(int currentFloor, int currentRoom) {
        String roomString = motelManager.getRoom(currentFloor, currentRoom).getRoomString();
        int status = motelManager.getRoom(currentFloor, currentRoom).getStatus();
        motelManager.setDesiredRoomChange(currentFloor, currentRoom);
        if (status == 3) {
            userInterface.getRoomChangeView().getSelectedLabel().setText("NO DISPONIBLE");
        } else {
            userInterface.getRoomChangeView().getSelectedLabel().setText(roomString);
        }
    }

    private void roomChangeViewFloorChange(int i) {
        int currentFloor = userInterface.getRoomChangeView().getCurrentFloorIndex();
        userInterface.getRoomChangeView().switchFloor(currentFloor + i);
    }

    private void changeRoomTime() {
        motelManager.timeInformationUpdate();
        boolean validReturn = motelManager.changeRoomTimeToAnother();
        if (validReturn) {
            userInterface.setFloorView();
        }
    }

    //Separate class for all item checks if additional selection is required
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
                    view.getTurnManagerView().getPrintButton().setEnabled(true);
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

                if (selectedCheckBox == view.getHistoryView().getTurnDetailsView().getNoPrintCheckBox()
                        || selectedCheckBox == view.getHistoryView().getTurnDetailsView().getSummarizedPrintCheckBox()
                        || selectedCheckBox == view.getHistoryView().getTurnDetailsView().getDetailedPrintCheckBox()) {
                    view.getHistoryView().getTurnDetailsView().getPrintButton().setEnabled(true);
                }
                if (selectedCheckBox != view.getHistoryView().getTurnDetailsView().getNoPrintCheckBox()) {
                    view.getHistoryView().getTurnDetailsView().getNoPrintCheckBox().setSelected(false);
                }
                if (selectedCheckBox != view.getHistoryView().getTurnDetailsView().getSummarizedPrintCheckBox()) {
                    view.getHistoryView().getTurnDetailsView().getSummarizedPrintCheckBox().setSelected(false);
                }
                if (selectedCheckBox != view.getHistoryView().getTurnDetailsView().getDetailedPrintCheckBox()) {
                    view.getHistoryView().getTurnDetailsView().getDetailedPrintCheckBox().setSelected(false);
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
        userInterface.getRoomView().getBooking12HoursButton().setBackground(Color.WHITE);
        userInterface.getRoomView().getBooking24HoursButton().setBackground(Color.WHITE);
        userInterface.getRoomView().getBooking3HoursButton().setBackground(Color.WHITE);

        switch (roomStatus) {
            case 1:
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
                userInterface.getRoomView().getAddTimeButton().setVisible(true);

                userInterface.getRoomView().getPriceTextField().setText("0");

                //Hiding buttons that are not required
                userInterface.getRoomView().getRoomSellingButton().setVisible(false);
                userInterface.getRoomView().getEndTimeButton().setVisible(false);
                userInterface.getRoomView().getAddTimeButton().setEnabled(false);
                userInterface.getRoomView().getBooking12HoursButton().setVisible(true);
                userInterface.getRoomView().getBooking24HoursButton().setVisible(true);
                userInterface.getRoomView().getBooking3HoursButton().setVisible(true);
                userInterface.getRoomView().getRoomChangeButton().setVisible(false);
                break;
            case 2:
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
                userInterface.getRoomView().getAddTimeButton().setVisible(false);
                userInterface.getRoomView().getBooking12HoursButton().setVisible(false);
                userInterface.getRoomView().getBooking24HoursButton().setVisible(false);
                userInterface.getRoomView().getBooking3HoursButton().setVisible(false);
                userInterface.getRoomView().getRoomChangeButton().setVisible(false);
                break;
            case 3:
                userInterface.getRoomView().getRoomStatusBackground().setBackground(new Color(205, 97, 85));
                userInterface.getRoomView().getStatusLabel().setText("SERVICIO");

                userInterface.getRoomView().getRoomStatusInformative().setText("OCUPADA");

                //hiding all informative labels that are not required
                userInterface.getRoomView().getStartInformativeLabel().setVisible(true);
                userInterface.getRoomView().getRemainingInformativeLabel().setVisible(true);
                userInterface.getRoomView().getRemainingTimeLabel().setVisible(true);
                userInterface.getRoomView().getStartTimeLabel().setVisible(true);
                userInterface.getRoomView().getAddBigQuantityButton().setVisible(true);
                userInterface.getRoomView().getRemoveBigQuantity().setVisible(true);
                userInterface.getRoomView().getAddSmallQuantityButton().setVisible(true);
                userInterface.getRoomView().getRemoveSmallQuantityButton().setVisible(true);
                userInterface.getRoomView().getPriceTextField().setVisible(true);
                userInterface.getRoomView().getPrintingCheckBox().setVisible(true);
                userInterface.getRoomView().getStartDateLabel().setVisible(true);
                userInterface.getRoomView().getAddTimeButton().setVisible(true);

                userInterface.getRoomView().getPriceTextField().setText("0");

                //showing required buttons
                userInterface.getRoomView().getRoomSellingButton().setVisible(true);
                userInterface.getRoomView().getEndTimeButton().setVisible(true);
                userInterface.getRoomView().getAddTimeButton().setEnabled(false);
                userInterface.getRoomView().getBooking12HoursButton().setVisible(true);
                userInterface.getRoomView().getBooking24HoursButton().setVisible(true);
                userInterface.getRoomView().getBooking3HoursButton().setVisible(true);
                userInterface.getRoomView().getRoomChangeButton().setVisible(true);
                break;
        }
        userInterface.setRoomView();
    }

    private void roomSale(boolean receptionSale) {
        if (receptionSale) {
            motelManager.setCurrentFloorRoom(-1, -1);
            userInterface.getSellingView().getCourtesySaleButton().setVisible(true);
        } else {
            userInterface.getSellingView().getCourtesySaleButton().setVisible(false);
        }
        motelManager.restartSaleManager();
        String roomString = motelManager.getRoom(motelManager.getCurrentFloorViewed(), motelManager.getCurrentRoomViewed()).getRoomString();
        userInterface.getSellingView().getSellingToLabel().setText("VENIENDO A: " + roomString);
        userInterface.setSellingView();
        userInterface.getSellingView().getAddItemButton().setEnabled(false);
        userInterface.getSellingView().getItemDeleteButton().setEnabled(false);
        userInterface.getSellingView().getFinishSaleButton().setEnabled(false);
        userInterface.getSellingView().updateItemListed(motelManager.getInventoryData());
        userInterface.getSellingView().updateSellingListed(motelManager.getCurrentSellingList());
    }

    private void itemRemovedFromRegisterList() {
        JSONObject itemSelected = userInterface.getSellingView().getCurrentSelectedSellingListed(userInterface.getSellingView().getSellingTable().getSelectedRow());
        long itemID = itemSelected.getLong("itemID");
        motelManager.removeItemToSelling(itemID);
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();
        userInterface.getSellingView().getTotalPriceLabel().setText(String.valueOf(totalPrice));
        userInterface.getSellingView().updateSellingListed(motelManager.getCurrentSellingList());
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
        JSONObject itemSelected = new JSONObject(userInterface.getSellingView().getCurrentSelectedItemListed(userInterface.getSellingView().getItemTable().getSelectedRow()).toString());
        int quantity = Integer.parseInt(userInterface.getSellingView().getQuantityTextField().getText());
        long itemID = itemSelected.getLong("itemID");
        userInterface.getSellingView().getFinishSaleButton().setEnabled(true);
        motelManager.addItemToSelling(itemID, quantity, false);
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();
        userInterface.getSellingView().getTotalPriceLabel().setText(String.valueOf(totalPrice));
        userInterface.getSellingView().updateSellingListed(motelManager.getCurrentSellingList());
        userInterface.getSellingView().getItemTable().clearSelection();
        userInterface.getSellingView().getSellingTable().clearSelection();
        userInterface.getSellingView().getAddItemButton().setEnabled(false);
    }

    private void addCourtesyItemToRegister() {
        JSONObject itemSelected = new JSONObject(userInterface.getSellingView().getCurrentSelectedItemListed(userInterface.getSellingView().getItemTable().getSelectedRow()).toString());
        int quantity = Integer.parseInt(userInterface.getSellingView().getQuantityTextField().getText());
        long itemID = itemSelected.getLong("itemID");
        userInterface.getSellingView().getFinishSaleButton().setEnabled(true);
        motelManager.addItemToSelling(itemID, quantity, true);
        long totalPrice = motelManager.getCurrentTotalPriceSellingList();
        userInterface.getSellingView().getTotalPriceLabel().setText(String.valueOf(totalPrice));
        userInterface.getSellingView().updateSellingListed(motelManager.getCurrentSellingList());
        userInterface.getSellingView().getItemTable().clearSelection();
        userInterface.getSellingView().getSellingTable().clearSelection();
        userInterface.getSellingView().getAddItemButton().setEnabled(false);
    }

    private void finishSale() {
        boolean print = userInterface.getSellingView().getPrintingCheckBox().isSelected();
        if (!print) {
            boolean noPrintingConfirmation = userInterface.confirmPrinting();
            if (noPrintingConfirmation) {
                motelManager.roomSaleFinished(false);
                userInterface.setFloorView();
            }
        } else {
            motelManager.roomSaleFinished(true);
            userInterface.setFloorView();
        }
    }

    private void backFromSelling() {
        motelManager.restartSaleManager();
        userInterface.setFloorView();
    }

    private void roomTimeSale() {
        motelManager.timeInformationUpdate();
        int roomNumber = motelManager.getCurrentRoomViewed();
        int floorNumber = motelManager.getCurrentFloorViewed();
        int service = motelManager.getCurrentServiceDesired();
        int price = Integer.parseInt(userInterface.getRoomView().getPriceTextField().getText());
        boolean print = userInterface.getRoomView().getPrintingCheckBox().isSelected();
        if (!print) {
            boolean noPrintingConfirmation = userInterface.confirmPrinting();
            if (noPrintingConfirmation) {
                motelManager.registerRoomTimeAdded(floorNumber, roomNumber, service, price, false);
                userInterface.setFloorView();
            }
        } else {
            motelManager.registerRoomTimeAdded(floorNumber, roomNumber, service, price, true);
            userInterface.setFloorView();
        }
    }

    private void updateRoomPrice(long price) {
        long newPrice = Long.parseLong(userInterface.getRoomView().getPriceTextField().getText()) + price;
        userInterface.getRoomView().getPriceTextField().setText(String.valueOf(newPrice));
    }

    private void roomTimeModification(int amount) {
        //Adds the default price for each service
        userInterface.getRoomView().getAddTimeButton().setEnabled(true);
        switch (amount) {
            case 3:
                userInterface.getRoomView().getPriceTextField().setText("30000");
                userInterface.getRoomView().getBooking3HoursButton().setBackground(new Color(103, 159, 51));
                userInterface.getRoomView().getBooking12HoursButton().setBackground(Color.WHITE);
                userInterface.getRoomView().getBooking24HoursButton().setBackground(Color.WHITE);
                motelManager.setCurrentServiceDesired(amount);
                break;
            case 12:
                userInterface.getRoomView().getPriceTextField().setText("35000");
                userInterface.getRoomView().getBooking3HoursButton().setBackground(Color.WHITE);
                userInterface.getRoomView().getBooking12HoursButton().setBackground(new Color(103, 159, 51));
                userInterface.getRoomView().getBooking24HoursButton().setBackground(Color.WHITE);
                motelManager.setCurrentServiceDesired(amount);
                break;
            case 24:
                userInterface.getRoomView().getPriceTextField().setText("88000");
                userInterface.getRoomView().getBooking3HoursButton().setBackground(Color.WHITE);
                userInterface.getRoomView().getBooking12HoursButton().setBackground(Color.WHITE);
                userInterface.getRoomView().getBooking24HoursButton().setBackground(new Color(103, 159, 51));
                motelManager.setCurrentServiceDesired(amount);
                break;
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
        boolean isRoomChangeShown = userInterface.isRoomChangeShown();
        //We update each button for the current information required.
        if (isFloorShown) {
            int roomArray[] = motelManager.getRoomsArray();
            for (int floor = 0; floor < roomArray.length; floor++) {
                for (int room = 0; room < roomArray[floor]; room++) {
                    int status = motelManager.getRoom(floor, room).getStatus();
                    String roomString = motelManager.getRoom(floor, room).getRoomString();
                    switch (status) {
                        case 1:
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setText("<html><center>" + roomString + "</center></html>");
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setBackground(new Color(39, 174, 96));
                            break;
                        case 2:
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setText("<html><center>" + roomString + "</center></html>");
                            userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room).setBackground(new Color(84, 153, 199));
                            break;
                        case 3:
                            String remainingTime = motelManager.getRemainingTimeRoom(floor, room);
                            JButton roomButton = userInterface.getFloorView().getRoomButtonGrid().get(floor).get(room);
                            if (remainingTime.contains("-")) {
                                roomButton.setText("<html><center>" + roomString + "<br>SOBRETIEMPO</center></html>");
                                roomButton.setBackground(new Color(241, 196, 15)); // Yellow color
                            } else {
                                roomButton.setText("<html><center>" + roomString + "<br>QUEDAN " + remainingTime + "</center></html>");
                                roomButton.setBackground(new Color(231, 76, 60)); // Red color
                            }
                            break;
                        default:
                            // Optionally handle unexpected status values if needed
                            break;
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
                case 2:
                    String startTime = motelManager.getStartTimeRoom(floor, room);
                    userInterface.getRoomView().getStartTimeLabel().setText(startTime);
                    break;
                case 3:
                    String startTimeRoom = motelManager.getStartTimeRoom(floor, room);
                    String startDate = motelManager.getStartDateRoom(floor, room);
                    String remainingTime = motelManager.getRemainingTimeRoom(floor, room);
                    if (remainingTime.contains("-")) {
                        userInterface.getRoomView().getRoomStatusBackground().setBackground(new Color(241, 196, 15));
                        userInterface.getRoomView().getStatusLabel().setText("SOBRETIEMPO");
                        userInterface.getRoomView().getRoomStatusInformative().setText("SOBRETIEMPO");
                    }
                    userInterface.getRoomView().getStartTimeLabel().setText(startTimeRoom);
                    userInterface.getRoomView().getRemainingTimeLabel().setText(remainingTime);
                    userInterface.getRoomView().getStartDateLabel().setText(startDate);
                    break;
            }
        }
        if (isRoomChangeShown) {
            int roomArray[] = motelManager.getRoomsArray();
            for (int floor = 0; floor < roomArray.length; floor++) {
                for (int room = 0; room < roomArray[floor]; room++) {
                    int status = motelManager.getRoom(floor, room).getStatus();
                    String roomString = motelManager.getRoom(floor, room).getRoomString();
                    switch (status) {
                        case 1:
                            userInterface.getRoomChangeView().getRoomButtonGrid().get(floor).get(room).setText("<html><center>" + roomString + "</center></html>");
                            userInterface.getRoomChangeView().getRoomButtonGrid().get(floor).get(room).setBackground(new Color(39, 174, 96));
                            break;
                        case 2:
                            userInterface.getRoomChangeView().getRoomButtonGrid().get(floor).get(room).setText("<html><center>" + roomString + "</center></html>");
                            userInterface.getRoomChangeView().getRoomButtonGrid().get(floor).get(room).setBackground(new Color(84, 153, 199));
                            break;
                        case 3:
                            JButton roomButton = userInterface.getRoomChangeView().getRoomButtonGrid().get(floor).get(room);
                            roomButton.setText("<html><center>NO DISPONIBLE</center></html>");
                            roomButton.setBackground(new Color(231, 76, 60)); // Red color
                            break;
                        default:
                            // Optionally handle unexpected status values if needed
                            break;
                    }
                }
            }
        }
    }

    private void changeFloor(int i) {
        int currentFloorGUI = userInterface.getFloorView().getCurrentFloorIndex();
        userInterface.getFloorView().switchFloor(currentFloorGUI + i);
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

    private void managementTurnSelected() {
        userInterface.getTurnManagerView().getEndTurnButton().setEnabled(false);
        userInterface.getTurnManagerView().getPrintButton().setEnabled(false);
        userInterface.getTurnManagerView().setTurnDetailsData(motelManager.getCurrentTurnData());
        userInterface.getTurnManagerView().getNoPrintCheckBox().setSelected(false);
        userInterface.getTurnManagerView().getSummarizedPrintCheckBox().setSelected(false);
        userInterface.getTurnManagerView().getDetailedPrintCheckBox().setSelected(false);
        userInterface.setTurnManagerView();
    }

    private void managementInventorySelected() {
        userInterface.setInventoryView();
        setInventoryModificators(false);
    }

    private void managementHistorySelected() {
        userInterface.getHistoryView().setTurnHistoryDetails(motelManager.getHistoryData());
        userInterface.setHistoryView();
    }

    private void turnHistoryDetails() {
        int selectedRow = userInterface.getHistoryView().getTurnHistoryTable().getSelectedRow();
        userInterface.getHistoryView().getTurnDetailsView().setTurnDetailsData(motelManager.getDetailedTurnHistoryData(selectedRow));
        userInterface.getHistoryView().getPopupTurn().setVisible(true);
    }

    private void closeHistoryDetails() {
        userInterface.getHistoryView().getPopupTurn().setVisible(false);
    }

    private void printTurn() {
        motelManager.timeInformationUpdate();
        userInterface.getTurnManagerView().getBackButton().setEnabled(false);
        userInterface.getTurnManagerView().getEndTurnButton().setEnabled(true);
        motelManager.turnEnded();
        if (userInterface.getTurnManagerView().getNoPrintCheckBox().isSelected()) {
            motelManager.turnEndPrint(1);
        } else if (userInterface.getTurnManagerView().getSummarizedPrintCheckBox().isSelected()) {
            motelManager.turnEndPrint(2);
        } else if (userInterface.getTurnManagerView().getDetailedPrintCheckBox().isSelected()) {
            motelManager.turnEndPrint(3);
        }
    }

    private void turnChange() {
        userInterface.setTurnSelectView();
    }
}
