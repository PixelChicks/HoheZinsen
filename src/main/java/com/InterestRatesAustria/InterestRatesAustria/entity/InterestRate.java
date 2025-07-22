package com.InterestRatesAustria.InterestRatesAustria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String interestRate;
    private String duration;
    private String provider;
    private String paymentFrequency;
    private String interestType;

    @OneToOne(cascade = CascadeType.ALL)
    private MoreInfo moreInfo;

    @OneToMany(mappedBy = "interestRate", cascade = CascadeType.ALL)
    private List<InterestRateFieldValue> fieldValues = new ArrayList<>();
}
