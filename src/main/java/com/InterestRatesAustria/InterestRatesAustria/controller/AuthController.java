package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.AuthRequest;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.AuthResponse;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.RegisterRequest;
import com.InterestRatesAustria.InterestRatesAustria.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller for handling authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok("Registration successful. Please check your email for verification link.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully. Please wait for admin approval.");
    }

    @PostMapping("/enable-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> enableUser(@PathVariable String userId) {
        authenticationService.enableUser(userId);
        return ResponseEntity.ok("User enabled successfully.");
    }

    @GetMapping("/pending-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingUsers() {
        return ResponseEntity.ok(authenticationService.getPendingUsers());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // JWT tokens are stateless, logout is handled client-side by removing the token
        return ResponseEntity.ok("Logged out successfully.");
    }
}