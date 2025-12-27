package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.SiteSettings;
import com.InterestRatesAustria.InterestRatesAustria.service.SiteSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/site-settings")
@RequiredArgsConstructor
public class SiteSettingsController {

    private final SiteSettingsService siteSettingsService;

    @GetMapping
    public String showEditForm(Model model) {
        SiteSettings settings = siteSettingsService.getSiteSettings();
        model.addAttribute("siteSettings", settings);
        return "admin/site-settings/edit";
    }

    @PostMapping
    public String updateSettings(@ModelAttribute SiteSettings settings,
                                  RedirectAttributes redirectAttributes) {
        try {
            siteSettingsService.updateSiteSettings(settings);
            redirectAttributes.addFlashAttribute("successMessage", "Site settings updated successfully!");
            return "redirect:/admin/site-settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating settings: " + e.getMessage());
            return "redirect:/admin/site-settings";
        }
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<SiteSettings> getSiteSettingsAPI() {
        return ResponseEntity.ok(siteSettingsService.getSiteSettings());
    }
}