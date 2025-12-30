package com.loganalyzr.application;

import com.loganalyzr.application.pipeline.LogPipeline;
import com.loganalyzr.core.exception.LogReadException;
import com.loganalyzr.core.ports.LogRule;
import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.ports.ReportPublisher;
import com.loganalyzr.core.service.RuleEngine;
import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;
import com.loganalyzr.infrastructure.config.mapper.RuleFactory;
import com.loganalyzr.core.model.LogEvent;

import com.loganalyzr.infrastructure.persistence.ConfigLoader;

import java.util.ArrayList;
import java.util.List;


public class Agent {

    private final LogSource logSource;
    private final ReportPublisher publisher;

    public Agent(LogSource logSource, ReportPublisher publisher) {
        this.logSource = logSource;
        this.publisher = publisher;
    }

    public void run() {
        try {
            System.out.println(">>> Iniciando LoganAlyzr Agent (Modo Enterprise)...");

            // -- Configuración --
            ConfigLoader configLoader = new ConfigLoader();
            FilterRulesDTO config = configLoader.loadConfig("rules.json");

            if (config == null) {
                System.out.println("Error: No se pudo cargar la configuración.");
                return;
            }

            RuleFactory factory = new RuleFactory();
            List<LogRule> rules = factory.createRules(config);
            RuleEngine engine = new RuleEngine(rules, config.getMatchMode());

            LogPipeline pipeline = new LogPipeline(logSource, engine, publisher);

            pipeline.start();

            System.out.println("Agente corriendo en segundo plano. Presione Ctrl+C para detener.");
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Agente detenido.");
            }

        } catch (LogReadException e) {
            System.err.println("ERROR DE E/S: No se pudieron leer los logs.");
            System.err.println("Detalle: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO INESPERADO:");
            e.printStackTrace();
        }
    }
}
