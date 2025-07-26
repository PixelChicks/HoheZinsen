package com.InterestRatesAustria.InterestRatesAustria.model.dto;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.*;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class InterestRateDTO {
    private Long id;
    private MoreInfoDTO moreInfo;

    public static InterestRateDTO fromEntity(InterestRate entity) {
        InterestRateDTO dto = new InterestRateDTO();
        dto.setId(entity.getId());

        if (entity.getMoreInfo() != null) {
            dto.setMoreInfo(MoreInfoDTO.fromEntity(entity.getMoreInfo()));
        }

        return dto;
    }

    @Data
    public static class MoreInfoDTO {
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

    @Data
    public static class TableSectionDTO {
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

    @Data
    public static class TextSectionDTO {
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

    @Data
    public static class MiniTableRowDTO {
        private String label;
        private String description;

        public static MiniTableRowDTO fromEntity(MiniTableRow entity) {
            MiniTableRowDTO dto = new MiniTableRowDTO();
            dto.setLabel(entity.getLabel());
            dto.setDescription(entity.getDescription());
            return dto;
        }
    }
}