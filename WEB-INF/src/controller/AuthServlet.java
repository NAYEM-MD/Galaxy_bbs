package controller;

import dao.UserDAO;
import dao.PasswordResetTokenDAO;
import model.User;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
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

            if (getUserDAO().findByEmail(email) != null) {
                request.setAttribute("errorMsg", "Email already registered");
                forwardTo(request, response, "/auth/register.jsp");
                return;
            }

            User user = new User(username, password, email);
            getUserDAO().save(user);
            System.out.println("User registered successfully: " + email); // Debug log

            // Only new account creators get welcome email — sent to the address they registered with (Gmail or any)
            sendWelcomeEmail(email);

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

            sendPasswordResetEmail(email, resetLink);
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

    /**
     * Sends a plain-text welcome email to the user after successful registration.
     * Uses Jakarta Mail API with Gmail SMTP. Credentials are read from environment
     * variables (SMTP_USER, SMTP_PASSWORD) or placeholders for development.
     * Email failure does not block registration; user is still redirected to login.
     */
    private void sendWelcomeEmail(String toEmail) {
        // Read from env: run set_smtp_env.bat in the SAME Command Prompt before starting Tomcat
        String user = System.getenv("SMTP_USER");
        String pass = System.getenv("SMTP_PASSWORD");
        if (user == null || user.isEmpty()) {
            user = "YOUR_GMAIL@gmail.com";
            System.err.println("[Galaxy_BBS] SMTP_USER not set. Run set_smtp_env.bat in the same window before starting Tomcat.");
        }
        if (pass == null || pass.isEmpty()) {
            pass = "YOUR_APP_PASSWORD";
            System.err.println("[Galaxy_BBS] SMTP_PASSWORD not set. Gmail will reject login.");
        }
        final String smtpUser = user;
        final String smtpPassword = pass;

        // Gmail SMTP (TLS 587); trust Gmail for SSL
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Authenticator supplies credentials when the server requests them
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        };

        Session session = Session.getInstance(props, authenticator);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(user, "Galaxy_BBS"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Welcome to Galaxy_BBS");
            msg.setSentDate(new Date());
            // Plain text body exactly as required (no HTML)
            msg.setText(
                "Hello,\n\n"
                + "Thank you for creating an account on Galaxy_BBS.\n\n"
                + "Your registration was successful, and you can now log in using your registered email address.\n\n"
                + "We hope you enjoy sharing and discussing ideas with the Galaxy_BBS community.\n\n"
                + "Best regards,\n"
                + "Galaxy_BBS Team"
            );

            Transport.send(msg);
            System.out.println("[Galaxy_BBS] Welcome email sent to: " + toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("[Galaxy_BBS] Failed to send welcome email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a password reset email with a unique link.
     */
    private void sendPasswordResetEmail(String toEmail, String resetLink) {
        String user = System.getenv("SMTP_USER");
        String pass = System.getenv("SMTP_PASSWORD");
        if (user == null || user.isEmpty()) {
            user = "YOUR_GMAIL@gmail.com";
            System.err.println("[Galaxy_BBS] SMTP_USER not set. Run set_smtp_env.bat in the same window before starting Tomcat.");
        }
        if (pass == null || pass.isEmpty()) {
            pass = "YOUR_APP_PASSWORD";
            System.err.println("[Galaxy_BBS] SMTP_PASSWORD not set. Gmail will reject login.");
        }
        final String smtpUser = user;
        final String smtpPassword = pass;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        };

        Session session = Session.getInstance(props, authenticator);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(user, "Galaxy_BBS"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Galaxy_BBS - Password Reset");
            msg.setSentDate(new Date());
            msg.setText(
                "Hello,\n\n"
                + "We received a request to reset the password for your Galaxy_BBS account.\n\n"
                + "To reset your password, click the link below (or copy and paste it into your browser):\n"
                + resetLink + "\n\n"
                + "This link will expire in 1 hour. If you did not request a password reset, you can safely ignore this email.\n\n"
                + "Best regards,\n"
                + "Galaxy_BBS Team"
            );

            Transport.send(msg);
            System.out.println("[Galaxy_BBS] Password reset email sent to: " + toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("[Galaxy_BBS] Failed to send password reset email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
