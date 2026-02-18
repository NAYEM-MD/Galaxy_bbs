package controller;

import dao.UserDAO;
import model.User;
import util.FileTypeUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Handles profile photo upload (multipart/form-data).
 * Saves file under /uploads/profiles/, stores relative path in USERS.profile_image,
 * and sets updated user in session. Redirects back to profile.
 */
public class ProfilePhotoUploadServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    private static final String UPLOAD_SUBDIR = "uploads" + File.separator + "profiles";
    private static final String RELATIVE_PATH_PREFIX = "uploads/profiles/";
    /** Max profile photo size 5MB. */
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String usernameEnc = URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8);
        Part filePart = request.getPart("profilePhoto");
        if (filePart == null || filePart.getSize() == 0) {
            response.sendRedirect(request.getContextPath() + "/profile?username=" + usernameEnc);
            return;
        }
        if (filePart.getSize() > MAX_FILE_SIZE) {
            response.sendRedirect(request.getContextPath() + "/profile?username=" + usernameEnc + "&error=file_too_large");
            return;
        }

        // Validate by magic bytes (server-side); Content-Type can be spoofed
        byte[] fileBytes;
        try {
            fileBytes = readAllBytes(filePart.getInputStream(), MAX_FILE_SIZE);
        } catch (IOException e) {
            response.sendRedirect(request.getContextPath() + "/profile?username=" + usernameEnc + "&error=file_too_large");
            return;
        }
        String extension;
        try (InputStream in = new ByteArrayInputStream(fileBytes)) {
            extension = FileTypeUtil.detectImageExtension(in);
        }
        if (extension == null || !FileTypeUtil.ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            response.sendRedirect(request.getContextPath() + "/profile?username=" + usernameEnc + "&error=invalid_file");
            return;
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String basePath = getServletContext().getRealPath("");
        File uploadDir = new File(basePath, UPLOAD_SUBDIR);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        File dest = new File(uploadDir, uniqueFileName);
        // Sandbox: ensure dest stays under uploadDir (prevent path traversal)
        try {
            String canonicalDest = dest.getCanonicalPath();
            String canonicalDir = uploadDir.getCanonicalPath();
            if (!canonicalDest.startsWith(canonicalDir + File.separator) && !canonicalDest.equals(canonicalDir)) {
                response.sendRedirect(request.getContextPath() + "/profile?username=" + usernameEnc + "&error=invalid_file");
                return;
            }
        } catch (java.io.IOException e) {
            response.sendRedirect(request.getContextPath() + "/profile?username=" + usernameEnc + "&error=invalid_file");
            return;
        }
        java.nio.file.Files.write(dest.toPath(), fileBytes);

        String relativePath = RELATIVE_PATH_PREFIX + uniqueFileName;
        if (userDAO.updateProfileImage(user.getId(), relativePath)) {
            user.setProfileImage(relativePath);
            session.setAttribute("user", user);
        }

        response.sendRedirect(request.getContextPath() + "/profile?username=" + usernameEnc);
    }

    private static byte[] readAllBytes(InputStream in, int maxSize) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
            if (out.size() > maxSize) throw new IOException("File too large");
        }
        return out.toByteArray();
    }

    private String getSubmittedFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        for (String s : cd.split(";")) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
