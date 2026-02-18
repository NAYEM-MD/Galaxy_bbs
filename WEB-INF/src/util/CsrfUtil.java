package util;

import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF token generation and validation.
 */
public final class CsrfUtil {
    private static final String SESSION_ATTR = "csrfToken";
    private static final int TOKEN_BYTES = 24;
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {}

    /** Ensures the session has a CSRF token; creates one if missing. Returns the token. */
    public static String getOrCreateToken(HttpSession session) {
        if (session == null) return null;
        String token = (String) session.getAttribute(SESSION_ATTR);
        if (token == null || token.isEmpty()) {
            byte[] bytes = new byte[TOKEN_BYTES];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_ATTR, token);
        }
        return token;
    }

    /** Validates the request token against the session token. Accepts parameter "csrfToken" or header "X-CSRF-Token". */
    public static boolean isValid(HttpSession session, String requestToken) {
        if (session == null || requestToken == null || requestToken.isEmpty()) return false;
        String sessionToken = (String) session.getAttribute(SESSION_ATTR);
        return sessionToken != null && sessionToken.equals(requestToken.trim());
    }
}
