package controller;

import dao.UserDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Handles profile photo upload (multipart/form-data).
 * Saves file under /uploads/profiles/, stores relative path in USERS.profile_image,
 * and sets updated user in session. Redirects back to profile.
 */
public class ProfilePhotoUploadServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    private static final String UPLOAD_SUBDIR = "uploads" + File.separator + "profiles";
    private static final java.util.Set<String> ALLOWED_IMAGE_EXTENSIONS = java.util.Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");
    private static final String RELATIVE_PATH_PREFIX = "uploads/profiles/";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        Part filePart = request.getPart("profilePhoto");
        if (filePart == null || filePart.getSize() == 0) {
            response.sendRedirect(request.getContextPath() + "/profile?username=" + user.getUsername());
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.sendRedirect(request.getContextPath() + "/profile?username=" + user.getUsername());
            return;
        }

        String fileName = getSubmittedFileName(filePart);
        String extension = "";
        if (fileName != null) {
            int i = fileName.lastIndexOf('.');
            if (i > 0) extension = fileName.substring(i);
        }
        if (extension.isEmpty()) {
            if (contentType.contains("jpeg") || contentType.contains("jpg")) extension = ".jpg";
            else if (contentType.contains("png")) extension = ".png";
            else if (contentType.contains("gif")) extension = ".gif";
            else if (contentType.contains("webp")) extension = ".webp";
            else extension = ".jpg";
        }
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            response.sendRedirect(request.getContextPath() + "/profile?username=" + user.getUsername() + "&error=invalid_file");
            return;
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String basePath = getServletContext().getRealPath("");
        File uploadDir = new File(basePath, UPLOAD_SUBDIR);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        File dest = new File(uploadDir, uniqueFileName);
        filePart.write(dest.getAbsolutePath());

        String relativePath = RELATIVE_PATH_PREFIX + uniqueFileName;
        if (userDAO.updateProfileImage(user.getId(), relativePath)) {
            user.setProfileImage(relativePath);
            session.setAttribute("user", user);
        }

        response.sendRedirect(request.getContextPath() + "/profile?username=" + user.getUsername());
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
