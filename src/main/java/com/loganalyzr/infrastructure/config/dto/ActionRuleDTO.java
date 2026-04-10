package com.loganalyzr.infrastructure.config.dto;

import java.util.Collections;
import java.util.List;

/**
 * DTO que representa una regla de decisión individual.
 *
 * Mapea directamente a un objeto dentro del array "rules" en actions.json.
 *
 * Ejemplo de schema:
 * <pre>
 * {
 *   "eventType": "ERROR_SPIKE",
 *   "actions": [
 *     { "type": "LOG" },
 *     { "type": "HTTP_ALERT", "config": { "url": "http://...", "timeoutSeconds": 5 } }
 *   ],
 *   "cooldownSeconds": 60,
 *   "enabled": true
 * }
 * </pre>
 */
public class ActionRuleDTO {

    /** EventType al que aplica esta regla. Debe coincidir con el enum EventType. */
    private String eventType;

    /** Lista ordenada de declaraciones de acción a ejecutar. */
    private List<ActionDeclarationDTO> actions;

    /** Tiempo mínimo entre ejecuciones del mismo EventType, en segundos. 0 = sin cooldown. */
    private int cooldownSeconds;

    /** Si false, esta regla es ignorada completamente. */
    private boolean enabled = true;

    public ActionRuleDTO() {}

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public List<ActionDeclarationDTO> getActions() {
        return actions != null ? actions : Collections.emptyList();
    }
    public void setActions(List<ActionDeclarationDTO> actions) { this.actions = actions; }

    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
