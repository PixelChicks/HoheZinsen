package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.SectionAbout;
import com.InterestRatesAustria.InterestRatesAustria.service.SectionAboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/aboutS")
@RequiredArgsConstructor
public class SectionAboutController {

    private final SectionAboutService aboutService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("about", new SectionAbout());
        return "admin/aboutS/create";
    }

    @PostMapping("/create")
    public String createSectionAbout(@ModelAttribute("about") SectionAbout request,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/aboutS/create";
        }

        try {
            aboutService.createSectionAboutSection(request);
            redirectAttributes.addFlashAttribute("successMessage", "SectionAbout section created successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating about section: " + e.getMessage());
            return "redirect:/admin/aboutS/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            SectionAbout about = aboutService.getSectionAboutSectionById(id);

            SectionAbout updateRequest = new SectionAbout();
            updateRequest.setTitle(about.getTitle());
            updateRequest.setContent(about.getContent());
            updateRequest.setIsActive(about.getIsActive());

            model.addAttribute("about", updateRequest);
            model.addAttribute("aboutId", id);
            return "admin/aboutS/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "SectionAbout section not found: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateSectionAbout(@PathVariable Long id,
                              @ModelAttribute("about") SectionAbout request,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aboutId", id);
            return "admin/aboutS/edit";
        }

        try {
            aboutService.updateSectionAboutSection(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "SectionAbout section updated successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating about section: " + e.getMessage());
            return "redirect:/admin/aboutS/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteSectionAbout(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            aboutService.deleteSectionAboutSection(id);
            redirectAttributes.addFlashAttribute("successMessage", "SectionAbout section deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting about section: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<SectionAbout> getActiveSectionAboutAPI() {
        SectionAbout activeSectionAbout = aboutService.getActiveSectionAboutSection();
        if (activeSectionAbout == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(activeSectionAbout);
    }
}
