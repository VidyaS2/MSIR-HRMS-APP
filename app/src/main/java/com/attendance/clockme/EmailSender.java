package com.attendance.clockme;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    private static final String SMTP_HOST = "smtp.your-email-provider.com";
    private static final String SMTP_PORT = "587"; // Change if needed
    private static final String USERNAME = "your-email@example.com";
    private static final String PASSWORD = "your-email-password";

    public static void sendEmail(String recipient, String otp) {
        // Set up properties for the SMTP server
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Create a session with an Authenticator
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set the From and To addresses
            message.setFrom(new InternetAddress(USERNAME));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            // Set the subject and body of the email
            message.setSubject("Welcome to FoodApp!");
            message.setText("Dear User,\n\nWelcome to FoodApp! Your OTP for verification is: " + otp + "\n\nThank you for registering with us.\n\nBest Regards,\nFoodApp Team");

            // Send the message
            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
