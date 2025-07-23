package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.entity.GlobalField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GlobalFieldRepository extends JpaRepository<GlobalField, Long> {
    List<GlobalField> findAllByOrderBySortOrderAsc();

    @Query("SELECT COALESCE(MAX(g.sortOrder), 0) FROM GlobalField g")
    Integer findMaxSortOrder();
}