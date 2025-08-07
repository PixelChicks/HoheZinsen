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
        if (userRepository.existsByUsername(email)) {
            throw new Exception("User with this email already exists");
        }

        User user = new User();
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEnabled(false);
        user.setEmailVerified(false);

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpires(LocalDateTime.now().plusHours(24)); // Token expires in 24 hours

        User savedUser = userRepository.save(user);

        emailService.sendVerificationEmail(email, verificationToken);

        return savedUser;
    }

    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        if (user.isVerificationTokenExpired()) {
            return false;
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpires(null);

        userRepository.save(user);

        emailService.sendEmailVerifiedNotification(user.getEmail());

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

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpires(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        emailService.sendVerificationEmail(email, verificationToken);
    }

    public void initiatePasswordReset(String email) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        if (!user.isEmailVerified()) {
            throw new Exception("Please verify your email address first before resetting password");
        }

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpires(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

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

        if (user.isPasswordResetTokenExpired()) {
            return false;
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("Password must be at least 6 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpires(null);

        userRepository.save(user);

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
}