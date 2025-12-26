package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.About;
import com.InterestRatesAustria.InterestRatesAustria.service.AboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/about")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("about", new About());
        return "admin/about/create";
    }

    @PostMapping("/create")
    public String createAbout(@ModelAttribute("about") About request,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/about/create";
        }

        try {
            aboutService.createAboutSection(request);
            redirectAttributes.addFlashAttribute("successMessage", "About section created successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating about section: " + e.getMessage());
            return "redirect:/admin/about/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            About about = aboutService.getAboutSectionById(id);

            About updateRequest = new About();
            updateRequest.setTitle(about.getTitle());
            updateRequest.setContent(about.getContent());
            updateRequest.setIsActive(about.getIsActive());

            model.addAttribute("about", updateRequest);
            model.addAttribute("aboutId", id);
            return "admin/about/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "About section not found: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateAbout(@PathVariable Long id,
                              @ModelAttribute("about") About request,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aboutId", id);
            return "admin/about/edit";
        }

        try {
            aboutService.updateAboutSection(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "About section updated successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating about section: " + e.getMessage());
            return "redirect:/admin/about/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteAbout(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            aboutService.deleteAboutSection(id);
            redirectAttributes.addFlashAttribute("successMessage", "About section deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting about section: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<About> getActiveAboutAPI() {
        About activeAbout = aboutService.getActiveAboutSection();
        if (activeAbout == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(activeAbout);
    }
}
