package model.print;

import java.util.List;

public record PrintLine(
        List<PrintSegment> segments,
        boolean centered,
        int spacerAfter
) {
    public PrintLine {
        if (segments == null) segments = List.of();
    }
}
