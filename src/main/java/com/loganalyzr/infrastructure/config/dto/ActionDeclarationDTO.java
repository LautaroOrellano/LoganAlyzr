package com.loganalyzr.infrastructure.config.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Declara una acción individual dentro de una regla de decisión.
 *
 * Separa el TIPO de acción de su CONFIGURACIÓN, permitiendo que cada
 * acción tenga parámetros propios sin ensuciar el schema raíz.
 *
 * Ejemplo en actions.json:
 * <pre>
 * {
 *   "type": "HTTP_ALERT",
 *   "config": {
 *     "url": "http://localhost:9000/alerts",
 *     "timeoutSeconds": 5
 *   }
 * }
 * </pre>
 *
 * Para acciones sin configuración propia (ej: LOG), "config" puede omitirse:
 * <pre>
 *   { "type": "LOG" }
 * </pre>
 */
public class ActionDeclarationDTO {

    /** Nombre de la acción. Debe coincidir con Action.name(). */
    private String type;

    /**
     * Parámetros específicos de esta acción.
     * El tipo de acción es responsable de leer y validar sus propias keys.
     */
    private Map<String, Object> config = new HashMap<>();

    public ActionDeclarationDTO() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getConfig() {
        return config != null ? config : Collections.emptyMap();
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
