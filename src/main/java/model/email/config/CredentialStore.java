package model.email.config;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Machine-bound credential storage using AES-256-GCM encryption.
 * <p>
 * The encryption key is derived from a persisted machine ID (UUID stored in
 * {@code ~/.motel_management/machine-id}) via PBKDF2WithHmacSHA256.
 * The ID is created once on first use, so it survives reboots, network
 * adapter changes, VPN installations, and OS user name changes.
 * <p>
 * The encrypted file is useless if copied to another machine.
 */
public final class CredentialStore {

    private static final Logger LOG = System.getLogger(CredentialStore.class.getName());
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int AES_KEY_LENGTH = 256;
    private static final int PBKDF2_ITERATIONS = 100_000;
    private static final int SALT_LENGTH = 16;
    private static final String MACHINE_ID_FILE = System.getProperty("user.home")
            + "/.motel_management/machine-id";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static volatile String machineId;

    private CredentialStore() {}

    public static void saveEncryptedJson(String plainText, Path filePath) throws Exception {
        saveEncrypted(plainText, filePath);
    }

    public static Optional<String> loadEncryptedJson(Path filePath) throws Exception {
        return loadEncrypted(filePath);
    }

    private static void saveEncrypted(String plainText, Path filePath) throws Exception {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        SecretKey key = deriveKey(salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV();
        byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[salt.length + iv.length + ciphertext.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(ciphertext, 0, combined, salt.length + iv.length, ciphertext.length);

        String encoded = Base64.getEncoder().encodeToString(combined);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, encoded, StandardCharsets.UTF_8);
    }

    private static Optional<String> loadEncrypted(Path filePath) throws Exception {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        String encoded = Files.readString(filePath, StandardCharsets.UTF_8).trim();
        byte[] combined = Base64.getDecoder().decode(encoded);

        if (combined.length < SALT_LENGTH + GCM_IV_LENGTH + 1) {
            throw new IllegalArgumentException("Secure data file is corrupted");
        }

        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - SALT_LENGTH - GCM_IV_LENGTH];
        System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
        System.arraycopy(combined, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(combined, SALT_LENGTH + GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        SecretKey key = deriveKey(salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return Optional.of(new String(plaintext, StandardCharsets.UTF_8));
    }

    private static SecretKey deriveKey(byte[] salt) throws Exception {
        String hostname = getHostname();
        String id = getMachineId();
        String keyPass = hostname + "|" + id;
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(keyPass.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private static String getMachineId() throws IOException {
        if (machineId != null) return machineId;
        Path idPath = Path.of(MACHINE_ID_FILE);
        if (Files.exists(idPath)) {
            machineId = Files.readString(idPath, StandardCharsets.UTF_8).trim();
        } else {
            machineId = UUID.randomUUID().toString();
            Files.createDirectories(idPath.getParent());
            Files.writeString(idPath, machineId, StandardCharsets.UTF_8);
            LOG.log(Level.INFO, "Created persistent machine ID at " + MACHINE_ID_FILE);
        }
        return machineId;
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }
}
