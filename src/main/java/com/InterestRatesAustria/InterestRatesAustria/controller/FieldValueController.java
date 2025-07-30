package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.service.FieldValueService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/field-values")
public class FieldValueController {

    private final FieldValueService fieldValueService;

    public FieldValueController(FieldValueService fieldValueService) {
        this.fieldValueService = fieldValueService;
    }

    @PostMapping("/update")
    public String updateFieldValue(@RequestParam Long rateId,
                                   @RequestParam Long fieldId,
                                   @RequestParam String value) {
        fieldValueService.updateFieldValue(rateId, fieldId, value);
        return "redirect:/";
    }
}