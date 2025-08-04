package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterestRateRepository extends JpaRepository<InterestRate, Long> {

    Page<InterestRate> findByWebLinkContainingIgnoreCase(String webLink, Pageable pageable);

    @Query("SELECT ir FROM InterestRate ir JOIN ir.fieldValues fv WHERE " +
            "LOWER(ir.webLink) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(fv.value) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<InterestRate> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(ir) FROM InterestRate ir JOIN ir.fieldValues fv WHERE " +
            "LOWER(ir.webLink) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(fv.value) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    long countBySearchTerm(@Param("searchTerm") String searchTerm);
}