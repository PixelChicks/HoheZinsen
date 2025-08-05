package com.InterestRatesAustria.InterestRatesAustria.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

/**
 * Custom logout handler for JWT-based authentication.
 * Since JWT tokens are stateless, logout is primarily handled client-side.
 */
@Service
public class CustomLogoutHandler implements LogoutHandler {

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        // Clear the security context
        SecurityContextHolder.clearContext();
        
        // For JWT tokens, the actual logout is handled client-side by removing the token
        // In a more advanced implementation, you could maintain a blacklist of revoked tokens
    }
}