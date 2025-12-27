package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.CarouselImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarouselImageRepository extends JpaRepository<CarouselImage, Long> {
    List<CarouselImage> findByIsActiveTrueOrderByDisplayOrderAsc();
}