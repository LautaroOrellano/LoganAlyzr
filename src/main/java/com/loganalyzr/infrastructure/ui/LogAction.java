package com.loganalyzr.infrastructure.ui;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.ports.Action;

/**
 * Acción de logging estructurado a consola.
 *
 * Es la acción más simple y la primera en existir.
 * Sirve como:
 *   - Implementación de referencia para futuras acciones.
 *   - Mecanismo de auditoría: siempre se puede combinar con otras acciones.
 *   - Herramienta de debug durante desarrollo de nuevas acciones.
 *
 * name() = "LOG" — debe coincidir exactamente con el valor en actions.json.
 */
public class LogAction implements Action {

    @Override
    public String name() {
        return "LOG";
    }

    @Override
    public void execute(Event event) {
        System.out.printf(
                "[ACTION:LOG] id=%-36s | type=%-20s | source=%s%n  └─ origin: %s%n",
                event.getId(),
                event.getType(),
                event.getSource(),
                event.getOrigin()
        );
    }
}
