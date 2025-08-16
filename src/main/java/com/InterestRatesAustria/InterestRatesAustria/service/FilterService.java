package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.FilterDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.FilterOptionDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRateFieldValue;
import com.InterestRatesAustria.InterestRatesAustria.repository.GlobalFieldRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.criteria.Predicate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FilterService {

    private static final Logger logger = LoggerFactory.getLogger(FilterService.class);
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)%?");

    private final GlobalFieldRepository globalFieldRepository;
    private final InterestRateRepository interestRateRepository;
    private final FieldValueService fieldValueService;

    public FilterService(GlobalFieldRepository globalFieldRepository,
                         InterestRateRepository interestRateRepository,
                         FieldValueService fieldValueService) {
        this.globalFieldRepository = globalFieldRepository;
        this.interestRateRepository = interestRateRepository;
        this.fieldValueService = fieldValueService;
    }

    public List<FilterDTO> getAvailableFilters() {
        List<GlobalField> fields = globalFieldRepository.findByDeletedAtIsNullOrderBySortOrder();

        return fields.stream()
                .map(field -> {
                    FilterDTO filter = new FilterDTO();
                    filter.setFieldId(field.getId());
                    filter.setLabel(field.getLabel());
                    filter.setFieldKey(field.getFieldKey());
                    filter.setOptions(getFilterOptionsForField(field.getId()));
                    return filter;
                })
                .collect(Collectors.toList());
    }

    public List<FilterOptionDTO> getFilterOptionsForField(Long fieldId) {
        List<InterestRate> allRates = interestRateRepository.findAll();

        Map<String, Long> valueCount = new HashMap<>();

        for (InterestRate rate : allRates) {
            for (InterestRateFieldValue fieldValue : rate.getFieldValues()) {
                if (fieldValue.getGlobalField().getId().equals(fieldId)) {
                    String value = fieldValue.getValue();
                    if (value != null && !value.trim().isEmpty()) {
                        valueCount.put(value.trim(), valueCount.getOrDefault(value.trim(), 0L) + 1);
                    }
                }
            }
        }

        return valueCount.entrySet().stream()
                .map(entry -> {
                    FilterOptionDTO option = new FilterOptionDTO();
                    option.setValue(entry.getKey());
                    option.setCount(entry.getValue());
                    option.setLabel(entry.getKey() + " (" + entry.getValue() + ")");
                    return option;
                })
                .sorted(Comparator.comparing(FilterOptionDTO::getValue))
                .collect(Collectors.toList());
    }

    public Page<InterestRate> getFilteredInterestRates(Map<Long, List<String>> filters,
                                                       int page, int size,
                                                       String sortBy, String sortDir,
                                                       String search) {

        logger.info("Getting filtered rates - filters: {}, sortBy: {}, sortDir: {}", filters.size(), sortBy, sortDir);

        Specification<InterestRate> spec = createFilterSpecification(filters, search);
        List<InterestRate> filteredRates = interestRateRepository.findAll(spec);

        logger.info("Found {} filtered rates before sorting", filteredRates.size());

        List<InterestRate> sortedRates = applySortingToRates(filteredRates, sortBy, sortDir);

        return paginateResults(sortedRates, page, size);
    }

    private List<InterestRate> applySortingToRates(List<InterestRate> rates, String sortBy, String sortDir) {
        if (sortBy == null || sortBy.isEmpty() || sortBy.equals("field_1")) {
            String defaultSortDir = (sortDir == null || sortDir.isEmpty()) ? "desc" : sortDir;
            return sortByPercentage(rates, defaultSortDir);
        }

        if (sortBy.startsWith("field_")) {
            return sortByField(rates, sortBy, sortDir);
        }

        return sortByProperty(rates, sortBy, sortDir);
    }

    private List<InterestRate> sortByPercentage(List<InterestRate> rates, String sortDir) {
        Long percentageFieldId = findPercentageFieldId();

        if (percentageFieldId == null) {
            logger.warn("No percentage field found, using ID sorting");
            return sortByProperty(rates, "id", sortDir);
        }

        Map<Long, Map<Long, String>> fieldValuesMap = fieldValueService.getRateFieldValuesMap(rates);
        logger.info("Got field values for {} rates", fieldValuesMap.size());

        boolean ascending = "asc".equalsIgnoreCase(sortDir);

        List<InterestRate> sortedRates = rates.stream()
                .sorted((rate1, rate2) -> {
                    String value1 = fieldValuesMap.getOrDefault(rate1.getId(), Map.of())
                            .getOrDefault(percentageFieldId, "0");
                    String value2 = fieldValuesMap.getOrDefault(rate2.getId(), Map.of())
                            .getOrDefault(percentageFieldId, "0");

                    Double percentage1 = extractPercentageValue(value1);
                    Double percentage2 = extractPercentageValue(value2);

                    logger.debug("Comparing rates: {} ({}% = {}) vs {} ({}% = {})",
                            rate1.getId(), value1, percentage1, rate2.getId(), value2, percentage2);

                    if (percentage1 == null && percentage2 == null) {
                        return rate1.getId().compareTo(rate2.getId());
                    }
                    if (percentage1 == null) return 1;
                    if (percentage2 == null) return -1;

                    int result = ascending ? percentage1.compareTo(percentage2) : percentage2.compareTo(percentage1);

                    if (result == 0) {
                        result = rate1.getId().compareTo(rate2.getId());
                    }

                    return result;
                })
                .collect(Collectors.toList());

        logger.info("Sorted filtered rates by percentage {} (first 5):", ascending ? "ASC" : "DESC");
        sortedRates.stream().limit(5).forEach(rate -> {
            String percentageValue = fieldValuesMap.getOrDefault(rate.getId(), Map.of())
                    .getOrDefault(percentageFieldId, "N/A");
            logger.info("  Rate ID: {}, Percentage: {}", rate.getId(), percentageValue);
        });

        return sortedRates;
    }

    private List<InterestRate> sortByField(List<InterestRate> rates, String sortBy, String sortDir) {
        String fieldIdStr = sortBy.replace("field_", "");
        try {
            Long fieldId = Long.parseLong(fieldIdStr);
            Map<Long, Map<Long, String>> fieldValuesMap = fieldValueService.getRateFieldValuesMap(rates);

            boolean ascending = (sortDir == null || sortDir.isEmpty()) ? true : "asc".equalsIgnoreCase(sortDir);

            return rates.stream()
                    .sorted((rate1, rate2) -> {
                        String value1 = fieldValuesMap.getOrDefault(rate1.getId(), Map.of())
                                .getOrDefault(fieldId, "");
                        String value2 = fieldValuesMap.getOrDefault(rate2.getId(), Map.of())
                                .getOrDefault(fieldId, "");

                        if (value1.isEmpty() && value2.isEmpty()) {
                            return rate1.getId().compareTo(rate2.getId());
                        }
                        if (value1.isEmpty()) return 1;
                        if (value2.isEmpty()) return -1;

                        Double num1 = extractPercentageValue(value1);
                        Double num2 = extractPercentageValue(value2);

                        if (num1 != null && num2 != null) {
                            int result = num1.compareTo(num2);
                            return ascending ? result : -result;
                        }

                        int result = value1.compareToIgnoreCase(value2);
                        return ascending ? result : -result;
                    })
                    .collect(Collectors.toList());

        } catch (NumberFormatException e) {
            logger.warn("Invalid field ID in sortBy: {}", sortBy);
            return rates;
        }
    }

    private List<InterestRate> sortByProperty(List<InterestRate> rates, String sortBy, String sortDir) {
        boolean ascending = "asc".equalsIgnoreCase(sortDir);

        return rates.stream()
                .sorted((rate1, rate2) -> {
                    int result = 0;

                    switch (sortBy.toLowerCase()) {
                        case "id":
                            result = rate1.getId().compareTo(rate2.getId());
                            break;
                        case "weblink":
                            String link1 = rate1.getWebLink() != null ? rate1.getWebLink() : "";
                            String link2 = rate2.getWebLink() != null ? rate2.getWebLink() : "";
                            result = link1.compareToIgnoreCase(link2);
                            break;
                        default:
                            result = rate1.getId().compareTo(rate2.getId());
                            break;
                    }

                    return ascending ? result : -result;
                })
                .collect(Collectors.toList());
    }

    private Long findPercentageFieldId() {
        List<GlobalField> allFields = globalFieldRepository.findAllActiveByOrderBySortOrderAsc();

        Optional<GlobalField> field1 = allFields.stream()
                .filter(field -> field.getId().equals(1L))
                .findFirst();

        if (field1.isPresent()) {
            return field1.get().getId();
        }

        Optional<GlobalField> percentageField = allFields.stream()
                .filter(field -> {
                    String label = field.getLabel().toLowerCase();
                    String fieldKey = field.getFieldKey() != null ? field.getFieldKey().toLowerCase() : "";
                    return label.contains("percent") || label.contains("rate") ||
                            label.contains("interest") || label.contains("%") ||
                            fieldKey.contains("percent") || fieldKey.contains("rate");
                })
                .findFirst();

        return percentageField.map(GlobalField::getId).orElse(null);
    }

    private Double extractPercentageValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            String normalizedValue = value.replace(',', '.');

            Matcher matcher = PERCENTAGE_PATTERN.matcher(normalizedValue);
            if (matcher.find()) {
                String numStr = matcher.group(1);
                return Double.parseDouble(numStr);
            }

            String cleanValue = value.replaceAll("[^\\d.-]", "");
            if (!cleanValue.isEmpty()) {
                return Double.parseDouble(cleanValue);
            }

        } catch (NumberFormatException e) {
            logger.debug("Could not parse percentage value: '{}'", value);
        }

        return null;
    }

    private Page<InterestRate> paginateResults(List<InterestRate> results, int page, int size) {
        int start = page * size;
        int end = Math.min(start + size, results.size());

        if (start >= results.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), results.size());
        }

        List<InterestRate> pageContent = results.subList(start, end);
        return new PageImpl<>(pageContent, PageRequest.of(page, size), results.size());
    }

    private Specification<InterestRate> createFilterSpecification(Map<Long, List<String>> filters, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters != null && !filters.isEmpty()) {
                for (Map.Entry<Long, List<String>> filterEntry : filters.entrySet()) {
                    Long fieldId = filterEntry.getKey();
                    List<String> selectedValues = filterEntry.getValue();

                    if (selectedValues != null && !selectedValues.isEmpty()) {
                        Predicate fieldPredicate = root.join("fieldValues")
                                .get("globalField")
                                .get("id")
                                .in(fieldId);

                        Predicate valuePredicate = root.join("fieldValues")
                                .get("value")
                                .in(selectedValues);

                        predicates.add(criteriaBuilder.and(fieldPredicate, valuePredicate));
                    }
                }
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";

                Predicate webLinkPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("webLink")), searchPattern);

                Predicate fieldValuePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.join("fieldValues").get("value")), searchPattern);

                predicates.add(criteriaBuilder.or(webLinkPredicate, fieldValuePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}