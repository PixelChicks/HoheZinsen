package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.MoreInfo;
import com.InterestRatesAustria.InterestRatesAustria.repository.GlobalFieldRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InterestRateService {

    private static final Logger logger = LoggerFactory.getLogger(InterestRateService.class);
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)%?");
    private final InterestRateRepository interestRateRepository;
    private final FieldValueService fieldValueService;
    private final MoreInfoService moreInfoService;
    private final GlobalFieldRepository globalFieldRepository;
    private volatile Long percentageFieldId;

    public InterestRateService(InterestRateRepository interestRateRepository,
                               FieldValueService fieldValueService,
                               MoreInfoService moreInfoService,
                               GlobalFieldRepository globalFieldRepository) {
        this.interestRateRepository = interestRateRepository;
        this.fieldValueService = fieldValueService;
        this.moreInfoService = moreInfoService;
        this.globalFieldRepository = globalFieldRepository;
    }

    @PostConstruct
    public void initialize() {
        findPercentageField();
        logFieldInfo();
    }

    public Page<InterestRate> getAllInterestRatesPaginated(int page, int size, String sortBy, String sortDir) {
        logger.info("Getting paginated rates - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        List<InterestRate> allRates = interestRateRepository.findAll();
        logger.info("Loaded {} rates from database", allRates.size());

        List<InterestRate> sortedRates = applySortingToRates(allRates, sortBy, sortDir);

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

        logger.info("Sorted rates by percentage {} (first 5):", ascending ? "ASC" : "DESC");
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

    public Page<InterestRate> searchInterestRatesPaginated(String searchTerm, int page, int size, String sortBy, String sortDir) {
        logger.info("Searching rates with term: {}", searchTerm);

        List<InterestRate> searchResults = interestRateRepository.findBySearchTerm(searchTerm, Pageable.unpaged()).getContent();

        List<InterestRate> sortedResults = applySortingToRates(searchResults, sortBy, sortDir);

        return paginateResults(sortedResults, page, size);
    }

    public List<InterestRateDTO> getAllInterestRateDTOs() {
        List<InterestRate> rates = sortByPercentage(interestRateRepository.findAll(), "desc");
        return rates.stream()
                .map(InterestRateDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InterestRate> getAllInterestRates() {
        return sortByPercentage(interestRateRepository.findAll(), "desc");
    }


    public InterestRate getInterestRateById(Long id) {
        return interestRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interest rate not found with id: " + id));
    }

    public void updateSectionOrder(Long rateId, List<String> sectionOrder) {
        InterestRate rate = getInterestRateById(rateId);
        moreInfoService.updateSectionOrder(rateId, sectionOrder, rate);
    }

    @Transactional
    public void createInterestRate(InterestRate interestRate, Map<String, String> requestParams) {
        InterestRate saved = interestRateRepository.save(interestRate);
        fieldValueService.createFieldValuesForRate(saved, requestParams);
        MoreInfo moreInfo = moreInfoService.createMoreInfoWithSections(requestParams);
        if (moreInfo != null) {
            saved.setMoreInfo(moreInfo);
            interestRateRepository.save(saved);
        }
    }

    @Transactional
    public void updateInterestRate(Long id, InterestRate updatedRate, Map<String, String> requestParams) {
        InterestRate existingRate = getInterestRateById(id);
        existingRate.setWebLink(updatedRate.getWebLink());
        fieldValueService.updateFieldValuesForRate(existingRate, requestParams);
        MoreInfo updatedMoreInfo = moreInfoService.updateMoreInfoWithSections(existingRate.getMoreInfo(), requestParams);
        existingRate.setMoreInfo(updatedMoreInfo);
        interestRateRepository.save(existingRate);
    }

    @Transactional
    public void deleteInterestRate(Long id) {
        InterestRate rate = getInterestRateById(id);
        interestRateRepository.delete(rate);
    }

    // UTILITY METHODS

    private void findPercentageField() {
        List<GlobalField> allFields = globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
        logger.info("Looking for percentage field among {} fields", allFields.size());

        Optional<GlobalField> exactMatch = allFields.stream()
                .filter(field -> {
                    String label = field.getLabel().toLowerCase();
                    String fieldKey = field.getFieldKey() != null ? field.getFieldKey().toLowerCase() : "";

                    return label.equals("percent") || label.equals("percentage") ||
                            fieldKey.equals("percent") || fieldKey.equals("percentage");
                })
                .findFirst();

        if (exactMatch.isPresent()) {
            this.percentageFieldId = exactMatch.get().getId();
            logger.info("Found exact percentage field: {} (ID: {})", exactMatch.get().getLabel(), percentageFieldId);
            return;
        }

        Optional<GlobalField> partialMatch = allFields.stream()
                .filter(field -> {
                    String label = field.getLabel().toLowerCase();
                    String fieldKey = field.getFieldKey() != null ? field.getFieldKey().toLowerCase() : "";

                    return label.contains("percent") || label.contains("rate") ||
                            label.contains("interest") || label.contains("%") ||
                            fieldKey.contains("percent") || fieldKey.contains("rate") ||
                            fieldKey.contains("interest");
                })
                .findFirst();

        if (partialMatch.isPresent()) {
            this.percentageFieldId = partialMatch.get().getId();
            logger.info("Found partial percentage field: {} (ID: {})", partialMatch.get().getLabel(), percentageFieldId);
        } else {
            logger.warn("No percentage field found!");
        }
    }

    private void logFieldInfo() {
        List<GlobalField> allFields = globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
        logger.info("All available fields:");
        allFields.forEach(field ->
                logger.info("  Field ID: {}, Label: '{}', Key: '{}'",
                        field.getId(), field.getLabel(), field.getFieldKey()));

        if (percentageFieldId != null) {
            logger.info("Using percentage field ID: {}", percentageFieldId);
        }
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
}