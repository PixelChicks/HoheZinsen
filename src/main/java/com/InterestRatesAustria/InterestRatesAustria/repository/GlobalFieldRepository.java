package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GlobalFieldRepository extends JpaRepository<GlobalField, Long> {
    @Query("SELECT g FROM GlobalField g WHERE g.deletedAt IS NULL ORDER BY g.sortOrder ASC")
    List<GlobalField> findAllActiveByOrderBySortOrderAsc();
    @Query("SELECT COALESCE(MAX(g.sortOrder), 0) FROM GlobalField g WHERE g.deletedAt IS NULL")
    Integer findMaxSortOrder();
}