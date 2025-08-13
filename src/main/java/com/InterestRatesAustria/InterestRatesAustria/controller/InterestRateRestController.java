package com.InterestRatesAustria.InterestRatesAustria.controller;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.dto.PaginatedResponse;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.GlobalField;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.InterestRate;
import com.InterestRatesAustria.InterestRatesAustria.service.FieldValueService;
import com.InterestRatesAustria.InterestRatesAustria.service.GlobalFieldService;
import com.InterestRatesAustria.InterestRatesAustria.service.InterestRateService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class InterestRateRestController {

    private final InterestRateService interestRateService;
    private final GlobalFieldService globalFieldService;
    private final FieldValueService fieldValueService;

    public InterestRateRestController(InterestRateService interestRateService,
                                      GlobalFieldService globalFieldService,
                                      FieldValueService fieldValueService) {
        this.interestRateService = interestRateService;
        this.globalFieldService = globalFieldService;
        this.fieldValueService = fieldValueService;
    }

    @GetMapping("/interest-rates/paginated")
    public ResponseEntity<PaginatedResponse<InterestRateDTO>> getInterestRatesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        Page<InterestRate> interestRatesPage;

        if (page < 0) {
            page = 0;
        }

        if (search != null && !search.trim().isEmpty()) {
            interestRatesPage = interestRateService.searchInterestRatesPaginated(search, page, size, sortBy, sortDir);
        } else {
            interestRatesPage = interestRateService.getAllInterestRatesPaginated(page, size, sortBy, sortDir);
        }

        List<InterestRateDTO> content = interestRatesPage.getContent().stream()
                .map(InterestRateDTO::fromEntity)
                .collect(Collectors.toList());

        // Get field values for current page
        Map<Long, Map<Long, String>> rateFieldValuesMap =
                fieldValueService.getRateFieldValuesMap(interestRatesPage.getContent());

        PaginatedResponse<InterestRateDTO> response = new PaginatedResponse<>();
        response.setContent(content);
        response.setRateFieldValuesMap(rateFieldValuesMap);
        response.setCurrentPage(page);
        response.setTotalPages(interestRatesPage.getTotalPages());
        response.setTotalElements(interestRatesPage.getTotalElements());
        response.setPageSize(size);
        response.setFirst(interestRatesPage.isFirst());
        response.setLast(interestRatesPage.isLast());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/interest-rate/{id}")
    public ResponseEntity<InterestRateDTO> getInterestRateById(@PathVariable Long id) {
        try {
            InterestRate interestRate = interestRateService.getInterestRateById(id);
            InterestRateDTO dto = InterestRateDTO.fromEntity(interestRate);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/global-fields")
    public ResponseEntity<List<GlobalField>> getGlobalFields() {
        return ResponseEntity.ok(globalFieldService.getAllGlobalFieldsOrdered());
    }
}