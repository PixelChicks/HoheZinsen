package com.InterestRatesAustria.InterestRatesAustria.security.service;

import com.InterestRatesAustria.InterestRatesAustria.security.persistance.User;
import com.InterestRatesAustria.InterestRatesAustria.security.persistance.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public User registerUser(String email, String password) throws Exception {
        // Check if user already exists
        if (userRepository.existsByUsername(email)) {
            throw new Exception("User with this email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEnabled(false); // Will be enabled after email verification
        user.setEmailVerified(false);

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpires(LocalDateTime.now().plusHours(24)); // Token expires in 24 hours

        // Save user
        User savedUser = userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(email, verificationToken);

        return savedUser;
    }

    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Check if token is expired
        if (user.isVerificationTokenExpired()) {
            return false;
        }

        // Verify the user
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpires(null);

        userRepository.save(user);
        return true;
    }

    public void resendVerificationEmail(String email) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new Exception("User not found");
        }

        User user = userOpt.get();

        if (user.isEmailVerified()) {
            throw new Exception("Email is already verified");
        }

        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpires(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(email, verificationToken);
    }

    public void initiatePasswordReset(String email) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // For security, don't reveal if email exists or not
            // Just silently return - user will think email was sent
            return;
        }

        User user = userOpt.get();

        // Check if user is verified
        if (!user.isEmailVerified()) {
            throw new Exception("Please verify your email address first before resetting password");
        }

        // Generate password reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpires(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour

        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetEmail(email, resetToken);
    }

    public boolean validatePasswordResetToken(String token) {
        Optional<User> userOpt = userRepository.findByPasswordResetToken(token);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return !user.isPasswordResetTokenExpired();
    }

    public boolean resetPassword(String token, String newPassword) throws Exception {
        Optional<User> userOpt = userRepository.findByPasswordResetToken(token);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Check if token is expired
        if (user.isPasswordResetTokenExpired()) {
            return false;
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("Password must be at least 6 characters long");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpires(null);

        userRepository.save(user);

        // Send notification email
        emailService.sendPasswordChangedNotification(user.getEmail());

        return true;
    }

    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByUsername(email);
    }
}