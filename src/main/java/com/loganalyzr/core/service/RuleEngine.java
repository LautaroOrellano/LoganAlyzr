package com.loganalyzr.core.service;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.model.MatchMode;
import com.loganalyzr.core.ports.LogRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Motor de evaluación de reglas.
 *
 * Contrato:
 *   - Reglas mandatory: todas deben pasar. Si alguna falla, el log se descarta
 *     y se retorna una lista vacía. No generan eventos propios.
 *   - Reglas flexibles: evaluadas según MatchMode.
 *       ANY  → cada regla que matchea emite su propio Event (1 log → N eventos).
 *       ALL  → solo si TODAS pasan, cada una emite su Event.
 *
 * Un log puede generar 0, 1 o N eventos.
 */
public class RuleEngine {

    private final List<LogRule> mandatoryRules = new ArrayList<>();
    private final List<LogRule> flexibleRules  = new ArrayList<>();
    private final MatchMode matchMode;

    public RuleEngine(List<LogRule> rules, MatchMode matchMode) {
        this.matchMode = matchMode;
        for (LogRule rule : rules) {
            if (rule.isMandatory()) {
                mandatoryRules.add(rule);
            } else {
                flexibleRules.add(rule);
            }
        }
    }

    /**
     * Evalúa el log y retorna los eventos generados.
     *
     * @param log El log a evaluar.
     * @return Lista de eventos (puede ser vacía si el log no pasa los filtros).
     */
    public List<Event> evaluate(LogEvent log) {

        // 1. Las reglas mandatory actúan como gate: si alguna falla, descartamos el log.
        for (LogRule rule : mandatoryRules) {
            if (!rule.test(log)) {
                return List.of();
            }
        }

        // 2. Si no hay reglas flexibles, el log pasó el gate pero no hay clasificación.
        if (flexibleRules.isEmpty()) {
            return List.of();
        }

        // 3. Evaluación flexible según MatchMode.
        List<Event> events = new ArrayList<>();

        if (matchMode == MatchMode.ALL) {
            // Todas las reglas flexibles deben pasar.
            boolean allPass = flexibleRules.stream().allMatch(rule -> rule.test(log));
            if (allPass) {
                for (LogRule rule : flexibleRules) {
                    events.add(buildEvent(rule, log));
                }
            }
        } else {
            // ANY: cada regla que pase emite su propio evento.
            for (LogRule rule : flexibleRules) {
                if (rule.test(log)) {
                    events.add(buildEvent(rule, log));
                }
            }
        }

        return events;
    }

    private Event buildEvent(LogRule rule, LogEvent log) {
        return Event.builder(rule.eventType(), log)
                .source(log.toString())
                .addData("level", log.getLevel())
                .addData("message", log.getMessage())
                .build();
    }

    /**
     * @deprecated Usar {@link #evaluate(LogEvent)} en su lugar.
     * Mantenido temporalmente para compatibilidad con tests existentes.
     */
    @Deprecated
    public boolean matches(LogEvent log) {
        return !evaluate(log).isEmpty();
    }

}
