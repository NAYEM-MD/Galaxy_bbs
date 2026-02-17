package controller;

import dao.UserDAO;
import dao.PasswordResetTokenDAO;
import model.User;
import util.EmailUtil;
import util.ValidationUtil;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;

public class AuthServlet extends HttpServlet {
    private UserDAO userDAO; // lazy init in doPost so doGet never touches DB
    private PasswordResetTokenDAO tokenDAO = new PasswordResetTokenDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath(); // exact: /auth/login, /auth/register, /auth/logout, /auth/forgot-password, /auth/reset-password
        switch (path) {
            case "/auth/login":
                forwardTo(request, response, "/auth/login.jsp");
                break;
            case "/auth/register":
                forwardTo(request, response, "/auth/register.jsp");
                break;
            case "/auth/forgot-password":
                forwardTo(request, response, "/auth/forgot-password.jsp");
                break;
            case "/auth/reset-password":
                String token = request.getParameter("token");
                if (token == null || token.trim().isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/auth/login");
                    return;
                }
                request.setAttribute("token", token);
                forwardTo(request, response, "/auth/reset-password.jsp");
                break;
            case "/auth/logout":
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/auth/login");
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void forwardTo(HttpServletRequest request, HttpServletResponse response, String jspPath) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher(jspPath);
        if (rd == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        rd.forward(request, response);
    }

    private UserDAO getUserDAO() {
        if (userDAO == null) userDAO = new UserDAO();
        return userDAO;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/auth/login".equals(path)) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            if (email != null) email = email.trim();
            if (password != null) password = password.trim();
            if (email == null) email = "";
            if (password == null) password = "";
            
            System.out.println("Login attempt for: " + email); // Debug log
            
            User user = getUserDAO().checkLogin(email, password);
            if (user != null) {
                System.out.println("Login successful for: " + email); // Debug log
                HttpSession session = request.getSession();
                session.setAttribute("loginUser", user);  // used by AuthFilter
                session.setAttribute("user", user);      // used by JSPs and other servlets
                response.sendRedirect(request.getContextPath() + "/posts/feed");
            } else {
                System.out.println("Login failed for: " + email); // Debug log
                request.setAttribute("errorMsg", "Invalid email or password");
                forwardTo(request, response, "/auth/login.jsp");
            }
        } else if ("/auth/register".equals(path)) {
            String email = request.getParameter("email");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            if (email != null) email = email.trim();
            if (username != null) username = username.trim();
            if (password != null) password = password.trim();
            if (email == null) email = "";
            if (username == null) username = "";
            if (password == null) password = "";

            System.out.println("Registering user: " + email); // Debug log

            if (!ValidationUtil.isValidEmail(email)) {
                request.setAttribute("errorMsg", "Please enter a valid email address.");
                forwardTo(request, response, "/auth/register.jsp");
                return;
            }
            if (getUserDAO().findByEmail(email) != null) {
                request.setAttribute("errorMsg", "Email already registered");
                forwardTo(request, response, "/auth/register.jsp");
                return;
            }

            User user = new User(username, password, email);
            getUserDAO().save(user);
            System.out.println("User registered successfully: " + email); // Debug log

            // Only new account creators get welcome email — sent to the address they registered with (Gmail or any)
            EmailUtil.sendEmail(email, "Welcome to Galaxy_BBS",
                "Hello,\n\n"
                + "Thank you for creating an account on Galaxy_BBS.\n\n"
                + "Your registration was successful, and you can now log in using your registered email address.\n\n"
                + "We hope you enjoy sharing and discussing ideas with the Galaxy_BBS community.\n\n"
                + "Best regards,\n"
                + "Galaxy_BBS Team");

            // Redirect to login page
            response.sendRedirect(request.getContextPath() + "/auth/login");
        } else if ("/auth/forgot-password".equals(path)) {
            handleForgotPassword(request, response);
        } else if ("/auth/reset-password".equals(path)) {
            handleResetPassword(request, response);
        }
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        if (email != null) email = email.trim();
        if (email == null) email = "";

        User user = getUserDAO().findByEmail(email);

        // Always show a generic message to avoid user enumeration
        String genericMsg = "If an account with that email exists, a reset link has been sent.";
        request.setAttribute("infoMsg", genericMsg);

        if (user == null) {
            forwardTo(request, response, "/auth/forgot-password.jsp");
            return;
        }

        // Generate a random token
        String token = java.util.UUID.randomUUID().toString() + "-" + Long.toHexString(System.nanoTime());
        long expiresAt = System.currentTimeMillis() + (60L * 60L * 1000L); // 1 hour

        try {
            // Persist token in DB (requires password_reset_tokens table)
            tokenDAO.createToken(user.getId(), token, expiresAt);

            String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
            String resetLink = baseUrl + "/auth/reset-password?token=" + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);

            EmailUtil.sendEmail(email, "Galaxy_BBS - Password Reset",
                "Hello,\n\n"
                + "We received a request to reset the password for your Galaxy_BBS account.\n\n"
                + "To reset your password, click the link below (or copy and paste it into your browser):\n"
                + resetLink + "\n\n"
                + "This link will expire in 1 hour. If you did not request a password reset, you can safely ignore this email.\n\n"
                + "Best regards,\n"
                + "Galaxy_BBS Team");
        } catch (Throwable t) {
            // Most common causes:
            // - password_reset_tokens table does not exist
            // - JDBC error
            // - Jakarta Mail / SMTP misconfiguration
            System.err.println("[Galaxy_BBS] Failed to start password reset flow: " + t.getMessage());
            t.printStackTrace();
            request.setAttribute("errorMsg", "We could not send a reset email right now. Please try again later.");
        }

        forwardTo(request, response, "/auth/forgot-password.jsp");
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");

        if (token == null || token.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        if (password == null) password = "";
        if (confirm == null) confirm = "";

        if (!password.equals(confirm)) {
            request.setAttribute("errorMsg", "Passwords do not match.");
            request.setAttribute("token", token);
            forwardTo(request, response, "/auth/reset-password.jsp");
            return;
        }

        if (password.trim().isEmpty()) {
            request.setAttribute("errorMsg", "Password cannot be empty.");
            request.setAttribute("token", token);
            forwardTo(request, response, "/auth/reset-password.jsp");
            return;
        }

        int userId = tokenDAO.findUserIdByValidToken(token);
        if (userId <= 0) {
            request.setAttribute("errorMsg", "Reset link is invalid or has expired.");
            forwardTo(request, response, "/auth/forgot-password.jsp");
            return;
        }

        boolean updated = getUserDAO().updatePassword(userId, password);
        if (updated) {
            tokenDAO.markTokenUsed(token);
            response.sendRedirect(request.getContextPath() + "/auth/login?reset=1");
        } else {
            request.setAttribute("errorMsg", "Could not reset password. Please try again.");
            request.setAttribute("token", token);
            forwardTo(request, response, "/auth/reset-password.jsp");
        }
    }

}
