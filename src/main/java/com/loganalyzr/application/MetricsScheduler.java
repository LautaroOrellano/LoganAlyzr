package com.loganalyzr.application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Reporta métricas del agente a intervalos regulares.
 *
 * Corre en un daemon thread — no bloquea el shutdown de la JVM.
 * El intervalo es configurable; el default (60s) es apropiado para
 * monitoreo operacional sin saturar los logs de consola.
 *
 * Ciclo de vida:
 *   start() → reporta cada N segundos
 *   stop()  → detiene el scheduler limpiamente (graceful shutdown)
 */
public class MetricsScheduler {

    private static final int DEFAULT_INTERVAL_SECONDS = 60;

    private final AgentMetrics metrics;
    private final int intervalSeconds;
    private final ScheduledExecutorService scheduler;

    public MetricsScheduler(AgentMetrics metrics) {
        this(metrics, DEFAULT_INTERVAL_SECONDS);
    }

    public MetricsScheduler(AgentMetrics metrics, int intervalSeconds) {
        this.metrics         = metrics;
        this.intervalSeconds = intervalSeconds;

        // daemon=true: este thread no impide el shutdown de la JVM.
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "metrics-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                this::report,
                intervalSeconds,   // primer reporte después de N segundos
                intervalSeconds,
                TimeUnit.SECONDS
        );
        System.out.printf("[MetricsScheduler] Reporte de métricas cada %ds.%n", intervalSeconds);
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void report() {
        System.out.println(metrics.snapshot());
    }
}
