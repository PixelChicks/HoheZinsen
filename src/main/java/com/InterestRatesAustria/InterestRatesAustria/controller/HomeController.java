package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.service.FieldValueService;
import com.InterestRatesAustria.InterestRatesAustria.service.GlobalFieldService;
import com.InterestRatesAustria.InterestRatesAustria.service.InterestRateService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public String showRates(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int size,
                            @RequestParam(defaultValue = "id") String sortBy,
                            @RequestParam(defaultValue = "asc") String sortDir,
                            @RequestParam(required = false) String search) {

        // Get initial page of data for server-side rendering
        Page<InterestRate> interestRatesPage;
        if (search != null && !search.trim().isEmpty()) {
            interestRatesPage = interestRateService.searchInterestRatesPaginated(search, page, size, sortBy, sortDir);
        } else {
            interestRatesPage = interestRateService.getAllInterestRatesPaginated(page, size, sortBy, sortDir);
        }

        List<InterestRateDTO> interestRateDTOs = interestRatesPage.getContent().stream()
                .map(InterestRateDTO::fromEntity)
                .collect(Collectors.toList());

        List<GlobalField> globalFields = globalFieldService.getAllGlobalFieldsOrdered();

        Map<Long, Map<Long, String>> rateFieldValuesMap =
                fieldValueService.getRateFieldValuesMap(interestRatesPage.getContent());

        // Pass only the current page data, not all data
        model.addAttribute("interestRates", interestRateDTOs);
        model.addAttribute("globalFields", globalFields);
        model.addAttribute("rateFieldValuesMap", rateFieldValuesMap);
        model.addAttribute("newField", new GlobalField());

        // Pagination info
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", interestRatesPage.getTotalPages());
        model.addAttribute("totalElements", interestRatesPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);

        return "index";
    }

    // Fixed endpoint path to match what JavaScript is calling
    @GetMapping("/api/interest-rates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInterestRatesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        Page<InterestRate> interestRatesPage;

        if (search != null && !search.trim().isEmpty()) {
            interestRatesPage = interestRateService.searchInterestRatesPaginated(search, page, size, sortBy, sortDir);
        } else {
            interestRatesPage = interestRateService.getAllInterestRatesPaginated(page, size, sortBy, sortDir);
        }

        List<InterestRateDTO> interestRateDTOs = interestRatesPage.getContent().stream()
                .map(InterestRateDTO::fromEntity)
                .collect(Collectors.toList());

        Map<Long, Map<Long, String>> rateFieldValuesMap =
                fieldValueService.getRateFieldValuesMap(interestRatesPage.getContent());

        Map<String, Object> response = new HashMap<>();
        response.put("content", interestRateDTOs);
        response.put("rateFieldValuesMap", rateFieldValuesMap);
        response.put("currentPage", page);
        response.put("totalPages", interestRatesPage.getTotalPages());
        response.put("totalElements", interestRatesPage.getTotalElements());
        response.put("pageSize", size);
        response.put("isFirst", interestRatesPage.isFirst());
        response.put("isLast", interestRatesPage.isLast());

        return ResponseEntity.ok(response);
    }
}