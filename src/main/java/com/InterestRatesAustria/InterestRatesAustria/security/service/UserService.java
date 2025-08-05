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