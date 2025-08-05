package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.enums.Role;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.AuthRequest;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.AuthResponse;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.RegisterRequest;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.UserDto;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.User;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.VerificationToken;
import com.InterestRatesAustria.InterestRatesAustria.repository.UserRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling authentication operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    @Value("${server.backend.baseUrl}")
    private String baseUrl;

    public void register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with this email");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .emailVerified(false)
                .enabled(false)
                .build();

        userRepository.save(user);

        // Generate verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(verificationToken);

        // Send verification email
        sendVerificationEmail(user.getEmail(), token);
    }

    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email first");
        }
        
        if (!user.isEnabled()) {
            throw new RuntimeException("Account is pending admin approval");
        }

        String jwtToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Delete used token
        verificationTokenRepository.delete(verificationToken);

        // Notify admin about new registration
        notifyAdminNewRegistration(user.getEmail());
    }

    public void enableUser(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        // Send welcome email
        sendWelcomeEmail(user.getEmail());
    }

    public List<UserDto> getPendingUsers() {
        return userRepository.findByEmailVerifiedTrueAndEnabledFalse()
                .stream()
                .map(user -> UserDto.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private void sendVerificationEmail(String email, String token) {
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email Verification - Interest Rates Austria");
        message.setText("Please click the following link to verify your email: " + verificationUrl);
        
        mailSender.send(message);
    }

    private void notifyAdminNewRegistration(String userEmail) {
        // Get all admin users
        List<User> admins = userRepository.findByEnabledTrue();
        
        for (User admin : admins) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(admin.getEmail());
            message.setSubject("New User Registration - Approval Required");
            message.setText("A new user has registered and verified their email: " + userEmail + 
                          "\nPlease log in to the admin panel to approve this user.");
            
            mailSender.send(message);
        }
    }

    private void sendWelcomeEmail(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Account Approved - Interest Rates Austria");
        message.setText("Your account has been approved by an administrator. You can now log in to the system.");
        
        mailSender.send(message);
    }
}