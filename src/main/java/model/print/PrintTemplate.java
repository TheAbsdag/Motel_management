package model.print;

import java.util.List;

public record PrintTemplate(
        PrintTemplateType templateType,
        List<PrintBand> bands
) {
    public PrintTemplate {
        if (bands == null) bands = List.of();
    }
}
