package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRateFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterestRateFieldValueRepository extends JpaRepository<InterestRateFieldValue, Long> {
    Optional<InterestRateFieldValue> findByInterestRateIdAndGlobalFieldId(Long interestRateId, Long globalFieldId);

    List<InterestRateFieldValue> findAllByGlobalFieldId(Long globalFieldId);

    List<InterestRateFieldValue> findByInterestRateIdIn(List<Long> rateIds);

    @Query("SELECT fv FROM InterestRateFieldValue fv WHERE fv.interestRate.id IN :rateIds")
    List<InterestRateFieldValue> findByRateIds(@Param("rateIds") List<Long> rateIds);
}