package util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter by key (e.g. IP address).
 * Limits number of attempts per time window.
 */
public class RateLimiter {

    private final int maxAttempts;
    private final long windowMs;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(int maxAttempts, long windowMs) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMs;
    }

    /**
     * Returns true if the request is allowed, false if rate limit exceeded.
     */
    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        Window w = windows.compute(key, (k, old) -> {
            if (old == null || now - old.startMs >= windowMs) {
                return new Window(now, 1);
            }
            if (old.count >= maxAttempts) {
                return old;
            }
            return new Window(old.startMs, old.count + 1);
        });
        return w.count <= maxAttempts;
    }

    /**
     * Returns remaining attempts in current window, or 0 if limited.
     */
    public int getRemainingAttempts(String key) {
        long now = System.currentTimeMillis();
        Window w = windows.get(key);
        if (w == null) return maxAttempts;
        if (now - w.startMs >= windowMs) return maxAttempts;
        return Math.max(0, maxAttempts - w.count);
    }

    private static class Window {
        final long startMs;
        final int count;

        Window(long startMs, int count) {
            this.startMs = startMs;
            this.count = count;
        }
    }
}
