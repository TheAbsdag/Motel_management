 package model.turn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtraChangeActivity(
        @JsonProperty("changeDate") ZonedDateTime changeDate,
        @JsonProperty("extraType") ExtraChangeType extraType,
        @JsonProperty("extraChangeDescription") String description,
        @JsonProperty("value") long value,
        @JsonProperty("consecutiveTrans") int consecutiveTrans
) implements TurnActivity {

    @JsonCreator
    public static ExtraChangeActivity createFromJson(
            @JsonProperty("changeDate") ZonedDateTime changeDate,
            @JsonProperty("extraType") ExtraChangeType extraType,
            @JsonProperty("extraChangeDescription") String description,
            @JsonProperty("value") long value,
            @JsonProperty("consecutiveTrans") int consecutiveTrans) {
        return new ExtraChangeActivity(changeDate, extraType, description, value, consecutiveTrans);
    }
}
