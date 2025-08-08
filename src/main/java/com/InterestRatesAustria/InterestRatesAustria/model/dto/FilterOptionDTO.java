package com.InterestRatesAustria.InterestRatesAustria.model.dto;

import lombok.Data;

@Data
public class FilterOptionDTO {
    private String value;
    private String label;
    private Long count;
    private boolean selected = false;
}