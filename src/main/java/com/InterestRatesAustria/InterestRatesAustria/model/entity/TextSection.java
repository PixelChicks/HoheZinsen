package com.InterestRatesAustria.InterestRatesAustria.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "section_identifier") // e.g., "text-1", "text-2"
    private String sectionIdentifier;

    @ManyToOne
    @JoinColumn(name = "more_info_id")
    private MoreInfo moreInfo;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
