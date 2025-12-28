package com.loganalyzr.infrastructure.ui;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.ReportPublisher;

import java.util.ArrayList;
import java.util.List;

public class ConsoleReporter implements ReportPublisher {
    @Override
    public void publish(List<LogEvent> alerts) {


        if (alerts.isEmpty()) {
            System.out.println("Estado ok. Ningún log disparó las alertas configuradas");
        } else {
            System.out.println("¡ALERTA!: Se dectectaron " + alerts.size() + " eventos críticos:");
            for (LogEvent alert: alerts) {
                System.out.println("  -> " + alert);
            }
        }
    }
}
