package model.print;

import java.util.List;

public record PrintBand(
        String name,
        List<PrintLine> lines
) {
    public PrintBand {
        if (lines == null) lines = List.of();
    }
}
