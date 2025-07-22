package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.entity.InterestRateFieldValue;
import com.InterestRatesAustria.InterestRatesAustria.repository.GlobalFieldRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateFieldValueRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class InterestRateController {

    @Autowired
    private InterestRateRepository interestRateRepository;
    @Autowired
    private GlobalFieldRepository globalFieldRepository;
    @Autowired
    private InterestRateFieldValueRepository fieldValueRepository;

    @GetMapping("/")
    public String showRates(Model model) {
        List<InterestRate> interestRates = interestRateRepository.findAll();
        List<GlobalField> globalFields = globalFieldRepository.findAll();

        Map<Long, Map<Long, String>> rateFieldValuesMap = new HashMap<>();

        for (InterestRate rate : interestRates) {
            Map<Long, String> fieldValues = rate.getFieldValues().stream()
                    .collect(Collectors.toMap(
                            fv -> fv.getGlobalField().getId(),
                            InterestRateFieldValue::getValue
                    ));
            rateFieldValuesMap.put(rate.getId(), fieldValues);
        }

        model.addAttribute("interestRates", interestRates);
        model.addAttribute("globalFields", globalFields);
        model.addAttribute("rateFieldValuesMap", rateFieldValuesMap);
        model.addAttribute("newField", new GlobalField());

        return "index";
    }


    @PostMapping("/fields/add")
    public String addGlobalField(@ModelAttribute GlobalField field) {
        globalFieldRepository.save(field);

        List<InterestRate> rates = interestRateRepository.findAll();
        for (InterestRate rate : rates) {
            InterestRateFieldValue fv = new InterestRateFieldValue();
            fv.setInterestRate(rate);
            fv.setGlobalField(field);
            fv.setValue(""); // default blank
            fieldValueRepository.save(fv);
        }

        return "redirect:/";
    }

    @PostMapping("/field-values/update")
    public String updateFieldValue(@RequestParam Long rateId,
                                   @RequestParam Long fieldId,
                                   @RequestParam String value) {

        InterestRateFieldValue fieldValue = fieldValueRepository.findByInterestRateIdAndGlobalFieldId(rateId, fieldId)
                .orElseThrow();

        fieldValue.setValue(value);
        fieldValueRepository.save(fieldValue);
        return "redirect:/";
    }

    @GetMapping("/interest-rate/new")
    public String showCreateForm(Model model) {
        model.addAttribute("interestRate", new InterestRate());
        model.addAttribute("globalFields", globalFieldRepository.findAll());
        return "interest-rate-form";
    }

    @PostMapping("/interest-rate/create")
    public String createInterestRate(@ModelAttribute InterestRate interestRate,
                                     @RequestParam Map<String, String> requestParams) {

        InterestRate saved = interestRateRepository.save(interestRate);

        List<GlobalField> allFields = globalFieldRepository.findAll();
        for (GlobalField field : allFields) {
            String key = "extra_" + field.getId();
            if (requestParams.containsKey(key)) {
                String value = requestParams.get(key);
                InterestRateFieldValue fv = new InterestRateFieldValue();
                fv.setInterestRate(saved);
                fv.setGlobalField(field);
                fv.setValue(value);
                fieldValueRepository.save(fv);
            }
        }

        return "redirect:/";
    }

    @PostMapping("/interest-rates/update")
    public String updateInterestRateField(@RequestParam Long rateId,
                                          @RequestParam String fieldName,
                                          @RequestParam String value) {

        InterestRate rate = interestRateRepository.findById(rateId).orElseThrow();

        switch (fieldName) {
            case "interestRate" -> rate.setInterestRate(value);
            case "duration" -> rate.setDuration(value);
            case "provider" -> rate.setProvider(value);
            case "paymentFrequency" -> rate.setPaymentFrequency(value);
            case "interestType" -> rate.setInterestType(value);
            default -> throw new IllegalArgumentException("Invalid field name: " + fieldName);
        }

        interestRateRepository.save(rate);
        return "redirect:/";
    }
}