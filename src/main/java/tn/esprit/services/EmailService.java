package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {
    // Hardcoded credentials for immediate testing
    private final String senderEmail = "esouissi870@gmail.com";
    private final String senderPassword = "pcnk ggme rhkh caha";
    private final boolean isConfigured = true;

    public EmailService() {
        System.out.println("🔥 EmailService FORCE-ENABLED");
        System.out.println("Using SMTP: smtp.gmail.com:587");
    }

    public void sendEmail(String recipient, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email sent to: " + recipient);
        } catch (Exception e) {
            System.err.println("❌ Email failed: " + e.getMessage());
            throw new MessagingException("Failed to send email", e);
        }
    }

    public boolean isConfigured() {
        return true;
    }
}