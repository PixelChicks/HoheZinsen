package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.SectionAbout;
import com.InterestRatesAustria.InterestRatesAustria.repository.SectionAboutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionAboutService {

    private final SectionAboutRepository aboutRepository;

    public List<SectionAbout> getAllSectionAboutSections() {
        return aboutRepository.findAll();
    }

    public SectionAbout getActiveSectionAboutSection() {
        return aboutRepository.findByIsActive(true)
                .orElse(null);
    }

    public SectionAbout getSectionAboutSectionById(Long id) {
        return aboutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SectionAbout section not found with id: " + id));
    }

    @Transactional
    public SectionAbout createSectionAboutSection(SectionAbout about) {
        // If this is being set as active, deactivate all others
        if (about.getIsActive() != null && about.getIsActive()) {
            deactivateAllSections();
        }
        return aboutRepository.save(about);
    }

    @Transactional
    public SectionAbout updateSectionAboutSection(Long id, SectionAbout aboutRequest) {
        SectionAbout existingSectionAbout = getSectionAboutSectionById(id);

        existingSectionAbout.setTitle(aboutRequest.getTitle());
        existingSectionAbout.setContent(aboutRequest.getContent());
        existingSectionAbout.setIsActive(aboutRequest.getIsActive());

        // If this is being set as active, deactivate all others
        if (aboutRequest.getIsActive() != null && aboutRequest.getIsActive()) {
            deactivateAllSections();
            existingSectionAbout.setIsActive(true);
        }

        return aboutRepository.save(existingSectionAbout);
    }

    @Transactional
    public void deleteSectionAboutSection(Long id) {
        SectionAbout about = getSectionAboutSectionById(id);
        aboutRepository.delete(about);
    }

    @Transactional
    public void deactivateAllSections() {
        List<SectionAbout> allSections = aboutRepository.findAll();
        allSections.forEach(section -> section.setIsActive(false));
        aboutRepository.saveAll(allSections);
    }
}

