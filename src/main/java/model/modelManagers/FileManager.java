package model.modelManagers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Handles all file I/O for the application — JSON read/write, backups, and
 * history persistence.
 *
 * @author Santiago
 */
public class FileManager {

    public static final String PATH = System.getProperty("user.dir");

    private final String dataPath;
    private final String stagingDir;
    private final String backupPath;
    private final String historyPath;

    private static String sanitizeFileName(String name) {
        if (name == null || name.isEmpty()) {
            return "unnamed";
        }
        return name.replace('\\', '_')
                   .replace('/', '_')
                   .replace("..", "_")
                   .replace("\0", "_");
    }

    public FileManager() {
        this(PATH);
    }

    public FileManager(String basePath) {
        this.dataPath = basePath + File.separator + "data";
        this.stagingDir = dataPath + File.separator + ".staging";
        this.backupPath = basePath + File.separator + "backup";
        this.historyPath = basePath + File.separator + "history";
        Logger.getLogger(FileManager.class.getName()).fine("FileManager initialized");
        prepareFolders();
    }

    public void prepareFolders() {
        File prepareData = new File(dataPath);
        prepareData.mkdirs();
        File prepareBackup = new File(backupPath);
        prepareBackup.mkdirs();
        File prepareHistory = new File(historyPath);
        prepareHistory.mkdirs();
        deleteDirectory(new File(stagingDir));
        Logger.getLogger(FileManager.class.getName()).fine("Folders created");
    }

    /**
     * Reads a JSON data file from the data directory.
     *
     * @param dataNeeded the file name (without path) to read
     * @return the file content as a string, or {@code null} if the file does not exist
     */
    public String getJsonData(String dataNeeded) {
        String safeName = sanitizeFileName(dataNeeded);
        File file = new File(dataPath + File.separator + safeName);
        if (!file.exists()) {
            Logger.getLogger(FileManager.class.getName()).log(Level.INFO, "No se ha encontrado archivo de {0}", dataNeeded);
            return null;
        }
        StringBuilder outputString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputString.append(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return outputString.toString();
    }

    /**
     * Saves a JSON string to the data directory using an atomic write.
     */
    public void saveJsonMainDataPath(String data, String dataToSave) {
        String safeName = sanitizeFileName(dataToSave);
        File targetFile = new File(dataPath + File.separator + safeName);
        File tmpFile = new File(dataPath + File.separator + safeName + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            writer.write(data);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        try {
            Files.move(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, "Atomic move failed, falling back", ex);
            try {
                Files.move(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex2) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, "Fallback move also failed", ex2);
            }
        }
    }

    /**
     * Atomically saves multiple JSON data strings using a staging directory.
     */
    public void saveAllMainDataAtomic(Map<String, String> dataMap) {
        File stagingDirFile = new File(stagingDir);
        deleteDirectory(stagingDirFile);
        stagingDirFile.mkdirs();

        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            String safeName = sanitizeFileName(entry.getKey());
            File stagingFile = new File(stagingDirFile, safeName);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(stagingFile))) {
                writer.write(entry.getValue());
            } catch (IOException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, "Failed to write staging: " + safeName, ex);
                deleteDirectory(stagingDirFile);
                return;
            }
        }

        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            String safeName = sanitizeFileName(entry.getKey());
            File stagingFile = new File(stagingDirFile, safeName);
            File targetFile = new File(dataPath + File.separator + safeName);
            try {
                Files.move(stagingFile.toPath(), targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE,
                        "Atomic move failed for " + safeName + ", falling back", ex);
                try {
                    Files.move(stagingFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex2) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE,
                            "Fallback also failed for " + safeName, ex2);
                }
            }
        }

        deleteDirectory(stagingDirFile);
    }

    /**
     * Saves a JSON backup string into a timestamped subdirectory under {@code backup/}.
     */
    public void saveJsonBackupDataPath(String data, String dataToSave, ZonedDateTime time, String saveType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String safeFolder = sanitizeFileName(time.format(formatter) + "-" + saveType);
        String folderPath = backupPath + File.separator + safeFolder;
        File newFolderBackup = new File(folderPath);
        newFolderBackup.mkdirs();
        String safeName = sanitizeFileName(dataToSave);
        File targetFile = new File(folderPath + File.separator + safeName);
        File tmpFile = new File(folderPath + File.separator + safeName + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            writer.write(data);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        try {
            Files.move(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, "Atomic move failed for backup, falling back", ex);
            try {
                Files.move(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex2) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, "Fallback move also failed for backup", ex2);
            }
        }
        enforceBackupRetention();
    }

    public void clearBackupFiles() {
        File backupPathFile = new File(backupPath);
        deleteDirectory(backupPathFile);
    }

    /**
     * Keeps only the N most recent backup directories (by timestamp).
     * Call after each backup save to prevent unbounded disk growth.
     */
    private static final int MAX_BACKUPS = 50;

    public void enforceBackupRetention() {
        File backupDir = new File(backupPath);
        File[] dirs = backupDir.listFiles(File::isDirectory);
        if (dirs == null || dirs.length <= MAX_BACKUPS) return;
        java.util.Arrays.sort(dirs, (a, b) -> a.getName().compareTo(b.getName()));
        for (int i = 0; i < dirs.length - MAX_BACKUPS; i++) {
            deleteDirectory(dirs[i]);
        }
    }

    private void deleteDirectory(File file) {
        File[] files = file.listFiles();
        if (files == null) return;
        for (File subfile : files) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
            if (!subfile.delete()) {
                Logger.getLogger(FileManager.class.getName()).log(Level.WARNING, "Could not delete file: {0}", subfile.getAbsolutePath());
            }
        }
        file.delete();
    }

    /**
     * Saves a JSON string to the history directory with a timestamped file name.
     */
    public void saveHistoryData(String data, String dataToSave, ZonedDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String timeString = time.format(formatter);
        String safeName = sanitizeFileName(dataToSave);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyPath + File.separator + safeName + "-" + timeString))) {
            writer.write(data);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads all JSON files from the history directory and returns their contents as strings.
     */
    public List<String> getHistoryFiles() {
        List<String> list = new ArrayList<>();

        try {
            Path path = Paths.get(historyPath);

            try (Stream<Path> files = Files.list(path)) {
                files.forEach(file -> {
                    try {
                        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                        list.add(content);
                    } catch (IOException e) {
                        Logger.getLogger(FileManager.class.getName()).log(Level.WARNING,
                                "Error reading history file: " + file.getFileName(), e);
                    }
                });
            }

        } catch (IOException e) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, "Error listing history files", e);
        }
        return list;
    }
}
