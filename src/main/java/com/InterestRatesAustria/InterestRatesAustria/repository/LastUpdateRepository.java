package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.LastUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LastUpdateRepository extends JpaRepository<LastUpdate, Long> {

    Optional<LastUpdate> findFirstByOrderByIdDesc();
}