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

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request - Interest Rates Austria");
        
        String resetLink = baseUrl + "/reset-password?token=" + resetToken;
        
        String emailContent = String.format(
            "Password Reset Request\n\n" +
            "We received a request to reset your password for your Interest Rates Austria account.\n\n" +
            "Click the link below to reset your password:\n\n" +
            "%s\n\n" +
            "This link will expire in 1 hour for security reasons.\n\n" +
            "If you didn't request a password reset, please ignore this email. Your password will remain unchanged.\n\n" +
            "For security reasons, please do not share this link with anyone.\n\n" +
            "Best regards,\n" +
            "Interest Rates Austria Team",
            resetLink
        );
        
        message.setText(emailContent);
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
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

    public void sendPasswordChangedNotification(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Changed Successfully - Interest Rates Austria");
        
        String emailContent = 
            "Password Changed Successfully\n\n" +
            "Your password has been successfully changed for your Interest Rates Austria account.\n\n" +
            "If you didn't make this change, please contact our support team immediately.\n\n" +
            "For security:\n" +
            "- Never share your password with anyone\n" +
            "- Use a strong, unique password\n" +
            "- Log out from shared computers\n\n" +
            "Best regards,\n" +
            "Interest Rates Austria Team";
        
        message.setText(emailContent);
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send password changed notification: " + e.getMessage());
        }
    }

    public void sendEmailVerifiedNotification(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Email Verified - Awaiting Account Approval");

        String emailContent =
                "Email Verification Successful!\n\n" +
                        "Your email has been successfully verified for your Interest Rates Austria account.\n\n" +
                        "Your account is now awaiting approval from our administrators. " +
                        "You will receive another email once your account has been activated and you can log in.\n\n" +
                        "Thank you for your patience!\n\n" +
                        "Best regards,\n" +
                        "Interest Rates Austria Team";

        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email verified notification: " + e.getMessage());
        }
    }

    public void sendAccountDisabledNotification(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Account Disabled - Interest Rates Austria");

        String emailContent =
                "Account Disabled\n\n" +
                        "Your Interest Rates Austria account has been disabled by an administrator.\n\n" +
                        "If you believe this was done in error, please contact our support team.\n\n" +
                        "Best regards,\n" +
                        "Interest Rates Austria Team";

        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send account disabled notification: " + e.getMessage());
        }
    }
}