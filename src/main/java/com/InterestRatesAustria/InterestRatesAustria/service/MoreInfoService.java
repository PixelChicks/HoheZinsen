package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.*;
import com.InterestRatesAustria.InterestRatesAustria.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MoreInfoService {

    private final MoreInfoRepository moreInfoRepository;
    private final TableSectionRepository tableSectionRepository;
    private final TextSectionRepository textSectionRepository;
    private final MiniTableRowRepository miniTableRowRepository;

    public MoreInfoService(MoreInfoRepository moreInfoRepository,
                           TableSectionRepository tableSectionRepository,
                           TextSectionRepository textSectionRepository,
                           MiniTableRowRepository miniTableRowRepository) {
        this.moreInfoRepository = moreInfoRepository;
        this.tableSectionRepository = tableSectionRepository;
        this.textSectionRepository = textSectionRepository;
        this.miniTableRowRepository = miniTableRowRepository;
    }

    public void updateSectionOrder(Long rateId, List<String> sectionOrder, InterestRate rate) {
        if (rate.getMoreInfo() != null) {
            rate.getMoreInfo().setSectionOrderList(sectionOrder);
            moreInfoRepository.save(rate.getMoreInfo());
        } else {
            throw new RuntimeException("No more info found for this rate");
        }
    }

    @Transactional
    public MoreInfo createMoreInfoWithSections(Map<String, String> requestParams) {
        if (!hasAnySectionData(requestParams)) {
            return null;
        }

        MoreInfo moreInfo = new MoreInfo();
        MoreInfo savedMoreInfo = moreInfoRepository.save(moreInfo);

        List<String> sectionOrder = new ArrayList<>();
        processTableSections(requestParams, savedMoreInfo, sectionOrder);
        processTextSections(requestParams, savedMoreInfo, sectionOrder);

        savedMoreInfo.setSectionOrderList(sectionOrder);
        moreInfoRepository.save(savedMoreInfo);

        return savedMoreInfo;
    }

    @Transactional
    public MoreInfo updateMoreInfoWithSections(MoreInfo moreInfo, Map<String, String> requestParams) {
        if (!hasAnySectionData(requestParams)) {
            if (moreInfo != null) {
                clearExistingSections(moreInfo);
                moreInfoRepository.deleteById(moreInfo.getId());
            }
            return null;
        }

        if (moreInfo == null) {
            moreInfo = new MoreInfo();
            moreInfo = moreInfoRepository.save(moreInfo);
        } else {
            clearExistingSections(moreInfo);
        }

        List<String> sectionOrder = new ArrayList<>();
        processTableSections(requestParams, moreInfo, sectionOrder);
        processTextSections(requestParams, moreInfo, sectionOrder);

        return moreInfoRepository.save(moreInfo);
    }

    private boolean hasAnySectionData(Map<String, String> requestParams) {
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

    @Transactional
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

    private void processTableSections(Map<String, String> requestParams, MoreInfo moreInfo, List<String> sectionOrder) {
        Map<String, String> tableTitles = new HashMap<>();
        Map<String, String> tableLabelsString = new HashMap<>();
        Map<String, String> tableDescriptionsString = new HashMap<>();

        // Extract table data from request parameters
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("tableTitle_")) {
                String sectionId = key.replace("tableTitle_", "");
                tableTitles.put(sectionId, value);
            } else if (key.startsWith("tableRowLabels_")) {
                String sectionId = key.replace("tableRowLabels_", "");
                tableLabelsString.put(sectionId, value);
            } else if (key.startsWith("tableRowDescriptions_")) {
                String sectionId = key.replace("tableRowDescriptions_", "");
                tableDescriptionsString.put(sectionId, value);
            }
        }

        int tableIndex = 1;
        for (Map.Entry<String, String> titleEntry : tableTitles.entrySet()) {
            String sectionId = titleEntry.getKey();
            String title = titleEntry.getValue();

            if (title != null && !title.trim().isEmpty()) {
                String sectionIdentifier = "table-" + tableIndex;

                TableSection tableSection = new TableSection();
                tableSection.setTitle(title);
                tableSection.setSectionIdentifier(sectionIdentifier);
                tableSection.setMoreInfo(moreInfo);

                TableSection savedTableSection = tableSectionRepository.save(tableSection);

                String labelsString = tableLabelsString.get(sectionId);
                String descriptionsString = tableDescriptionsString.get(sectionId);

                if (labelsString != null && !labelsString.trim().isEmpty() &&
                        descriptionsString != null && !descriptionsString.trim().isEmpty()) {

                    String[] labels = labelsString.split(",");
                    String[] descriptions = descriptionsString.split(",");

                    List<MiniTableRow> miniTableRows = new ArrayList<>();
                    int maxLength = Math.max(labels.length, descriptions.length);

                    for (int i = 0; i < maxLength; i++) {
                        String label = i < labels.length ? labels[i].trim() : "";
                        String description = i < descriptions.length ? descriptions[i].trim() : "";

                        if (!label.isEmpty() || !description.isEmpty()) {
                            MiniTableRow row = new MiniTableRow();
                            row.setLabel(label);
                            row.setDescription(description);
                            row.setTableSectionId(savedTableSection.getId());

                            MiniTableRow savedRow = miniTableRowRepository.save(row);
                            miniTableRows.add(savedRow);
                        }
                    }
                    savedTableSection.setMiniTableRows(miniTableRows);
                }

                sectionOrder.add(sectionIdentifier);
                tableIndex++;
            }
        }
    }

    private void processTextSections(Map<String, String> requestParams, MoreInfo moreInfo, List<String> sectionOrder) {
        Map<String, String> textTitles = new LinkedHashMap<>();
        Map<String, String> textContents = new LinkedHashMap<>();

        // Extract text data from request parameters
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("textTitle_")) {
                String sectionId = key.replace("textTitle_", "");
                textTitles.put(sectionId, value);
            } else if (key.startsWith("textContent_")) {
                String sectionId = key.replace("textContent_", "");
                textContents.put(sectionId, value);
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

            if (title != null && !title.trim().isEmpty()) {
                String sectionIdentifier = "text-" + textIndex;

                TextSection textSection = new TextSection();
                textSection.setTitle(title);
                textSection.setSectionIdentifier(sectionIdentifier);
                textSection.setMoreInfo(moreInfo);

                String contentString = textContents.get(sectionId);
                if (contentString != null && !contentString.trim().isEmpty()) {
                    textSection.setContent(contentString);
                }

                textSectionRepository.save(textSection);
                sectionOrder.add(sectionIdentifier);
                textIndex++;
            }
        }
    }
}