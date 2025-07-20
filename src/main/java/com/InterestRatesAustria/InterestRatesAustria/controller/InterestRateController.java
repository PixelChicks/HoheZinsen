package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.entity.ColumnLabel;
import com.InterestRatesAustria.InterestRatesAustria.entity.ColumnOrder;
import com.InterestRatesAustria.InterestRatesAustria.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.repository.ColumnLabelRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.ColumnOrderRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.InterestRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class InterestRateController {

    @Autowired
    private InterestRateRepository rateRepo;
    @Autowired
    private ColumnLabelRepository columnLabelRepo;

    @Autowired
    private ColumnOrderRepository columnOrderRepo;

    @GetMapping("/")
    public String index(Model model) {
        List<InterestRate> rates = rateRepo.findAll();

        List<String> columnOrder = columnOrderRepo.findById(1L)
                .map(ColumnOrder::getColumnKeys)
                .orElse(List.of("interestRate", "duration", "provider", "paymentFrequency", "interestType", "actions"));

        Map<String, String> columnLabels = columnLabelRepo.findAll()
                .stream()
                .collect(Collectors.toMap(ColumnLabel::getColumnKey, ColumnLabel::getLabel));

        model.addAttribute("interestRates", rates);
        model.addAttribute("columnOrder", columnOrder);
        model.addAttribute("columnLabels", columnLabels);
        return "index";
    }


}

