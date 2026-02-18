package util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 30;
    /** Basic RFC 5322-style email validation. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );
    /** Username: alphanumeric, underscore, hyphen; 3-30 chars. */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_-]{" + USERNAME_MIN_LENGTH + "," + USERNAME_MAX_LENGTH + "}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        String s = username.trim();
        return s.length() >= USERNAME_MIN_LENGTH && s.length() <= USERNAME_MAX_LENGTH
            && USERNAME_PATTERN.matcher(s).matches();
    }
}
