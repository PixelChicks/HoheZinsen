package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.*;
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

    public InterestRateController(InterestRateService interestRateService) {
        this.interestRateService = interestRateService;
    }

    @GetMapping("/")
    public String showRates(Model model) {
        List<InterestRate> interestRates = interestRateService.getAllInterestRates();
        List<InterestRateDTO> interestRateDTOs = interestRateService.getAllInterestRateDTOs();
        List<GlobalField> globalFields = interestRateService.getAllGlobalFieldsOrdered();
        Map<Long, Map<Long, String>> rateFieldValuesMap = interestRateService.getRateFieldValuesMap(interestRates);

        model.addAttribute("interestRates", interestRateDTOs);
        model.addAttribute("globalFields", globalFields);
        model.addAttribute("rateFieldValuesMap", rateFieldValuesMap);
        model.addAttribute("newField", new GlobalField());

        return "index";
    }

    @PostMapping("/fields/add")
    public String addGlobalField(@ModelAttribute GlobalField field) {
        interestRateService.addGlobalField(field);
        return "redirect:/";
    }

    @PostMapping("/fields/update")
    public String updateGlobalField(@RequestParam Long fieldId,
                                    @RequestParam String label) {
        interestRateService.updateGlobalField(fieldId, label);
        return "redirect:/";
    }

    @PostMapping("/fields/reorder")
    @ResponseBody
    public ResponseEntity<String> reorderFields(@RequestBody List<Long> fieldIds) {
        try {
            interestRateService.reorderGlobalFields(fieldIds);
            return ResponseEntity.ok("Order updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating order: " + e.getMessage());
        }
    }

    @PostMapping("/more-info/reorder-sections")
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

    @PostMapping("/field-values/update")
    public String updateFieldValue(@RequestParam Long rateId,
                                   @RequestParam Long fieldId,
                                   @RequestParam String value) {
        interestRateService.updateFieldValue(rateId, fieldId, value);
        return "redirect:/";
    }

    @GetMapping("/interest-rate/new")
    public String showCreateForm(Model model) {
        model.addAttribute("interestRate", new InterestRate());
        model.addAttribute("globalFields", interestRateService.getAllGlobalFieldsOrdered());
        return "interest-rate-form";
    }

    @PostMapping("/interest-rate/create")
    public String createInterestRate(
            @ModelAttribute InterestRate interestRate,
            @RequestParam Map<String, String> requestParams,
            @RequestParam(name = "tableRowLabels[]", required = false) List<String> tableRowLabels,
            @RequestParam(name = "tableRowDescriptions[]", required = false) List<String> tableRowDescriptions
    ) {
        interestRateService.createInterestRate(interestRate, requestParams, tableRowLabels, tableRowDescriptions);
        return "redirect:/";
    }
}