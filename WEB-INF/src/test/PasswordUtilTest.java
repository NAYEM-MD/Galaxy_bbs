package test;

import org.junit.Test;
import util.PasswordUtil;

import static org.junit.Assert.*;

/**
 * Unit tests for PasswordUtil (hashing and verification).
 */
public class PasswordUtilTest {

    @Test
    public void hashPassword_returnsBcryptHash() {
        String hash = PasswordUtil.hashPassword("mypassword");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(hash.length() > 20);
    }

    @Test
    public void hashPassword_nullReturnsNull() {
        assertNull(PasswordUtil.hashPassword(null));
    }

    @Test
    public void verifyPassword_correctPassword() {
        String hash = PasswordUtil.hashPassword("secret123");
        assertTrue(PasswordUtil.verifyPassword("secret123", hash));
    }

    @Test
    public void verifyPassword_wrongPassword() {
        String hash = PasswordUtil.hashPassword("secret123");
        assertFalse(PasswordUtil.verifyPassword("wrong", hash));
        assertFalse(PasswordUtil.verifyPassword("", hash));
    }

    @Test
    public void verifyPassword_nullInput() {
        String hash = PasswordUtil.hashPassword("x");
        assertFalse(PasswordUtil.verifyPassword(null, hash));
        assertFalse(PasswordUtil.verifyPassword("x", null));
    }

    @Test
    public void hashForToken_deterministic() {
        String t = "reset-token-123";
        String h1 = PasswordUtil.hashForToken(t);
        String h2 = PasswordUtil.hashForToken(t);
        assertNotNull(h1);
        assertEquals(h1, h2);
    }

    @Test
    public void hashForToken_differentInputDifferentOutput() {
        String h1 = PasswordUtil.hashForToken("token1");
        String h2 = PasswordUtil.hashForToken("token2");
        assertNotEquals(h1, h2);
    }
}
