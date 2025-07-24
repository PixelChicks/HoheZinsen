package com.InterestRatesAustria.InterestRatesAustria.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    private String tableTitle;
    private String textTitle;

    @Column(columnDefinition = "TEXT")
    private String textDescription;

    // Store section order - "table,text" / "text,table"
    @Column(name = "section_order")
    private String sectionOrder;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "more_info_id")
    private List<MiniTableRow> miniTableRows = new ArrayList<>();

    public List<String> getSectionOrderList() {
        if (sectionOrder == null || sectionOrder.trim().isEmpty()) {
            return List.of("table", "text"); // Default order
        }
        return List.of(sectionOrder.split(","));
    }

    public void setSectionOrderList(List<String> sections) {
        this.sectionOrder = String.join(",", sections);
    }
}