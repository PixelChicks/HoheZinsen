package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.About;
import com.InterestRatesAustria.InterestRatesAustria.repository.AboutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AboutService {

    private final AboutRepository aboutRepository;

    public List<About> getAllAboutSections() {
        return aboutRepository.findAll();
    }

    public About getActiveAboutSection() {
        return aboutRepository.findByIsActive(true)
                .orElse(null);
    }

    public About getAboutSectionById(Long id) {
        return aboutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("About section not found with id: " + id));
    }

    @Transactional
    public About createAboutSection(About about) {
        // If this is being set as active, deactivate all others
        if (about.getIsActive() != null && about.getIsActive()) {
            deactivateAllSections();
        }
        return aboutRepository.save(about);
    }

    @Transactional
    public About updateAboutSection(Long id, About aboutRequest) {
        About existingAbout = getAboutSectionById(id);

        existingAbout.setTitle(aboutRequest.getTitle());
        existingAbout.setContent(aboutRequest.getContent());
        existingAbout.setIsActive(aboutRequest.getIsActive());

        // If this is being set as active, deactivate all others
        if (aboutRequest.getIsActive() != null && aboutRequest.getIsActive()) {
            deactivateAllSections();
            existingAbout.setIsActive(true);
        }

        return aboutRepository.save(existingAbout);
    }

    @Transactional
    public void deleteAboutSection(Long id) {
        About about = getAboutSectionById(id);
        aboutRepository.delete(about);
    }

    @Transactional
    public void deactivateAllSections() {
        List<About> allSections = aboutRepository.findAll();
        allSections.forEach(section -> section.setIsActive(false));
        aboutRepository.saveAll(allSections);
    }
}

