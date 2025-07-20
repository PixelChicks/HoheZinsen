package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.entity.ColumnOrder;
import com.InterestRatesAustria.InterestRatesAustria.repository.ColumnOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ColumnOrderApiController {

    @Autowired
    private ColumnOrderRepository columnOrderRepository;

    @PostMapping("/column-order")
    public ResponseEntity<?> saveOrder(@RequestBody List<String> order) {
        ColumnOrder saved = new ColumnOrder(order);
        saved.setId(1L); // use user ID if needed
        columnOrderRepository.save(saved);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/column-order")
    public ResponseEntity<List<String>> getOrder() {
        return ResponseEntity.ok(
            columnOrderRepository.findById(1L)
                .map(ColumnOrder::getColumnKeys)
                .orElse(List.of("interestRate", "duration", "provider", "paymentFrequency", "interestType", "actions"))
        );
    }
}
