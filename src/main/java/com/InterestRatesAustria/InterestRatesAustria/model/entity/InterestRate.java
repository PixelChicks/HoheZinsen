package com.InterestRatesAustria.InterestRatesAustria.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "more_info_id")
    private MoreInfo moreInfo;

    private String webLink;

    @OneToMany(mappedBy = "interestRate", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InterestRateFieldValue> fieldValues = new ArrayList<>();
}