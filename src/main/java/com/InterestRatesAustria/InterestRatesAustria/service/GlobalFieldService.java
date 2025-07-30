package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRateFieldValue;
import com.InterestRatesAustria.InterestRatesAustria.repository.GlobalFieldRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateFieldValueRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GlobalFieldService {

    private final GlobalFieldRepository globalFieldRepository;
    private final InterestRateRepository interestRateRepository;
    private final InterestRateFieldValueRepository fieldValueRepository;

    public GlobalFieldService(GlobalFieldRepository globalFieldRepository,
                             InterestRateRepository interestRateRepository,
                             InterestRateFieldValueRepository fieldValueRepository) {
        this.globalFieldRepository = globalFieldRepository;
        this.interestRateRepository = interestRateRepository;
        this.fieldValueRepository = fieldValueRepository;
    }

    public List<GlobalField> getAllGlobalFieldsOrdered() {
        return globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
    }

    public void addGlobalField(GlobalField field) {
        Integer maxSortOrder = globalFieldRepository.findMaxSortOrder();
        field.setSortOrder(maxSortOrder + 1);
        field.setFieldKey(field.getLabel().toLowerCase().replaceAll("\\s+", ""));
        GlobalField savedField = globalFieldRepository.save(field);

        // Create field values for all existing interest rates
        interestRateRepository.findAll().forEach(rate -> {
            InterestRateFieldValue fv = new InterestRateFieldValue();
            fv.setInterestRate(rate);
            fv.setGlobalField(savedField);
            fv.setValue("");
            fieldValueRepository.save(fv);
        });
    }

    public void updateGlobalField(Long fieldId, String label) {
        GlobalField field = globalFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));

        field.setLabel(label);
        field.setFieldKey(label.toLowerCase().replaceAll("\\s+", ""));
        globalFieldRepository.save(field);
    }

    public void reorderGlobalFields(List<Long> fieldIds) {
        for (int i = 0; i < fieldIds.size(); i++) {
            Long fieldId = fieldIds.get(i);
            GlobalField field = globalFieldRepository.findById(fieldId)
                    .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
            field.setSortOrder(i + 1);
            globalFieldRepository.save(field);
        }
    }

    public void deleteGlobalField(Long fieldId) {
        GlobalField field = globalFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
        field.setDeletedAt(LocalDateTime.now());
        globalFieldRepository.save(field);
    }
}