package com.InterestRatesAustria.InterestRatesAustria.model.dto;

import java.util.List;
import java.util.Map;

public class PaginatedResponse<T> {
    private List<T> content;
    private Map<Long, Map<Long, String>> rateFieldValuesMap;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean first;
    private boolean last;

    public PaginatedResponse() {}

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public Map<Long, Map<Long, String>> getRateFieldValuesMap() {
        return rateFieldValuesMap;
    }

    public void setRateFieldValuesMap(Map<Long, Map<Long, String>> rateFieldValuesMap) {
        this.rateFieldValuesMap = rateFieldValuesMap;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}