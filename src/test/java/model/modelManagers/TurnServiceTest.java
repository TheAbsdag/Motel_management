package model.modelManagers;

import java.time.Instant;
import java.time.ZoneId;
import model.Turn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnServiceTest {

    private TurnService service;

    @BeforeEach
    void setUp() {
        service = new TurnService(
                new Turn(Instant.now(), ZoneId.of("America/Bogota")), null, null, null);
    }

    /**
     * Verifies that the three print options accepted by the view are valid for the model:
     * {@code 1} (no print), {@code 2} (summarized) and {@code 3} (detailed).
     * Expected: No exception is thrown for any of them.
     * Failure: A valid option is rejected, so the turn cannot be printed or closed.
     */
    @Test
    void shouldAcceptEveryValidPrintOption() {
        assertThatCode(() -> TurnService.requireValidPrintOption(1)).doesNotThrowAnyException();
        assertThatCode(() -> TurnService.requireValidPrintOption(2)).doesNotThrowAnyException();
        assertThatCode(() -> TurnService.requireValidPrintOption(3)).doesNotThrowAnyException();
        assertThatCode(() -> service.turnPrintNoEnd(1)).doesNotThrowAnyException();
    }

    /**
     * Verifies that an out-of-range print option is rejected before the model touches
     * the turn, the printer or the files.
     * Expected: {@link IllegalArgumentException} for options below 1 or above 3.
     * Failure: An invalid option is silently ignored, ending the turn without a report.
     */
    @Test
    void shouldRejectInvalidPrintOptionBeforePrinting() {
        assertThatThrownBy(() -> service.turnPrintNoEnd(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0");
        assertThatThrownBy(() -> service.turnPrintNoEnd(4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("4");
        assertThatThrownBy(() -> service.turnEndPrint(9, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.turnHistoryPrint(-1, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
