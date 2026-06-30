package model.email.config;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;

class CredentialStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void encryptDecryptRoundtrip() throws Exception {
        String original = "{\"user\":\"admin\",\"pass\":\"secret123\"}";
        Path file = tempDir.resolve("secure.dat");

        CredentialStore.saveEncryptedJson(original, file);
        assertThat(Files.exists(file)).isTrue();

        String decrypted = CredentialStore.loadEncryptedJson(file).orElseThrow();
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void loadNonExistentFile_shouldReturnEmpty() throws Exception {
        Path missing = tempDir.resolve("nonexistent.dat");
        assertThat(CredentialStore.loadEncryptedJson(missing)).isEmpty();
    }

    @Test
    void corruptedFile_shouldThrow() {
        Path corrupted = tempDir.resolve("corrupt.dat");
        assertThatCode(() -> {
            Files.writeString(corrupted, "not-valid-base64!!");
            CredentialStore.loadEncryptedJson(corrupted);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void emptyStringRoundtrip() throws Exception {
        Path file = tempDir.resolve("empty.dat");
        CredentialStore.saveEncryptedJson("", file);
        String decrypted = CredentialStore.loadEncryptedJson(file).orElseThrow();
        assertThat(decrypted).isEmpty();
    }

    @Test
    void specialCharactersRoundtrip() throws Exception {
        String original = "user@domain.com:app-pass-123!@#$%^&*()_+<>?|";
        Path file = tempDir.resolve("special.dat");

        CredentialStore.saveEncryptedJson(original, file);
        String decrypted = CredentialStore.loadEncryptedJson(file).orElseThrow();
        assertThat(decrypted).isEqualTo(original);
    }
}
