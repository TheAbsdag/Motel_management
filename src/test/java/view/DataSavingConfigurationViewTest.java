package view;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractButton;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies {@link DataSavingConfigurationView} encapsulation and behavior.
 */
class DataSavingConfigurationViewTest {

    @Test
    void shouldInvokeCallbackWhenBackButtonClicked() throws Exception {
        DataSavingConfigurationView view = new DataSavingConfigurationView();
        AtomicBoolean invoked = new AtomicBoolean(false);

        view.onBackButton(() -> invoked.set(true));

        clickPrivateButton(view, "backButton");

        assertThat(invoked).isTrue();
    }

    /** Simulates a click on a private JButton field accessible to the test. */
    private static void clickPrivateButton(Component parent, String fieldName) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        AbstractButton button = (AbstractButton) field.get(parent);
        button.doClick();
    }
}
