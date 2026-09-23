package model.print;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PrintSegment(
        SegmentType type,
        String text,
        String style,
        boolean bold
) {
    @JsonCreator
    public PrintSegment(
            @JsonProperty("type") SegmentType type,
            @JsonProperty("text") String text,
            @JsonProperty("style") String style,
            @JsonProperty("bold") boolean bold) {
        this.type = type;
        this.text = text;
        this.style = style != null ? style : "DefaultStyle";
        this.bold = bold;
    }
}
