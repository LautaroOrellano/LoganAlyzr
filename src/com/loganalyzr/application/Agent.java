package com.loganalyzr.application;

import com.loganalyzr.core.ports.LogRule;
import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.service.RuleEngine;
import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;
import com.loganalyzr.infrastructure.config.mapper.RuleFactory;
import com.loganalyzr.infrastructure.persistence.FileReader;
import com.loganalyzr.infrastructure.config.dto.KeywordCriteriaDTO;
import com.loganalyzr.infrastructure.config.dto.DateRangeDTO;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.model.MatchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Agent {

    private final LogSource logSource;

    public Agent(LogSource logSource) {
        this.logSource = logSource;
    }

    public void run() {
        try {
            List<LogEvent> logs = logSource.fetchNewLogs();

            if (logs.isEmpty()) {
                System.out.println("No se encontraron logs nuevos.");
            }

            FilterRulesDTO config = new FilterRulesDTO();
            config.setMatchMode(MatchMode.ALL);
            config.setLevels(List.of("ERROR", "INFO"));

            KeywordCriteriaDTO k1 = new KeywordCriteriaDTO(
                    "desconectado",
                    false,
                    true,
                    false,
                    false
            );
            KeywordCriteriaDTO k2 = new KeywordCriteriaDTO(
                    "usuario",
                    false,
                    true,
                    false,
                    true
            );

            config.setKeywords(List.of(k1, k2));

            DateRangeDTO dateRange = new DateRangeDTO();
            dateRange.setStart("2025-09-01T00:00:00");
            dateRange.setEnd("2025-09-21T23:59:00");
            config.setDateRange(dateRange);

            RuleFactory factory = new RuleFactory();
            List<LogRule> rules = factory.createRules(config);

            System.out.println("Se crearon " + rules.size() +" reglas de filtrado.");

            RuleEngine engine = new RuleEngine(rules, config.getMatchMode());

            List<LogEvent> filteredLogs = new ArrayList<>();

            for (LogEvent log: logs) {
                if (engine.matches(log)) {
                    filteredLogs.add(log);
                }
            }

            if (filteredLogs.isEmpty()) {
                System.out.println("No se encontraron logs que coincidan con los criterios de busqueda.");
            } else {
                System.out.println("=== RESULTADO DEL FILTRO ===");
                for (LogEvent log : filteredLogs) {
                    System.out.println(log);
                }
            }

        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
