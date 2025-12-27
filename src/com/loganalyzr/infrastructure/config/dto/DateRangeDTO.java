package com.loganalyzr.infrastructure.config.dto;

public class DateRangeDTO {
    private String start;
    private String end;

    public DateRangeDTO() {
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
