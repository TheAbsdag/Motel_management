import controller.Controller;
import view.UserGUI;
import model.modelManagers.MotelManagement;
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
    private static final Path LOCK_PATH = Path.of(LOCK_FILE);

    private static RandomAccessFile lockRaf;
    private static FileChannel lockChannel;
    private static FileLock lock;

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

            lockRaf = new RandomAccessFile(LOCK_FILE, "rw");
            lockChannel = lockRaf.getChannel();
            lock = lockChannel.tryLock();

            if (lock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread(App::releaseLock, "LockCleanup"));
                return true;
            }

            // Lock failed — another instance is running
            lockRaf.close();
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

    private static void releaseLock() {
        try {
            if (lock != null && lock.isValid()) {
                lock.release();
            }
            if (lockChannel != null) {
                lockChannel.close();
            }
            if (lockRaf != null) {
                lockRaf.close();
            }
            Files.deleteIfExists(LOCK_PATH);
        } catch (IOException e) {
            // Best-effort cleanup on shutdown
        }
    }
}