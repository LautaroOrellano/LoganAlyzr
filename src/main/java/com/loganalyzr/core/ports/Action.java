package com.loganalyzr.core.ports;

import com.loganalyzr.core.model.Event;

/**
 * Contrato de una acción ejecutable por el agente.
 *
 * Reglas de implementación:
 *   - execute() debe ser idempotente en la medida de lo posible.
 *   - execute() NO debe lanzar excepciones no controladas.
 *     El ActionExecutor las captura, pero un fallo silencioso es preferible
 *     a propagar una excepción que detenga otras acciones en secuencia.
 *   - name() debe ser único por tipo de acción. Es la clave usada en actions.json.
 */
public interface Action {

    /**
     * Identificador único de esta acción. Debe coincidir con el valor
     * declarado en el campo "actions" de actions.json.
     * Ejemplo: "LOG", "HTTP_ALERT", "RESTART".
     */
    String name();

    /**
     * Ejecuta la acción en el contexto del evento recibido.
     * El orden de ejecución entre acciones es determinado por ActionExecutor.
     *
     * @param event El evento que disparó esta acción.
     */
    void execute(Event event);
}
