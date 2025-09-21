package com.loganalizyr.service.filterLog;

import com.loganalizyr.model.LogEntry;
import com.loganalizyr.service.LogFilterCriteria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilterLogsService implements IFilterLogs {


    /**
     * filtra los logs que contienen la palabra clave
     * @param logs lista de logs a filtrar
     * @return lista de logs filtradros
     */
    public List<LogEntry> filterLogs(List<LogEntry> logs, LogFilterCriteria criteria) {
        List<LogEntry> filterLogs = new ArrayList<>();
        return logs.stream()
                .filter(log -> {
                    boolean matches = true;

                    if (criteria.getStartDate() != null) {
                        matches &= !log.getTimestamp().isBefore(criteria.getStartDate());
                    }
                    if (criteria.getEndDate() != null) {
                        matches &= !log.getTimestamp().isAfter(criteria.getEndDate());
                    }
                    if (criteria.getLevel() != null && !criteria.getLevel().isEmpty()) {
                        matches &= log.getLevel().equalsIgnoreCase(criteria.getLevel());
                    }
                    if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
                        matches &= log.getMessage().toLowerCase().contains(criteria.getKeyword().toLowerCase());
                    }

                    return matches;

                })
                .collect(Collectors.toList());
    }

}
