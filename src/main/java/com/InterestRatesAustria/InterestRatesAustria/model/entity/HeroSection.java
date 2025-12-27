package com.InterestRatesAustria.InterestRatesAustria.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hero_sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeroSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String mainTitle;

    @Column(nullable = false, length = 1000)
    private String subtitle;

    @Column(length = 500)
    private String heroImageUrl;

    @Column(nullable = false)
    private Boolean isActive = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}