package com.InterestRatesAustria.InterestRatesAustria.security.controller;

import com.InterestRatesAustria.InterestRatesAustria.security.persistance.User;
import com.InterestRatesAustria.InterestRatesAustria.security.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Only admin users can access these endpoints
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers", adminService.getTotalUsers());
        model.addAttribute("pendingUsers", adminService.getPendingUsersCount());
        model.addAttribute("enabledUsers", adminService.getEnabledUsersCount());
        model.addAttribute("unverifiedUsers", adminService.getUnverifiedUsersCount());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "all") String filter, Model model) {
        List<User> users;

        switch (filter) {
            case "pending":
                users = adminService.getPendingUsers();
                break;
            case "enabled":
                users = adminService.getEnabledUsers();
                break;
            case "unverified":
                users = adminService.getUnverifiedUsers();
                break;
            default:
                users = adminService.getAllUsers();
        }

        model.addAttribute("users", users);
        model.addAttribute("currentFilter", filter);

        return "admin/users";
    }

    @PostMapping("/users/{userId}/enable")
    public String enableUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = adminService.enableUser(userId);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "User enabled successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found!");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error enabling user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/disable")
    public String disableUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = adminService.disableUser(userId);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "User disabled successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error disabling user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = adminService.deleteUser(userId);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }
}