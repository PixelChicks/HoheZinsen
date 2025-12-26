package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.FAQ;
import com.InterestRatesAustria.InterestRatesAustria.service.FAQService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/faqs")
@RequiredArgsConstructor
public class FAQController {

    private final FAQService faqService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("faq", new FAQ());
        return "admin/faq/create";
    }

    @PostMapping("/create")
    public String createFAQ(@ModelAttribute("faq") FAQ request,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/faq/create";
        }

        try {
            faqService.createFAQ(request);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ created successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating FAQ: " + e.getMessage());
            return "redirect:/admin/faqs/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            FAQ faq = faqService.getFAQById(id);

            FAQ updateRequest = new FAQ();
            updateRequest.setQuestion(faq.getQuestion());
            updateRequest.setAnswer(faq.getAnswer());
            updateRequest.setDisplayOrder(faq.getDisplayOrder());
            updateRequest.setIsActive(faq.getIsActive());

            model.addAttribute("faq", updateRequest);
            model.addAttribute("faqId", id);
            return "admin/faq/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "FAQ not found: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateFAQ(@PathVariable Long id,
                            @ModelAttribute("faq") FAQ request,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("faqId", id);
            return "admin/faq/edit";
        }

        try {
            faqService.updateFAQ(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ updated successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating FAQ: " + e.getMessage());
            return "redirect:/admin/faqs/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteFAQ(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            faqService.deleteFAQ(id);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting FAQ: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<List<FAQ>> getAllFAQsAPI() {
        return ResponseEntity.ok(faqService.getAllFAQs());
    }

    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<List<FAQ>> getActiveFAQsAPI() {
        return ResponseEntity.ok(faqService.getActiveFAQs());
    }
}