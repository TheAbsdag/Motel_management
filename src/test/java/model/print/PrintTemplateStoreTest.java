package model.print;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PrintTemplateStore}: custom template persistence in
 * {@code data/printTemplates} and the packaged factory defaults.
 */
class PrintTemplateStoreTest {

    @TempDir
    Path tempDir;

    private PrintTemplateStore store;

    @BeforeEach
    void setUp() {
        store = new PrintTemplateStore(tempDir);
    }

    /**
     * Verifies that a missing custom template is not an error.
     * Expected: load() returns null and exists() is false.
     * Failure: the store reports a template that was never saved.
     */
    @Test
    void shouldReturnNullWhenNoCustomTemplateExists() {
        assertThat(store.exists(PrintTemplateType.ROOM_RECEIPT)).isFalse();
        assertThat(store.load(PrintTemplateType.ROOM_RECEIPT)).isNull();
    }

    /**
     * Verifies the file name convention used by the printer.
     * Expected: room_receipt.json inside data/printTemplates.
     * Failure: the file name no longer matches what Printer looks up.
     */
    @Test
    void shouldNameTemplateFileAfterType() {
        assertThat(store.fileFor(PrintTemplateType.ROOM_RECEIPT))
                .isEqualTo(tempDir.resolve("data").resolve("printTemplates").resolve("room_receipt.json"));
    }

    /**
     * Verifies that a saved template can be read back unchanged.
     * Expected: the loaded template equals the saved one and exists() is true.
     * Failure: serialization drops bands, lines or segments.
     */
    @Test
    void shouldRoundTripSavedTemplate() {
        PrintTemplate template = sampleTemplate(PrintTemplateType.SALE_RECEIPT, "VENTA A LA HABITACION");

        assertThat(store.save(PrintTemplateType.SALE_RECEIPT, template)).isTrue();
        assertThat(store.exists(PrintTemplateType.SALE_RECEIPT)).isTrue();
        assertThat(store.load(PrintTemplateType.SALE_RECEIPT)).isEqualTo(template);
    }

    /**
     * Verifies that deleting the custom template restores the built-in layout state.
     * Expected: delete() returns true and the template is gone afterwards.
     * Failure: delete() reports success without removing the file.
     */
    @Test
    void shouldDeleteCustomTemplate() {
        store.save(PrintTemplateType.TURN_SUMMARY, sampleTemplate(PrintTemplateType.TURN_SUMMARY, "RESUMEN"));

        assertThat(store.delete(PrintTemplateType.TURN_SUMMARY)).isTrue();
        assertThat(store.exists(PrintTemplateType.TURN_SUMMARY)).isFalse();
        assertThat(store.load(PrintTemplateType.TURN_SUMMARY)).isNull();
    }

    /**
     * Verifies that every template type ships a factory default.
     * Expected: defaultTemplate() returns a template with at least one band for all four types.
     * Failure: a type cannot be customized because there is no layout to start from.
     */
    @ParameterizedTest
    @EnumSource(PrintTemplateType.class)
    void shouldProvideDefaultTemplateForEveryType(PrintTemplateType type) {
        PrintTemplate template = store.defaultTemplate(type);

        assertThat(template).isNotNull();
        assertThat(template.bands()).isNotEmpty();
    }

    /**
     * Verifies the editor's loading preference.
     * Expected: loadOrDefault() returns the custom template when it exists.
     * Failure: the editor shows the default layout even though a custom one is saved.
     */
    @Test
    void shouldPreferCustomTemplateOverDefault() {
        PrintTemplate custom = sampleTemplate(PrintTemplateType.ROOM_RECEIPT, "PERSONALIZADA");
        store.save(PrintTemplateType.ROOM_RECEIPT, custom);

        assertThat(store.loadOrDefault(PrintTemplateType.ROOM_RECEIPT)).isEqualTo(custom);
    }

    /**
     * Verifies the editor's fallback when nothing was customized yet.
     * Expected: loadOrDefault() returns the packaged default.
     * Failure: the editor opens empty for an uncustomized type.
     */
    @Test
    void shouldFallBackToDefaultTemplate() {
        assertThat(store.loadOrDefault(PrintTemplateType.TURN_DETAIL))
                .isEqualTo(store.defaultTemplate(PrintTemplateType.TURN_DETAIL));
    }

    private static PrintTemplate sampleTemplate(PrintTemplateType type, String text) {
        PrintLine line = new PrintLine(
                List.of(new PrintSegment(SegmentType.TEXT, text, "DefaultStyle", false)), false, 0);
        return new PrintTemplate(type, List.of(new PrintBand("BODY", List.of(line))));
    }
}
