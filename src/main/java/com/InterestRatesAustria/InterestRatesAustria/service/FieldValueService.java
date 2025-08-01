package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRateFieldValue;
import com.InterestRatesAustria.InterestRatesAustria.repository.GlobalFieldRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateFieldValueRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FieldValueService {

    private final InterestRateFieldValueRepository fieldValueRepository;
    private final InterestRateRepository interestRateRepository;
    private final GlobalFieldRepository globalFieldRepository;

    public FieldValueService(InterestRateFieldValueRepository fieldValueRepository,
                            InterestRateRepository interestRateRepository,
                            GlobalFieldRepository globalFieldRepository) {
        this.fieldValueRepository = fieldValueRepository;
        this.interestRateRepository = interestRateRepository;
        this.globalFieldRepository = globalFieldRepository;
    }

    public Map<Long, String> getFieldValuesForRate(Long rateId) {
        InterestRate rate = interestRateRepository.findById(rateId)
                .orElseThrow(() -> new RuntimeException("Interest rate not found with id: " + rateId));
        
        return rate.getFieldValues().stream()
                .collect(Collectors.toMap(
                        fv -> fv.getGlobalField().getId(),
                        InterestRateFieldValue::getValue
                ));
    }

    public Map<Long, Map<Long, String>> getRateFieldValuesMap(List<InterestRate> rates) {
        Map<Long, Map<Long, String>> result = new HashMap<>();

        for (InterestRate rate : rates) {
            Map<Long, String> fieldValues = rate.getFieldValues().stream()
                    .collect(Collectors.toMap(
                            fv -> fv.getGlobalField().getId(),
                            InterestRateFieldValue::getValue
                    ));
            result.put(rate.getId(), fieldValues);
        }
        return result;
    }

    public void updateFieldValue(Long rateId, Long fieldId, String value) {
        InterestRateFieldValue fieldValue = fieldValueRepository
                .findByInterestRateIdAndGlobalFieldId(rateId, fieldId)
                .orElseThrow(() -> new RuntimeException("Field value not found for rate: " + rateId + " and field: " + fieldId));

        fieldValue.setValue(value);
        fieldValueRepository.save(fieldValue);
    }

    public void createFieldValuesForRate(InterestRate rate, Map<String, String> requestParams) {
        List<GlobalField> allFields = globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
        
        for (GlobalField field : allFields) {
            String key = "extra_" + field.getId();
            if (requestParams.containsKey(key)) {
                String value = requestParams.get(key);
                InterestRateFieldValue fv = new InterestRateFieldValue();
                fv.setInterestRate(rate);
                fv.setGlobalField(field);
                fv.setValue(value);
                fieldValueRepository.save(fv);
            }
        }
    }

    public void updateFieldValuesForRate(InterestRate rate, Map<String, String> requestParams) {
        List<GlobalField> allFields = globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
        
        for (GlobalField field : allFields) {
            String key = "extra_" + field.getId();
            if (requestParams.containsKey(key)) {
                String value = requestParams.get(key);
                InterestRateFieldValue fieldValue = fieldValueRepository
                        .findByInterestRateIdAndGlobalFieldId(rate.getId(), field.getId())
                        .orElse(new InterestRateFieldValue());
                        
                fieldValue.setInterestRate(rate);
                fieldValue.setGlobalField(field);
                fieldValue.setValue(value);
                fieldValueRepository.save(fieldValue);
            }
        }
    }
}