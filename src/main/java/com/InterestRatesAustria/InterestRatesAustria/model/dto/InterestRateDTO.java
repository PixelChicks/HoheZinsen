package com.InterestRatesAustria.InterestRatesAustria.model.dto;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import lombok.Data;

@Data
public class InterestRateDTO {
    private Long id;
    private String webLink;
    private MoreInfoDTO moreInfo;

    public static InterestRateDTO fromEntity(InterestRate entity) {
        InterestRateDTO dto = new InterestRateDTO();
        dto.setId(entity.getId());
        dto.setWebLink(entity.getWebLink());

        if (entity.getMoreInfo() != null) {
            dto.setMoreInfo(MoreInfoDTO.fromEntity(entity.getMoreInfo()));
        }

        return dto;
    }
}