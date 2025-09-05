package com.InterestRatesAustria.InterestRatesAustria;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.service.FieldValueService;
import com.InterestRatesAustria.InterestRatesAustria.service.GlobalFieldService;
import com.InterestRatesAustria.InterestRatesAustria.service.InterestRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class InterestRateController {

    private final InterestRateService interestRateService;
    private final GlobalFieldService globalFieldService;
    private final FieldValueService fieldValueService;

    public InterestRateController(InterestRateService interestRateService,
                                  GlobalFieldService globalFieldService,
                                  FieldValueService fieldValueService) {
        this.interestRateService = interestRateService;
        this.globalFieldService = globalFieldService;
        this.fieldValueService = fieldValueService;
    }

    @PostMapping("/interest-rate/create")
    public String createInterestRate(
            @ModelAttribute InterestRate interestRate,
            @RequestParam Map<String, String> requestParams) {

        interestRateService.createInterestRate(interestRate, requestParams);
        return "redirect:/";
    }

    @PostMapping("/interest-rate/update/{id}")
    public String updateInterestRate(
            @PathVariable Long id,
            @ModelAttribute InterestRate interestRate,
            @RequestParam Map<String, String> requestParams) {

        interestRateService.updateInterestRate(id, interestRate, requestParams);
        return "redirect:/";
    }

    @PostMapping("/sections/reorder")
    @ResponseBody
    public ResponseEntity<String> reorderSections(@RequestParam Long rateId,
                                                  @RequestBody List<String> sectionOrder) {
        try {
            interestRateService.updateSectionOrder(rateId, sectionOrder);
            return ResponseEntity.ok("Section order updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating section order: " + e.getMessage());
        }
    }

    @GetMapping("/interest-rate/new")
    public String showCreateForm(Model model) {
        model.addAttribute("interestRate", new InterestRate());
        model.addAttribute("globalFields", globalFieldService.getAllGlobalFieldsOrdered());
        return "interest-rate-form";
    }

    @GetMapping("/interest-rate/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        InterestRate interestRate = interestRateService.getInterestRateById(id);
        Map<Long, String> fieldValues = fieldValueService.getFieldValuesForRate(id);

        model.addAttribute("interestRate", interestRate);
        model.addAttribute("globalFields", globalFieldService.getAllGlobalFieldsOrdered());
        model.addAttribute("fieldValues", fieldValues);
        model.addAttribute("isEdit", true);

        return "interest-rate-edit-form";
    }

    @PostMapping("/interest-rate/delete/{id}")
    public String deleteInterestRate(@PathVariable Long id) {
        interestRateService.deleteInterestRate(id);
        return "redirect:/";
    }

    @DeleteMapping("/interest-rate/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteInterestRateAjax(@PathVariable Long id) {
        try {
            interestRateService.deleteInterestRate(id);
            return ResponseEntity.ok("Interest rate deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting interest rate: " + e.getMessage());
        }
    }
}