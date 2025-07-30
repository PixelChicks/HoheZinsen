package com.InterestRatesAustria.InterestRatesAustria.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    @Column(name = "section_identifier") // e.g., "table-1", "table-2"
    private String sectionIdentifier;

    @ManyToOne
    @JoinColumn(name = "more_info_id")
    private MoreInfo moreInfo;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "table_section_id")
    private List<MiniTableRow> miniTableRows = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
