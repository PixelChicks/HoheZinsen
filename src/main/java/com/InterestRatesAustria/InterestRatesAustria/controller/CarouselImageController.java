package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.CarouselImage;
import com.InterestRatesAustria.InterestRatesAustria.service.CarouselImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/carousel")
@RequiredArgsConstructor
public class CarouselImageController {

    private final CarouselImageService carouselImageService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("carouselImage", new CarouselImage());
        return "admin/carousel/create";
    }

    @PostMapping("/create")
    public String createCarouselImage(@ModelAttribute("carouselImage") CarouselImage request,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/carousel/create";
        }

        try {
            carouselImageService.createCarouselImage(request);
            redirectAttributes.addFlashAttribute("successMessage", "Carousel image created successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating carousel image: " + e.getMessage());
            return "redirect:/admin/carousel/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CarouselImage carouselImage = carouselImageService.getCarouselImageById(id);

            model.addAttribute("carouselImage", carouselImage);
            model.addAttribute("carouselId", id);
            return "admin/carousel/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Carousel image not found: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCarouselImage(@PathVariable Long id,
                                      @ModelAttribute("carouselImage") CarouselImage request,
                                      BindingResult result,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("carouselId", id);
            return "admin/carousel/edit";
        }

        try {
            carouselImageService.updateCarouselImage(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Carousel image updated successfully!");
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating carousel image: " + e.getMessage());
            return "redirect:/admin/carousel/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCarouselImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            carouselImageService.deleteCarouselImage(id);
            redirectAttributes.addFlashAttribute("successMessage", "Carousel image deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting carousel image: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/reorder")
    @ResponseBody
    public ResponseEntity<String> reorderCarouselImages(@RequestBody List<Long> orderedIds) {
        try {
            carouselImageService.reorderCarouselImages(orderedIds);
            return ResponseEntity.ok("Order updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating order: " + e.getMessage());
        }
    }

    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<List<CarouselImage>> getActiveCarouselImagesAPI() {
        return ResponseEntity.ok(carouselImageService.getActiveCarouselImages());
    }
}