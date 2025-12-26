package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.FAQ;
import com.InterestRatesAustria.InterestRatesAustria.repository.FAQRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FAQService {

    private final FAQRepository faqRepository;

    public List<FAQ> getAllFAQs() {
        return faqRepository.findAllByOrderByDisplayOrderAsc();
    }

    public List<FAQ> getActiveFAQs() {
        return faqRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public FAQ getFAQById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
    }

    @Transactional
    public FAQ createFAQ(FAQ faq) {
        if (faq.getDisplayOrder() == null) {
            Integer maxOrder = faqRepository.findMaxDisplayOrder();
            faq.setDisplayOrder(maxOrder != null ? maxOrder + 1 : 0);
        }

        if (faq.getIsActive() == null) {
            faq.setIsActive(true);
        }

        return faqRepository.save(faq);
    }

    @Transactional
    public FAQ updateFAQ(Long id, FAQ updateRequest) {
        FAQ existingFaq = getFAQById(id);

        existingFaq.setQuestion(updateRequest.getQuestion());
        existingFaq.setAnswer(updateRequest.getAnswer());
        existingFaq.setDisplayOrder(updateRequest.getDisplayOrder());
        existingFaq.setIsActive(updateRequest.getIsActive());

        return faqRepository.save(existingFaq);
    }

    @Transactional
    public void deleteFAQ(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("FAQ not found with id: " + id);
        }
        faqRepository.deleteById(id);
    }

    @Transactional
    public FAQ toggleActiveStatus(Long id) {
        FAQ faq = getFAQById(id);
        faq.setIsActive(!faq.getIsActive());
        return faqRepository.save(faq);
    }
}