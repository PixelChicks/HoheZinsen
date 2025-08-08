package com.InterestRatesAustria.InterestRatesAustria.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class FilterDTO {
    private Long fieldId;
    private String label;
    private String fieldKey;
    private List<FilterOptionDTO> options;
}