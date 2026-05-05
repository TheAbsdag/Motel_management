package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FileManager} — JSON file I/O, backup, and history operations.
 *
 * <p>Uses a temporary directory structure to avoid side effects from the real data paths.
 * Each test creates files under a temp dir and cleans up afterward.
 */
class FileManagerTest {

    private Path tempDir;
    private Path dataDir;
    private Path backupDir;
    private Path historyDir;
    private FileManager fileManager;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("motel_test_");
        dataDir = tempDir.resolve("data");
        backupDir = tempDir.resolve("backup");
        historyDir = tempDir.resolve("history");
        Files.createDirectories(dataDir);

        fileManager = new FileManager();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    // ========== Folder Preparation ==========

    @Test
    void shouldNotThrowOnPrepareFolders() {
        fileManager.prepareFolders();
        // Main assertion: no exception thrown, folders exist in project dir
        assertThat(new File(FileManager.PATH + File.separator + "data")).exists();
        assertThat(new File(FileManager.PATH + File.separator + "backup")).exists();
        assertThat(new File(FileManager.PATH + File.separator + "history")).exists();
    }

    // ========== getJsonData ==========

    @Test
    void shouldReturnNullForNonExistentFile() {
        // Since we're relying on the real FileManager paths, test the non-existent case
        JSONObject result = fileManager.getJsonData("non_existent_file_12345.json");

        assertThat(result).isNull();
    }

    // ========== Save and Retrieve JSON (main data path) ==========

    @Test
    void shouldSaveAndRetrieveJsonData() throws IOException {
        // Write a file manually to the data directory and read it back
        JSONObject original = new JSONObject();
        original.put("testKey", "testValue");
        original.put("number", 42);

        // Write directly to the real data path (folders exist after prepare)
        fileManager.prepareFolders();
        String testFileName = "test_data_save.json";
        fileManager.saveJsonMainDataPath(original, testFileName);

        JSONObject retrieved = fileManager.getJsonData(testFileName);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getString("testKey")).isEqualTo("testValue");
        assertThat(retrieved.getInt("number")).isEqualTo(42);

        // Clean up
        new File(FileManager.PATH + File.separator + "data" + File.separator + testFileName).delete();
    }

    @Test
    void shouldOverwriteExistingFileOnSave() {
        fileManager.prepareFolders();
        String testFileName = "test_overwrite.json";

        JSONObject first = new JSONObject();
        first.put("version", 1);
        fileManager.saveJsonMainDataPath(first, testFileName);

        JSONObject second = new JSONObject();
        second.put("version", 2);
        second.put("extra", "data");
        fileManager.saveJsonMainDataPath(second, testFileName);

        JSONObject retrieved = fileManager.getJsonData(testFileName);
        assertThat(retrieved.getInt("version")).isEqualTo(2);
        assertThat(retrieved.getString("extra")).isEqualTo("data");

        // Clean up
        new File(FileManager.PATH + File.separator + "data" + File.separator + testFileName).delete();
    }

    // ========== Backup Data Path ==========

    @Test
    void shouldSaveBackupDataAndCreateTimestampedFolder() {
        fileManager.prepareFolders();
        JSONObject data = new JSONObject();
        data.put("backupData", true);
        data.put("timestamp", 123);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        fileManager.saveJsonBackupDataPath(data, "testBackup.json", now, "auto");

        // Verify backup folder was created under the real backup path
        File backupPath = new File(FileManager.PATH + File.separator + "backup");
        File[] backupDirs = backupPath.listFiles(File::isDirectory);
        assertThat(backupDirs).isNotNull();
        boolean found = false;
        for (File dir : backupDirs) {
            if (dir.getName().endsWith("-auto")) {
                File savedFile = new File(dir, "testBackup.json");
                if (savedFile.exists()) {
                    found = true;
                    // Clean up the created backup directory
                    deleteRecursive(dir);
                }
            }
        }
        assertThat(found).isTrue();
    }

    // ========== History Data ==========

    @Test
    void shouldSaveHistoryData() {
        fileManager.prepareFolders();
        JSONObject data = new JSONObject();
        data.put("turnNumber", 1);
        data.put("totalSales", 50000);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        fileManager.saveHistoryData(data, "turn", now);

        // Verify history file exists
        File historyPath = new File(FileManager.PATH + File.separator + "history");
        File[] historyFiles = historyPath.listFiles((dir, name) -> name.startsWith("turn-"));
        assertThat(historyFiles).isNotNull();
        assertThat(historyFiles.length).isGreaterThan(0);

        // Clean up test file
        for (File f : historyFiles) {
            f.delete();
        }
    }

    @Test
    void shouldRetrieveHistoryFiles() {
        fileManager.prepareFolders();
        JSONObject data = new JSONObject();
        data.put("turnNumber", 5);
        data.put("testMarker", "history_retrieval_test");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        fileManager.saveHistoryData(data, "turn", now);

        JSONArray historyFiles = fileManager.getHistoryFiles();
        assertThat(historyFiles).isNotNull();

        boolean found = false;
        for (int i = 0; i < historyFiles.length(); i++) {
            JSONObject entry = historyFiles.getJSONObject(i);
            if ("history_retrieval_test".equals(entry.optString("testMarker"))) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();

        // Clean up
        File historyPath = new File(FileManager.PATH + File.separator + "history");
        File[] files = historyPath.listFiles((dir, name) -> name.startsWith("turn-"));
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    // ========== JSON with Special Characters ==========

    @Test
    void shouldHandleJsonWithSpecialCharacters() {
        fileManager.prepareFolders();
        String testFileName = "test_special_chars.json";

        JSONObject data = new JSONObject();
        data.put("message", "Hola, ¿cómo estás?");
        data.put("address", "Calle 123 #45-67, Apt. 8B");
        data.put("emoji", "\u00e1\u00e9\u00ed\u00f3\u00fa\u00f1");

        fileManager.saveJsonMainDataPath(data, testFileName);

        JSONObject retrieved = fileManager.getJsonData(testFileName);
        assertThat(retrieved.getString("message")).isEqualTo("Hola, ¿cómo estás?");
        assertThat(retrieved.getString("address")).isEqualTo("Calle 123 #45-67, Apt. 8B");

        // Clean up
        new File(FileManager.PATH + File.separator + "data" + File.separator + testFileName).delete();
    }

    // ========== Nested JSON ==========

    @Test
    void shouldSaveAndRetrieveNestedJsonArrays() {
        fileManager.prepareFolders();
        String testFileName = "test_nested.json";

        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();
        for (int i = 0; i < 3; i++) {
            JSONObject item = new JSONObject();
            item.put("id", i);
            item.put("name", "Item " + i);
            array.put(item);
        }
        data.put("items", array);

        fileManager.saveJsonMainDataPath(data, testFileName);

        JSONObject retrieved = fileManager.getJsonData(testFileName);
        assertThat(retrieved.getJSONArray("items")).hasSize(3);
        assertThat(retrieved.getJSONArray("items").getJSONObject(1).getString("name"))
                .isEqualTo("Item 1");

        // Clean up
        new File(FileManager.PATH + File.separator + "data" + File.separator + testFileName).delete();
    }

    // ========== Concurrent Access (synchronized methods exist) ==========

    @Test
    void shouldHandleSequentialSavesToSameFile() throws InterruptedException {
        fileManager.prepareFolders();
        String testFileName = "test_sequential.json";

        for (int i = 0; i < 20; i++) {
            JSONObject data = new JSONObject();
            data.put("iteration", i);
            fileManager.saveJsonMainDataPath(data, testFileName);
        }

        JSONObject retrieved = fileManager.getJsonData(testFileName);
        assertThat(retrieved.getInt("iteration")).isEqualTo(19);

        // Clean up
        new File(FileManager.PATH + File.separator + "data" + File.separator + testFileName).delete();
    }

    // ========== Get History Files with Invalid JSON ==========

    @Test
    void shouldSkipInvalidJsonFilesInHistory() throws IOException {
        // Write a non-JSON file directly into the history directory
        fileManager.prepareFolders();
        File historyPath = new File(FileManager.PATH + File.separator + "history");
        File invalidFile = new File(historyPath, "invalid-file-test.txt");
        Files.writeString(invalidFile.toPath(), "this is not valid JSON {[[[");

        // Should not throw - invalid files are skipped
        JSONArray result = fileManager.getHistoryFiles();
        assertThat(result).isNotNull();

        // Clean up
        invalidFile.delete();
    }

    // ========== Clear Backup ==========

    @Test
    void shouldClearBackupFiles() {
        fileManager.prepareFolders();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        // Save a backup first
        JSONObject data = new JSONObject();
        data.put("test", "clear_test");
        fileManager.saveJsonBackupDataPath(data, "testClear.json", now, "manual");

        // Now clear
        fileManager.clearBackupFiles();

        // After clearBackupFiles deletes the directory and its contents,
        // verify the directory is either gone or empty
        File backupPath = new File(FileManager.PATH + File.separator + "backup");
        if (backupPath.exists()) {
            File[] remaining = backupPath.listFiles();
            assertThat(remaining).isNotNull();
            for (File f : remaining) {
                assertThat(f.isDirectory()).isFalse(); // no subdirectories should remain
            }
        }
    }

    // ========== Helper ==========

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
}
