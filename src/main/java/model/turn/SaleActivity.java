package model.turn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SaleActivity(
        @JsonProperty("changeDate") ZonedDateTime changeDate,
        @JsonProperty("roomSoldTo") String roomSoldTo,
        @JsonProperty("register") List<SaleItem> items,
        @JsonProperty("consecutiveTrans") int consecutiveTrans
) implements TurnActivity {

    @JsonCreator
    public static SaleActivity createFromJson(
            @JsonProperty("changeDate") ZonedDateTime changeDate,
            @JsonProperty("roomSoldTo") String roomSoldTo,
            @JsonProperty("register") List<SaleItem> items,
            @JsonProperty("consecutiveTrans") int consecutiveTrans) {
        return new SaleActivity(changeDate, roomSoldTo, items, consecutiveTrans);
    }
}
