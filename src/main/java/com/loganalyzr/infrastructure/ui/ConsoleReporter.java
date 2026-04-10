package com.loganalyzr.infrastructure.ui;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.ports.ReportPublisher;

import java.util.List;

/**
 * Implementación de ReportPublisher que reporta eventos a la consola estándar.
 *
 * Thread-safety: publish() está sincronizado para evitar intercalado
 * de output cuando múltiples workers del pool escriben concurrentemente.
 */
public class ConsoleReporter implements ReportPublisher {

    @Override
    public synchronized void publish(List<Event> events) {
        if (events.isEmpty()) return;

        System.out.println("╔══════════════════════════════════════════");
        System.out.println("║  ⚠ ALERTA: " + events.size() + " evento(s) detectado(s)");
        System.out.println("╠══════════════════════════════════════════");
        for (Event event : events) {
            System.out.printf("║  [%s] type=%-20s source=%s%n",
                    event.getTimestamp(),
                    event.getType(),
                    event.getSource());
            System.out.printf("║      origin → %s%n", event.getOrigin());
        }
        System.out.println("╚══════════════════════════════════════════");
    }
}
