package com.loganalyzr.cofigs;

import com.loganalyzr.collectors.FileLogCollector;
import com.loganalyzr.models.KeywordCriteria;
import com.loganalyzr.models.LogEntry;
import com.loganalyzr.services.LogFilterCriteria;
import com.loganalyzr.services.filterLog.FilterLogsService;
import enums.MatchMode;

import java.time.LocalDateTime;
import java.util.List;

public class AppInitializer {

    private final FileLogCollector collector;
    private final FilterLogsService filterService;

    KeywordCriteria keyword1 = new KeywordCriteria("desconectado", false,
            true, false, false);
    KeywordCriteria keyword2 = new KeywordCriteria("usuario", false,
            true, false, true);

    public AppInitializer() {
        this.collector = new FileLogCollector();
        this.filterService = new FilterLogsService();
    }

    public void run() {
        try {
            String filePath = "C:/Users/NoxiePC/Desktop/Programacion/LoganAnalyzr/LoganAlyzr/logs.txt";

            List<LogEntry> logs = collector.loadLogs(filePath, 1, 500);
            if (logs.isEmpty()) {
                System.out.println("No se encontro logs en el archivo.");
                return;
            }

            LogFilterCriteria criteria = new LogFilterCriteria();
            criteria.setStartDate(LocalDateTime.of(2025, 9, 1, 0, 0));
            criteria.setEndDate(LocalDateTime.of(2025, 9 ,  21, 23, 59));
            criteria.setMatchMode(MatchMode.ALL);
            criteria.setLevels(List.of("ERROR", "INFO"));
            criteria.setKeywords(List.of(keyword1, keyword2));

            List<LogEntry> filtered = filterService.filterLogs(logs, criteria);

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
