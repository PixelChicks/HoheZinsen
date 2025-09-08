package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRateFieldValue;
import com.InterestRatesAustria.InterestRatesAustria.repository.GlobalFieldRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateFieldValueRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<GlobalField> getTableFieldsOrdered() {
        return globalFieldRepository.findAllActiveTableFieldsOrdered();
    }

    public List<GlobalField> getCompareFieldsOrdered() {
        return globalFieldRepository.findAllActiveCompareFieldsOrdered();
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
        GlobalField field = globalFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));

        field.setLabel(label);

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

    public void addMultipleGlobalFields(Map<String, String> allParams) {
        Map<Integer, GlobalField> fieldsMap = parseFieldsFromParams(allParams);
        saveMultipleFieldsWithSortOrder(fieldsMap);
    }


    public void updateMultipleGlobalFields(Map<String, String> allParams) {
        Map<Integer, GlobalField> fieldsMap = parseFieldsFromParams(allParams);

        for (GlobalField updatedField : fieldsMap.values()) {
            if (updatedField.getId() != null) {
                GlobalField existing = globalFieldRepository.findById(updatedField.getId())
                        .orElseThrow(() -> new RuntimeException("Field not found with id: " + updatedField.getId()));

                existing.setLabel(updatedField.getLabel());
                existing.setFieldKey(generateFieldKey(updatedField.getLabel()));
                existing.setAtTable(updatedField.isAtTable());
                existing.setAtCompare(updatedField.isAtCompare());

                globalFieldRepository.save(existing);
            } else {
                addGlobalField(updatedField);
            }
        }
    }

    private Map<Integer, GlobalField> parseFieldsFromParams(Map<String, String> allParams) {
        Map<Integer, GlobalField> fieldsMap = new HashMap<>();

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (isFieldParameter(key)) {
                int fieldIndex = extractFieldIndex(key);
                String property = extractPropertyName(key);

                GlobalField field = getOrCreateField(fieldsMap, fieldIndex);
                setFieldProperty(field, property, value);
            }
        }

        return fieldsMap;
    }

    private boolean isFieldParameter(String key) {
        return key.startsWith("fields[") && key.contains("].");
    }

    private int extractFieldIndex(String key) {
        int startIndex = key.indexOf('[') + 1;
        int endIndex = key.indexOf(']');
        return Integer.parseInt(key.substring(startIndex, endIndex));
    }

    private String extractPropertyName(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    private GlobalField getOrCreateField(Map<Integer, GlobalField> fieldsMap, int fieldIndex) {
        return fieldsMap.computeIfAbsent(fieldIndex, k -> GlobalField.builder()
                .atTable(false)
                .atCompare(false)
                .build());
    }

    private String generateFieldKey(String label) {
        return label.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");
    }

    private void saveMultipleFieldsWithSortOrder(Map<Integer, GlobalField> fieldsMap) {
        Integer maxSortOrder = getNextAvailableSortOrder();
        List<Integer> sortedIndexes = getSortedFieldIndexes(fieldsMap);

        int currentSortOrder = maxSortOrder;

        for (Integer index : sortedIndexes) {
            GlobalField field = fieldsMap.get(index);
            if (isValidField(field)) {
                field.setSortOrder(currentSortOrder++);
                GlobalField savedField = globalFieldRepository.save(field);
                createFieldValuesForExistingRates(savedField);
            }
        }
    }

    private Integer getNextAvailableSortOrder() {
        Integer maxSortOrder = globalFieldRepository.findMaxSortOrder();
        return (maxSortOrder == null) ? 1 : maxSortOrder + 1;
    }

    private List<Integer> getSortedFieldIndexes(Map<Integer, GlobalField> fieldsMap) {
        return fieldsMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean isValidField(GlobalField field) {
        return field.getLabel() != null && !field.getLabel().trim().isEmpty();
    }

    private void createFieldValuesForExistingRates(GlobalField savedField) {
        interestRateRepository.findAll().forEach(rate -> {
            InterestRateFieldValue fieldValue = InterestRateFieldValue.builder()
                    .interestRate(rate)
                    .globalField(savedField)
                    .value("")
                    .build();
            fieldValueRepository.save(fieldValue);
        });
    }

    private void setFieldProperty(GlobalField field, String property, String value) {
        switch (property) {
            case "id":
                if (value != null && !value.isEmpty()) {
                    field.setId(Long.parseLong(value));
                }
                break;
            case "label":
                field.setLabel(value);
                field.setFieldKey(generateFieldKey(value));
                break;
            case "atTable":
                field.setAtTable("true".equalsIgnoreCase(value));
                break;
            case "atCompare":
                field.setAtCompare("true".equalsIgnoreCase(value));
                break;
        }
    }

}