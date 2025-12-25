package com.loganalyzr.application;

import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;
import com.loganalyzr.infrastructure.persistence.FileReader;
import com.loganalyzr.infrastructure.config.dto.KeywordCriteriaDTO;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.service.RuleEngine;
import com.loganalyzr.core.model.MatchMode;

import java.time.LocalDateTime;
import java.util.List;

public class Agent {

    private final FileReader collector;
    private final RuleEngine filterService;

    KeywordCriteriaDTO keyword1 = new KeywordCriteriaDTO("desconectado", false,
            true, false, false);
    KeywordCriteriaDTO keyword2 = new KeywordCriteriaDTO("usuario", false,
            true, false, true);

    public Agent() {
        this.collector = new FileReader();
        this.filterService = new RuleEngine();
    }

    public void run() {
        try {
            String filePath = "C:/Users/NoxiePC/Desktop/Programacion/LoganAnalyzr/LoganAlyzr/logs.txt";

            List<LogEvent> logs = collector.loadLogs(filePath, 1, 500);
            if (logs.isEmpty()) {
                System.out.println("No se encontro logs en el archivo.");
                return;
            }

            FilterRulesDTO criteria = new FilterRulesDTO();
            criteria.setStartDate(LocalDateTime.of(2025, 9, 1, 0, 0));
            criteria.setEndDate(LocalDateTime.of(2025, 9 ,  21, 23, 59));
            criteria.setMatchMode(MatchMode.ALL);
            criteria.setLevels(List.of("ERROR", "INFO"));
            criteria.setKeywords(List.of(keyword1, keyword2));

            List<LogEvent> filtered = filterService.filterLogs(logs, criteria);

            if (filtered.isEmpty()) {
                System.out.println("No se encontraron logs que coincidan con los criterios.");
            } else {
                filtered.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
