package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.entity.*;
import com.InterestRatesAustria.InterestRatesAustria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        List<GlobalField> globalFields = globalFieldRepository.findAllByOrderBySortOrderAsc();

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
        Integer maxSortOrder = globalFieldRepository.findMaxSortOrder();
        field.setSortOrder(maxSortOrder + 1);
        field.setFieldKey(field.getLabel().toLowerCase().replaceAll("\\s+", ""));

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

    @PostMapping("/fields/update")
    public String updateGlobalField(@RequestParam Long fieldId,
                                    @RequestParam String label) {

        GlobalField field = globalFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));

        field.setFieldKey(label.toLowerCase().replaceAll("\\s+", ""));
        field.setLabel(label);
        globalFieldRepository.save(field);

        return "redirect:/";
    }

    @PostMapping("/fields/reorder")
    @ResponseBody
    public ResponseEntity<String> reorderFields(@RequestBody List<Long> fieldIds) {
        try {
            for (int i = 0; i < fieldIds.size(); i++) {
                Long fieldId = fieldIds.get(i);
                GlobalField field = globalFieldRepository.findById(fieldId)
                        .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
                field.setSortOrder(i + 1);
                globalFieldRepository.save(field);
            }
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
            InterestRate rate = interestRateRepository.findById(rateId)
                    .orElseThrow(() -> new RuntimeException("Interest rate not found with id: " + rateId));

            if (rate.getMoreInfo() != null) {
                rate.getMoreInfo().setSectionOrderList(sectionOrder);
                moreInfoRepository.save(rate.getMoreInfo());
                return ResponseEntity.ok("Section order updated successfully");
            } else {
                return ResponseEntity.badRequest().body("No more info found for this rate");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating section order: " + e.getMessage());
        }
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
        model.addAttribute("globalFields", globalFieldRepository.findAllByOrderBySortOrderAsc());
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

        List<GlobalField> allFields = globalFieldRepository.findAllByOrderBySortOrderAsc();
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
            moreInfo.setSectionOrder("table,text");

            List<MiniTableRow> miniTableRows = getMiniTableRows(tableRowLabels, tableRowDescriptions);

            moreInfo.setMiniTableRows(miniTableRows);
            MoreInfo savedMoreInfo = moreInfoRepository.save(moreInfo);

            saved.setMoreInfo(savedMoreInfo);
            interestRateRepository.save(saved);
        }

        return "redirect:/";
    }

    private static List<MiniTableRow> getMiniTableRows(List<String> tableRowLabels, List<String> tableRowDescriptions) {
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
        return miniTableRows;
    }
}