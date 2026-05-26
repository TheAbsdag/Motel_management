package model.turn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpendingActivity(
        @JsonProperty("changeDate") ZonedDateTime changeDate,
        @JsonProperty("spendingDescription") String description,
        @JsonProperty("value") long value,
        @JsonProperty("consecutiveTrans") int consecutiveTrans
) implements TurnActivity {

    @JsonCreator
    public static SpendingActivity createFromJson(
            @JsonProperty("changeDate") ZonedDateTime changeDate,
            @JsonProperty("spendingDescription") String description,
            @JsonProperty("value") long value,
            @JsonProperty("consecutiveTrans") int consecutiveTrans) {
        return new SpendingActivity(changeDate, description, value, consecutiveTrans);
    }
}
