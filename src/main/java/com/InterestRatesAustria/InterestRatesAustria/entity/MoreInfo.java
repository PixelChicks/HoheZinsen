package com.InterestRatesAustria.InterestRatesAustria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class MoreInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String tableTitle;
    private String textTitle;
    private String textDescription;

    @OneToMany(cascade = CascadeType.ALL)
    private List<MiniTableRow> miniTableRows;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableTitle() {
        return tableTitle;
    }

    public void setTableTitle(String tableTitle) {
        this.tableTitle = tableTitle;
    }

    public String getTextTitle() {
        return textTitle;
    }

    public void setTextTitle(String textTitle) {
        this.textTitle = textTitle;
    }

    public String getTextDescription() {
        return textDescription;
    }

    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
    }

    public List<MiniTableRow> getMiniTableRows() {
        return miniTableRows;
    }

    public void setMiniTableRows(List<MiniTableRow> miniTableRows) {
        this.miniTableRows = miniTableRows;
    }
}
