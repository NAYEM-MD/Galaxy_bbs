package util;

import java.util.regex.Pattern;

public class ValidationUtil {
    /** Basic RFC 5322-style email validation. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
