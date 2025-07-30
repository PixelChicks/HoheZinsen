package com.InterestRatesAustria.InterestRatesAustria.model.dto;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.TableSection;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class TableSectionDTO {
    private Long id;
    private String title;
    private String sectionIdentifier;
    private List<MiniTableRowDTO> miniTableRows;

    public static TableSectionDTO fromEntity(TableSection entity) {
        TableSectionDTO dto = new TableSectionDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSectionIdentifier(entity.getSectionIdentifier());

        if (entity.getMiniTableRows() != null) {
            dto.setMiniTableRows(
                    entity.getMiniTableRows().stream()
                            .map(MiniTableRowDTO::fromEntity)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }
}