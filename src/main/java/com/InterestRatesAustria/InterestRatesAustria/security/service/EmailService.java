package com.InterestRatesAustria.InterestRatesAustria.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify Your Email - Interest Rates Austria");
        
        String verificationLink = baseUrl + "/verify-email?token=" + verificationToken;
        
        String emailContent = String.format(
            "Welcome to Interest Rates Austria!\n\n" +
            "Please click the link below to verify your email address:\n\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you didn't create an account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Interest Rates Austria Team",
            verificationLink
        );
        
        message.setText(emailContent);
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendWelcomeEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to Interest Rates Austria!");
        
        String emailContent = 
            "Welcome to Interest Rates Austria!\n\n" +
            "Your email has been successfully verified and your account is now active.\n\n" +
            "You can now log in and start exploring our interest rate offers.\n\n" +
            "Best regards,\n" +
            "Interest Rates Austria Team";
        
        message.setText(emailContent);
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Don't throw exception for welcome email failure
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
}