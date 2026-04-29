import controller.Controller;
import view.UserGUI;
import model.MotelManagement;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JOptionPane;

/**
 *
 * @author Santiago
 */
public class App {

    private static final String LOCK_DIR = System.getProperty("user.home")
            + "/.motel_management";
    private static final String LOCK_FILE = LOCK_DIR + "/lock";

    public static void main(String[] args) {
        if (!checkSingleInstance()) {
            System.exit(0);
        }

        MotelManagement motelManager = new MotelManagement();
        UserGUI userInterface = new UserGUI();
        Controller controller = new Controller(motelManager, userInterface);
        controller.start();
    }

    /**
     * Ensures only one instance runs at a time via a cross-process
     * {@link FileLock}. If another instance is already running, shows a
     * message and exits immediately — no data is read or written.
     */
    private static boolean checkSingleInstance() {
        try {
            Files.createDirectories(Path.of(LOCK_DIR));

            @SuppressWarnings("resource")
            RandomAccessFile raf = new RandomAccessFile(LOCK_FILE, "rw");
            FileChannel channel = raf.getChannel();
            FileLock lock = channel.tryLock();

            if (lock != null) {
                // Keep the lock for the lifetime of this instance.
                // It is released automatically when the JVM exits.
                return true;
            }

            JOptionPane.showMessageDialog(null,
                    "El programa ya se encuentra en ejecucion.\n"
                    + "Cierre la otra instancia antes de abrir una nueva.",
                    "PROGRAMA YA ABIERTO",
                    JOptionPane.WARNING_MESSAGE);
            return false;

        } catch (Exception e) {
            System.err.println("No se pudo verificar instancia unica: " + e.getMessage());
            return true;
        }
    }
}
