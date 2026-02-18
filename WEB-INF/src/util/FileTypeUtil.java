package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Validates file type by reading magic bytes (file header) server-side.
 * Content-Type header can be spoofed; magic bytes cannot.
 */
public final class FileTypeUtil {

    private FileTypeUtil() {}

    // Magic bytes for common image formats (first few bytes)
    private static final byte[] JPEG = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    private static final byte[] PNG = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    private static final byte[] GIF_89 = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61 }; // GIF89a
    private static final byte[] GIF_87 = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x37, 0x61 }; // GIF87a
    private static final byte[] WEBP_RIFF = new byte[] { 0x52, 0x49, 0x46, 0x46 }; // RIFF
    private static final byte[] WEBP_WEBP = new byte[] { 0x57, 0x45, 0x42, 0x50 }; // WEBP (at offset 8)
    private static final byte[] BMP = new byte[] { 0x42, 0x4D }; // BM

    /** Allowed image extensions that we validate by magic bytes. */
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");

    /**
     * Returns the detected image extension (e.g. ".jpg") from magic bytes, or null if not a supported image.
     */
    public static String detectImageExtension(InputStream in) throws IOException {
        if (in == null) return null;
        byte[] header = new byte[12];
        int n = in.read(header);
        if (n < 2) return null;

        if (matches(header, n, JPEG)) return ".jpg";
        if (matches(header, n, PNG)) return ".png";
        if (matches(header, n, GIF_89) || matches(header, n, GIF_87)) return ".gif";
        if (matches(header, n, BMP)) return ".bmp";
        if (n >= 12 && matches(header, 4, WEBP_RIFF) && matchesAt(header, 8, WEBP_WEBP)) return ".webp";

        return null;
    }

    /**
     * Returns true if the stream's magic bytes indicate a supported image type.
     */
    public static boolean isAllowedImageByMagicBytes(InputStream in) {
        try {
            return detectImageExtension(in) != null;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean matches(byte[] data, int len, byte[] magic) {
        if (len < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) return false;
        }
        return true;
    }

    private static boolean matchesAt(byte[] data, int offset, byte[] magic) {
        if (offset + magic.length > data.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if (data[offset + i] != magic[i]) return false;
        }
        return true;
    }
}
