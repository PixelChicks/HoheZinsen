package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.FilterDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.FilterOptionDTO;
import com.InterestRatesAustria.InterestRatesAustria.service.FilterService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/filters")
public class FilterController {

    private final FilterService filterService;

    public FilterController(FilterService filterService) {
        this.filterService = filterService;
    }

    @GetMapping("/field/{fieldId}/options")
    @ResponseBody
    public ResponseEntity<List<FilterOptionDTO>> getFilterOptions(@PathVariable Long fieldId) {
        try {
            List<FilterOptionDTO> options = filterService.getFilterOptionsForField(fieldId);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/available")
    @ResponseBody
    public ResponseEntity<List<FilterDTO>> getAvailableFilters() {
        try {
            List<FilterDTO> filters = filterService.getAvailableFilters();
            return ResponseEntity.ok(filters);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<String> saveFilterPreferences(@RequestBody Map<String, Object> filterData) {
        try {
            return ResponseEntity.ok("Filter preferences saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving filter preferences: " + e.getMessage());
        }
    }
}