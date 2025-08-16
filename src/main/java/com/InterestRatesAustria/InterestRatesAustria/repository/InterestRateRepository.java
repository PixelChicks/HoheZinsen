package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterestRateRepository extends JpaRepository<InterestRate, Long>, JpaSpecificationExecutor<InterestRate> {

    Page<InterestRate> findByWebLinkContainingIgnoreCase(String webLink, Pageable pageable);

    @Query("SELECT ir FROM InterestRate ir JOIN ir.fieldValues fv WHERE " +
            "LOWER(fv.value) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<InterestRate> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(ir) FROM InterestRate ir JOIN ir.fieldValues fv WHERE " +
            "LOWER(ir.webLink) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(fv.value) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    long countBySearchTerm(@Param("searchTerm") String searchTerm);

    @Query("SELECT DISTINCT ir FROM InterestRate ir " +
            "LEFT JOIN ir.fieldValues fv " +
            "WHERE LOWER(ir.webLink) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(fv.value) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<InterestRate> searchByWebLinkOrFieldValue(@Param("search") String search, Pageable pageable);

    @Query("SELECT ir.id, fv.value FROM InterestRate ir " +
            "LEFT JOIN ir.fieldValues fv ON fv.globalField.id = :fieldId " +
            "ORDER BY ir.id")
    List<Object[]> findRateIdAndPercentageValue(@Param("fieldId") Long fieldId);

    @Query("SELECT ir.id, fv.value FROM InterestRate ir " +
            "LEFT JOIN ir.fieldValues fv ON fv.globalField.id = :fieldId " +
            "ORDER BY ir.id")
    List<Object[]> findRateIdAndFieldValue(@Param("fieldId") Long fieldId);
}