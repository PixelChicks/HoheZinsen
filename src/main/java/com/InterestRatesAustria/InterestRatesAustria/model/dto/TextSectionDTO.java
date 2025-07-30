package com.InterestRatesAustria.InterestRatesAustria.model.dto;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.TextSection;
import lombok.Data;

@Data
public class TextSectionDTO {
    private Long id;
    private String title;
    private String content;
    private String sectionIdentifier;

    public static TextSectionDTO fromEntity(TextSection entity) {
        TextSectionDTO dto = new TextSectionDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setSectionIdentifier(entity.getSectionIdentifier());
        return dto;
    }
}