package com.loganalizyr.processor;

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
    public List<String> filterLogs(List<String> logs) {
        List<String> filterLogs = new ArrayList<>();
        for (String log : logs) {
            if(log.contains(keyword)) {
                filterLogs.add(log);
            }
        }
        return filterLogs;
    }
}
