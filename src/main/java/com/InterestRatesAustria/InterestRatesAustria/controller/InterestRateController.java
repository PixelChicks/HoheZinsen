package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.*;
import com.InterestRatesAustria.InterestRatesAustria.service.InterestRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InterestRateController {

    private final InterestRateService interestRateService;

    public InterestRateController(InterestRateService interestRateService) {
        this.interestRateService = interestRateService;
    }

    @PostMapping("/interest-rate/create")
    public String createInterestRate(
            @ModelAttribute InterestRate interestRate,
            @RequestParam Map<String, String> requestParams) {

        System.out.println("Create request params: " + requestParams);

        Map<String, List<String>> tableSectionData = new HashMap<>();
        Map<String, String> textSectionData = new HashMap<>();

        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("tableTitle_") || key.startsWith("tableRowLabels_") ||
                    key.startsWith("tableRowDescriptions_") || key.startsWith("textTitle_") ||
                    key.startsWith("textContent_")) {
                System.out.println("Processing: " + key + " = " + value);
            }
        }

        interestRateService.createInterestRate(interestRate, requestParams, tableSectionData, textSectionData);
        return "redirect:/";
    }

    @PostMapping("/interest-rate/update/{id}")
    public String updateInterestRate(
            @PathVariable Long id,
            @ModelAttribute InterestRate interestRate,
            @RequestParam Map<String, String> requestParams) {

        Map<String, List<String>> tableSectionData = new HashMap<>();
        Map<String, String> textSectionData = new HashMap<>();

        interestRateService.updateInterestRate(id, interestRate, requestParams, tableSectionData, textSectionData);
        return "redirect:/";
    }

    @PostMapping("/sections/add-table")
    @ResponseBody
    public ResponseEntity<String> addTableSection(@RequestParam Long rateId,
                                                  @RequestParam String title) {
        try {
            interestRateService.addTableSection(rateId, title);
            return ResponseEntity.ok("Table section added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding table section: " + e.getMessage());
        }
    }

    @PostMapping("/sections/add-text")
    @ResponseBody
    public ResponseEntity<String> addTextSection(@RequestParam Long rateId,
                                                 @RequestParam String title,
                                                 @RequestParam String content) {
        try {
            interestRateService.addTextSection(rateId, title, content);
            return ResponseEntity.ok("Text section added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding text section: " + e.getMessage());
        }
    }

    @DeleteMapping("/sections/{sectionIdentifier}")
    @ResponseBody
    public ResponseEntity<String> deleteSection(@RequestParam Long rateId,
                                                @PathVariable String sectionIdentifier) {
        try {
            interestRateService.deleteSection(rateId, sectionIdentifier);
            return ResponseEntity.ok("Section deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting section: " + e.getMessage());
        }
    }

    @PostMapping("/sections/reorder")
    @ResponseBody
    public ResponseEntity<String> reorderSections(@RequestParam Long rateId,
                                                  @RequestBody List<String> sectionOrder) {
        try {
            System.out.println("Reordering sections for rate " + rateId + " with order: " + sectionOrder);
            interestRateService.updateSectionOrder(rateId, sectionOrder);
            return ResponseEntity.ok("Section order updated successfully");
        } catch (Exception e) {
            System.err.println("Error reordering sections: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating section order: " + e.getMessage());
        }
    }

    private String extractSectionId(String key) {
        String[] parts = key.split("_");
        return parts.length > 1 ? parts[parts.length - 1] : "1";
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

    @GetMapping("/interest-rate/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        InterestRate interestRate = interestRateService.getInterestRateById(id);
        Map<Long, String> fieldValues = interestRateService.getFieldValuesForRate(id);

        model.addAttribute("interestRate", interestRate);
        model.addAttribute("globalFields", interestRateService.getAllGlobalFieldsOrdered());
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

    @PostMapping("/fields/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteGlobalField(@PathVariable Long id) {
        try {
            interestRateService.deleteGlobalField(id);
            return ResponseEntity.ok("Field deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting field: " + e.getMessage());
        }
    }

    @DeleteMapping("/fields/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteGlobalFieldRest(@PathVariable Long id) {
        try {
            interestRateService.deleteGlobalField(id);
            return ResponseEntity.ok("Field deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting field: " + e.getMessage());
        }
    }
}