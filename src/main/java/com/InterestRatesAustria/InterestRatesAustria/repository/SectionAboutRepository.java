package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.SectionAbout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SectionAboutRepository extends JpaRepository<SectionAbout, Long> {
    Optional<SectionAbout> findByIsActive(Boolean isActive);
}