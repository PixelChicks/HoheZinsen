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
public class GlobalField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fieldKey;   // e.g. "maxDeposit"
    private String label;      // e.g. "Max Deposit"
    private Integer sortOrder; // New field for column ordering

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}