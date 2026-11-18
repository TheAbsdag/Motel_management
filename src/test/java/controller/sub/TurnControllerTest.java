package controller.sub;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import model.modelManagers.MotelManagement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the turn lifecycle controller: closing a turn must leave the closed turn on
 * disk, not only in the history archive.
 */
@DisplayName("TurnController")
class TurnControllerTest {

    @Test
    @DisplayName("closing a turn ends it, forwards the print option and stores the main files")
    void finishTurn_shouldEndPrintAndPersist_whenTurnIsClosed() {
        FakeMotelManagement model = new FakeMotelManagement();
        AtomicBoolean saved = new AtomicBoolean(false);
        // finishTurn only touches the model, the print option and the save callback
        TurnController controller = new TurnController(model, null, null,
                () -> { }, () -> saved.set(true), () -> { });

        controller.finishTurn(2);

        assertThat(model.turnEnded).isTrue();
        assertThat(model.printOption).isEqualTo(2);
        assertThat(saved).isTrue();
    }

    /** Model double that records the end-of-turn calls without touching the filesystem. */
    private static final class FakeMotelManagement extends MotelManagement {

        private boolean turnEnded;
        private int printOption = -1;

        @Override
        public void turnEnded() {
            turnEnded = true;
        }

        @Override
        public void turnEndPrint(int option) {
            printOption = option;
        }
    }
}
