package com.loganalyzr.core.ports;

import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.model.LogEvent;

public interface LogRule {

    /**
     * Evalúa si el log cumple la condición de esta regla.
     */
    boolean test(LogEvent event);

    /**
     * Indica si esta regla es obligatoria (mandatory).
     * Si es mandatory y falla → el log se descarta completamente.
     * Si es flexible → participa en la evaluación por MatchMode.
     */
    boolean isMandatory();

    /**
     * Tipo de evento que esta regla emite cuando matchea.
     * El RuleEngine usa este valor para construir el Event correspondiente.
     */
    EventType eventType();

}
