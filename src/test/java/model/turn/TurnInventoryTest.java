package model.turn;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import model.Turn;
import model.json.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurnInventoryTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final ZonedDateTime START = ZonedDateTime.of(2026, 11, 17, 20, 30, 0, 0, BOGOTA);
    private static final Instant START_INSTANT = START.toInstant();

    @Test
    void shouldRoundtripAMovement() throws Exception {
        InventoryMovement original = movement(MovementType.INCREASE, 6, "COMPRA", 30_000L);

        String json = ObjectMapperFactory.get().writeValueAsString(original);
        InventoryMovement restored = ObjectMapperFactory.get().readValue(json, InventoryMovement.class);

        assertThat(restored).isEqualTo(original);
        assertThat(json).contains("\"movementType\" : \"increase\"");
    }

    @Test
    void shouldRoundtripAnSnapshotItem() throws Exception {
        InventorySnapshotItem original = new InventorySnapshotItem(7L, "Cerveza", 4, 2);

        String json = ObjectMapperFactory.get().writeValueAsString(original);
        InventorySnapshotItem restored = ObjectMapperFactory.get().readValue(json, InventorySnapshotItem.class);

        assertThat(restored).isEqualTo(original);
    }

    @Test
    void shouldPersistTheThreeInventoryListsAndStampVersion4() {
        TurnDetails details = new TurnDetails(42L, START, true);
        details.setOpeningInventory(List.of(new InventorySnapshotItem(1L, "Cerveza", 4, 2)));
        details.setClosingInventory(List.of(new InventorySnapshotItem(1L, "Cerveza", 8, 2)));
        details.addInventoryMovement(movement(MovementType.INCREASE, 4, "COMPRA", 20_000L));

        String json = details.toJson();
        TurnDetails restored = TurnDetails.fromJson(json);

        assertThat(json).contains("\"version\" : 4");
        assertThat(restored.getOpeningInventory()).containsExactly(new InventorySnapshotItem(1L, "Cerveza", 4, 2));
        assertThat(restored.getClosingInventory()).containsExactly(new InventorySnapshotItem(1L, "Cerveza", 8, 2));
        assertThat(restored.getInventoryMovements()).hasSize(1);
    }

    @Test
    void shouldLoadALegacyTurnWithoutInventoryListsAsEmpty() {
        String legacy = """
                {"turnNumber":1,"turnStart":"2026-11-17T20:30:00-05:00[America/Bogota]",
                 "isTurnActive":true,"version":3,"turnActivity":[]}""";

        TurnDetails details = TurnDetails.fromJson(legacy);

        assertThat(details.getInventoryMovements()).isEmpty();
        assertThat(details.getOpeningInventory()).isEmpty();
        assertThat(details.getClosingInventory()).isEmpty();
    }

    @Test
    void shouldNotChangeFinancialTotalsWhenInventoryMoves() {
        TurnDetails details = new TurnDetails(42L, START, true);
        details.addInventoryMovement(movement(MovementType.INCREASE, 6, "COMPRA", 30_000L));

        assertThat(details.getTotalNet()).isZero();
        assertThat(details.getTotalSpending()).isZero();
        assertThat(details.getTotalTurn()).isZero();
    }

    @Test
    void shouldCaptureOpeningAndClosingOnTheTurn() {
        Turn turn = new Turn(START_INSTANT, BOGOTA);

        turn.setNewTurn(7L, START_INSTANT, List.of(new InventorySnapshotItem(1L, "Cerveza", 4, 2)));
        turn.setClosingInventory(List.of(new InventorySnapshotItem(1L, "Cerveza", 8, 2)));
        turn.registerInventoryMovement(movement(MovementType.INCREASE, 4, "COMPRA", 20_000L));

        TurnDetails details = turn.getDetailedTurnInformation();
        assertThat(details.getOpeningInventory()).hasSize(1);
        assertThat(details.getClosingInventory()).hasSize(1);
        assertThat(details.getInventoryMovements()).hasSize(1);
        assertThat(turn.isActive()).isTrue();
    }

    @Test
    void shouldStartASecondTurnAfterImmutableSnapshotLists() {
        Turn turn = new Turn(START_INSTANT, BOGOTA);
        turn.setNewTurn(1L, START_INSTANT, List.of(new InventorySnapshotItem(1L, "Cerveza", 4, 2)));
        turn.setClosingInventory(List.of(new InventorySnapshotItem(1L, "Cerveza", 8, 2)));
        turn.registerInventoryMovement(movement(MovementType.INCREASE, 4, "COMPRA", 20_000L));

        turn.setNewTurn(2L, START_INSTANT.plusSeconds(3_600), List.of());

        TurnDetails details = turn.getDetailedTurnInformation();
        assertThat(details.getOpeningInventory()).isEmpty();
        assertThat(details.getClosingInventory()).isEmpty();
        assertThat(details.getInventoryMovements()).isEmpty();
    }

    private static InventoryMovement movement(MovementType type, long quantity, String concept, long cost) {
        return new InventoryMovement(START, 1L, "Cerveza", type, quantity, concept, cost);
    }
}
