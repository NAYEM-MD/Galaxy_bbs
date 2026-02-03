package controller;

import dao.UserDAO;
import model.User;

import java.io.IOException;
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath(); // exact: /auth/login, /auth/register, /auth/logout
        switch (path) {
            case "/auth/login":
                forwardTo(request, response, "/auth/login.jsp");
                break;
            case "/auth/register":
                forwardTo(request, response, "/auth/register.jsp");
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
            msg.setFrom(new InternetAddress(user));
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
        } catch (MessagingException e) {
            System.err.println("[Galaxy_BBS] Failed to send welcome email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
