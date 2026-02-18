package test;

import org.junit.Test;
import util.ValidationUtil;

import static org.junit.Assert.*;

/**
 * Unit tests for ValidationUtil (username and email validation).
 */
public class ValidationUtilTest {

    @Test
    public void isValidUsername_valid() {
        assertTrue(ValidationUtil.isValidUsername("abc"));
        assertTrue(ValidationUtil.isValidUsername("user123"));
        assertTrue(ValidationUtil.isValidUsername("user_name"));
        assertTrue(ValidationUtil.isValidUsername("user-name"));
        assertTrue(ValidationUtil.isValidUsername("a1b2c3"));
        assertTrue(ValidationUtil.isValidUsername("User30Chars________________"));
    }

    @Test
    public void isValidUsername_tooShort() {
        assertFalse(ValidationUtil.isValidUsername("ab"));
        assertFalse(ValidationUtil.isValidUsername("a"));
        assertFalse(ValidationUtil.isValidUsername(""));
    }

    @Test
    public void isValidUsername_tooLong() {
        assertFalse(ValidationUtil.isValidUsername("user31chars_________________"));
    }

    @Test
    public void isValidUsername_invalidChars() {
        assertFalse(ValidationUtil.isValidUsername("user name"));
        assertFalse(ValidationUtil.isValidUsername("user.name"));
        assertFalse(ValidationUtil.isValidUsername("user@name"));
    }

    @Test
    public void isValidUsername_nullOrBlank() {
        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername(""));
        assertFalse(ValidationUtil.isValidUsername("   "));
    }

    @Test
    public void isValidEmail_valid() {
        assertTrue(ValidationUtil.isValidEmail("a@b.co"));
        assertTrue(ValidationUtil.isValidEmail("user@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user.name@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user+tag@example.org"));
    }

    @Test
    public void isValidEmail_invalid() {
        assertFalse(ValidationUtil.isValidEmail("notanemail"));
        assertFalse(ValidationUtil.isValidEmail("@nodomain.com"));
        assertFalse(ValidationUtil.isValidEmail("noatsign.com"));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail("   "));
    }

    @Test
    public void isValidEmail_tooLong() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 255; i++) sb.append('a');
        sb.append("@b.co");
        assertFalse(ValidationUtil.isValidEmail(sb.toString()));
    }

    @Test
    public void isValidEmail_trimmed() {
        assertTrue(ValidationUtil.isValidEmail("  user@example.com  "));
    }
}
