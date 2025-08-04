package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.MoreInfo;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InterestRateService {

    private final InterestRateRepository interestRateRepository;
    private final FieldValueService fieldValueService;
    private final MoreInfoService moreInfoService;

    public InterestRateService(InterestRateRepository interestRateRepository,
                               FieldValueService fieldValueService,
                               MoreInfoService moreInfoService) {
        this.interestRateRepository = interestRateRepository;
        this.fieldValueService = fieldValueService;
        this.moreInfoService = moreInfoService;
    }

    public Page<InterestRate> getAllInterestRatesPaginated(int page, int size, String sortBy, String sortDir) {
        // Check if sorting by field value
        if (sortBy.startsWith("field_")) {
            return getAllInterestRatesPaginatedWithFieldSort(page, size, sortBy, sortDir);
        }

        // Regular sorting by InterestRate properties
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return interestRateRepository.findAll(pageable);
    }

    private Page<InterestRate> getAllInterestRatesPaginatedWithFieldSort(int page, int size, String sortBy, String sortDir) {
        // Extract field ID from sortBy (e.g., "field_123" -> "123")
        String fieldIdStr = sortBy.replace("field_", "");
        Long fieldId;
        try {
            fieldId = Long.parseLong(fieldIdStr);
        } catch (NumberFormatException e) {
            // Fallback to regular sorting if field ID is invalid
            return getAllInterestRatesPaginated(page, size, "id", sortDir);
        }

        // Get all interest rates
        List<InterestRate> allRates = interestRateRepository.findAll();

        // Get field values map
        Map<Long, Map<Long, String>> fieldValuesMap = fieldValueService.getRateFieldValuesMap(allRates);

        // Sort by field value
        List<InterestRate> sortedRates = allRates.stream()
                .sorted((rate1, rate2) -> {
                    String value1 = fieldValuesMap.getOrDefault(rate1.getId(), Map.of())
                            .getOrDefault(fieldId, "");
                    String value2 = fieldValuesMap.getOrDefault(rate2.getId(), Map.of())
                            .getOrDefault(fieldId, "");

                    // Try to parse as numbers first
                    try {
                        Double num1 = Double.parseDouble(value1.replaceAll("[^\\d.-]", ""));
                        Double num2 = Double.parseDouble(value2.replaceAll("[^\\d.-]", ""));
                        int result = num1.compareTo(num2);
                        return sortDir.equalsIgnoreCase("desc") ? -result : result;
                    } catch (NumberFormatException e) {
                        // Fallback to string comparison
                        int result = value1.compareToIgnoreCase(value2);
                        return sortDir.equalsIgnoreCase("desc") ? -result : result;
                    }
                })
                .collect(Collectors.toList());

        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, sortedRates.size());

        if (start >= sortedRates.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), sortedRates.size());
        }

        List<InterestRate> pageContent = sortedRates.subList(start, end);
        return new PageImpl<>(pageContent, PageRequest.of(page, size), sortedRates.size());
    }

    public Page<InterestRate> searchInterestRatesPaginated(String searchTerm, int page, int size, String sortBy, String sortDir) {
        // Check if sorting by field value during search
        if (sortBy.startsWith("field_")) {
            return searchInterestRatesPaginatedWithFieldSort(searchTerm, page, size, sortBy, sortDir);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return interestRateRepository.findBySearchTerm(searchTerm, pageable);
    }

    public List<InterestRateDTO> getAllInterestRateDTOs() {
        return interestRateRepository.findAll().stream()
                .map(InterestRateDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InterestRate> getAllInterestRates() {
        return interestRateRepository.findAll();
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

    private Page<InterestRate> searchInterestRatesPaginatedWithFieldSort(String searchTerm, int page, int size, String sortBy, String sortDir) {
        String fieldIdStr = sortBy.replace("field_", "");
        Long fieldId;
        try {
            fieldId = Long.parseLong(fieldIdStr);
        } catch (NumberFormatException e) {
            return searchInterestRatesPaginated(searchTerm, page, size, "id", sortDir);
        }

        List<InterestRate> allSearchResults = interestRateRepository.findBySearchTerm(searchTerm, Pageable.unpaged()).getContent();

        Map<Long, Map<Long, String>> fieldValuesMap = fieldValueService.getRateFieldValuesMap(allSearchResults);

        List<InterestRate> sortedResults = allSearchResults.stream()
                .sorted((rate1, rate2) -> {
                    String value1 = fieldValuesMap.getOrDefault(rate1.getId(), Map.of())
                            .getOrDefault(fieldId, "");
                    String value2 = fieldValuesMap.getOrDefault(rate2.getId(), Map.of())
                            .getOrDefault(fieldId, "");

                    try {
                        Double num1 = Double.parseDouble(value1.replaceAll("[^\\d.-]", ""));
                        Double num2 = Double.parseDouble(value2.replaceAll("[^\\d.-]", ""));
                        int result = num1.compareTo(num2);
                        return sortDir.equalsIgnoreCase("desc") ? -result : result;
                    } catch (NumberFormatException e) {
                        int result = value1.compareToIgnoreCase(value2);
                        return sortDir.equalsIgnoreCase("desc") ? -result : result;
                    }
                })
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, sortedResults.size());

        if (start >= sortedResults.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), sortedResults.size());
        }

        List<InterestRate> pageContent = sortedResults.subList(start, end);
        return new PageImpl<>(pageContent, PageRequest.of(page, size), sortedResults.size());
    }
}