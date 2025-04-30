package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private final String senderEmail = "workawayespritproject@gmail.com";
    private final String senderPassword = "udwk asll fodw irlc"; // App Password

    public void sendEmail(String recipient, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);

        // HTML Email Template
        String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: #4CAF50;">Account Verification</h2>
                    <p>Click the button below to verify your account:</p>
                    <a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Verify Account
                    </a>
                    <p>Or copy this link: <br>%s</p>
                </body>
            </html>
            """.formatted(body, body);

        message.setContent(htmlContent, "text/html");
        Transport.send(message);
        System.out.println("Email sent successfully to " + recipient);
    }
}