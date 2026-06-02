package view;

import view.helpers.NavigationState;
import java.awt.CardLayout;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import view.interfaces.TimeLabelInterface;

/**
 *
 * @author Santiago
 */
public class UserGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private TurnSelectView turnSelect;
    private FloorView floorView;
    private RoomView roomView;
    private SellingView sellingView;
    private ManagementSelectView managementSelection;
    private InventoryManagementView inventoryView;
    private HistoryView historyView;
    private TurnManagerView turnManagerView;
    private RoomChangeView roomChangeView;
    private AppOptionsView appOptions;
    private PrinterConfigurationView printerConfigView;
    private MotelDataConfigurationView motelDataConfigView;
    private DataSavingConfigurationView dataSavingConfigView;
    private FloorConfigurationView floorConfigView;
    private RoomConfigurationView roomConfigView;
    private TimeConfigurationView timeConfigView;
    private SpendingRegisterView spendingRegisterView;
    private ExtraTurnChangesView extraTurnChangesView;
    private RoomSummaryView roomSummaryView;
    private ExportConfigurationView exportConfigView;
    private EmailConfigurationView emailConfigView;

    private ViewCard currentCard;
    private Map<ViewCard, TimeLabelInterface> timeDisplayPanels;
    private NavigationState navigationState;
    private String lastTimeShown;
    private String lastDateShown;

    public UserGUI() {
        timeDisplayPanels = new EnumMap<>(ViewCard.class);
        navigationState = new NavigationState();
        initComponents();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Creation of the different views, and assignation to the cardLayout
        turnSelect = new TurnSelectView();
        addView(turnSelect, ViewCard.TURN_SELECT_VIEW);

        floorView = new FloorView(navigationState);
        addView(floorView, ViewCard.FLOOR_VIEW);

        roomView = new RoomView();
        addView(roomView, ViewCard.ROOM_VIEW);

        sellingView = new SellingView();
        addView(sellingView, ViewCard.SELLING_VIEW);

        managementSelection = new ManagementSelectView();
        addView(managementSelection, ViewCard.MANAGEMENT_SELECT_VIEW);

        inventoryView = new InventoryManagementView();
        addView(inventoryView, ViewCard.INVENTORY_VIEW);

        historyView = new HistoryView();
        addView(historyView, ViewCard.HISTORY_VIEW);

        turnManagerView = new TurnManagerView();
        addView(turnManagerView, ViewCard.TURN_MANAGER_VIEW);

        roomChangeView = new RoomChangeView(navigationState);
        addView(roomChangeView, ViewCard.ROOM_CHANGE_VIEW);

        appOptions = new AppOptionsView();
        addView(getAppOptions(), ViewCard.APP_OPTIONS_VIEW);

        printerConfigView = new PrinterConfigurationView();
        addView(printerConfigView, ViewCard.PRINTER_CONFIG_VIEW);

        motelDataConfigView = new MotelDataConfigurationView();
        addView(motelDataConfigView, ViewCard.MOTEL_DATA_CONFIG_VIEW);

        dataSavingConfigView = new DataSavingConfigurationView();
        addView(dataSavingConfigView, ViewCard.DATA_SAVING_CONFIG_VIEW);

        floorConfigView = new FloorConfigurationView();
        addView(floorConfigView, ViewCard.FLOOR_CONFIG_VIEW);

        roomConfigView = new RoomConfigurationView();
        addView(roomConfigView, ViewCard.ROOM_CONFIG_VIEW);

        timeConfigView = new TimeConfigurationView();
        addView(timeConfigView, ViewCard.TIME_CONFIG_VIEW);

        spendingRegisterView = new SpendingRegisterView();
        addView(spendingRegisterView, ViewCard.SPENDING_REGISTER_VIEW);

        extraTurnChangesView = new ExtraTurnChangesView();
        addView(extraTurnChangesView, ViewCard.EXTRA_TURN_CHANGES_VIEW);

        roomSummaryView = new RoomSummaryView();
        addView(roomSummaryView, ViewCard.ROOM_SUMMARY_VIEW);

        exportConfigView = new ExportConfigurationView();
        addView(getExportConfigView(), ViewCard.EXPORT_CONFIG_VIEW);

        emailConfigView = new EmailConfigurationView();
        addView(emailConfigView, ViewCard.EMAIL_CONFIG_VIEW);

        //Default window configuration
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        add(mainPanel);
        setVisible(true);
    }

    private void addView(JPanel panel, ViewCard card) {
        String name = card.getCardName();
        panel.setName(name);
        mainPanel.add(panel, name);
        if (panel instanceof TimeLabelInterface htl) {
            timeDisplayPanels.put(card, htl);
        }
    }

    public void setupFloors(int[][] arr) {
        floorView.createButtonsForTowers(arr);
        roomChangeView.createButtonsForTowers(arr);
    }

    public boolean isFloorShown() {
        return ViewCard.FLOOR_VIEW.equals(currentCard);
    }

    public boolean isRoomShown() {
        return ViewCard.ROOM_VIEW.equals(currentCard);
    }

    public boolean isRoomChangeShown() {
        return ViewCard.ROOM_CHANGE_VIEW.equals(currentCard);
    }

    public boolean isRoomSummaryShown() {
        return ViewCard.ROOM_SUMMARY_VIEW.equals(currentCard);
    }

    public void updateDateTime(String timeShown, String dateShown) {
        lastTimeShown = timeShown;
        lastDateShown = dateShown;
        if (currentCard != null) {
            TimeLabelInterface panel = timeDisplayPanels.get(currentCard);
            if (panel != null) {
                panel.updateTimeDisplay(timeShown, dateShown);
            }
        }
    }

    public void setFloorView() {
        setCard(ViewCard.FLOOR_VIEW);
    }

    public void setTurnSelect() {
        setCard(ViewCard.TURN_SELECT_VIEW);
    }

    public void setTurnManagerView() {
        setCard(ViewCard.TURN_MANAGER_VIEW);
    }

    public void setRoomView() {
        setCard(ViewCard.ROOM_VIEW);
    }

    public void setManagementSelection() {
        setCard(ViewCard.MANAGEMENT_SELECT_VIEW);
    }

    public void setInventoryView() {
        setCard(ViewCard.INVENTORY_VIEW);
    }

    public void setSellingView() {
        setCard(ViewCard.SELLING_VIEW);
    }

    public void setHistoryView() {
        setCard(ViewCard.HISTORY_VIEW);
    }

    public void setRoomChangeView() {
        setCard(ViewCard.ROOM_CHANGE_VIEW);
    }

    public void setAppOptionsView() {
        setCard(ViewCard.APP_OPTIONS_VIEW);
    }

    public void setPrinterConfigView() {
        setCard(ViewCard.PRINTER_CONFIG_VIEW);
    }

    public void setMotelDataConfigView() {
        setCard(ViewCard.MOTEL_DATA_CONFIG_VIEW);
    }

    public void setDataSavingConfigView() {
        setCard(ViewCard.DATA_SAVING_CONFIG_VIEW);
    }

    public void setFloorConfigView() {
        setCard(ViewCard.FLOOR_CONFIG_VIEW);
    }

    public void setTimeConfigView() {
        setCard(ViewCard.TIME_CONFIG_VIEW);
    }

    public void setSpendingRegisterView() {
        setCard(ViewCard.SPENDING_REGISTER_VIEW);
    }

    public void setExtraTurnChangesView() {
        setCard(ViewCard.EXTRA_TURN_CHANGES_VIEW);
    }

    public void setRoomSummaryView() {
        setCard(ViewCard.ROOM_SUMMARY_VIEW);
    }

    public void setExportConfigView() {
        setCard(ViewCard.EXPORT_CONFIG_VIEW);
    }

    public void setEmailConfigView() {
        setCard(ViewCard.EMAIL_CONFIG_VIEW);
    }

    private void setCard(ViewCard card) {
        currentCard = card;
        cardLayout.show(mainPanel, card.getCardName());
        if (lastTimeShown != null) {
            updateDateTime(lastTimeShown, lastDateShown);
        }
    }

    /**
     * @return the turnSelect
     */
    public TurnSelectView getTurnSelect() {
        return turnSelect;
    }

    /**
     * @return the floorView
     */
    public FloorView getFloorView() {
        return floorView;
    }

    /**
     * @return the roomView
     */
    public RoomView getRoomView() {
        return roomView;
    }

    /**
     * @return the sellingView
     */
    public SellingView getSellingView() {
        return sellingView;
    }

    /**
     * @return the managementSelection
     */
    public ManagementSelectView getManagementSelection() {
        return managementSelection;
    }

    /**
     * @return the inventoryView
     */
    public InventoryManagementView getInventoryView() {
        return inventoryView;
    }

    /**
     * @return the historyView
     */
    public HistoryView getHistoryView() {
        return historyView;
    }

    /**
     * @return the turnManagerView
     */
    public TurnManagerView getTurnManagerView() {
        return turnManagerView;
    }

    /**
     * @return the roomChangeView
     */
    public RoomChangeView getRoomChangeView() {
        return roomChangeView;
    }

    /**
     * @return the appOptions
     */
    public AppOptionsView getAppOptions() {
        return appOptions;
    }

    public PrinterConfigurationView getPrinterConfigView() {
        return printerConfigView;
    }

    public MotelDataConfigurationView getMotelDataConfigView() {
        return motelDataConfigView;
    }

    public DataSavingConfigurationView getDataSavingConfigView() {
        return dataSavingConfigView;
    }

    public FloorConfigurationView getFloorConfigView() {
        return floorConfigView;
    }

    public RoomConfigurationView getRoomConfigView() {
        return roomConfigView;
    }

    public void setRoomConfigView() {
        setCard(ViewCard.ROOM_CONFIG_VIEW);
    }

    public TimeConfigurationView getTimeConfigView() {
        return timeConfigView;
    }

    /**
     * @return the spendingRegisterView
     */
    public SpendingRegisterView getSpendingRegisterView() {
        return spendingRegisterView;
    }

    /**
     * @return the extraTurnChangesView
     */
    public ExtraTurnChangesView getExtraTurnChangesView() {
        return extraTurnChangesView;
    }

    /**
     * @return the roomSummaryView
     */
    public RoomSummaryView getRoomSummaryView() {
        return roomSummaryView;
    }

    public ExportConfigurationView getExportConfigView() {
        return exportConfigView;
    }

    public EmailConfigurationView getEmailConfigView() {
        return emailConfigView;
    }

}
