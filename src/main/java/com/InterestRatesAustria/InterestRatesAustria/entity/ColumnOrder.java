package com.InterestRatesAustria.InterestRatesAustria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Getter
@Setter
@ToString
public class ColumnOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ElementCollection
    private List<String> columnKeys;

    public ColumnOrder() {}
    public ColumnOrder(List<String> columnKeys) {
        this.columnKeys = columnKeys;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getColumnKeys() {
        return columnKeys;
    }

    public void setColumnKeys(List<String> columnKeys) {
        this.columnKeys = columnKeys;
    }
}
