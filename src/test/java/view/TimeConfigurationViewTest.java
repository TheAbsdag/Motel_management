package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies {@link TimeConfigurationView} encapsulation and behavior.
 */
class TimeConfigurationViewTest {

    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        TimeConfigurationView view = new TimeConfigurationView();
        AtomicBoolean invoked = new AtomicBoolean(false);

        view.onBackButton(() -> invoked.set(true));

        clickPrivateButton(view, "button1");

        assertThat(invoked).isTrue();
    }

    private static void clickPrivateButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }
}
