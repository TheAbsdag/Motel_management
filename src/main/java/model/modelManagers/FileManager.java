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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handles all file I/O for the application — JSON read/write, backups, and
 * history persistence.
 *
 * <p>Uses atomic-write patterns (write to {@code .tmp}, then rename) to prevent
 * data corruption. Backup and history directories are created automatically.
 *
 * @author Santiago
 */
public class FileManager {

    public static final String PATH = System.getProperty("user.dir");
    private static final String DATA_PATH = PATH + File.separator + "data";
    private static final String STAGING_DIR = DATA_PATH + File.separator + ".staging";
    private static final String BACKUP_PATH = PATH + File.separator + "backup";
    private static final String HISTORY_PATH = PATH + File.separator + "history";

    private static String sanitizeFileName(String name) {
        if (name == null || name.isEmpty()) {
            return "unnamed";
        }
        return name.replace('\\', '_')
                   .replace('/', '_')
                   .replace("..", "_")
                   .replace("\0", "_");
    }

    /**
     * Creates a FileManager and prepares all required directories.
     */
    public FileManager() {
        System.out.println("FileManager initialized");
        prepareFolders();
    }

    /**
     * Ensures the data, backup, history, and staging directories exist.
     * Clears any leftover staging directory from a previous run.
     */
    public void prepareFolders() {
        //All folders are created regardless of the path used
        File prepareData = new File(DATA_PATH);
        prepareData.mkdirs();
        File prepareBackup = new File(BACKUP_PATH);
        prepareBackup.mkdirs();
        File prepareHistory = new File(HISTORY_PATH);
        prepareHistory.mkdirs();
        deleteDirectory(new File(STAGING_DIR));
        System.out.println("Folders created");
    }

    /*
    All data retreival functions the same way, as it's a standard JSON
    For each data therre may be
        * turn
        * rooms
        * inventoryData
        * applicationProperties
     */
    /**
     * Reads a JSON object from the data directory.
     *
     * @param dataNeeded the file name (without path) to read
     * @return the parsed JSON object, or {@code null} if the file does not exist or is invalid
     */
    public JSONObject getJsonData(String dataNeeded) {
        String safeName = sanitizeFileName(dataNeeded);
        File file = new File(DATA_PATH + File.separator + safeName);
        if (!file.exists()) {
            System.out.println("No se ha encontrado archivo de " + dataNeeded);
            return null;
        }
        StringBuilder outputString = new StringBuilder();
        JSONObject output = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputString.append(line);
            }
            try {
                output = new JSONObject(outputString.toString());
            } catch (JSONException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, "Invalid JSON in " + dataNeeded, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }

    /**
     * Saves a JSON object to the data directory using an atomic write
     * (writes to {@code .tmp}, then renames).
     *
     * @param data       the JSON data to save
     * @param dataToSave the file name (without path)
     */
    public synchronized void saveJsonMainDataPath(JSONObject data, String dataToSave) {
        String safeName = sanitizeFileName(dataToSave);
        String saveString = data.toString();
        File targetFile = new File(DATA_PATH + File.separator + safeName);
        File tmpFile = new File(DATA_PATH + File.separator + safeName + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            writer.write(saveString);
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
     * Atomically saves multiple JSON data files using a staging directory.
     * All files are written to staging first, then moved to the data directory
     * in a second pass. If any staging write fails, the staging directory is
     * deleted and no files are moved.
     *
     * @param dataMap map of file names to JSON data objects
     */
    public synchronized void saveAllMainDataAtomic(Map<String, JSONObject> dataMap) {
        File stagingDir = new File(STAGING_DIR);
        deleteDirectory(stagingDir);
        stagingDir.mkdirs();

        for (Map.Entry<String, JSONObject> entry : dataMap.entrySet()) {
            String safeName = sanitizeFileName(entry.getKey());
            File stagingFile = new File(stagingDir, safeName);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(stagingFile))) {
                writer.write(entry.getValue().toString());
            } catch (IOException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, "Failed to write staging: " + safeName, ex);
                deleteDirectory(stagingDir);
                return;
            }
        }

        for (Map.Entry<String, JSONObject> entry : dataMap.entrySet()) {
            String safeName = sanitizeFileName(entry.getKey());
            File stagingFile = new File(stagingDir, safeName);
            File targetFile = new File(DATA_PATH + File.separator + safeName);
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

        deleteDirectory(stagingDir);
    }

    /**
     * Saves a JSON backup file into a timestamped subdirectory under {@code backup/}.
     *
     * @param data       the JSON data to back up
     * @param dataToSave the file name (without path)
     * @param time       the timestamp used for the backup folder
     * @param saveType   a label included in the folder name
     */
    public synchronized void saveJsonBackupDataPath(JSONObject data, String dataToSave, ZonedDateTime time, String saveType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String safeFolder = sanitizeFileName(time.format(formatter) + "-" + saveType);
        String folderPath = BACKUP_PATH + File.separator + safeFolder;
        File newFolderBackup = new File(folderPath);
        newFolderBackup.mkdirs();
        String safeName = sanitizeFileName(dataToSave);
        String saveString = data.toString();
        File targetFile = new File(folderPath + File.separator + safeName);
        File tmpFile = new File(folderPath + File.separator + safeName + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            writer.write(saveString);
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
    }
    
    
    /**
     * Deletes the entire backup directory and all its contents.
     */
    public synchronized void clearBackupFiles() {
        File backupPathFile = new File(BACKUP_PATH);
        deleteDirectory(backupPathFile);
    }

    private void deleteDirectory(File file) {
        File[] files = file.listFiles();
        if (files == null) return;
        for (File subfile : files) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
            if (!subfile.delete()) {
                System.out.println("Could not delete file: " + subfile.getAbsolutePath());
            }
        }
        file.delete();
    }

    /**
     * Saves a JSON object to the history directory with a timestamped file name.
     *
     * @param data       the JSON data to save
     * @param dataToSave the base file name (without path)
     * @param time       the timestamp appended to the file name
     */
    public synchronized void saveHistoryData(JSONObject data, String dataToSave, ZonedDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String timeString = time.format(formatter);
        String safeName = sanitizeFileName(dataToSave);
        String saveString = data.toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_PATH + File.separator + safeName + "-" + timeString))) {
            writer.write(saveString);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads all JSON files from the history directory and returns them as a JSON array.
     * Files that do not contain valid JSON objects are silently skipped.
     *
     * @return a JSON array of all parseable history entries
     */
    public JSONArray getHistoryFiles() {
        JSONArray list = new JSONArray();

        try {
            // Get the path object representing the directory
            Path path = Paths.get(HISTORY_PATH);

            // List the files in the directory
            try (Stream<Path> files = Files.list(path)) {
                files.forEach(file -> {
                    try {
                        // Read the content of the file
                        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);

                        // Try to parse the content as a JSONObject
                        try {
                            JSONObject jsonObject = new JSONObject(content);
                            // If parsing is successful, add it to the list
                            list.put(jsonObject);
                        } catch (JSONException e) {
                            // If content is not a valid JSONObject, skip this file
                            System.out.println("Invalid JSON in file: " + file.getFileName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
        
    }
}
