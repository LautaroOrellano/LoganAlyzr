package com.loganalizyr.service;

import com.loganalizyr.model.KeywordCriteria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LogFilterCriteria {
    List<String> levels;
    List<KeywordCriteria> keywords;
    private LocalDateTime startDate;
    private LocalDateTime endDate;


    public LogFilterCriteria() {
        this.levels = new ArrayList<>();
        this.keywords = new ArrayList<>();
    }

    public LogFilterCriteria(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public List<KeywordCriteria> getKeywords() {
        return keywords != null ? keywords : Collections.emptyList();
    }

    public void setKeywords(List<KeywordCriteria> keywords) {
        this.keywords = keywords;
    }

    public List<String> getLevels() {
        return levels != null ? levels : Collections.emptyList();
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
    }

    public boolean hasLevels() {
        return levels != null && !levels.isEmpty();
    }

    public boolean hasKeywords() {
        return keywords != null && !keywords.isEmpty();
    }
}
