package com.loganalyzr.agent;

import com.loganalyzr.processing.FilterRules;
import com.loganalyzr.tailing.FileReader;
import com.loganalyzr.models.KeywordCriteria;
import com.loganalyzr.models.LogEvent;
import com.loganalyzr.processing.RuleEngine;
import com.loganalyzr.enums.MatchMode;

import java.time.LocalDateTime;
import java.util.List;

public class Agent {

    private final FileReader collector;
    private final RuleEngine filterService;

    KeywordCriteria keyword1 = new KeywordCriteria("desconectado", false,
            true, false, false);
    KeywordCriteria keyword2 = new KeywordCriteria("usuario", false,
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

            FilterRules criteria = new FilterRules();
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
