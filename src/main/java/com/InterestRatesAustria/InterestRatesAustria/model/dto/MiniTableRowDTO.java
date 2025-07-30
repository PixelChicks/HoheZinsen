package com.InterestRatesAustria.InterestRatesAustria.model.dto;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.MiniTableRow;
import lombok.Data;

@Data
public class MiniTableRowDTO {
    private String label;
    private String description;

    public static MiniTableRowDTO fromEntity(MiniTableRow entity) {
        MiniTableRowDTO dto = new MiniTableRowDTO();
        dto.setLabel(entity.getLabel());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}