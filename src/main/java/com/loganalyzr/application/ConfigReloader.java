package com.loganalyzr.application;

import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.ports.Action;
import com.loganalyzr.core.service.DecisionEngine;
import com.loganalyzr.core.service.RuleEngine;
import com.loganalyzr.infrastructure.config.dto.ActionsConfigDTO;
import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;
import com.loganalyzr.infrastructure.config.mapper.ActionFactory;
import com.loganalyzr.infrastructure.config.mapper.RuleFactory;
import com.loganalyzr.infrastructure.persistence.ActionConfigLoader;
import com.loganalyzr.infrastructure.persistence.ConfigLoader;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Orquesta la recarga en caliente de la configuración del agente.
 *
 * Responsabilidades:
 *   1. Leer rules.json y actions.json desde disco.
 *   2. Construir nuevas instancias de RuleEngine y DecisionEngine.
 *   3. Actualizar el EngineRegistry solo si AMBOS se construyeron sin error.
 *
 * Principio de seguridad:
 *   Si la recarga falla (config inválida, parse error, etc.), los engines
 *   actuales permanecen intactos. El agente nunca queda sin engines válidos.
 *
 * Llamado por: ConfigWatcher (en respuesta a cambios de archivo).
 */
public class ConfigReloader {

    private final EngineRegistry registry;
    private final String rulesPath;
    private final String actionsPath;

    public ConfigReloader(EngineRegistry registry, String rulesPath, String actionsPath) {
        this.registry    = registry;
        this.rulesPath   = rulesPath;
        this.actionsPath = actionsPath;
    }

    /**
     * Ejecuta la recarga completa. Thread-safe: puede ser llamado desde
     * el hilo del ConfigWatcher sin problemas.
     */
    public void reload() {
        Instant start = Instant.now();
        System.out.println("[ConfigReloader] Iniciando recarga de configuración...");

        try {
            // 1. Cargar y validar rules.json.
            FilterRulesDTO rulesConfig = new ConfigLoader().loadConfig(rulesPath);
            if (rulesConfig == null) {
                System.err.println("[ConfigReloader] ✗ rules.json inválido. Recarga abortada. Engines anteriores intactos.");
                return;
            }

            RuleEngine newRuleEngine = new RuleEngine(
                    new RuleFactory().createRules(rulesConfig),
                    rulesConfig.getMatchMode()
            );

            // 2. Cargar y validar actions.json.
            ActionsConfigDTO actionsConfig = new ActionConfigLoader().load(actionsPath);
            ActionFactory actionFactory    = new ActionFactory();

            Map<EventType, List<Action>> actionMap  = actionFactory.buildActionMap(actionsConfig);
            Map<EventType, Integer>      cooldownMap = actionFactory.buildCooldownMap(actionsConfig);

            DecisionEngine newDecisionEngine = new DecisionEngine(actionMap, cooldownMap);

            // 3. Swap atómico — solo si ambos engines están listos.
            registry.update(newRuleEngine, newDecisionEngine);

            long ms = Instant.now().toEpochMilli() - start.toEpochMilli();
            System.out.printf("[ConfigReloader] ✓ Recarga completada en %dms.%n", ms);

        } catch (IllegalArgumentException e) {
            // Config inválida (ej: fecha mal formateada en rules.json).
            System.err.println("[ConfigReloader] ✗ Config inválida: " + e.getMessage() + ". Engines anteriores intactos.");
        } catch (Exception e) {
            System.err.println("[ConfigReloader] ✗ Error inesperado durante recarga: " + e.getMessage());
        }
    }
}
