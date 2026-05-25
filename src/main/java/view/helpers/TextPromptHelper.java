package view.helpers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.LayerUI;
import net.miginfocom.swing.MigLayout;

/**
 * Paints gray placeholder text inside a {@link JTextField} when it is empty
 * and does not have focus, without modifying the field's document or color.
 *
 * <p>Usage: after the field has been added to its parent container, call
 * {@link #install(JTextField, String)}. The field will be transparently
 * wrapped in a {@link JLayer} that draws the prompt.</p>
 */
public class TextPromptHelper {

    private TextPromptHelper() {
    }

    /**
     * Installs a gray prompt string on the given text field. The prompt
     * appears when the field is empty and unfocused, and disappears when
     * the user focuses or types. The field's actual text content is never
     * touched.
     */
    public static void install(JTextField field, String prompt) {
        Container parent = field.getParent();
        if (parent == null) {
            return;
        }

        // Preserve layout constraints and Z-order
        int index = parent.getComponentZOrder(field);
        Object constraints = getLayoutConstraint(parent, field);

        parent.remove(field);

        PromptLayerUI ui = new PromptLayerUI(prompt);
        JLayer<JTextField> layer = new JLayer<>(field, ui);

        // Repaint the layer when focus or document changes
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                layer.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                layer.repaint();
            }
        });
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                layer.repaint();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                layer.repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                layer.repaint();
            }
        });

        parent.add(layer, constraints, Math.max(index, 0));
        parent.revalidate();
        parent.repaint();
    }

    private static Object getLayoutConstraint(Container parent, Component child) {
        if (parent.getLayout() instanceof MigLayout mig) {
            return mig.getComponentConstraints(child);
        }
        return null;
    }

    private static class PromptLayerUI extends LayerUI<JTextField> {

        private final String prompt;

        PromptLayerUI(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            JLayer<JTextField> layer = (JLayer<JTextField>) c;
            JTextField field = layer.getView();

            if (field.getText().isEmpty() && !field.hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.GRAY);
                g2.setFont(field.getFont());
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Insets insets = field.getInsets();
                FontMetrics fm = field.getFontMetrics(field.getFont());
                int x = insets.left + 2;
                int y = insets.top + fm.getAscent() + 1;

                g2.drawString(prompt, x, y);
                g2.dispose();
            }
        }
    }
}
