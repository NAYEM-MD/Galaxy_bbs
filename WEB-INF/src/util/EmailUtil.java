package util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 * Shared email sending utility (welcome, password reset, etc.).
 * Reads SMTP_USER and SMTP_PASSWORD from environment.
 */
public final class EmailUtil {

    private static final String DEFAULT_USER = "YOUR_GMAIL@gmail.com";
    private static final String DEFAULT_PASS = "YOUR_APP_PASSWORD";

    private EmailUtil() {}

    public static void sendEmail(String toEmail, String subject, String body) {
        String user = System.getenv("SMTP_USER");
        String pass = System.getenv("SMTP_PASSWORD");
        if (user == null || user.isEmpty()) {
            user = DEFAULT_USER;
            System.err.println("[Galaxy_BBS] SMTP_USER not set. Run set_smtp_env.bat before starting Tomcat.");
        }
        if (pass == null || pass.isEmpty()) {
            pass = DEFAULT_PASS;
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
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(body);
            Transport.send(msg);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("[Galaxy_BBS] Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
