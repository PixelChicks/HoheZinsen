package com.InterestRatesAustria.InterestRatesAustria.model.dto;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.MoreInfo;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class MoreInfoDTO {
    private List<String> sectionOrder;
    private List<TableSectionDTO> tableSections;
    private List<TextSectionDTO> textSections;

    public static MoreInfoDTO fromEntity(MoreInfo entity) {
        MoreInfoDTO dto = new MoreInfoDTO();
        dto.setSectionOrder(entity.getSectionOrderList());

        if (entity.getTableSections() != null) {
            dto.setTableSections(
                    entity.getTableSections().stream()
                            .map(TableSectionDTO::fromEntity)
                            .collect(Collectors.toList())
            );
        }

        if (entity.getTextSections() != null) {
            dto.setTextSections(
                    entity.getTextSections().stream()
                            .map(TextSectionDTO::fromEntity)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }
}