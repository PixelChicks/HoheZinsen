package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.service.FieldValueService;
import com.InterestRatesAustria.InterestRatesAustria.service.GlobalFieldService;
import com.InterestRatesAustria.InterestRatesAustria.service.InterestRateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final InterestRateService interestRateService;
    private final GlobalFieldService globalFieldService;
    private final FieldValueService fieldValueService;

    public HomeController(InterestRateService interestRateService,
                         GlobalFieldService globalFieldService,
                         FieldValueService fieldValueService) {
        this.interestRateService = interestRateService;
        this.globalFieldService = globalFieldService;
        this.fieldValueService = fieldValueService;
    }

    @GetMapping("/")
    public String showRates(Model model) {
        List<InterestRate> interestRates = interestRateService.getAllInterestRates();
        List<InterestRateDTO> interestRateDTOs = interestRateService.getAllInterestRateDTOs();
        List<GlobalField> globalFields = globalFieldService.getAllGlobalFieldsOrdered();
        Map<Long, Map<Long, String>> rateFieldValuesMap = fieldValueService.getRateFieldValuesMap(interestRates);

        model.addAttribute("interestRates", interestRateDTOs);
        model.addAttribute("globalFields", globalFields);
        model.addAttribute("rateFieldValuesMap", rateFieldValuesMap);
        model.addAttribute("newField", new GlobalField());

        return "index";
    }
}