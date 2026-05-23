package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurnSelectViewTest {

    private TurnSelectView view;

    @BeforeEach
    void setUp() {
        view = new TurnSelectView();
    }

    /**
     * Verifies that clicking the turn1Button fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on turn1Button.
     * Failure: The turn1Button's action listener is not wired to the onTurn1Button callback.
     */
    @Test
    void shouldInvokeCallbackWhenTurn1ButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onTurn1Button(() -> invoked.set(true));
        clickButton(view, "turn1Button");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the turn2Button fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on turn2Button.
     * Failure: The turn2Button's action listener is not wired to the onTurn2Button callback.
     */
    @Test
    void shouldInvokeCallbackWhenTurn2ButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onTurn2Button(() -> invoked.set(true));
        clickButton(view, "turn2Button");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that clicking the turn3Button fires the registered callback.
     * Expected: AtomicBoolean is set to true after doClick() on turn3Button.
     * Failure: The turn3Button's action listener is not wired to the onTurn3Button callback.
     */
    @Test
    void shouldInvokeCallbackWhenTurn3ButtonClicked() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean(false);
        view.onTurn3Button(() -> invoked.set(true));
        clickButton(view, "turn3Button");
        assertThat(invoked).isTrue();
    }

    /**
     * Verifies that setTurnSelectLabel() updates the text of the turnSelectLabel JLabel.
     * Expected: After calling setTurnSelectLabel("Seleccione un turno activo"), the label text matches.
     * Failure: The setter does not update the label text or the label field name is incorrect.
     */
    @Test
    void shouldSetTurnSelectLabel() throws Exception {
        view.setTurnSelectLabel("Seleccione un turno activo");
        assertThat(textOf(view, "turnSelectLabel")).isEqualTo("Seleccione un turno activo");
    }

    /**
     * Verifies that updateTimeDisplay() can be called without throwing exceptions.
     * Expected: The method completes normally, confirming the time display API is wired.
     * Failure: The time display labels are not properly initialized, causing a NullPointerException.
     */
    @Test
    void shouldUpdateTimeDisplay() {
        view.updateTimeDisplay("12:00 PM", "23 de mayo");
    }

    private static void clickButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }

    private static String textOf(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        JLabel label = (JLabel) field.get(parent);
        return label.getText();
    }
}
