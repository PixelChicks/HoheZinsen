package com.InterestRatesAustria.InterestRatesAustria.entity;

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
        private String tableTitle;
        private String textTitle;
        private String textDescription;
        private List<String> sectionOrder;
        private List<MiniTableRowDTO> miniTableRows;

        public static MoreInfoDTO fromEntity(MoreInfo entity) {
            MoreInfoDTO dto = new MoreInfoDTO();
            dto.setTableTitle(entity.getTableTitle());
            dto.setTextTitle(entity.getTextTitle());
            dto.setTextDescription(entity.getTextDescription());
            dto.setSectionOrder(entity.getSectionOrderList());

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