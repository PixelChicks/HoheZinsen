package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.dto.InterestRateDTO;
import com.InterestRatesAustria.InterestRatesAustria.model.entity.*;
import com.InterestRatesAustria.InterestRatesAustria.repository.*;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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

        if (hasAnySectionData(requestParams, tableSectionData, textSectionData)) {
            MoreInfo moreInfo = new MoreInfo();
            MoreInfo savedMoreInfo = moreInfoRepository.save(moreInfo);

            List<String> sectionOrder = new ArrayList<>();

            processTableSections(requestParams, savedMoreInfo, sectionOrder);

            processTextSections(requestParams, savedMoreInfo, sectionOrder);

            savedMoreInfo.setSectionOrderList(sectionOrder);
            moreInfoRepository.save(savedMoreInfo);

            saved.setMoreInfo(savedMoreInfo);
            interestRateRepository.save(saved);
        }
    }

    private boolean hasAnySectionData(Map<String, String> requestParams,
                                      Map<String, List<String>> tableSectionData,
                                      Map<String, String> textSectionData) {
        for (String key : requestParams.keySet()) {
            if (key.startsWith("tableTitle_") && !requestParams.get(key).trim().isEmpty()) {
                return true;
            }
            if (key.startsWith("textTitle_") && !requestParams.get(key).trim().isEmpty()) {
                return true;
            }
            if (key.startsWith("textContent_") && !requestParams.get(key).trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void processTableSections(Map<String, String> requestParams, MoreInfo moreInfo, List<String> sectionOrder) {
        Map<String, String> tableTitles = new HashMap<>();
        Map<String, String> tableLabelsString = new HashMap<>();
        Map<String, String> tableDescriptionsString = new HashMap<>();

        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            System.out.println("Processing key: " + key + ", value: " + value);

            if (key.startsWith("tableTitle_")) {
                String sectionId = key.replace("tableTitle_", "");
                tableTitles.put(sectionId, value);
                System.out.println("Found table title for section " + sectionId + ": " + value);
            } else if (key.startsWith("tableRowLabels_")) {
                String sectionId = key.replace("tableRowLabels_", "");
                tableLabelsString.put(sectionId, value);
                System.out.println("Found table labels for section " + sectionId + ": " + value);
            } else if (key.startsWith("tableRowDescriptions_")) {
                String sectionId = key.replace("tableRowDescriptions_", "");
                tableDescriptionsString.put(sectionId, value);
                System.out.println("Found table descriptions for section " + sectionId + ": " + value);
            }
        }

        int tableIndex = 1;
        for (Map.Entry<String, String> titleEntry : tableTitles.entrySet()) {
            String sectionId = titleEntry.getKey();
            String title = titleEntry.getValue();

            System.out.println("Creating table section for sectionId: " + sectionId + ", title: " + title);

            if (title != null && !title.trim().isEmpty()) {
                String sectionIdentifier = "table-" + tableIndex;

                TableSection tableSection = new TableSection();
                tableSection.setTitle(title);
                tableSection.setSectionIdentifier(sectionIdentifier);
                tableSection.setMoreInfo(moreInfo);

                TableSection savedTableSection = tableSectionRepository.save(tableSection);
                System.out.println("Saved table section with ID: " + savedTableSection.getId());

                String labelsString = tableLabelsString.get(sectionId);
                String descriptionsString = tableDescriptionsString.get(sectionId);

                System.out.println("Labels string: " + labelsString);
                System.out.println("Descriptions string: " + descriptionsString);

                if (labelsString != null && !labelsString.trim().isEmpty() &&
                        descriptionsString != null && !descriptionsString.trim().isEmpty()) {

                    String[] labels = labelsString.split(",");
                    String[] descriptions = descriptionsString.split(",");

                    System.out.println("Split labels array length: " + labels.length);
                    System.out.println("Split descriptions array length: " + descriptions.length);

                    List<MiniTableRow> miniTableRows = new ArrayList<>();
                    int maxLength = Math.max(labels.length, descriptions.length);

                    for (int i = 0; i < maxLength; i++) {
                        String label = i < labels.length ? labels[i].trim() : "";
                        String description = i < descriptions.length ? descriptions[i].trim() : "";

                        System.out.println("Processing row " + i + " - Label: '" + label + "', Description: '" + description + "'");

                        if (!label.isEmpty() || !description.isEmpty()) {
                            MiniTableRow row = new MiniTableRow();
                            row.setLabel(label);
                            row.setDescription(description);
                            row.setTableSectionId(savedTableSection.getId());

                            MiniTableRow savedRow = miniTableRowRepository.save(row);
                            miniTableRows.add(savedRow);

                            System.out.println("Saved mini table row with ID: " + savedRow.getId() +
                                    ", Label: '" + savedRow.getLabel() +
                                    "', Description: '" + savedRow.getDescription() + "'");
                        }
                    }
                    savedTableSection.setMiniTableRows(miniTableRows);
                } else {
                    System.out.println("No valid labels or descriptions found for section: " + sectionId);
                }

                sectionOrder.add(sectionIdentifier);
                tableIndex++;
            }
        }
    }

    @Transactional
    public void updateInterestRate(Long id, InterestRate updatedRate, Map<String, String> requestParams,
                                   Map<String, List<String>> tableSectionData,
                                   Map<String, String> textSectionData) {
        InterestRate existingRate = getInterestRateById(id);

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

        MoreInfo moreInfo = existingRate.getMoreInfo();

        if (hasAnySectionData(requestParams, tableSectionData, textSectionData)) {
            if (moreInfo == null) {
                moreInfo = new MoreInfo();
                moreInfo = moreInfoRepository.save(moreInfo);
                existingRate.setMoreInfo(moreInfo);
            } else {
                clearExistingSections(moreInfo);
            }

            List<String> sectionOrder = new ArrayList<>();

            processTableSections(requestParams, moreInfo, sectionOrder);
            processTextSections(requestParams, moreInfo, sectionOrder);

            moreInfo = moreInfoRepository.save(moreInfo);

            existingRate.setMoreInfo(moreInfo);
            interestRateRepository.save(existingRate);
        } else if (moreInfo != null) {
            existingRate.setMoreInfo(null);
            interestRateRepository.save(existingRate);

            clearExistingSections(moreInfo);
            moreInfoRepository.deleteById(moreInfo.getId());
        }
    }

    private void clearExistingSections(MoreInfo moreInfo) {
        if (moreInfo.getTableSections() != null) {
            moreInfo.getTableSections().clear();
        }
        if (moreInfo.getTextSections() != null) {
            moreInfo.getTextSections().clear();
        }

        moreInfoRepository.saveAndFlush(moreInfo);

        tableSectionRepository.deleteByMoreInfoId(moreInfo.getId());
        textSectionRepository.deleteByMoreInfoId(moreInfo.getId());

        tableSectionRepository.flush();
        textSectionRepository.flush();
    }

    private void processTextSections(Map<String, String> requestParams, MoreInfo moreInfo, List<String> sectionOrder) {
        Map<String, String> textTitles = new LinkedHashMap<>();
        Map<String, String> textContents = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            System.out.println("Processing key: " + key + ", value: " + value);

            if (key.startsWith("textTitle_")) {
                String sectionId = key.replace("textTitle_", "");
                textTitles.put(sectionId, value);
                System.out.println("Found text title for section " + sectionId + ": " + value);
            } else if (key.startsWith("textContent_")) {
                String sectionId = key.replace("textContent_", "");
                textContents.put(sectionId, value);
                System.out.println("Found text content for section " + sectionId + ": " + value);
            }
        }

        int textIndex = 1;
        List<TextSection> existingTextSections = textSectionRepository.findByMoreInfoId(moreInfo.getId());
        if (!existingTextSections.isEmpty()) {
            textIndex = existingTextSections.stream()
                    .mapToInt(section -> {
                        String identifier = section.getSectionIdentifier();
                        if (identifier != null && identifier.startsWith("text-")) {
                            try {
                                return Integer.parseInt(identifier.replace("text-", ""));
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        }
                        return 0;
                    })
                    .max()
                    .orElse(0) + 1;
        }

        for (Map.Entry<String, String> titleEntry : textTitles.entrySet()) {
            String sectionId = titleEntry.getKey();
            String title = titleEntry.getValue();

            System.out.println("Creating text section for sectionId: " + sectionId + ", title: " + title);

            if (title != null && !title.trim().isEmpty()) {
                String sectionIdentifier = "text-" + textIndex;

                TextSection textSection = new TextSection();
                textSection.setTitle(title);
                textSection.setSectionIdentifier(sectionIdentifier);
                textSection.setMoreInfo(moreInfo);

                TextSection savedTextSection = textSectionRepository.save(textSection);
                System.out.println("Saved text section with ID: " + savedTextSection.getId());

                String contentString = textContents.get(sectionId);
                System.out.println("Content string: " + contentString);

                if (contentString != null && !contentString.trim().isEmpty()) {
                    textSection.setContent(contentString);
                    textSectionRepository.save(textSection);
                    System.out.println("Updated text section content for ID: " + savedTextSection.getId());
                } else {
                    System.out.println("No valid content found for section: " + sectionId);
                }

                sectionOrder.add(sectionIdentifier);
                textIndex++;
            }
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

        List<TableSection> existingTables = tableSectionRepository.findByMoreInfoId(moreInfo.getId());
        int nextIndex = existingTables.size() + 1;
        String sectionIdentifier = "table-" + nextIndex;

        TableSection tableSection = new TableSection();
        tableSection.setTitle(title);
        tableSection.setSectionIdentifier(sectionIdentifier);
        tableSection.setMoreInfo(moreInfo);

        tableSectionRepository.save(tableSection);

        List<String> currentOrder = moreInfo.getSectionOrderList();
        if (currentOrder == null) {
            currentOrder = new ArrayList<>();
        }
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

        List<TextSection> existingTexts = textSectionRepository.findByMoreInfoId(moreInfo.getId());
        int nextIndex = existingTexts.size() + 1;
        String sectionIdentifier = "text-" + nextIndex;

        TextSection textSection = new TextSection();
        textSection.setTitle(title);
        textSection.setContent(content);
        textSection.setSectionIdentifier(sectionIdentifier);
        textSection.setMoreInfo(moreInfo);

        textSectionRepository.save(textSection);

        List<String> currentOrder = moreInfo.getSectionOrderList();
        if (currentOrder == null) {
            currentOrder = new ArrayList<>();
        }
        currentOrder.add(sectionIdentifier);
        moreInfo.setSectionOrderList(currentOrder);
        moreInfoRepository.save(moreInfo);
    }

    public void deleteSection(Long rateId, String sectionIdentifier){
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

        List<String> currentOrder = moreInfo.getSectionOrderList();
        if (currentOrder != null) {
            currentOrder.remove(sectionIdentifier);
            moreInfo.setSectionOrderList(currentOrder);
            moreInfoRepository.save(moreInfo);
        }
    }
}