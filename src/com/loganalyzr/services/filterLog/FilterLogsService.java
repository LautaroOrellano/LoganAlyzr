package com.loganalyzr.services.filterLog;

import com.loganalyzr.models.LogEntry;
import com.loganalyzr.services.LogFilterCriteria;
import enums.MatchMode;

import java.util.List;
import java.util.stream.Collectors;

public class FilterLogsService implements IFilterLogs {

    /**
     * filtra los logs que contienen la palabra clave
     * @param logs lista de logs a filtrar
     * @return lista de logs filtradros
     */
    public List<LogEntry> filterLogs(List<LogEntry> logs, LogFilterCriteria criteria) {
        return logs.stream()
                .filter(log -> {
                    boolean matches = true;

                    if (criteria.getStartDate() != null) {
                        matches &= !log.getTimestamp().isBefore(criteria.getStartDate());
                    }
                    if (criteria.getEndDate() != null) {
                        matches &= !log.getTimestamp().isAfter(criteria.getEndDate());
                    }
                    if (criteria.hasLevels()) {
                        matches &= criteria.getLevels()
                                .stream()
                                .anyMatch(level ->
                                        log.getLevel().equalsIgnoreCase(level));
                    }
                    if (criteria.hasKeywords()) {
                        if (criteria.getMatchMode() == MatchMode.ANY) {
                            matches = matches && criteria.getKeywords()
                                    .stream()
                                    .anyMatch(keywordCriteria ->
                                            keywordCriteria.matches(log.getMessage()));
                        } else if (criteria.getMatchMode() == MatchMode.ALL) {
                            matches = matches && criteria.getKeywords()
                                    .stream()
                                    .allMatch(keywordCriteria ->
                                            keywordCriteria.matches(log.getMessage()));
                        }
                    }
                    return matches;
                })
                .collect(Collectors.toList());
    }

}
