package view;

import java.awt.CardLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
    private SpendingRegisterView spendingRegisterView;
    private ExtraTurnChangesView extraTurnChangesView;
    private RoomSummaryView roomSummaryView;

    private String currentCard;
    private Map<String, JLabel> timeLabels;
    private Map<String, JLabel> dateLabels;

    public UserGUI() {
        timeLabels = new HashMap<>();
        dateLabels = new HashMap<>();
        initComponents();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Creation of the different views, and assignation to the cardLayout
        turnSelect = new TurnSelectView();
        addView(turnSelect, "turnSelectView");

        floorView = new FloorView();
        addView(floorView, "floorView");

        roomView = new RoomView();
        addView(roomView, "roomView");

        sellingView = new SellingView();
        addView(sellingView, "sellingView");

        managementSelection = new ManagementSelectView();
        addView(managementSelection, "managementSelectView");

        inventoryView = new InventoryManagementView();
        addView(inventoryView, "inventoryView");

        historyView = new HistoryView();
        addView(historyView, "historyView");

        turnManagerView = new TurnManagerView();
        addView(turnManagerView, "turnManagerView");

        roomChangeView = new RoomChangeView();
        addView(roomChangeView, "roomChangeView");

        appOptions = new AppOptionsView();
        addView(getAppOptions(), "appOptionsView");

        spendingRegisterView = new SpendingRegisterView();
        addView(spendingRegisterView, "spendingRegisterView");

        extraTurnChangesView = new ExtraTurnChangesView();
        addView(extraTurnChangesView, "extraTurnChangesView");

        roomSummaryView = new RoomSummaryView();
        addView(roomSummaryView, "roomSummaryView");

        //Default window configuration
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        add(mainPanel);
        setVisible(true);
    }

    private void addView(JPanel panel, String name) {
        panel.setName(name);
        mainPanel.add(panel, name);
        if (panel instanceof TimeLabelInterface htl) {
            timeLabels.put(name, htl.getTimeLabel());
            dateLabels.put(name, htl.getDateLabel());
        }
    }

    public void setupFloors(int[][] arr) {
        floorView.createButtonsForTowers(arr);
        roomChangeView.createButtonsForTowers(arr);
    }

    private String getCurrentCard() {
        return currentCard;
    }

    public boolean isFloorShown() {
        return "floorView".equals(currentCard);
    }

    public boolean isRoomShown() {
        return "roomView".equals(currentCard);
    }

    public boolean isRoomChangeShown() {
        return "roomChangeView".equals(currentCard);
    }

    public boolean isRoomSummaryShown() {
        return "roomSummaryView".equals(currentCard);
    }

    public void updateDateTime(String timeShown, String dateShown) {
        String currentCard = getCurrentCard();
        if (currentCard != null) {
            JLabel timeLabel = timeLabels.get(currentCard);
            JLabel dateLabel = dateLabels.get(currentCard);
            if (timeLabel != null && dateLabel != null) {
                timeLabel.setText(timeShown);
                dateLabel.setText(dateShown);
            }
        }
    }

    public void setFloorView() {
        currentCard = "floorView";
        cardLayout.show(mainPanel, "floorView");
    }

    public void setTurnSelect() {
        currentCard = "turnSelectView";
        cardLayout.show(mainPanel, "turnSelectView");
    }

    public void setTurnManagerView() {
        currentCard = "turnManagerView";
        cardLayout.show(mainPanel, "turnManagerView");
    }

    public void setRoomView() {
        currentCard = "roomView";
        cardLayout.show(mainPanel, "roomView");
    }

    public void setManagementSelection() {
        currentCard = "managementSelectView";
        cardLayout.show(mainPanel, "managementSelectView");
    }

    public void setInventoryView() {
        currentCard = "inventoryView";
        cardLayout.show(mainPanel, "inventoryView");
    }

    public void setSellingView() {
        currentCard = "sellingView";
        cardLayout.show(mainPanel, "sellingView");
    }

    public void setHistoryView() {
        currentCard = "historyView";
        cardLayout.show(mainPanel, "historyView");
    }

    public void setRoomChangeView() {
        currentCard = "roomChangeView";
        cardLayout.show(mainPanel, "roomChangeView");
    }

    public void setAppOptionsView() {
        currentCard = "appOptionsView";
        cardLayout.show(mainPanel, "appOptionsView");
    }

    public void setSpendingRegisterView() {
        currentCard = "spendingRegisterView";
        cardLayout.show(mainPanel, "spendingRegisterView");
    }

    public void setExtraTurnChangesView() {
        currentCard = "extraTurnChangesView";
        cardLayout.show(mainPanel, "extraTurnChangesView");
    }

    public void setRoomSummaryView() {
        currentCard = "roomSummaryView";
        cardLayout.show(mainPanel, "roomSummaryView");
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

    public boolean confirmPrinting() {
        int response = JOptionPane.showConfirmDialog(
                null,
                "¿ESTA SEGURO DE NO IMPRIMIR RECIBO?",
                "CONFIRMACION",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return response == JOptionPane.YES_OPTION;
    }

    public boolean confirmTurnEnd() {
        int response = JOptionPane.showConfirmDialog(
                null,
                "¿ESTA SEGURO DE TERMINAR EL TURNO?",
                "CONFIRMACION",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * @return the roomChangeView
     */
    public RoomChangeView getRoomChangeView() {
        return roomChangeView;
    }

    public void showInfoMessage(String message, String title) {
        JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * @return the appOptions
     */
    public AppOptionsView getAppOptions() {
        return appOptions;
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

}
