package me.nethma.bookdiary.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility for hashing and verifying passwords.
 *
 * Format stored in DB:  <base64-salt>$<base64-sha256-hash>
 *
 * A random 16-byte salt is generated per password so the same password
 * produces a different stored value every time — preventing rainbow-table attacks.
 */
public class PasswordUtils {

    private static final int SALT_BYTES = 16;
    private static final String DELIMITER = "$";

    /**
     * Hash a plain-text password.
     * @return  "base64(salt)$base64(sha256(salt+password))"
     */
    public static String hash(String plainPassword) {
        byte[] salt = generateSalt();
        byte[] hash = sha256(salt, plainPassword);
        String saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP);
        String hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP);
        return saltB64 + DELIMITER + hashB64;
    }

    /**
     * Verify a plain-text password against a stored hash.
     * @param plainPassword  the password the user typed
     * @param storedHash     the value stored in the database
     * @return true if they match
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;

        // Support legacy plain-text passwords (no delimiter) — auto-migrated on login
        if (!storedHash.contains(DELIMITER)) {
            return plainPassword.equals(storedHash);
        }

        String[] parts = storedHash.split("\\" + DELIMITER, 2);
        if (parts.length != 2) return false;

        try {
            byte[] salt         = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] expectedHash = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] actualHash   = sha256(salt, plainPassword);
            return MessageDigest.isEqual(actualHash, expectedHash);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static byte[] sha256(byte[] salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            md.update(password.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
