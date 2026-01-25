package com.loganalyzr.infrastructure.config.dto;

import com.loganalyzr.core.model.MatchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterRulesDTO {
    private List<String> levels;
    private List<KeywordCriteriaDTO> keywords;
    private DateRangeDTO dateRange;
    private MatchMode matchMode;

    public FilterRulesDTO() {
    }

    public List<String> getLevels() {
        return levels != null ? levels : Collections.emptyList();
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
    }

    public List<KeywordCriteriaDTO> getKeywords() {
        return keywords != null ? keywords : Collections.emptyList();
    }

    public void setKeywords(List<KeywordCriteriaDTO> keywords) {
        this.keywords = keywords;
    }

    public MatchMode getMatchMode() {
        return matchMode != null ? matchMode : matchMode.ALL;
    }

    public void setMatchMode(MatchMode matchMode) {
        this.matchMode = matchMode;
    }

    public DateRangeDTO getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRangeDTO dateRange) {
        this.dateRange = dateRange;
    }

    public boolean hasLevels() {
        return levels != null && !levels.isEmpty();
    }

    public boolean hasKeywords() {
        return keywords != null && !keywords.isEmpty();
    }

    public boolean hasDateRange() {
        return dateRange != null &&
                (dateRange.getStart() != null || dateRange.getEnd() != null);
    }
}
