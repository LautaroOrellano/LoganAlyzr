package com.loganalyzr.infrastructure.config.dto;

import com.loganalyzr.core.model.MatchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterRulesDTO {
    List<String> levels;
    List<KeywordCriteriaDTO> keywords;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private MatchMode matchMode;

    public FilterRulesDTO() {
        this.levels = new ArrayList<>();
        this.keywords = new ArrayList<>();
    }

    public FilterRulesDTO(LocalDateTime startDate, LocalDateTime endDate) {
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

    public List<KeywordCriteriaDTO> getKeywords() {
        return keywords != null ? keywords : Collections.emptyList();
    }

    public void setKeywords(List<KeywordCriteriaDTO> keywords) {
        this.keywords = keywords;
    }

    public List<String> getLevels() {
        return levels != null ? levels : Collections.emptyList();
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
    }

    public MatchMode getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(MatchMode matchMode) {
        this.matchMode = matchMode;
    }

    public boolean hasLevels() {
        return levels != null && !levels.isEmpty();
    }

    public boolean hasKeywords() {
        return keywords != null && !keywords.isEmpty();
    }
}
