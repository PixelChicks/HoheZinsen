package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRateFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestRateFieldValueRepository extends JpaRepository<InterestRateFieldValue, Long> {
    Optional<InterestRateFieldValue> findByInterestRateIdAndGlobalFieldId(Long interestRateId, Long globalFieldId);
}
