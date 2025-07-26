package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.TextSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TextSectionRepository extends JpaRepository<TextSection, Long> {
    List<TextSection> findByMoreInfoId(Long moreInfoId);
    Optional<TextSection> findBySectionIdentifier(String sectionIdentifier);
    void deleteByMoreInfoId(Long moreInfoId);
}