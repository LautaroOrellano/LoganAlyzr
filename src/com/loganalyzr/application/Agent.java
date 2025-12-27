package com.loganalyzr.application;

import com.loganalyzr.core.ports.LogRule;
import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.service.RuleEngine;
import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;
import com.loganalyzr.infrastructure.config.mapper.RuleFactory;
import com.loganalyzr.infrastructure.config.dto.KeywordCriteriaDTO;
import com.loganalyzr.infrastructure.config.dto.DateRangeDTO;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.model.MatchMode;
import com.loganalyzr.infrastructure.persistence.ConfigLoader;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;

public class Agent {

    private final LogSource logSource;

    public Agent(LogSource logSource) {
        this.logSource = logSource;
    }

    public void run() {
        try {
            System.out.println(">>> Iniciando LoganAlyzr Agent...");
            ConfigLoader configLoader = new ConfigLoader();
            FilterRulesDTO config = configLoader.loadConfig("rules.json");

            if (config == null) {
                System.out.println("Error: No se pudo cargar la configuración.");
                return;
            }
            System.out.printf("Configuración cargada correctamente");

            RuleFactory factory = new RuleFactory();
            List<LogRule> rules = factory.createRules(config);

            RuleEngine engine = new RuleEngine(rules, config.getMatchMode());

            System.out.println(
                    "Motor iniciado con " + rules.size()  + " regalas activas (Modo: " + config.getMatchMode() + ")"
            );

            List<LogEvent> logs = logSource.fetchNewLogs();

            if (logs.isEmpty()) {
                System.out.println("No se encontraron logs nuevos para procesar.");
                return;
            }
            System.out.println("Procesando " + logs.size() + " eventos recibidos...");

            List<LogEvent> alerts = new ArrayList<>();
            for (LogEvent log :logs) {
                if (engine.matches(log)) {
                    alerts.add(log);
                }
            }

            if (alerts.isEmpty()) {
                System.out.println("Estado ok. Ningún log disparó las alertas configuradas");
            } else {
                System.out.println("¡ALERTA!: Se dectectaron " + alerts.size() + " eventos críticos:");
                for (LogEvent alert: alerts) {
                    System.out.println("  -> " + alert);
                }
            }

        } catch (Exception e) {
            System.err.println("Error crítico durante la ejecución del Agente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
