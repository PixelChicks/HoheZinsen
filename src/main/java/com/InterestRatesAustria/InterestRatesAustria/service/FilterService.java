package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.FilterDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.FilterOptionDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRateFieldValue;
import com.InterestRatesAustria.InterestRatesAustria.repository.GlobalFieldRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilterService {

    private final GlobalFieldRepository globalFieldRepository;
    private final InterestRateRepository interestRateRepository;

    public FilterService(GlobalFieldRepository globalFieldRepository,
                        InterestRateRepository interestRateRepository) {
        this.globalFieldRepository = globalFieldRepository;
        this.interestRateRepository = interestRateRepository;
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
        
        Specification<InterestRate> spec = createFilterSpecification(filters, search);
        
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return interestRateRepository.findAll(spec, pageable);
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

    private Sort createSort(String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;

        if (sortBy != null && sortBy.startsWith("field_")) {
            return Sort.by(direction, "id");
        }
        
        return Sort.by(direction, sortBy != null ? sortBy : "id");
    }
}