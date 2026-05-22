package model;

import model.modelManagers.FileManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FileManager} — JSON file I/O, backup, and history operations.
 *
 * <p>Uses {@link TempDir} for test isolation — no real filesystem side effects.
 */
class FileManagerTest {

    @TempDir
    Path tempDir;

    private FileManager newFileManager() {
        return new FileManager(tempDir.toString());
    }

    // ========== Folder Preparation ==========

    @Test
    void shouldNotThrowOnPrepareFolders() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        assertThat(tempDir.resolve("data")).exists();
        assertThat(tempDir.resolve("backup")).exists();
        assertThat(tempDir.resolve("history")).exists();
    }

    // ========== getJsonData ==========

    @Test
    void shouldReturnNullForNonExistentFile() {
        FileManager fm = newFileManager();
        JSONObject result = fm.getJsonData("non_existent_file_12345.json");
        assertThat(result).isNull();
    }

    // ========== Save and Retrieve JSON (main data path) ==========

    @Test
    void shouldSaveAndRetrieveJsonData() {
        FileManager fm = newFileManager();
        fm.prepareFolders();

        JSONObject original = new JSONObject();
        original.put("testKey", "testValue");
        original.put("number", 42);

        String testFileName = "test_data_save.json";
        fm.saveJsonMainDataPath(original, testFileName);

        JSONObject retrieved = fm.getJsonData(testFileName);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getString("testKey")).isEqualTo("testValue");
        assertThat(retrieved.getInt("number")).isEqualTo(42);
    }

    @Test
    void shouldOverwriteExistingFileOnSave() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        String testFileName = "test_overwrite.json";

        JSONObject first = new JSONObject();
        first.put("version", 1);
        fm.saveJsonMainDataPath(first, testFileName);

        JSONObject second = new JSONObject();
        second.put("version", 2);
        second.put("extra", "data");
        fm.saveJsonMainDataPath(second, testFileName);

        JSONObject retrieved = fm.getJsonData(testFileName);
        assertThat(retrieved.getInt("version")).isEqualTo(2);
        assertThat(retrieved.getString("extra")).isEqualTo("data");
    }

    // ========== Backup Data Path ==========

    @Test
    void shouldSaveBackupDataAndCreateTimestampedFolder() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        JSONObject data = new JSONObject();
        data.put("backupData", true);
        data.put("timestamp", 123);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        fm.saveJsonBackupDataPath(data, "testBackup.json", now, "auto");

        File backupPath = tempDir.resolve("backup").toFile();
        File[] backupDirs = backupPath.listFiles(File::isDirectory);
        assertThat(backupDirs).isNotNull();
        boolean found = false;
        for (File dir : backupDirs) {
            if (dir.getName().endsWith("-auto")) {
                File savedFile = new File(dir, "testBackup.json");
                if (savedFile.exists()) {
                    found = true;
                    break;
                }
            }
        }
        assertThat(found).isTrue();
    }

    // ========== History Data ==========

    @Test
    void shouldSaveHistoryData() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        JSONObject data = new JSONObject();
        data.put("turnNumber", 1);
        data.put("totalSales", 50000);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        fm.saveHistoryData(data, "turn", now);

        File historyPath = tempDir.resolve("history").toFile();
        File[] historyFiles = historyPath.listFiles((dir, name) -> name.startsWith("turn-"));
        assertThat(historyFiles).isNotNull();
        assertThat(historyFiles.length).isGreaterThan(0);
    }

    @Test
    void shouldRetrieveHistoryFiles() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        JSONObject data = new JSONObject();
        data.put("turnNumber", 5);
        data.put("testMarker", "history_retrieval_test");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        fm.saveHistoryData(data, "turn", now);

        JSONArray historyFiles = fm.getHistoryFiles();
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
    }

    // ========== JSON with Special Characters ==========

    @Test
    void shouldHandleJsonWithSpecialCharacters() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        String testFileName = "test_special_chars.json";

        JSONObject data = new JSONObject();
        data.put("message", "Hola, ¿cómo estás?");
        data.put("address", "Calle 123 #45-67, Apt. 8B");
        data.put("emoji", "\u00e1\u00e9\u00ed\u00f3\u00fa\u00f1");

        fm.saveJsonMainDataPath(data, testFileName);

        JSONObject retrieved = fm.getJsonData(testFileName);
        assertThat(retrieved.getString("message")).isEqualTo("Hola, ¿cómo estás?");
        assertThat(retrieved.getString("address")).isEqualTo("Calle 123 #45-67, Apt. 8B");
    }

    // ========== Nested JSON ==========

    @Test
    void shouldSaveAndRetrieveNestedJsonArrays() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
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

        fm.saveJsonMainDataPath(data, testFileName);

        JSONObject retrieved = fm.getJsonData(testFileName);
        assertThat(retrieved.getJSONArray("items")).hasSize(3);
        assertThat(retrieved.getJSONArray("items").getJSONObject(1).getString("name"))
                .isEqualTo("Item 1");
    }

    // ========== Sequential Access ==========

    @Test
    void shouldHandleSequentialSavesToSameFile() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        String testFileName = "test_sequential.json";

        for (int i = 0; i < 20; i++) {
            JSONObject data = new JSONObject();
            data.put("iteration", i);
            fm.saveJsonMainDataPath(data, testFileName);
        }

        JSONObject retrieved = fm.getJsonData(testFileName);
        assertThat(retrieved.getInt("iteration")).isEqualTo(19);
    }

    // ========== Get History Files with Invalid JSON ==========

    @Test
    void shouldSkipInvalidJsonFilesInHistory() throws IOException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        File historyPath = tempDir.resolve("history").toFile();
        File invalidFile = new File(historyPath, "invalid-file-test.txt");
        Files.writeString(invalidFile.toPath(), "this is not valid JSON {[[[");

        JSONArray result = fm.getHistoryFiles();
        assertThat(result).isNotNull();
    }

    // ========== Clear Backup ==========

    @Test
    void shouldClearBackupFiles() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        JSONObject data = new JSONObject();
        data.put("test", "clear_test");
        fm.saveJsonBackupDataPath(data, "testClear.json", now, "manual");

        fm.clearBackupFiles();

        File backupPath = tempDir.resolve("backup").toFile();
        if (backupPath.exists()) {
            File[] remaining = backupPath.listFiles();
            assertThat(remaining).isNotNull();
            for (File f : remaining) {
                assertThat(f.isDirectory()).isFalse();
            }
        }
    }
}
