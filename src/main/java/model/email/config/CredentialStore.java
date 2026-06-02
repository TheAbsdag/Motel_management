package model.email.config;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Machine-bound credential storage using AES-256-GCM encryption.
 * <p>
 * The encryption key is derived from a fingerprint of the current machine
 * (primary MAC address + OS user + hostname) via PBKDF2WithHmacSHA256.
 * This means the {@code .env} file is useless if copied to another machine
 * or used under a different OS user.
 * <p>
 * The .env file contains only the base64-encoded ciphertext with no
 * comments or metadata.
 */
public final class CredentialStore {

    private static final Logger LOG = System.getLogger(CredentialStore.class.getName());
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int AES_KEY_LENGTH = 256;
    private static final int PBKDF2_ITERATIONS = 100_000;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CredentialStore() {}

    /**
     * Saves an arbitrary plaintext string (e.g., JSON) encrypted to the given file.
     * The file will contain only the base64-encoded ciphertext (salt + IV + data).
     */
    public static void saveEncryptedJson(String plainText, Path filePath) throws Exception {
        saveEncrypted(plainText, filePath);
    }

    /**
     * Loads and decrypts arbitrary content from the given file.
     *
     * @return the decrypted string, or empty if the file does not exist
     * @throws Exception if decryption fails (wrong machine, corrupted file, etc.)
     */
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

    /**
     * Saves an encrypted credential to the given file path.
     *
     * @deprecated use {@link #saveEncryptedJson(String, Path)} instead
     */
    @Deprecated
    public static void saveCredential(String credential, Path filePath) throws Exception {
        saveEncrypted(credential, filePath);
    }

    /**
     * Loads and decrypts a credential from the given file path.
     *
     * @return the decrypted credential, or empty if the file does not exist
     * @throws Exception if decryption fails (wrong machine, corrupted file, etc.)
     * @deprecated use {@link #loadEncryptedJson(Path)} instead
     */
    @Deprecated
    public static Optional<String> loadCredential(Path filePath) throws Exception {
        return loadEncrypted(filePath);
    }

    /**
     * Deletes the credential file if it exists.
     */
    public static void deleteCredential(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to delete credential file: " + filePath, e);
        }
    }

    private static SecretKey deriveKey(byte[] salt) throws Exception {
        String fingerprint = buildMachineFingerprint();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(fingerprint.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /**
     * Builds a machine-specific fingerprint used as the key derivation password.
     * Combines primary MAC address + OS user name + host name.
     */
    private static String buildMachineFingerprint() throws Exception {
        String mac = getPrimaryMacAddress();
        String user = System.getProperty("user.name", "unknown");
        String host = getHostName();
        return mac + "|" + user + "|" + host;
    }

    private static String getPrimaryMacAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) {
                continue;
            }
            byte[] mac = iface.getHardwareAddress();
            if (mac != null && mac.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (byte b : mac) {
                    sb.append(String.format("%02X", b));
                }
                return sb.toString();
            }
        }
        return "00-00-00-00-00-00";
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }
}
