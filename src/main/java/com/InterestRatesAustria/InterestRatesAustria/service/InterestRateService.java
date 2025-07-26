package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.*;
import com.InterestRatesAustria.InterestRatesAustria.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InterestRateService {

    private final InterestRateRepository interestRateRepository;
    private final GlobalFieldRepository globalFieldRepository;
    private final InterestRateFieldValueRepository fieldValueRepository;
    private final MoreInfoRepository moreInfoRepository;
    private final MiniTableRowRepository miniTableRowRepository;
    private final TableSectionRepository tableSectionRepository;
    private final TextSectionRepository textSectionRepository;

    public InterestRateService(InterestRateRepository interestRateRepository,
                               GlobalFieldRepository globalFieldRepository,
                               InterestRateFieldValueRepository fieldValueRepository,
                               MoreInfoRepository moreInfoRepository,
                               MiniTableRowRepository miniTableRowRepository,
                               TableSectionRepository tableSectionRepository,
                               TextSectionRepository textSectionRepository) {
        this.interestRateRepository = interestRateRepository;
        this.globalFieldRepository = globalFieldRepository;
        this.fieldValueRepository = fieldValueRepository;
        this.moreInfoRepository = moreInfoRepository;
        this.miniTableRowRepository = miniTableRowRepository;
        this.tableSectionRepository = tableSectionRepository;
        this.textSectionRepository = textSectionRepository;
    }

    public List<InterestRateDTO> getAllInterestRateDTOs() {
        return interestRateRepository.findAll().stream().map(InterestRateDTO::fromEntity).collect(Collectors.toList());
    }

    public List<InterestRate> getAllInterestRates() {
        return interestRateRepository.findAll();
    }

    public InterestRate getInterestRateById(Long id) {
        return interestRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interest rate not found with id: " + id));
    }

    public Map<Long, String> getFieldValuesForRate(Long rateId) {
        InterestRate rate = getInterestRateById(rateId);
        return rate.getFieldValues().stream()
                .collect(Collectors.toMap(
                        fv -> fv.getGlobalField().getId(),
                        InterestRateFieldValue::getValue
                ));
    }

    public List<GlobalField> getAllGlobalFieldsOrdered() {
        return globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
    }

    public Map<Long, Map<Long, String>> getRateFieldValuesMap(List<InterestRate> rates) {
        Map<Long, Map<Long, String>> result = new HashMap<>();

        for (InterestRate rate : rates) {
            Map<Long, String> fieldValues = rate.getFieldValues().stream().collect(Collectors.toMap(fv -> fv.getGlobalField().getId(), InterestRateFieldValue::getValue));
            result.put(rate.getId(), fieldValues);
        }
        return result;
    }

    public void addGlobalField(GlobalField field) {
        Integer maxSortOrder = globalFieldRepository.findMaxSortOrder();
        field.setSortOrder(maxSortOrder + 1);
        field.setFieldKey(field.getLabel().toLowerCase().replaceAll("\\s+", ""));
        GlobalField savedField = globalFieldRepository.save(field);

        interestRateRepository.findAll().forEach(rate -> {
            InterestRateFieldValue fv = new InterestRateFieldValue();
            fv.setInterestRate(rate);
            fv.setGlobalField(savedField);
            fv.setValue("");
            fieldValueRepository.save(fv);
        });
    }

    public void updateGlobalField(Long fieldId, String label) {
        GlobalField field = globalFieldRepository.findById(fieldId).orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));

        field.setLabel(label);
        field.setFieldKey(label.toLowerCase().replaceAll("\\s+", ""));
        globalFieldRepository.save(field);
    }

    public void reorderGlobalFields(List<Long> fieldIds) {
        for (int i = 0; i < fieldIds.size(); i++) {
            Long fieldId = fieldIds.get(i);
            GlobalField field = globalFieldRepository.findById(fieldId).orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
            field.setSortOrder(i + 1);
            globalFieldRepository.save(field);
        }
    }

    public void updateSectionOrder(Long rateId, List<String> sectionOrder) {
        InterestRate rate = interestRateRepository.findById(rateId).orElseThrow(() -> new RuntimeException("Interest rate not found with id: " + rateId));

        if (rate.getMoreInfo() != null) {
            rate.getMoreInfo().setSectionOrderList(sectionOrder);
            moreInfoRepository.save(rate.getMoreInfo());
        } else {
            throw new RuntimeException("No more info found for this rate");
        }
    }

    public void updateFieldValue(Long rateId, Long fieldId, String value) {
        InterestRateFieldValue fieldValue = fieldValueRepository.findByInterestRateIdAndGlobalFieldId(rateId, fieldId).orElseThrow();

        fieldValue.setValue(value);
        fieldValueRepository.save(fieldValue);
    }

    @Transactional
    public void deleteInterestRate(Long id) {
        InterestRate rate = getInterestRateById(id);

        fieldValueRepository.deleteAll(rate.getFieldValues());

        if (rate.getMoreInfo() != null) {
            moreInfoRepository.delete(rate.getMoreInfo());
        }

        interestRateRepository.delete(rate);
    }

    private List<MiniTableRow> getMiniTableRows(List<String> tableRowLabels, List<String> tableRowDescriptions) {
        List<MiniTableRow> miniTableRows = new ArrayList<>();

        if (tableRowLabels != null && tableRowDescriptions != null) {
            int size = Math.min(tableRowLabels.size(), tableRowDescriptions.size());
            for (int i = 0; i < size; i++) {
                String label = tableRowLabels.get(i).trim();
                String desc = tableRowDescriptions.get(i).trim();
                if (!label.isEmpty() || !desc.isEmpty()) {
                    MiniTableRow row = new MiniTableRow();
                    row.setLabel(label);
                    row.setDescription(desc);
                    miniTableRows.add(row);
                }
            }
        }

        return miniTableRows;
    }

    public void deleteGlobalField(Long fieldId) {
        GlobalField field = globalFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
        field.setDeletedAt(LocalDateTime.now());
        globalFieldRepository.save(field);
    }

    @Transactional
    public void createInterestRate(InterestRate interestRate, Map<String, String> requestParams,
                                   Map<String, List<String>> tableSectionData,
                                   Map<String, String> textSectionData) {
        InterestRate saved = interestRateRepository.save(interestRate);

        // Handle basic field values
        List<GlobalField> allFields = globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
        for (GlobalField field : allFields) {
            String key = "extra_" + field.getId();
            if (requestParams.containsKey(key)) {
                String value = requestParams.get(key);
                InterestRateFieldValue fv = new InterestRateFieldValue();
                fv.setInterestRate(saved);
                fv.setGlobalField(field);
                fv.setValue(value);
                fieldValueRepository.save(fv);
            }
        }

        // Handle multiple sections
        if (!tableSectionData.isEmpty() || !textSectionData.isEmpty()) {
            MoreInfo moreInfo = new MoreInfo();

            // Save MoreInfo first to get ID
            MoreInfo savedMoreInfo = moreInfoRepository.save(moreInfo);

            List<String> sectionOrder = new ArrayList<>();

            // Create table sections
            int tableIndex = 1;
            for (Map.Entry<String, List<String>> entry : tableSectionData.entrySet()) {
                if (entry.getKey().startsWith("tableTitle_")) {
                    String sectionId = entry.getKey().replace("tableTitle_", "");
                    String title = entry.getValue().get(0);

                    if (title != null && !title.trim().isEmpty()) {
                        String sectionIdentifier = "table-" + tableIndex;

                        TableSection tableSection = new TableSection();
                        tableSection.setTitle(title);
                        tableSection.setSectionIdentifier(sectionIdentifier);
                        tableSection.setMoreInfo(savedMoreInfo);

                        // Add table rows
                        List<String> labels = tableSectionData.get("tableRowLabels_" + sectionId);
                        List<String> descriptions = tableSectionData.get("tableRowDescriptions_" + sectionId);

                        if (labels != null && descriptions != null) {
                            List<MiniTableRow> miniTableRows = createMiniTableRows(labels, descriptions);
                            tableSection.setMiniTableRows(miniTableRows);
                        }

                        tableSectionRepository.save(tableSection);
                        sectionOrder.add(sectionIdentifier);
                        tableIndex++;
                    }
                }
            }

            // Create text sections
            int textIndex = 1;
            for (Map.Entry<String, String> entry : textSectionData.entrySet()) {
                if (entry.getKey().startsWith("textTitle_")) {
                    String sectionId = entry.getKey().replace("textTitle_", "");
                    String title = entry.getValue();
                    String content = textSectionData.get("textContent_" + sectionId);

                    if ((title != null && !title.trim().isEmpty()) ||
                            (content != null && !content.trim().isEmpty())) {
                        String sectionIdentifier = "text-" + textIndex;

                        TextSection textSection = new TextSection();
                        textSection.setTitle(title);
                        textSection.setContent(content);
                        textSection.setSectionIdentifier(sectionIdentifier);
                        textSection.setMoreInfo(savedMoreInfo);

                        textSectionRepository.save(textSection);
                        sectionOrder.add(sectionIdentifier);
                        textIndex++;
                    }
                }
            }

            // Update section order
            savedMoreInfo.setSectionOrderList(sectionOrder);
            moreInfoRepository.save(savedMoreInfo);

            saved.setMoreInfo(savedMoreInfo);
            interestRateRepository.save(saved);
        }
    }

    @Transactional
    public void updateInterestRate(Long id, InterestRate updatedRate, Map<String, String> requestParams,
                                   Map<String, List<String>> tableSectionData,
                                   Map<String, String> textSectionData) {
        InterestRate existingRate = getInterestRateById(id);

        // Update basic field values
        List<GlobalField> allFields = globalFieldRepository.findAllActiveByOrderBySortOrderAsc();
        for (GlobalField field : allFields) {
            String key = "extra_" + field.getId();
            if (requestParams.containsKey(key)) {
                String value = requestParams.get(key);
                InterestRateFieldValue fieldValue = fieldValueRepository
                        .findByInterestRateIdAndGlobalFieldId(id, field.getId())
                        .orElse(new InterestRateFieldValue());
                fieldValue.setInterestRate(existingRate);
                fieldValue.setGlobalField(field);
                fieldValue.setValue(value);
                fieldValueRepository.save(fieldValue);
            }
        }

        // Handle sections update
        MoreInfo moreInfo = existingRate.getMoreInfo();

        if (!tableSectionData.isEmpty() || !textSectionData.isEmpty()) {
            if (moreInfo == null) {
                moreInfo = new MoreInfo();
                moreInfo = moreInfoRepository.save(moreInfo);
            } else {
                // Clear existing sections
                tableSectionRepository.deleteByMoreInfoId(moreInfo.getId());
                textSectionRepository.deleteByMoreInfoId(moreInfo.getId());
            }

            // Recreate sections (similar to create method)
            // ... (implementation similar to createInterestRate)

            existingRate.setMoreInfo(moreInfo);
            interestRateRepository.save(existingRate);
        } else if (moreInfo != null) {
            // Remove all sections if none provided
            Long moreInfoId = moreInfo.getId();
            existingRate.setMoreInfo(null);
            interestRateRepository.save(existingRate);
            moreInfoRepository.deleteById(moreInfoId);
        }
    }

    public void addTableSection(Long rateId, String title) {
        InterestRate rate = getInterestRateById(rateId);
        MoreInfo moreInfo = rate.getMoreInfo();

        if (moreInfo == null) {
            moreInfo = new MoreInfo();
            moreInfo = moreInfoRepository.save(moreInfo);
            rate.setMoreInfo(moreInfo);
            interestRateRepository.save(rate);
        }

        // Generate next table identifier
        List<TableSection> existingTables = tableSectionRepository.findByMoreInfoId(moreInfo.getId());
        int nextIndex = existingTables.size() + 1;
        String sectionIdentifier = "table-" + nextIndex;

        TableSection tableSection = new TableSection();
        tableSection.setTitle(title);
        tableSection.setSectionIdentifier(sectionIdentifier);
        tableSection.setMoreInfo(moreInfo);

        tableSectionRepository.save(tableSection);

        // Update section order
        List<String> currentOrder = moreInfo.getSectionOrderList();
        currentOrder.add(sectionIdentifier);
        moreInfo.setSectionOrderList(currentOrder);
        moreInfoRepository.save(moreInfo);
    }

    public void addTextSection(Long rateId, String title, String content) {
        InterestRate rate = getInterestRateById(rateId);
        MoreInfo moreInfo = rate.getMoreInfo();

        if (moreInfo == null) {
            moreInfo = new MoreInfo();
            moreInfo = moreInfoRepository.save(moreInfo);
            rate.setMoreInfo(moreInfo);
            interestRateRepository.save(rate);
        }

        // Generate next text identifier
        List<TextSection> existingTexts = textSectionRepository.findByMoreInfoId(moreInfo.getId());
        int nextIndex = existingTexts.size() + 1;
        String sectionIdentifier = "text-" + nextIndex;

        TextSection textSection = new TextSection();
        textSection.setTitle(title);
        textSection.setContent(content);
        textSection.setSectionIdentifier(sectionIdentifier);
        textSection.setMoreInfo(moreInfo);

        textSectionRepository.save(textSection);

        // Update section order
        List<String> currentOrder = moreInfo.getSectionOrderList();
        currentOrder.add(sectionIdentifier);
        moreInfo.setSectionOrderList(currentOrder);
        moreInfoRepository.save(moreInfo);
    }

    public void deleteSection(Long rateId, String sectionIdentifier) {
        InterestRate rate = getInterestRateById(rateId);
        MoreInfo moreInfo = rate.getMoreInfo();

        if (moreInfo == null) return;

        if (sectionIdentifier.startsWith("table-")) {
            tableSectionRepository.findBySectionIdentifier(sectionIdentifier)
                    .ifPresent(tableSectionRepository::delete);
        } else if (sectionIdentifier.startsWith("text-")) {
            textSectionRepository.findBySectionIdentifier(sectionIdentifier)
                    .ifPresent(textSectionRepository::delete);
        }

        // Update section order
        List<String> currentOrder = moreInfo.getSectionOrderList();
        currentOrder.remove(sectionIdentifier);
        moreInfo.setSectionOrderList(currentOrder);
        moreInfoRepository.save(moreInfo);
    }

    private List<MiniTableRow> createMiniTableRows(List<String> labels, List<String> descriptions) {
        List<MiniTableRow> miniTableRows = new ArrayList<>();

        if (labels != null && descriptions != null) {
            int size = Math.min(labels.size(), descriptions.size());
            for (int i = 0; i < size; i++) {
                String label = labels.get(i).trim();
                String desc = descriptions.get(i).trim();
                if (!label.isEmpty() || !desc.isEmpty()) {
                    MiniTableRow row = new MiniTableRow();
                    row.setLabel(label);
                    row.setDescription(desc);
                    miniTableRows.add(row);
                }
            }
        }

        return miniTableRows;
    }
}