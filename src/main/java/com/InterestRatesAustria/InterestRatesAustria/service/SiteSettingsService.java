package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.SiteSettings;
import com.InterestRatesAustria.InterestRatesAustria.repository.SiteSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiteSettingsService {

    private final SiteSettingsRepository siteSettingsRepository;

    public SiteSettings getSiteSettings() {
        return siteSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    SiteSettings settings = new SiteSettings();
                    return siteSettingsRepository.save(settings);
                });
    }

    @Transactional
    public SiteSettings updateSiteSettings(SiteSettings settings) {
        SiteSettings existing = getSiteSettings();
        existing.setNavCompareText(settings.getNavCompareText());
        existing.setNavFaqsText(settings.getNavFaqsText());
        existing.setCompareButtonText(settings.getCompareButtonText());
        existing.setComparisonWarningText(settings.getComparisonWarningText());
        existing.setEmptyComparisonText(settings.getEmptyComparisonText());
        existing.setCardViewText(settings.getCardViewText());
        existing.setTableViewText(settings.getTableViewText());
        existing.setLoadMoreButtonText(settings.getLoadMoreButtonText());
        existing.setScrollIndicatorText(settings.getScrollIndicatorText());
        existing.setEmptyStateTitle(settings.getEmptyStateTitle());
        existing.setEmptyStateDescription(settings.getEmptyStateDescription());
        existing.setMehrInfoButtonText(settings.getMehrInfoButtonText());
        existing.setNoInfoButtonText(settings.getNoInfoButtonText());
        existing.setZumAngebotButtonText(settings.getZumAngebotButtonText());
        existing.setVergleichenButtonText(settings.getVergleichenButtonText());
        existing.setFilterAllText(settings.getFilterAllText());
        existing.setMobileComparisonEmptyText(settings.getMobileComparisonEmptyText());
        existing.setFaqTitle(settings.getFaqTitle());
        existing.setFaqEmptyText(settings.getFaqEmptyText());
        existing.setFooterTagline(settings.getFooterTagline());
        existing.setFooterSitemapTitle(settings.getFooterSitemapTitle());
        existing.setFooterLegalTitle(settings.getFooterLegalTitle());
        existing.setFooterTermsText(settings.getFooterTermsText());
        existing.setFooterPrivacyText(settings.getFooterPrivacyText());
        existing.setFooterCopyright(settings.getFooterCopyright());
        existing.setStatusRateAdded(settings.getStatusRateAdded());
        existing.setStatusRateRemoved(settings.getStatusRateRemoved());
        existing.setStatusMaxComparisons(settings.getStatusMaxComparisons());
        existing.setStatusComparisonCleared(settings.getStatusComparisonCleared());
        existing.setStatusLoadingRateDetails(settings.getStatusLoadingRateDetails());
        existing.setStatusNoAdditionalInfo(settings.getStatusNoAdditionalInfo());

        return siteSettingsRepository.save(existing);
    }
}