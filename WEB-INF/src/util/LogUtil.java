package util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central logging for Galaxy BBS using java.util.logging.
 * Use this instead of e.printStackTrace() for consistent, configurable logging.
 */
public final class LogUtil {

    private LogUtil() {}

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Log an error with optional message and throwable.
     */
    public static void logError(Logger log, String message, Throwable t) {
        if (log != null) {
            String msg = (message != null && !message.isEmpty()) ? message : (t != null ? t.getMessage() : "Unknown error");
            log.log(Level.SEVERE, msg, t);
        }
    }

    public static void logError(Logger log, Throwable t) {
        logError(log, null, t);
    }
}
