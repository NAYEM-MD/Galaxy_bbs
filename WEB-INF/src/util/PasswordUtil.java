package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password hashing using bcrypt (with salt).
 * Requires jbcrypt-0.4.jar in WEB-INF/lib (Maven: org.mindrot:jbcrypt:0.4)
 */
public class PasswordUtil {

    /** Bcrypt hashes start with this prefix. */
    private static final String BCRYPT_PREFIX = "$2";

    /**
     * Hashes a password using bcrypt with automatic salt.
     */
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            return org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifies a plain password against a stored hash.
     * Supports bcrypt and legacy SHA-256 (for migration).
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        String input = plainPassword.trim();

        // Bcrypt hash
        if (storedHash.startsWith(BCRYPT_PREFIX)) {
            try {
                return org.mindrot.jbcrypt.BCrypt.checkpw(input, storedHash);
            } catch (Exception e) {
                return false;
            }
        }

        // Legacy SHA-256 (migration)
        String hashedInput = legacyHash(input);
        return storedHash.equals(hashedInput);
    }

    /**
     * Deterministic hash for reset tokens (SHA-256). Tokens need lookup by hash, so we use
     * a fixed hash rather than bcrypt (which uses random salt).
     */
    public static String hashForToken(String token) {
        return legacyHash(token);
    }

    /**
     * Legacy SHA-256 hash (no salt). Used for tokens and for verifying old passwords during migration.
     */
    public static String legacyHash(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
