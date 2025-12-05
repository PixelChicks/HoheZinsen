package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.service.FieldValueService;
import com.InterestRatesAustria.InterestRatesAustria.service.FilterService;
import com.InterestRatesAustria.InterestRatesAustria.service.GlobalFieldService;
import com.InterestRatesAustria.InterestRatesAustria.service.InterestRateService;
import com.InterestRatesAustria.InterestRatesAustria.service.LastUpdateService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final InterestRateService interestRateService;
    private final GlobalFieldService globalFieldService;
    private final FieldValueService fieldValueService;
    private final FilterService filterService;
    private final LastUpdateService lastUpdateService;

    public HomeController(InterestRateService interestRateService,
                          GlobalFieldService globalFieldService,
                          FieldValueService fieldValueService,
                          FilterService filterService,
                          LastUpdateService lastUpdateService) {
        this.interestRateService = interestRateService;
        this.globalFieldService = globalFieldService;
        this.fieldValueService = fieldValueService;
        this.filterService = filterService;
        this.lastUpdateService = lastUpdateService;
    }

    @GetMapping("/")
    public String showRates(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "6") int size,
                            @RequestParam(defaultValue = "field_1") String sortBy,
                            @RequestParam(defaultValue = "desc") String sortDir,
                            @RequestParam(required = false) String search,
                            @RequestParam Map<String, String> allParams) {

        Map<Long, List<String>> filters = extractFilters(allParams);

        Page<InterestRate> interestRatesPage;
        if ((search != null && !search.trim().isEmpty()) || !filters.isEmpty()) {
            interestRatesPage = filterService.getFilteredInterestRates(filters, page, size, sortBy, sortDir, search);
        } else {
            interestRatesPage = interestRateService.getAllInterestRatesPaginated(page, size, sortBy, sortDir);
        }

        List<InterestRateDTO> interestRateDTOs = interestRatesPage.getContent().stream()
                .map(InterestRateDTO::fromEntity)
                .collect(Collectors.toList());

        List<GlobalField> globalFields = globalFieldService.getTableFieldsOrdered();
        List<GlobalField> globalFieldsCompare = globalFieldService.getCompareFieldsOrdered();

        Map<Long, Map<Long, String>> rateFieldValuesMap =
                fieldValueService.getRateFieldValuesMap(interestRatesPage.getContent());

        model.addAttribute("interestRates", interestRateDTOs);
        model.addAttribute("globalFields", globalFields);
        model.addAttribute("globalFieldsCompare", globalFieldsCompare);
        model.addAttribute("rateFieldValuesMap", rateFieldValuesMap);
        model.addAttribute("newField", new GlobalField());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", interestRatesPage.getTotalPages());
        model.addAttribute("totalElements", interestRatesPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);

        model.addAttribute("activeFilters", filters);
        model.addAttribute("availableFilters", filterService.getAvailableFilters());

        model.addAttribute("lastUpdateMessage", lastUpdateService.getFormattedLastUpdateMessage());

        return "index";
    }

    @GetMapping("/admin")
    public String showRatesAdmin(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 @RequestParam(defaultValue = "field_1") String sortBy,
                                 @RequestParam(defaultValue = "desc") String sortDir,
                                 @RequestParam(required = false) String search,
                                 @RequestParam Map<String, String> allParams) {

        Map<Long, List<String>> filters = extractFilters(allParams);

        Page<InterestRate> interestRatesPage;
        if ((search != null && !search.trim().isEmpty()) || !filters.isEmpty()) {
            interestRatesPage = filterService.getFilteredInterestRates(filters, page, size, sortBy, sortDir, search);
        } else {
            interestRatesPage = interestRateService.getAllInterestRatesPaginated(page, size, sortBy, sortDir);
        }

        List<InterestRateDTO> interestRateDTOs = interestRatesPage.getContent().stream()
                .map(InterestRateDTO::fromEntity)
                .collect(Collectors.toList());

        List<GlobalField> globalFields = globalFieldService.getTableFieldsOrdered();

        Map<Long, Map<Long, String>> rateFieldValuesMap =
                fieldValueService.getRateFieldValuesMap(interestRatesPage.getContent());

        model.addAttribute("interestRates", interestRateDTOs);
        model.addAttribute("globalFields", globalFields);
        model.addAttribute("rateFieldValuesMap", rateFieldValuesMap);
        model.addAttribute("newField", new GlobalField());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", interestRatesPage.getTotalPages());
        model.addAttribute("totalElements", interestRatesPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);

        model.addAttribute("activeFilters", filters);
        model.addAttribute("availableFilters", filterService.getAvailableFilters());

        model.addAttribute("lastUpdateMessage", lastUpdateService.getFormattedLastUpdateMessage());

        return "admin/indexAdmin";
    }

    @GetMapping("/api/interest-rates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInterestRatesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "field_1") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam Map<String, String> allParams) {

        Map<Long, List<String>> filters = extractFilters(allParams);

        Page<InterestRate> interestRatesPage;

        if ((search != null && !search.trim().isEmpty()) || !filters.isEmpty()) {
            interestRatesPage = filterService.getFilteredInterestRates(filters, page, size, sortBy, sortDir, search);
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
        response.put("activeFilters", filters);
        response.put("lastUpdateMessage", lastUpdateService.getFormattedLastUpdateMessage());

        return ResponseEntity.ok(response);
    }

    private Map<Long, List<String>> extractFilters(Map<String, String> allParams) {
        Map<Long, List<String>> filters = new HashMap<>();

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("filter_") && value != null && !value.trim().isEmpty()) {
                try {
                    Long fieldId = Long.parseLong(key.substring(7));

                    String decodedValue = java.net.URLDecoder.decode(value, "UTF-8");
                    List<String> values;

                    if (decodedValue.contains("|")) {
                        values = Arrays.asList(decodedValue.split("\\|"));
                    } else {
                        values = Arrays.asList(decodedValue);
                    }

                    values = values.stream()
                            .map(String::trim)
                            .filter(v -> !v.isEmpty())
                            .collect(Collectors.toList());

                    if (!values.isEmpty()) {
                        filters.put(fieldId, values);
                    }
                } catch (NumberFormatException e) {
                    // Invalid field ID, skip this filter
                } catch (java.io.UnsupportedEncodingException e) {
                    try {
                        Long fieldId = Long.parseLong(key.substring(7));
                        List<String> values = Arrays.asList(value);
                        filters.put(fieldId, values);
                    } catch (NumberFormatException ex) {
                        // Skip invalid field ID
                    }
                }
            }
        }

        return filters;
    }
}