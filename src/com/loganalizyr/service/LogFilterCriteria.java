package com.loganalizyr.service;

import java.time.LocalDateTime;

public class LogFilterCriteria {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String Level;
    private String Keyword;

    public LogFilterCriteria() {}

    public LogFilterCriteria(LocalDateTime startDate, LocalDateTime endDate, String level, String keyword) {
        this.startDate = startDate;
        this.endDate = endDate;
        Level = level;
        Keyword = keyword;
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

    public String getLevel() {
        return Level;
    }

    public void setLevel(String level) {
        Level = level;
    }

    public String getKeyword() {
        return Keyword;
    }

    public void setKeyword(String keyword) {
        Keyword = keyword;
    }
}
