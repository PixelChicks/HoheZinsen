package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {

    List<FAQ> findAllByOrderByDisplayOrderAsc();

    List<FAQ> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT MAX(f.displayOrder) FROM FAQ f")
    Integer findMaxDisplayOrder();
}