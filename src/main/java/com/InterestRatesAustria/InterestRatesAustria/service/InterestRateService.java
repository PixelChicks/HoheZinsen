package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.*;
import com.InterestRatesAustria.InterestRatesAustria.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InterestRateService {

    private final InterestRateRepository interestRateRepository;
    private final GlobalFieldRepository globalFieldRepository;
    private final InterestRateFieldValueRepository fieldValueRepository;
    private final MoreInfoRepository moreInfoRepository;
    private final InterestRateFieldValueRepository interestRateFieldValueRepository;
    private final MiniTableRowRepository miniTableRowRepository;

    public InterestRateService(InterestRateRepository interestRateRepository,
                               GlobalFieldRepository globalFieldRepository,
                               InterestRateFieldValueRepository fieldValueRepository,
                               MoreInfoRepository moreInfoRepository, InterestRateFieldValueRepository interestRateFieldValueRepository,
                               MiniTableRowRepository miniTableRowRepository) {
        this.interestRateRepository = interestRateRepository;
        this.globalFieldRepository = globalFieldRepository;
        this.fieldValueRepository = fieldValueRepository;
        this.moreInfoRepository = moreInfoRepository;
        this.interestRateFieldValueRepository = interestRateFieldValueRepository;
        this.miniTableRowRepository = miniTableRowRepository;
    }

    public List<InterestRateDTO> getAllInterestRateDTOs() {
        return interestRateRepository.findAll().stream().map(InterestRateDTO::fromEntity).collect(Collectors.toList());
    }

    public List<InterestRate> getAllInterestRates() {
        return interestRateRepository.findAll();
    }

    public InterestRate getInterestRateById(Long id) {
        return interestRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interest rate not found with id: " + id));
    }

    public Map<Long, String> getFieldValuesForRate(Long rateId) {
        InterestRate rate = getInterestRateById(rateId);
        return rate.getFieldValues().stream()
                .collect(Collectors.toMap(
                        fv -> fv.getGlobalField().getId(),
                        InterestRateFieldValue::getValue
                ));
    }

    public List<GlobalField> getAllGlobalFieldsOrdered() {
        return globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
    }

    public Map<Long, Map<Long, String>> getRateFieldValuesMap(List<InterestRate> rates) {
        Map<Long, Map<Long, String>> result = new HashMap<>();

        for (InterestRate rate : rates) {
            Map<Long, String> fieldValues = rate.getFieldValues().stream().collect(Collectors.toMap(fv -> fv.getGlobalField().getId(), InterestRateFieldValue::getValue));
            result.put(rate.getId(), fieldValues);
        }
        return result;
    }

    public void addGlobalField(GlobalField field) {
        Integer maxSortOrder = globalFieldRepository.findMaxSortOrder();
        field.setSortOrder(maxSortOrder + 1);
        field.setFieldKey(field.getLabel().toLowerCase().replaceAll("\\s+", ""));
        GlobalField savedField = globalFieldRepository.save(field);

        interestRateRepository.findAll().forEach(rate -> {
            InterestRateFieldValue fv = new InterestRateFieldValue();
            fv.setInterestRate(rate);
            fv.setGlobalField(savedField);
            fv.setValue("");
            fieldValueRepository.save(fv);
        });
    }

    public void updateGlobalField(Long fieldId, String label) {
        GlobalField field = globalFieldRepository.findById(fieldId).orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));

        field.setLabel(label);
        field.setFieldKey(label.toLowerCase().replaceAll("\\s+", ""));
        globalFieldRepository.save(field);
    }

    public void reorderGlobalFields(List<Long> fieldIds) {
        for (int i = 0; i < fieldIds.size(); i++) {
            Long fieldId = fieldIds.get(i);
            GlobalField field = globalFieldRepository.findById(fieldId).orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
            field.setSortOrder(i + 1);
            globalFieldRepository.save(field);
        }
    }

    public void updateSectionOrder(Long rateId, List<String> sectionOrder) {
        InterestRate rate = interestRateRepository.findById(rateId).orElseThrow(() -> new RuntimeException("Interest rate not found with id: " + rateId));

        if (rate.getMoreInfo() != null) {
            rate.getMoreInfo().setSectionOrderList(sectionOrder);
            moreInfoRepository.save(rate.getMoreInfo());
        } else {
            throw new RuntimeException("No more info found for this rate");
        }
    }

    public void updateFieldValue(Long rateId, Long fieldId, String value) {
        InterestRateFieldValue fieldValue = fieldValueRepository.findByInterestRateIdAndGlobalFieldId(rateId, fieldId).orElseThrow();

        fieldValue.setValue(value);
        fieldValueRepository.save(fieldValue);
    }

    public void createInterestRate(InterestRate interestRate, Map<String, String> requestParams, List<String> tableRowLabels, List<String> tableRowDescriptions) {
        InterestRate saved = interestRateRepository.save(interestRate);

        List<GlobalField> allFields = globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
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
    }

    @Transactional
    public void updateInterestRate(Long id, InterestRate updatedRate, Map<String, String> requestParams, List<String> tableRowLabels, List<String> tableRowDescriptions) {
        InterestRate existingRate = getInterestRateById(id);

        List<GlobalField> allFields = globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
        for (GlobalField field : allFields) {
            String key = "extra_" + field.getId();
            if (requestParams.containsKey(key)) {
                String value = requestParams.get(key);

                InterestRateFieldValue fieldValue = fieldValueRepository
                        .findByInterestRateIdAndGlobalFieldId(id, field.getId())
                        .orElse(new InterestRateFieldValue());

                fieldValue.setInterestRate(existingRate);
                fieldValue.setGlobalField(field);
                fieldValue.setValue(value);
                fieldValueRepository.save(fieldValue);
            }
        }

        String tableTitle = requestParams.get("tableTitle");
        String textTitle = requestParams.get("textTitle");
        String textDescription = requestParams.get("textDescription");

        if (tableTitle != null || textTitle != null || textDescription != null) {
            MoreInfo moreInfo = existingRate.getMoreInfo();
            if (moreInfo == null) {
                moreInfo = new MoreInfo();
                moreInfo.setSectionOrder("table,text");
            }

            moreInfo.setTableTitle(tableTitle);
            moreInfo.setTextTitle(textTitle);
            moreInfo.setTextDescription(textDescription);

            if (moreInfo.getMiniTableRows() != null) {
                moreInfo.getMiniTableRows().clear();
            }

            List<MiniTableRow> miniTableRows = getMiniTableRows(tableRowLabels, tableRowDescriptions);
            moreInfo.setMiniTableRows(miniTableRows);

            MoreInfo savedMoreInfo = moreInfoRepository.save(moreInfo);
            existingRate.setMoreInfo(savedMoreInfo);
            interestRateRepository.save(existingRate);
        } else {
            if (existingRate.getMoreInfo() != null) {
                Long moreInfoId = existingRate.getMoreInfo().getId();
                existingRate.setMoreInfo(null);
                interestRateRepository.save(existingRate);
                moreInfoRepository.deleteById(moreInfoId);
            }
        }
    }

    @Transactional
    public void deleteInterestRate(Long id) {
        InterestRate rate = getInterestRateById(id);

        fieldValueRepository.deleteAll(rate.getFieldValues());

        if (rate.getMoreInfo() != null) {
            moreInfoRepository.delete(rate.getMoreInfo());
        }

        interestRateRepository.delete(rate);
    }

    private List<MiniTableRow> getMiniTableRows(List<String> tableRowLabels, List<String> tableRowDescriptions) {
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

    public void deleteGlobalField(Long fieldId) {
        GlobalField field = globalFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
        field.setDeletedAt(LocalDateTime.now());
        globalFieldRepository.save(field);
    }
}