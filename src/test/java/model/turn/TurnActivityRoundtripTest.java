package model.turn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.RoomStatus;
import model.json.ObjectMapperFactory;
import org.junit.jupiter.api.Test;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class TurnActivityRoundtripTest {

    private static final ObjectMapper MAPPER = ObjectMapperFactory.get();
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final ZonedDateTime NOW = ZonedDateTime.of(2026, 6, 28, 14, 30, 0, 0, BOGOTA);

    @Test
    void roomBookingRoundtrip() throws JsonProcessingException {
        var original = new RoomBookingActivity(NOW, "101", 1, 0, 0,
                RoomStatus.OCCUPIED, NOW, NOW.plusHours(3),
                120000L, 10800L, 0L, 0L, 1, false);
        String json = MAPPER.writeValueAsString(original);
        var restored = MAPPER.readValue(json, TurnActivity.class);
        assertThat(restored).isInstanceOf(RoomBookingActivity.class);
        var r = (RoomBookingActivity) restored;
        assertThat(r.roomString()).isEqualTo("101");
        assertThat(r.price()).isEqualTo(120000L);
    }

    @Test
    void saleRoundtrip() throws JsonProcessingException {
        var items = List.of(new SaleItem("Coca-Cola", 1L, 2L, 5000L, false));
        var original = new SaleActivity(NOW, "101", items, 2);
        String json = MAPPER.writeValueAsString(original);
        var restored = MAPPER.readValue(json, TurnActivity.class);
        assertThat(restored).isInstanceOf(SaleActivity.class);
        var s = (SaleActivity) restored;
        assertThat(s.roomSoldTo()).isEqualTo("101");
        assertThat(s.items()).hasSize(1);
        assertThat(s.items().get(0).itemName()).isEqualTo("Coca-Cola");
    }

    @Test
    void roomSwapRoundtrip() throws JsonProcessingException {
        var original = new RoomSwapActivity(NOW, "101", 1, 0, 0,
                "102", 2, 0, 0);
        String json = MAPPER.writeValueAsString(original);
        var restored = MAPPER.readValue(json, TurnActivity.class);
        assertThat(restored).isInstanceOf(RoomSwapActivity.class);
        var s = (RoomSwapActivity) restored;
        assertThat(s.originalRoom()).isEqualTo("101");
        assertThat(s.swappedRoom()).isEqualTo("102");
    }

    @Test
    void refundRoundtrip() throws JsonProcessingException {
        var original = new RefundActivity(NOW, RefundType.ROOM_REFUND, 3, 1,
                "101", 60000L, 10800L, 0L, 0L, null);
        String json = MAPPER.writeValueAsString(original);
        var restored = MAPPER.readValue(json, TurnActivity.class);
        assertThat(restored).isInstanceOf(RefundActivity.class);
        var r = (RefundActivity) restored;
        assertThat(r.refundType()).isEqualTo(RefundType.ROOM_REFUND);
        assertThat(r.refundRoom()).isEqualTo("101");
    }

    @Test
    void spendingRoundtrip() throws JsonProcessingException {
        var original = new SpendingActivity(NOW, "Limpieza", 50000L, 4);
        String json = MAPPER.writeValueAsString(original);
        var restored = MAPPER.readValue(json, TurnActivity.class);
        assertThat(restored).isInstanceOf(SpendingActivity.class);
        var s = (SpendingActivity) restored;
        assertThat(s.description()).isEqualTo("Limpieza");
        assertThat(s.value()).isEqualTo(50000L);
    }

    @Test
    void extraChangeRoundtrip() throws JsonProcessingException {
        var original = new ExtraChangeActivity(NOW, ExtraChangeType.BANK_TRANSFER, "Transferencia banco", 200000L, 5);
        String json = MAPPER.writeValueAsString(original);
        var restored = MAPPER.readValue(json, TurnActivity.class);
        assertThat(restored).isInstanceOf(ExtraChangeActivity.class);
        var e = (ExtraChangeActivity) restored;
        assertThat(e.extraType()).isEqualTo(ExtraChangeType.BANK_TRANSFER);
        assertThat(e.value()).isEqualTo(200000L);
    }
}
