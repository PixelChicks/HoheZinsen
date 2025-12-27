package com.InterestRatesAustria.InterestRatesAustria.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "site_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Navigation
    @Column(length = 100)
    private String navCompareText = "Compare";

    @Column(length = 100)
    private String navFaqsText = "FAQs";

    // Comparison Section
    @Column(length = 200)
    private String compareButtonText = "Vergleichen";

    @Column(length = 200)
    private String comparisonWarningText = "Sie können maximal 3 Zinssätze gleichzeitig vergleichen. Entfernen Sie einen Vergleich, um einen neuen hinzuzufügen.";

    @Column(length = 200)
    private String emptyComparisonText = "click a provider from the table below to add to compare";

    // Table Section
    @Column(length = 100)
    private String cardViewText = "Card View";

    @Column(length = 100)
    private String tableViewText = "Table View";

    @Column(length = 100)
    private String loadMoreButtonText = "Load More";

    @Column(length = 200)
    private String scrollIndicatorText = "← Scroll horizontally to see more →";

    @Column(length = 200)
    private String emptyStateTitle = "No interest rates found";

    @Column(length = 300)
    private String emptyStateDescription = "Try adjusting your search criteria or add a new interest rate.";

    // Buttons
    @Column(length = 100)
    private String mehrInfoButtonText = "MEHR INFO";

    @Column(length = 100)
    private String noInfoButtonText = "No Info";

    @Column(length = 100)
    private String zumAngebotButtonText = "ZUM ANGEBOT";

    @Column(length = 100)
    private String vergleichenButtonText = "VERGLEICHEN";

    // Filter Section
    @Column(length = 100)
    private String filterAllText = "Alle";

    // Mobile Comparison
    @Column(length = 200)
    private String mobileComparisonEmptyText = "Click a provider<br>to compare";

    // FAQ Section
    @Column(length = 200)
    private String faqTitle = "FAQs";

    @Column(length = 200)
    private String faqEmptyText = "No FAQs available at the moment.";

    // Footer
    @Column(length = 500)
    private String footerTagline = "Die aktuell höchsten Zinsangebote für Ihr Erspartes – transparent und objektiv.";

    @Column(length = 100)
    private String footerSitemapTitle = "Sitemap";

    @Column(length = 100)
    private String footerLegalTitle = "Legal";

    @Column(length = 100)
    private String footerTermsText = "Terms and Conditions";

    @Column(length = 100)
    private String footerPrivacyText = "Privacy Policy";

    @Column(length = 200)
    private String footerCopyright = "© 2025 HoheZinsen.at | All rights reserved.";

    // Status Messages
    @Column(length = 200)
    private String statusRateAdded = "added to comparison";

    @Column(length = 200)
    private String statusRateRemoved = "Aus dem Vergleich entfernt";

    @Column(length = 200)
    private String statusMaxComparisons = "Maximum number of comparisons reached";

    @Column(length = 200)
    private String statusComparisonCleared = "All comparisons cleared";

    @Column(length = 200)
    private String statusLoadingRateDetails = "Rate data not found locally, fetching details...";

    @Column(length = 200)
    private String statusNoAdditionalInfo = "No additional information available for this rate.";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}