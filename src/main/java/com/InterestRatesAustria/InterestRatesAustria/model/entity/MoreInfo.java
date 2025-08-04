package com.InterestRatesAustria.InterestRatesAustria.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoreInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Store section order as JSON-like string: "table-1,text-1,table-2,text-2"
    @Column(name = "section_order", columnDefinition = "TEXT")
    private String sectionOrder;

    @OneToMany(mappedBy = "moreInfo", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TableSection> tableSections = new ArrayList<>();

    @OneToMany(mappedBy = "moreInfo", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TextSection> textSections = new ArrayList<>();

    private static final String SECTION_ORDER_DELIMITER = "||";

    public List<String> getSectionOrderList() {
        if (sectionOrder == null || sectionOrder.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(sectionOrder.split(Pattern.quote(SECTION_ORDER_DELIMITER)));
    }

    public void setSectionOrderList(List<String> sections) {
        this.sectionOrder = String.join(SECTION_ORDER_DELIMITER, sections);
    }
}