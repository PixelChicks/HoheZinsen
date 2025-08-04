package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.MoreInfo;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import org.springframework.data.domain.Page;
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
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return interestRateRepository.findAll(pageable);
    }

    public Page<InterestRate> searchInterestRatesPaginated(String searchTerm, int page, int size, String sortBy, String sortDir) {
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
}