package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.HeroSection;
import com.InterestRatesAustria.InterestRatesAustria.repository.HeroSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroSectionService {

    private final HeroSectionRepository heroSectionRepository;

    public HeroSection getActiveHeroSection() {
        return heroSectionRepository.findFirstByIsActiveTrue().orElse(null);
    }

    public List<HeroSection> getAllHeroSections() {
        return heroSectionRepository.findAll();
    }

    public HeroSection getHeroSectionById(Long id) {
        return heroSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hero section not found with id: " + id));
    }

    @Transactional
    public HeroSection createHeroSection(HeroSection heroSection) {
        // If this is set as active, deactivate all others
        if (heroSection.getIsActive() != null && heroSection.getIsActive()) {
            deactivateAllHeroSections();
        }
        return heroSectionRepository.save(heroSection);
    }

    @Transactional
    public HeroSection updateHeroSection(Long id, HeroSection request) {
        HeroSection existing = getHeroSectionById(id);
        
        existing.setMainTitle(request.getMainTitle());
        existing.setSubtitle(request.getSubtitle());
        existing.setHeroImageUrl(request.getHeroImageUrl());
        
        // If setting this as active, deactivate all others first
        if (request.getIsActive() != null && request.getIsActive() && !existing.getIsActive()) {
            deactivateAllHeroSections();
        }
        
        existing.setIsActive(request.getIsActive() != null ? request.getIsActive() : false);
        
        return heroSectionRepository.save(existing);
    }

    @Transactional
    public void deleteHeroSection(Long id) {
        heroSectionRepository.deleteById(id);
    }

    @Transactional
    public void deactivateAllHeroSections() {
        List<HeroSection> allHeroSections = heroSectionRepository.findAll();
        for (HeroSection section : allHeroSections) {
            section.setIsActive(false);
        }
        heroSectionRepository.saveAll(allHeroSections);
    }
}