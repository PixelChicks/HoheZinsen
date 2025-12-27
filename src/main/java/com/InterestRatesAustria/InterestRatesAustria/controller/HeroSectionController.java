package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.HeroSection;
import com.InterestRatesAustria.InterestRatesAustria.service.HeroSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/hero")
@RequiredArgsConstructor
public class HeroSectionController {

    private final HeroSectionService heroSectionService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("heroSection", new HeroSection());
        return "admin/hero/create";
    }

    @PostMapping("/create")
    public String createHeroSection(@ModelAttribute("heroSection") HeroSection request,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/hero/create";
        }

        try {
            heroSectionService.createHeroSection(request);
            redirectAttributes.addFlashAttribute("successMessage", "Hero section created successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating hero section: " + e.getMessage());
            return "redirect:/admin/hero/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            HeroSection heroSection = heroSectionService.getHeroSectionById(id);

            model.addAttribute("heroSection", heroSection);
            model.addAttribute("heroId", id);
            return "admin/hero/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hero section not found: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateHeroSection(@PathVariable Long id,
                                    @ModelAttribute("heroSection") HeroSection request,
                                    BindingResult result,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("heroId", id);
            return "admin/hero/edit";
        }

        try {
            heroSectionService.updateHeroSection(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Hero section updated successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating hero section: " + e.getMessage());
            return "redirect:/admin/hero/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteHeroSection(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            heroSectionService.deleteHeroSection(id);
            redirectAttributes.addFlashAttribute("successMessage", "Hero section deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting hero section: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<HeroSection> getActiveHeroSectionAPI() {
        HeroSection activeHero = heroSectionService.getActiveHeroSection();
        if (activeHero == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(activeHero);
    }
}