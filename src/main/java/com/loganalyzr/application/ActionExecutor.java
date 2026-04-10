package com.loganalyzr.application;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.ports.Action;

import java.util.List;

/**
 * Executor de acciones.
 *
 * Responsabilidades:
 *   1. Ejecutar acciones en el orden en que fueron recibidas (orden importa).
 *   2. Aislar excepciones por acción: un fallo en acción N no detiene la acción N+1.
 *   3. Registrar fallos sin propagar — el pipeline nunca se rompe por una acción fallida.
 *   4. Actualizar métricas de ejecución y fallo por cada acción.
 *
 * Diseño intencional:
 *   La ejecución es SINCRÓNICA y SECUENCIAL por decisión de diseño.
 *   Ejecución async o paralela de acciones se introducirá en Fase 6+
 *   cuando haya una razón concreta, no antes.
 */
public class ActionExecutor {

    private final AgentMetrics metrics;

    public ActionExecutor(AgentMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * Ejecuta la lista de acciones en orden, para el evento dado.
     *
     * @param actions Lista ordenada de acciones a ejecutar.
     * @param event   El evento que disparó estas acciones.
     */
    public void execute(List<Action> actions, Event event) {
        for (Action action : actions) {
            try {
                action.execute(event);
                metrics.incrementActionsExecuted();
            } catch (Exception e) {
                metrics.incrementActionsFailed();
                metrics.incrementActionsExecuted(); // cuenta como ejecutada aunque fallara
                System.err.printf("[ActionExecutor] La acción '%s' falló para evento %s: %s%n",
                        action.name(),
                        event.getType(),
                        e.getMessage());
            }
        }
    }
}
