package com.loganalyzr.core.service;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.ports.Action;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Motor de decisión: dado un Event, determina qué acciones ejecutar.
 *
 * Responsabilidades:
 *   1. Resolver qué acciones corresponden a un EventType (vía actionMap).
 *   2. Respetar el cooldown configurado por EventType.
 *   3. Garantizar que la lista retornada preserve el orden de declaración en config.
 *
 * Thread-safety:
 *   decide() está sincronizado a nivel de instancia para evitar que dos threads
 *   evalúen el cooldown del mismo EventType simultáneamente y ambos lo pasen.
 *   Es aceptable a este nivel de concurrencia (un lock liviano por evento).
 *
 * Idempotencia futura:
 *   El campo event.getId() está disponible para implementar deduplicación
 *   basada en ID cuando sea necesario (fase posterior).
 */
public class DecisionEngine {

    /**
     * Mapa de EventType → acciones a ejecutar (en orden).
     * Configurado externamente en Agent y pasado por constructor.
     */
    private final Map<EventType, List<Action>> actionMap;

    /**
     * Cooldown en segundos por EventType.
     * 0 = sin cooldown.
     */
    private final Map<EventType, Integer> cooldownSeconds;

    /**
     * Registro del último momento en que cada EventType disparó sus acciones.
     * ConcurrentHashMap para reads seguros desde múltiples threads;
     * la escritura está protegida por la sincronización de decide().
     */
    private final ConcurrentHashMap<EventType, Instant> lastExecution = new ConcurrentHashMap<>();

    public DecisionEngine(Map<EventType, List<Action>> actionMap,
                          Map<EventType, Integer> cooldownSeconds) {
        this.actionMap       = actionMap;
        this.cooldownSeconds = cooldownSeconds;
    }

    /**
     * Decide qué acciones ejecutar para el evento dado.
     *
     * @param event El evento a procesar.
     * @return Lista ordenada de acciones a ejecutar. Vacía si no hay regla,
     *         si el tipo está deshabilitado, o si el cooldown está activo.
     */
    public synchronized List<Action> decide(Event event) {
        EventType type = event.getType();

        // 1. Sin regla configurada para este tipo → nada que hacer.
        List<Action> actions = actionMap.getOrDefault(type, Collections.emptyList());
        if (actions.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Cooldown check.
        int cooldown = cooldownSeconds.getOrDefault(type, 0);
        if (cooldown > 0) {
            Instant last = lastExecution.get(type);
            if (last != null && Instant.now().isBefore(last.plusSeconds(cooldown))) {
                System.out.printf("[DecisionEngine] Cooldown activo para %s. Faltan %ds.%n",
                        type,
                        cooldown - Instant.now().getEpochSecond() + last.getEpochSecond());
                return Collections.emptyList();
            }
        }

        // 3. Actualizar timestamp de última ejecución.
        lastExecution.put(type, Instant.now());

        return actions;
    }
}
