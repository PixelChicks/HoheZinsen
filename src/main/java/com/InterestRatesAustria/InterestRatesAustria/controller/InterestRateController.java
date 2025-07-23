package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.entity.*;
import com.InterestRatesAustria.InterestRatesAustria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
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
    @Autowired
    private MoreInfoRepository moreInfoRepository;
    @Autowired
    private MiniTableRowRepository miniTableRowRepository;

    @GetMapping("/")
    public String showRates(Model model) {
        List<InterestRate> interestRates = interestRateRepository.findAll();
        List<GlobalField> globalFields = globalFieldRepository.findAll();

        List<InterestRateDTO> interestRateDTOs = interestRates.stream()
                .map(InterestRateDTO::fromEntity)
                .collect(Collectors.toList());

        Map<Long, Map<Long, String>> rateFieldValuesMap = new HashMap<>();

        for (InterestRate rate : interestRates) {
            Map<Long, String> fieldValues = rate.getFieldValues().stream()
                    .collect(Collectors.toMap(
                            fv -> fv.getGlobalField().getId(),
                            InterestRateFieldValue::getValue
                    ));
            rateFieldValuesMap.put(rate.getId(), fieldValues);
        }

        model.addAttribute("interestRates", interestRateDTOs);
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
            fv.setValue("");
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
    public String createInterestRate(
            @ModelAttribute InterestRate interestRate,
            @RequestParam Map<String, String> requestParams,
            @RequestParam(name = "tableRowLabels[]", required = false) List<String> tableRowLabels,
            @RequestParam(name = "tableRowDescriptions[]", required = false) List<String> tableRowDescriptions
    ) {

        InterestRate saved = interestRateRepository.save(interestRate);

        // Save extra fields
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

        String tableTitle = requestParams.get("tableTitle");
        String textTitle = requestParams.get("textTitle");
        String textDescription = requestParams.get("textDescription");

        if (tableTitle != null || textTitle != null || textDescription != null) {
            MoreInfo moreInfo = new MoreInfo();
            moreInfo.setTableTitle(tableTitle);
            moreInfo.setTextTitle(textTitle);
            moreInfo.setTextDescription(textDescription);

            List<MiniTableRow> miniTableRows = new ArrayList<>();

            if (tableRowLabels != null && tableRowDescriptions != null) {
                int size = Math.min(tableRowLabels.size(), tableRowDescriptions.size());
                for (int i = 0; i < size; i++) {
                    String label = tableRowLabels.get(i).trim();
                    String desc = tableRowDescriptions.get(i).trim();
                    if (!label.isEmpty() || !desc.isEmpty()) {
                        MiniTableRow row = new MiniTableRow();
                        row.setLabel(label);
                        row.setDescription(desc);
                        miniTableRows.add(row);
                    }
                }
            }

            moreInfo.setMiniTableRows(miniTableRows);
            MoreInfo savedMoreInfo = moreInfoRepository.save(moreInfo);

            saved.setMoreInfo(savedMoreInfo);
            interestRateRepository.save(saved);
        }

        return "redirect:/";
    }
}