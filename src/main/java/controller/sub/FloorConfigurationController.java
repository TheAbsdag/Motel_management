package controller.sub;

import javax.swing.event.ListSelectionEvent;
import model.Room;
import model.RoomTime;
import model.ProgramConfig;
import model.modelManagers.MotelManagement;
import org.json.JSONArray;
import org.json.JSONObject;
import view.FloorConfigurationView;
import view.RoomConfigurationView;
import view.helpers.DialogHelper;
import view.helpers.InputParser;

/**
 * Controls the floor/room configuration view. Wires tower and floor lists,
 * manages CRUD operations (add/remove towers, floors, and rooms), handles
 * room time/pricing edits via {@link RoomConfigurationView}, and persists
 * configuration changes through the model layer with confirmation dialogs.
 *
 * @author SECC
 */
public class FloorConfigurationController {

    private final MotelManagement motelManager;
    private final FloorConfigurationView view;
    private final RoomConfigurationView roomConfigView;
    private final Runnable saveMainFiles;
    private final Runnable rebuildFloorView;
    private final Runnable onBack;
    private final Runnable showRoomConfigCard;
    private final Runnable showFloorConfigCard;

    private boolean initializing;

    public FloorConfigurationController(MotelManagement motelManager,
                                        FloorConfigurationView view,
                                        RoomConfigurationView roomConfigView,
                                        Runnable saveMainFiles,
                                        Runnable rebuildFloorView,
                                        Runnable onBack,
                                        Runnable showRoomConfigCard,
                                        Runnable showFloorConfigCard) {
        this.motelManager = motelManager;
        this.view = view;
        this.roomConfigView = roomConfigView;
        this.saveMainFiles = saveMainFiles;
        this.rebuildFloorView = rebuildFloorView;
        this.onBack = onBack;
        this.showRoomConfigCard = showRoomConfigCard;
        this.showFloorConfigCard = showFloorConfigCard;
        this.initializing = false;
    }

    public void initListeners() {
        view.onTowerSelection(this::onTowerSelected);
        view.onFloorSelection(this::onFloorSelected);

        view.onTowerListLeft(() -> scrollTowerList(-1));
        view.onTowerListRight(() -> scrollTowerList(1));
        view.onFloorListUp(() -> scrollFloorList(-1));
        view.onFloorListDown(() -> scrollFloorList(1));

        view.onNewTower(this::addNewTower);
        view.onDeleteTower(this::deleteTower);
        view.onDeleteFloor(this::deleteFloor);
        view.onNewRoomButton(this::addNewRoom);
        view.onNewFloor(this::addNewFloor);

        view.onBackButton(this::onBackPressed);
        view.onSaveButton(this::onSavePressed);

        roomConfigView.onBackButton(this::onRoomConfigBack);
        roomConfigView.onSaveButton(this::onRoomConfigSave);
        roomConfigView.onDeleteRoom(this::onDeleteRoom);
    }

    // ========== View Population ==========

    public void populateView() {
        initializing = true;
        populateTowerList();
        populateFloorList();

        int[][] roomsArray = motelManager.getRoomsArray();
        if (roomsArray.length > 0) {
            view.createRoomButtons(roomsArray);
            wireRoomButtons();
            view.switchTower(0);
        }

        view.clearDirty();
        initializing = false;
    }

    private void populateTowerList() {
        JSONArray roomsPerTower = motelManager.getProgramConfig().getRoomsPerTower();
        if (roomsPerTower != null) {
            String[] items = new String[roomsPerTower.length()];
            for (int i = 0; i < roomsPerTower.length(); i++) {
                JSONObject tower = roomsPerTower.getJSONObject(i);
                items[i] = "Torre " + tower.getInt("towerNumber");
            }
            view.setTowerItems(items);
        }
    }

    private void populateFloorList() {
        JSONArray roomsPerTower = motelManager.getProgramConfig().getRoomsPerTower();
        int towerIndex = view.getCurrentTowerIndex();
        if (roomsPerTower != null && towerIndex < roomsPerTower.length()) {
            JSONObject tower = roomsPerTower.getJSONObject(towerIndex);
            JSONArray towerRooms = tower.getJSONArray("towerRooms");
            String[] items = new String[towerRooms.length()];
            for (int i = 0; i < towerRooms.length(); i++) {
                JSONObject floorData = towerRooms.getJSONObject(i);
                items[i] = "Piso " + (floorData.getInt("floor") + 1);
            }
            view.setFloorItems(items);
        }
    }

    // ========== List Selection ==========

    private void onTowerSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || initializing) return;
        int index = view.getSelectedTowerIndex();
        if (index >= 0) {
            view.switchTower(index);
            populateFloorList();
            wireRoomButtons();
            view.switchToFloorGrid();
        }
    }

    private void onFloorSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || initializing) return;
        int index = view.getSelectedFloorIndex();
        if (index >= 0) {
            view.switchFloor(index);
        }
    }

    // ========== Tower/Floor Scroll ==========

    private void scrollTowerList(int direction) {
        int current = view.getSelectedTowerIndex();
        int newIndex = current + direction;
        if (newIndex >= 0 && newIndex < view.getTowerCount()) {
            view.selectTower(newIndex);
        }
    }

    private void scrollFloorList(int direction) {
        int current = view.getSelectedFloorIndex();
        int newIndex = current + direction;
        if (newIndex >= 0 && newIndex < view.getFloorCount()) {
            view.selectFloor(newIndex);
        }
    }

    // ========== Room Button Wiring ==========

    private void wireRoomButtons() {
        int[][] roomsArray = motelManager.getRoomsArray();
        for (int tower = 0; tower < roomsArray.length; tower++) {
            for (int floor = 0; floor < roomsArray[tower].length; floor++) {
                for (int room = 0; room < roomsArray[tower][floor]; room++) {
                    final int t = tower, f = floor, r = room;
                    view.onRoomClick(t, f, r, () -> onRoomClicked(t, f, r));
                    view.setRoomButtonText(t, f, r, String.valueOf(room + 1));
                }
            }
        }
    }

    // ========== Configuration Actions ==========

    private void onRoomClicked(int tower, int floor, int room) {
        Room roomData = motelManager.getRoom(tower, floor, room);
        roomConfigView.loadRoom(tower, floor, room, roomData);
        showRoomConfigCard.run();
    }

    private void onRoomConfigBack() {
        if (roomConfigView.isDirty()) {
            boolean discard = DialogHelper.confirmDialog(
                    "Hay cambios sin guardar en la configuracion de la habitacion. Perdera los cambios?",
                    "CAMBIOS SIN GUARDAR");
            if (!discard) {
                return;
            }
        }
        showFloorConfigCard.run();
        view.switchToFloorGrid();
    }

    private void onRoomConfigSave() {
        boolean confirm = DialogHelper.confirmDialog(
                "Guardar cambios de configuracion para habitacion "
                        + roomConfigView.getModifiedRoomString() + "?",
                "CONFIRMAR GUARDAR");
        if (!confirm) return;

        int tower = roomConfigView.getCurrentTower();
        int floor = roomConfigView.getCurrentFloor();
        int room = roomConfigView.getCurrentRoom();

        motelManager.getRoomManager().setRoomCustomTimeData(
                tower, floor, room, roomConfigView.getModifiedTimeSlots());

        String newName = roomConfigView.getModifiedRoomString();
        motelManager.getProgramConfig().setRoomString(
                tower, floor, room, newName);
        motelManager.getRoomManager().setRoomString(tower, floor, room, newName);

        roomConfigView.clearDirty();
        view.markDirty();
        showFloorConfigCard.run();
        view.switchToFloorGrid();

        updateRoomButtonLabels();
    }

    private void onDeleteRoom() {
        boolean confirm = DialogHelper.confirmDialog(
                "Esta seguro de eliminar la habitacion "
                        + roomConfigView.getModifiedRoomString() + "?",
                "CONFIRMAR ELIMINACION");
        if (!confirm) return;

        int tower = roomConfigView.getCurrentTower();
        int floor = roomConfigView.getCurrentFloor();
        int roomIdx = roomConfigView.getCurrentRoom();

        motelManager.getProgramConfig().removeRoomFromFloor(tower, floor, roomIdx);
        motelManager.getRoomManager().removeRoomFromGrid(tower, floor, roomIdx);
        view.markDirty();

        int[][] roomsArray = motelManager.getRoomsArray();
        view.createRoomButtons(roomsArray);
        wireRoomButtons();
        populateFloorList();

        view.selectTower(tower);
        view.selectFloor(floor);
        view.setTowerAndFloor(tower, floor);

        showFloorConfigCard.run();
        view.switchToFloorGrid();
    }

    private void addNewTower() {
        int nextTowerNum = 1;
        JSONArray roomsPerTower = motelManager.getProgramConfig().getRoomsPerTower();
        if (roomsPerTower != null && roomsPerTower.length() > 0) {
            int maxNum = 0;
            for (int i = 0; i < roomsPerTower.length(); i++) {
                int num = roomsPerTower.getJSONObject(i).getInt("towerNumber");
                if (num > maxNum) maxNum = num;
            }
            nextTowerNum = maxNum + 1;
        }

        Integer towerNum = DialogHelper.showNumericInputDialog(
                "Numero de la nueva torre:", "NUEVA TORRE",
                String.valueOf(nextTowerNum));
        if (towerNum == null) return;

        int floors = 1;
        JSONArray towerRooms = new JSONArray();
        for (int f = 0; f < floors; f++) {
            JSONObject floorData = new JSONObject();
            floorData.put("floor", f);
            floorData.put("rooms", new JSONArray());
            towerRooms.put(floorData);
        }

        motelManager.getProgramConfig().addTower(towerNum, floors, towerRooms);
        motelManager.getRoomManager().addTowerToGrid(towerNum, floors);
        view.markDirty();

        int[][] roomsArray = motelManager.getRoomsArray();
        view.createRoomButtons(roomsArray);
        wireRoomButtons();

        populateTowerList();
        int newIndex = view.getTowerCount() - 1;
        view.selectTower(newIndex);
        populateFloorList();
        view.setTowerAndFloor(newIndex, 0);
    }

    private void deleteTower() {
        int towerIndex = view.getSelectedTowerIndex();
        if (towerIndex < 0) return;

        boolean confirm = DialogHelper.confirmDialog(
                "Esta seguro de eliminar la torre " + (towerIndex + 1) + " y todas sus habitaciones?",
                "CONFIRMAR ELIMINACION");
        if (!confirm) return;

        motelManager.getProgramConfig().removeTower(towerIndex);
        motelManager.getRoomManager().removeTowerFromGrid(towerIndex);
        view.markDirty();

        int[][] roomsArray = motelManager.getRoomsArray();
        if (roomsArray.length > 0) {
            view.createRoomButtons(roomsArray);
            wireRoomButtons();
        }
        populateTowerList();
        populateFloorList();

        if (roomsArray.length > 0) {
            int newTowerIdx = Math.min(towerIndex, roomsArray.length - 1);
            view.selectTower(newTowerIdx);
            view.setTowerAndFloor(newTowerIdx, 0);
        }
    }

    private void deleteFloor() {
        int towerIndex = view.getCurrentTowerIndex();
        int floorListIndex = view.getSelectedFloorIndex();
        if (floorListIndex < 0) return;

        boolean confirm = DialogHelper.confirmDialog(
                "Esta seguro de eliminar el piso " + (floorListIndex + 1)
                        + " y todas sus habitaciones?",
                "CONFIRMAR ELIMINACION");
        if (!confirm) return;

        motelManager.getProgramConfig().removeFloorFromTower(towerIndex, floorListIndex);
        motelManager.getRoomManager().removeFloorFromGrid(towerIndex, floorListIndex);
        view.markDirty();

        int[][] roomsArray = motelManager.getRoomsArray();
        view.createRoomButtons(roomsArray);
        wireRoomButtons();

        populateFloorList();
        int newFloorIdx = Math.min(floorListIndex,
                view.getFloorCount() - 1);
        if (newFloorIdx >= 0) {
            view.selectFloor(newFloorIdx);
            view.setTowerAndFloor(towerIndex, newFloorIdx);
        }
    }

    private void addNewFloor() {
        int towerIndex = view.getCurrentTowerIndex();
        JSONArray roomsPerTower = motelManager.getProgramConfig().getRoomsPerTower();
        if (roomsPerTower == null || towerIndex >= roomsPerTower.length()) return;

        JSONObject tower = roomsPerTower.getJSONObject(towerIndex);
        int floorNumber = tower.getInt("towerFloors");

        motelManager.getProgramConfig().addFloorToTower(towerIndex, floorNumber, 0);
        motelManager.getRoomManager().addFloorToGrid(towerIndex, floorNumber);
        view.markDirty();

        int[][] roomsArray = motelManager.getRoomsArray();
        view.createRoomButtons(roomsArray);
        wireRoomButtons();

        populateFloorList();
        int newFloorIdx = view.getFloorCount() - 1;
        if (newFloorIdx >= 0) {
            view.selectFloor(newFloorIdx);
            view.setTowerAndFloor(towerIndex, newFloorIdx);
        }
    }

    private void addNewRoom() {
        int towerIndex = view.getCurrentTowerIndex();
        int floorListIndex = view.getSelectedFloorIndex();
        if (floorListIndex < 0) {
            DialogHelper.showInfoMessage("Seleccione un piso primero", "ERROR");
            return;
        }

        JSONArray roomsPerTower = motelManager.getProgramConfig().getRoomsPerTower();
        if (roomsPerTower == null || towerIndex >= roomsPerTower.length()) return;

        JSONObject tower = roomsPerTower.getJSONObject(towerIndex);
        JSONArray towerRooms = tower.getJSONArray("towerRooms");
        if (floorListIndex >= towerRooms.length()) return;

        JSONObject floorData = towerRooms.getJSONObject(floorListIndex);
        JSONArray rooms = floorData.getJSONArray("rooms");
        int floorNumber = floorData.getInt("floor");
        int newRoomNumber = rooms.length();

        int towerNum = tower.getInt("towerNumber");
        String defaultName = ProgramConfig.buildRoomString(towerNum, floorNumber, newRoomNumber);

        motelManager.getProgramConfig().addRoomToFloor(towerIndex, floorListIndex,
                defaultName, floorNumber, newRoomNumber);
        motelManager.getRoomManager().addRoomToGrid(towerIndex, floorListIndex, floorNumber,
                newRoomNumber, defaultName, towerNum);
        view.markDirty();

        int[][] roomsArray = motelManager.getRoomsArray();
        view.createRoomButtons(roomsArray);
        wireRoomButtons();
        populateFloorList();

        view.selectTower(towerIndex);
        view.selectFloor(floorListIndex);
        view.setTowerAndFloor(towerIndex, floorListIndex);
    }

    private void updateRoomButtonLabels() {
        int[][] roomsArray = motelManager.getRoomsArray();
        for (int tower = 0; tower < roomsArray.length; tower++) {
            for (int floor = 0; floor < roomsArray[tower].length; floor++) {
                for (int room = 0; room < roomsArray[tower][floor]; room++) {
                    view.setRoomButtonText(tower, floor, room,
                            motelManager.getRoom(tower, floor, room).getRoomString());
                }
            }
        }
    }

    // ========== Save / Back ==========

    private void onSavePressed() {
        boolean confirm = DialogHelper.confirmDialog(
                "Guardar todos los cambios de configuracion?",
                "CONFIRMAR GUARDAR");
        if (!confirm) return;

        motelManager.getProgramConfig().ensureSchemaVersion();
        saveMainFiles.run();
        view.clearDirty();
        roomConfigView.clearDirty();
        rebuildFloorView.run();

        DialogHelper.showInfoMessage("Configuracion guardada exitosamente", "GUARDADO");
    }

    private void onBackPressed() {
        if (view.isDirty() || roomConfigView.isDirty()) {
            boolean discard = DialogHelper.confirmDialog(
                    "Hay cambios sin guardar en la configuracion. Perdera los cambios?",
                    "CAMBIOS SIN GUARDAR");
            if (!discard) {
                return;
            }
            motelManager.revertToSavedConfig();
            rebuildFloorView.run();
        }
        onBack.run();
    }

    // ========== First Boot Support ==========

    /**
     * Configures this controller for the first-boot flow.
     * Disables the back button and overrides save to call the provided callback
     * instead of returning to the options hub.
     *
     * @param onCompleted callback invoked after successful save (next step in flow)
     */
    public void configureForFirstBoot(Runnable onCompleted) {
        view.removeBackListeners();
        view.setBackEnabled(false);

        // Re-wire save button: after saving, proceed to next step instead of returning to options hub
        view.removeSaveListeners();
        view.onSaveButton(() -> {
            boolean confirm = DialogHelper.confirmDialog(
                    "Guardar todos los cambios de configuracion?",
                    "CONFIRMAR GUARDAR");
            if (!confirm) return;

            motelManager.getProgramConfig().ensureSchemaVersion();
            saveMainFiles.run();
            view.clearDirty();
            roomConfigView.clearDirty();
            rebuildFloorView.run();
            DialogHelper.showInfoMessage("Configuracion guardada exitosamente", "GUARDADO");
            onCompleted.run();
        });
    }
}
