package com.InterestRatesAustria.InterestRatesAustria.security.controller;

import com.InterestRatesAustria.InterestRatesAustria.security.service.UserService;
import com.InterestRatesAustria.InterestRatesAustria.security.service.EmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {

        // Validation
        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email is required");
            return "redirect:/register";
        }

        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long");
            return "redirect:/register";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/register";
        }

        try {
            userService.registerUser(email, password);
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please check your email to verify your account.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token,
                              RedirectAttributes redirectAttributes) {

        boolean verified = userService.verifyEmail(token);

        if (verified) {
            redirectAttributes.addFlashAttribute("success",
                    "Email verified successfully! You can now log in.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid or expired verification token. Please request a new one.");
            return "redirect:/resend-verification";
        }
    }

    @GetMapping("/resend-verification")
    public String showResendVerificationForm() {
        return "resend-verification";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam String email,
                                     RedirectAttributes redirectAttributes) {
        try {
            userService.resendVerificationEmail(email);
            redirectAttributes.addFlashAttribute("success",
                    "Verification email sent! Please check your inbox.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/resend-verification";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.initiatePasswordReset(email);
            redirectAttributes.addFlashAttribute("success",
                    "If an account with this email exists, a password reset link has been sent to your email.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model,
                                        RedirectAttributes redirectAttributes) {
        boolean validToken = userService.validatePasswordResetToken(token);

        if (!validToken) {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid or expired password reset token. Please request a new one.");
            return "redirect:/forgot-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {

        // Validation
        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long");
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }

        try {
            boolean success = userService.resetPassword(token, password);

            if (success) {
                redirectAttributes.addFlashAttribute("success",
                        "Password reset successfully! You can now log in with your new password.");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Invalid or expired password reset token. Please request a new one.");
                return "redirect:/forgot-password";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {

        if (error != null) {
            model.addAttribute("error", "Login failed. Please check your credentials and make sure your email is verified and your account is enabled by an administrator.");
        }

        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }

        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Cookie cookieRememberMe = new Cookie("interest-rates-remember-me", null);
        cookieRememberMe.setPath("/");
        cookieRememberMe.setHttpOnly(true);
        cookieRememberMe.setMaxAge(0);
        response.addCookie(cookieRememberMe);

        return "redirect:/login?logout=true";
    }
}