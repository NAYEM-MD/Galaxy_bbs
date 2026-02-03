package controller;

import dao.UserDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class SettingsServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        request.setAttribute("user", user);
        request.getRequestDispatcher("/WEB-INF/views/settings.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        int userId = user.getId();

        String action = request.getParameter("action");
        if (action == null) action = "";

        if ("updateEmail".equals(action)) {
            String newEmail = request.getParameter("newEmail");
            if (newEmail != null && !newEmail.trim().isEmpty()) {
                if (userDAO.updateEmail(userId, newEmail.trim())) {
                    user.setEmail(newEmail.trim());
                    session.setAttribute("user", user);
                    request.setAttribute("message", "Email updated successfully.");
                } else {
                    request.setAttribute("error", "Could not update email. It may already be in use.");
                }
            } else {
                request.setAttribute("error", "Email cannot be empty.");
            }
        } else if ("changePassword".equals(action)) {
            String oldPassword = request.getParameter("oldPassword");
            String newPassword = request.getParameter("newPassword");
            if (oldPassword == null) oldPassword = "";
            if (newPassword == null) newPassword = "";
            if (!userDAO.verifyPassword(userId, oldPassword)) {
                request.setAttribute("error", "Current password is incorrect.");
            } else if (newPassword.trim().isEmpty()) {
                request.setAttribute("error", "New password cannot be empty.");
            } else if (userDAO.updatePassword(userId, newPassword.trim())) {
                request.setAttribute("message", "Password changed successfully.");
            } else {
                request.setAttribute("error", "Could not update password.");
            }
        } else if ("deleteAccount".equals(action)) {
            String password = request.getParameter("deletePassword");
            if (password == null) password = "";
            if (!userDAO.verifyPassword(userId, password)) {
                request.setAttribute("error", "Password is incorrect. Account not deleted.");
            } else if (userDAO.deleteUser(userId)) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/auth/login?deleted=1");
                return;
            } else {
                request.setAttribute("error", "Could not delete account. Please try again.");
            }
        }

        request.setAttribute("user", user);
        request.getRequestDispatcher("/WEB-INF/views/settings.jsp").forward(request, response);
    }
}
