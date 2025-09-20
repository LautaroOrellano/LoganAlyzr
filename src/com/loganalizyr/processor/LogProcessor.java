package com.loganalizyr.processor;

import com.loganalizyr.model.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class LogProcessor {

    private String keyword;

    public LogProcessor(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * filtra los logs que contienen la palabra clave
     * @param logs lista de logs a filtrar
     * @return lista de logs filtradros
     */
    public List<LogEntry> filterLogs(List<LogEntry> logs) {
        List<LogEntry> filterLogs = new ArrayList<>();
        for (LogEntry log : logs) {
            if (log.getMessage().contains(keyword) ||
                log.getLevel().contains(keyword) ||
                log.getTimestamp().contains(keyword)) {
                filterLogs.add(log);
            }
        }
        return filterLogs;
    }
}
