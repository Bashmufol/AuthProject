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
    private final JavaMailSender mailSender;  // Injects Spring's mail sender

    @Value("${spring.mail.username}")   // Injects the sender email address
    private String fromMail;

//    Sends a password reset token to the specified recipient email.
    public void sendPasswordResetEmail(String toEmail, String token, String username) throws MailException {

        // Create a new simple mail message object
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request for Your Account");


        // Construct the email body with the reset token
        message.setText("Dear " + username + ",\n\n"
                + "You have requested to reset your password. Your password reset is :\n"
                + token +"\n\n"
                + "This code will expire in 10 minutes.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Regards,\nYour App Team");

        // Send the constructed email
        mailSender.send(message);
        System.out.println("Password reset email successfully sent to: " + toEmail); // Log success
    }
}
