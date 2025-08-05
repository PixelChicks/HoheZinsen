package com.InterestRatesAustria.InterestRatesAustria.security;

import com.InterestRatesAustria.InterestRatesAustria.security.service.UserService;
import com.InterestRatesAustria.InterestRatesAustria.security.service.EmailService;
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

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "logout", required = false) String logout,
                              Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please make sure your email is verified.");
        }
        
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }
        
        return "login";
    }
}