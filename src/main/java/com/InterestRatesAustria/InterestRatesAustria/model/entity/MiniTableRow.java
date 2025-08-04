package com.InterestRatesAustria.InterestRatesAustria.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiniTableRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String label;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "table_section_id")
    private Long tableSectionId; // Foreign key to TableSection
}