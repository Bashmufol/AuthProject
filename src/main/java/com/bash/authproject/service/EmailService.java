package com.bash.authproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    public void sendPasswordResetEmail(String toEmail, String token, String username) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail); // Must match the 'spring.mail.username' in application.properties
        message.setTo(toEmail);
        message.setSubject("Password Reset Request for Your Account");

        // Construct the reset link. In a real-world app, "http://localhost:8080" would be your frontend's base URL.
        String resetLink = "http://localhost:8080/reset-password?token=" + token;

        message.setText("Dear " + username + ",\n\n"
                + "You have requested to reset your password. Please click on the following link to proceed:\n"
                + resetLink + "\n\n"
                + "This link will expire in 10 minutes.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Regards,\nYour App Team");

        mailSender.send(message);
        System.out.println("Password reset email successfully sent to: " + toEmail); // Log success
    }
}
