package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Uses SHA-256 with a random salt.  Stored format: "base64salt:hexhash"
 */
public class PasswordUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Hash a plain-text password and return the storable string. */
    public static String hashPassword(String plainText) {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String hash = sha256(salt + plainText);
        return salt + ":" + hash;
    }

    /** Return true if plainText matches the stored hash. */
    public static boolean verifyPassword(String plainText, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        String[] parts = stored.split(":", 2);
        String salt = parts[0];
        String expectedHash = parts[1];
        return sha256(salt + plainText).equals(expectedHash);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
