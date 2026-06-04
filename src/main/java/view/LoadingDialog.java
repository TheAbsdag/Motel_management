package view;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

/**
 * Modal indeterminate-progress dialog that auto-closes when a background task completes.
 * Usage:
 *   LoadingDialog ld = new LoadingDialog(parentWindow, "Enviando correo...");
 *   ld.showAsync(() -> { doWork(); ld.close(); });
 */
public class LoadingDialog extends JDialog {

    private final JLabel messageLabel;
    private final JProgressBar progressBar;
    private volatile boolean completed = false;

    public LoadingDialog(Window parent, String message) {
        super(parent, "PROCESANDO", Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setLayout(new MigLayout("fill,wrap 1", "[300]", "[]20[]20"));

        messageLabel = new JLabel(message, JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI Black", Font.PLAIN, 18));
        add(messageLabel, "growx");

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(280, 20));
        add(progressBar, "growx");

        pack();
        setLocationRelativeTo(parent);
    }

    /** Runs the task in a background thread; closes dialog automatically on completion. */
    public void showAsync(Runnable task) {
        new Thread(() -> {
            try {
                task.run();
            } finally {
                completed = true;
                SwingUtilities.invokeLater(this::dispose);
            }
        }).start();
        setVisible(true);
    }

    public boolean isCompleted() {
        return completed;
    }
}
