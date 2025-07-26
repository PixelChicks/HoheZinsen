package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.TableSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableSectionRepository extends JpaRepository<TableSection, Long> {
    List<TableSection> findByMoreInfoId(Long moreInfoId);
    Optional<TableSection> findBySectionIdentifier(String sectionIdentifier);
    void deleteByMoreInfoId(Long moreInfoId);
}