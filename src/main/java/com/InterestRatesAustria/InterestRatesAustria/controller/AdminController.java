package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.UserDto;
import com.InterestRatesAustria.InterestRatesAustria.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for admin-specific operations.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationService authenticationService;

    @GetMapping("/users")
    public String adminUsersPage(Model model) {
        List<UserDto> pendingUsers = authenticationService.getPendingUsers();
        model.addAttribute("pendingUsers", pendingUsers);
        return "admin/users";
    }

    @PostMapping("/users/{userId}/enable")
    @ResponseBody
    public ResponseEntity<String> enableUser(@PathVariable String userId) {
        try {
            authenticationService.enableUser(userId);
            return ResponseEntity.ok("User enabled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error enabling user: " + e.getMessage());
        }
    }

    @GetMapping("/users/pending")
    @ResponseBody
    public ResponseEntity<List<UserDto>> getPendingUsers() {
        return ResponseEntity.ok(authenticationService.getPendingUsers());
    }
}