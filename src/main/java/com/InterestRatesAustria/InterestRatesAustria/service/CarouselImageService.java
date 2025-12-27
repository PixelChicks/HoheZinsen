package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.CarouselImage;
import com.InterestRatesAustria.InterestRatesAustria.repository.CarouselImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarouselImageService {

    private final CarouselImageRepository carouselImageRepository;

    public List<CarouselImage> getActiveCarouselImages() {
        return carouselImageRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public List<CarouselImage> getAllCarouselImages() {
        return carouselImageRepository.findAll();
    }

    public CarouselImage getCarouselImageById(Long id) {
        return carouselImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carousel image not found with id: " + id));
    }

    @Transactional
    public CarouselImage createCarouselImage(CarouselImage carouselImage) {
        // If no display order is set, set it to the next highest number
        if (carouselImage.getDisplayOrder() == null || carouselImage.getDisplayOrder() == 0) {
            List<CarouselImage> existing = carouselImageRepository.findAll();
            int maxOrder = existing.stream()
                    .mapToInt(CarouselImage::getDisplayOrder)
                    .max()
                    .orElse(0);
            carouselImage.setDisplayOrder(maxOrder + 1);
        }
        return carouselImageRepository.save(carouselImage);
    }

    @Transactional
    public CarouselImage updateCarouselImage(Long id, CarouselImage request) {
        CarouselImage existing = getCarouselImageById(id);
        
        existing.setImageUrl(request.getImageUrl());
        existing.setAltText(request.getAltText());
        existing.setDisplayOrder(request.getDisplayOrder());
        existing.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return carouselImageRepository.save(existing);
    }

    @Transactional
    public void deleteCarouselImage(Long id) {
        carouselImageRepository.deleteById(id);
    }

    @Transactional
    public void reorderCarouselImages(List<Long> orderedIds) {
        for (int i = 0; i < orderedIds.size(); i++) {
            Long id = orderedIds.get(i);
            CarouselImage image = carouselImageRepository.findById(id).orElse(null);
            if (image != null) {
                image.setDisplayOrder(i + 1);
                carouselImageRepository.save(image);
            }
        }
    }
}