package com.loganalyzr.application;

import com.loganalyzr.application.pipeline.LogPipeline;
import com.loganalyzr.core.exception.LogReadException;
import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.ports.Action;
import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.ports.ReportPublisher;
import com.loganalyzr.core.service.DecisionEngine;
import com.loganalyzr.core.service.RuleEngine;
import com.loganalyzr.infrastructure.config.dto.ActionsConfigDTO;
import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;
import com.loganalyzr.infrastructure.config.mapper.ActionFactory;
import com.loganalyzr.infrastructure.config.mapper.RuleFactory;
import com.loganalyzr.infrastructure.persistence.ActionConfigLoader;
import com.loganalyzr.infrastructure.persistence.ConfigLoader;
import com.loganalyzr.infrastructure.persistence.ConfigWatcher;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orquestador del agente. Construye el grafo completo de dependencias y
 * gestiona el ciclo de vida de todos los componentes.
 *
 * Orden de construcción:
 *   1. Métricas
 *   2. Engines iniciales (rules.json + actions.json)
 *   3. EngineRegistry (wrapper hot-swappable)
 *   4. ConfigReloader (reconstruye engines en caliente)
 *   5. ConfigWatcher (WatchService → dispara ConfigReloader)
 *   6. Pipeline (usa EngineRegistry, no los engines directamente)
 *   7. MetricsScheduler
 *   8. Shutdown hook
 */
public class Agent {

    private static final String RULES_FILE   = "rules.json";
    private static final String ACTIONS_FILE = "actions.json";

    private final LogSource logSource;
    private final ReportPublisher publisher;

    public Agent(LogSource logSource, ReportPublisher publisher) {
        this.logSource = logSource;
        this.publisher = publisher;
    }

    public void run() {
        try {
            System.out.println(">>> Iniciando LoganAlyzr Agent...");

            // ── 1. Métricas ────────────────────────────────────────────────────
            AgentMetrics metrics = new AgentMetrics();

            // ── 2. Engines iniciales ───────────────────────────────────────────
            RuleEngine    initialRuleEngine    = buildRuleEngine();
            DecisionEngine initialDecisionEngine = buildDecisionEngine();

            if (initialRuleEngine == null) {
                System.err.println("Error fatal: No se pudo cargar rules.json.");
                return;
            }

            // ── 3. EngineRegistry ──────────────────────────────────────────────
            EngineRegistry registry = new EngineRegistry(initialRuleEngine, initialDecisionEngine);

            // ── 4. ConfigReloader ──────────────────────────────────────────────
            ConfigReloader reloader = new ConfigReloader(registry, RULES_FILE, ACTIONS_FILE);

            // ── 5. ConfigWatcher ───────────────────────────────────────────────
            ConfigWatcher watcher = new ConfigWatcher(
                    Paths.get("."),                          // directorio actual
                    Set.of(RULES_FILE, ACTIONS_FILE),        // archivos a monitorear
                    reloader::reload                         // callback de recarga
            );
            watcher.start();

            // ── 6. ActionExecutor + Pipeline ──────────────────────────────────
            ActionExecutor actionExecutor = new ActionExecutor(metrics);

            LogPipeline pipeline = new LogPipeline(
                    logSource,
                    registry,
                    actionExecutor,
                    publisher,
                    metrics
            );

            // ── 7. MetricsScheduler ────────────────────────────────────────────
            MetricsScheduler metricsScheduler = new MetricsScheduler(metrics);
            metricsScheduler.start();

            // ── 8. Shutdown hook ───────────────────────────────────────────────
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n>>> Señal de apagado recibida. Deteniendo componentes...");
                watcher.stop();
                pipeline.stop();
                metricsScheduler.stop();
                System.out.println("\n>>> REPORTE FINAL:");
                System.out.println(metrics.snapshot());
            }, "shutdown-hook"));

            pipeline.start();

            System.out.println(">>> Agente activo. Ctrl+C para detener.");
            System.out.println(">>> Hot-reload: modificar " + RULES_FILE + " o " + ACTIONS_FILE + " se aplica sin reiniciar.");
            Thread.currentThread().join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (LogReadException e) {
            System.err.println("ERROR DE E/S: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO:");
            e.printStackTrace();
        }
    }

    // ── Helpers de construcción ─────────────────────────────────────────────

    private RuleEngine buildRuleEngine() {
        FilterRulesDTO config = new ConfigLoader().loadConfig(RULES_FILE);
        if (config == null) return null;
        return new RuleEngine(new RuleFactory().createRules(config), config.getMatchMode());
    }

    private DecisionEngine buildDecisionEngine() {
        ActionsConfigDTO config     = new ActionConfigLoader().load(ACTIONS_FILE);
        ActionFactory    factory    = new ActionFactory();
        Map<EventType, List<Action>> actionMap  = factory.buildActionMap(config);
        Map<EventType, Integer>      cooldownMap = factory.buildCooldownMap(config);
        return new DecisionEngine(actionMap, cooldownMap);
    }
}
