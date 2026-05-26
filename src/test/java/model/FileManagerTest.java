package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.json.ObjectMapperFactory;
import model.modelManagers.FileManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class FileManagerTest {

    @TempDir
    Path tempDir;

    private FileManager newFileManager() {
        return new FileManager(tempDir.toString());
    }

    @Test
    void shouldNotThrowOnPrepareFolders() {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        assertThat(tempDir.resolve("data")).exists();
        assertThat(tempDir.resolve("backup")).exists();
        assertThat(tempDir.resolve("history")).exists();
    }

    @Test
    void shouldReturnNullForNonExistentFile() {
        FileManager fm = newFileManager();
        String result = fm.getJsonData("non_existent_file_12345.json");
        assertThat(result).isNull();
    }

    @Test
    void shouldSaveAndRetrieveJsonData() throws JsonProcessingException {
        FileManager fm = newFileManager();
        fm.prepareFolders();

        ObjectNode original = ObjectMapperFactory.get().createObjectNode();
        original.put("testKey", "testValue");
        original.put("number", 42);
        String jsonStr = ObjectMapperFactory.get().writeValueAsString(original);

        String testFileName = "test_data_save.json";
        fm.saveJsonMainDataPath(jsonStr, testFileName);

        String retrieved = fm.getJsonData(testFileName);
        assertThat(retrieved).isNotNull();
        JsonNode node = ObjectMapperFactory.get().readTree(retrieved);
        assertThat(node.get("testKey").asText()).isEqualTo("testValue");
        assertThat(node.get("number").asInt()).isEqualTo(42);
    }

    @Test
    void shouldOverwriteExistingFileOnSave() throws JsonProcessingException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        String testFileName = "test_overwrite.json";

        ObjectNode first = ObjectMapperFactory.get().createObjectNode();
        first.put("version", 1);
        fm.saveJsonMainDataPath(ObjectMapperFactory.get().writeValueAsString(first), testFileName);

        ObjectNode second = ObjectMapperFactory.get().createObjectNode();
        second.put("version", 2);
        second.put("extra", "data");
        fm.saveJsonMainDataPath(ObjectMapperFactory.get().writeValueAsString(second), testFileName);

        String retrieved = fm.getJsonData(testFileName);
        JsonNode node = ObjectMapperFactory.get().readTree(retrieved);
        assertThat(node.get("version").asInt()).isEqualTo(2);
        assertThat(node.get("extra").asText()).isEqualTo("data");
    }

    @Test
    void shouldSaveBackupDataAndCreateTimestampedFolder() throws JsonProcessingException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        ObjectNode data = ObjectMapperFactory.get().createObjectNode();
        data.put("backupData", true);
        data.put("timestamp", 123);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        fm.saveJsonBackupDataPath(ObjectMapperFactory.get().writeValueAsString(data), "testBackup.json", now, "auto");

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

    @Test
    void shouldSaveHistoryData() throws JsonProcessingException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        ObjectNode data = ObjectMapperFactory.get().createObjectNode();
        data.put("turnNumber", 1);
        data.put("totalSales", 50000);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        fm.saveHistoryData(ObjectMapperFactory.get().writeValueAsString(data), "turn", now);

        File historyPath = tempDir.resolve("history").toFile();
        File[] historyFiles = historyPath.listFiles((dir, name) -> name.startsWith("turn-"));
        assertThat(historyFiles).isNotNull();
        assertThat(historyFiles.length).isGreaterThan(0);
    }

    @Test
    void shouldRetrieveHistoryFiles() throws JsonProcessingException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        ObjectNode data = ObjectMapperFactory.get().createObjectNode();
        data.put("turnNumber", 5);
        data.put("testMarker", "history_retrieval_test");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        fm.saveHistoryData(ObjectMapperFactory.get().writeValueAsString(data), "turn", now);

        List<String> historyFiles = fm.getHistoryFiles();
        assertThat(historyFiles).isNotNull();

        boolean found = false;
        for (String jsonStr : historyFiles) {
            JsonNode entry = ObjectMapperFactory.get().readTree(jsonStr);
            if ("history_retrieval_test".equals(entry.get("testMarker").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    void shouldHandleJsonWithSpecialCharacters() throws JsonProcessingException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        String testFileName = "test_special_chars.json";

        ObjectNode data = ObjectMapperFactory.get().createObjectNode();
        data.put("message", "Hola, \u00bfc\u00f3mo est\u00e1s?");
        data.put("address", "Calle 123 #45-67, Apt. 8B");

        fm.saveJsonMainDataPath(ObjectMapperFactory.get().writeValueAsString(data), testFileName);

        String retrieved = fm.getJsonData(testFileName);
        JsonNode node = ObjectMapperFactory.get().readTree(retrieved);
        assertThat(node.get("message").asText()).isEqualTo("Hola, \u00bfc\u00f3mo est\u00e1s?");
        assertThat(node.get("address").asText()).isEqualTo("Calle 123 #45-67, Apt. 8B");
    }

    @Test
    void shouldSaveAndRetrieveNestedJsonArrays() throws JsonProcessingException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        String testFileName = "test_nested.json";

        ObjectNode data = ObjectMapperFactory.get().createObjectNode();
        ArrayNode array = ObjectMapperFactory.get().createArrayNode();
        for (int i = 0; i < 3; i++) {
            ObjectNode item = ObjectMapperFactory.get().createObjectNode();
            item.put("id", i);
            item.put("name", "Item " + i);
            array.add(item);
        }
        data.set("items", array);

        fm.saveJsonMainDataPath(ObjectMapperFactory.get().writeValueAsString(data), testFileName);

        String retrieved = fm.getJsonData(testFileName);
        JsonNode node = ObjectMapperFactory.get().readTree(retrieved);
        assertThat(node.get("items")).hasSize(3);
        assertThat(node.get("items").get(1).get("name").asText()).isEqualTo("Item 1");
    }

    @Test
    void shouldHandleSequentialSavesToSameFile() throws JsonProcessingException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        String testFileName = "test_sequential.json";

        for (int i = 0; i < 20; i++) {
            ObjectNode data = ObjectMapperFactory.get().createObjectNode();
            data.put("iteration", i);
            fm.saveJsonMainDataPath(ObjectMapperFactory.get().writeValueAsString(data), testFileName);
        }

        String retrieved = fm.getJsonData(testFileName);
        JsonNode node = ObjectMapperFactory.get().readTree(retrieved);
        assertThat(node.get("iteration").asInt()).isEqualTo(19);
    }

    @Test
    void shouldSkipInvalidJsonFilesInHistory() throws IOException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        File historyPath = tempDir.resolve("history").toFile();
        File invalidFile = new File(historyPath, "invalid-file-test.txt");
        Files.writeString(invalidFile.toPath(), "this is not valid JSON {[[[");
        invalidFile.setLastModified(System.currentTimeMillis() + 1000);

        List<String> result = fm.getHistoryFiles();
        assertThat(result).isNotNull();
    }

    @Test
    void shouldClearBackupFiles() throws JsonProcessingException {
        FileManager fm = newFileManager();
        fm.prepareFolders();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Bogota"));

        ObjectNode data = ObjectMapperFactory.get().createObjectNode();
        data.put("test", "clear_test");
        fm.saveJsonBackupDataPath(ObjectMapperFactory.get().writeValueAsString(data), "testClear.json", now, "manual");

        fm.clearBackupFiles();

        File backupPath = tempDir.resolve("backup").toFile();
        if (backupPath.exists()) {
            File[] remaining = backupPath.listFiles();
            if (remaining != null) {
                for (File f : remaining) {
                    assertThat(f.isDirectory()).isFalse();
                }
            }
        }
    }
}
