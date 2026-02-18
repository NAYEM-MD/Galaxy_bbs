package test;

import org.junit.Test;
import util.FileTypeUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for FileTypeUtil (magic-byte image detection).
 */
public class FileTypeUtilTest {

    @Test
    public void detectImageExtension_jpeg() throws IOException {
        byte[] jpeg = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00 };
        try (ByteArrayInputStream in = new ByteArrayInputStream(jpeg)) {
            assertEquals(".jpg", FileTypeUtil.detectImageExtension(in));
        }
    }

    @Test
    public void detectImageExtension_png() throws IOException {
        byte[] png = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        try (ByteArrayInputStream in = new ByteArrayInputStream(png)) {
            assertEquals(".png", FileTypeUtil.detectImageExtension(in));
        }
    }

    @Test
    public void detectImageExtension_gif() throws IOException {
        byte[] gif89 = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61 };
        try (ByteArrayInputStream in = new ByteArrayInputStream(gif89)) {
            assertEquals(".gif", FileTypeUtil.detectImageExtension(in));
        }
    }

    @Test
    public void detectImageExtension_webp() throws IOException {
        byte[] webp = new byte[12];
        webp[0] = 0x52; webp[1] = 0x49; webp[2] = 0x46; webp[3] = 0x46; // RIFF
        webp[8] = 0x57; webp[9] = 0x45; webp[10] = 0x42; webp[11] = 0x50; // WEBP
        try (ByteArrayInputStream in = new ByteArrayInputStream(webp)) {
            assertEquals(".webp", FileTypeUtil.detectImageExtension(in));
        }
    }

    @Test
    public void detectImageExtension_invalidReturnsNull() throws IOException {
        byte[] random = new byte[] { 0x00, 0x01, 0x02 };
        try (ByteArrayInputStream in = new ByteArrayInputStream(random)) {
            assertNull(FileTypeUtil.detectImageExtension(in));
        }
    }

    @Test
    public void isAllowedImageByMagicBytes_valid() {
        byte[] jpeg = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
        assertTrue(FileTypeUtil.isAllowedImageByMagicBytes(new ByteArrayInputStream(jpeg)));
    }

    @Test
    public void isAllowedImageByMagicBytes_invalid() {
        byte[] random = new byte[] { 0x00, 0x01 };
        assertFalse(FileTypeUtil.isAllowedImageByMagicBytes(new ByteArrayInputStream(random)));
    }
}
