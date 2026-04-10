package com.loganalyzr.application;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas operacionales del agente.
 *
 * Thread-safety: todos los contadores usan AtomicLong — incrementos
 * desde múltiples workers del pool sin locks.
 *
 * Diseño:
 *   - Los contadores son monotónicamente crecientes desde el arranque.
 *   - snapshot() genera una vista puntual del estado (no atómica en su
 *     conjunto, pero aceptable para métricas de observabilidad).
 *   - No persiste métricas — solo en memoria. Fase 5+ puede agregar exportación.
 */
public class AgentMetrics {

    private final Instant startedAt = Instant.now();

    /** Logs leídos del LogSource y entregados al RuleEngine. */
    private final AtomicLong logsEvaluated   = new AtomicLong(0);

    /** Logs descartados por el RuleEngine (no generaron ningún evento). */
    private final AtomicLong logsDiscarded   = new AtomicLong(0);

    /** Eventos semánticos emitidos por el RuleEngine (suma de todos los eventos). */
    private final AtomicLong eventsDetected  = new AtomicLong(0);

    /** Total de acciones ejecutadas (incluyendo las que fallaron). */
    private final AtomicLong actionsExecuted = new AtomicLong(0);

    /** Acciones que lanzaron una excepción durante execute(). */
    private final AtomicLong actionsFailed   = new AtomicLong(0);

    /** Logs que fallaron en el procesamiento por error interno del pipeline. */
    private final AtomicLong processingErrors = new AtomicLong(0);

    // ── Incrementos ───────────────────────────────────────────────────────────

    public void incrementLogsEvaluated()   { logsEvaluated.incrementAndGet(); }
    public void incrementLogsDiscarded()   { logsDiscarded.incrementAndGet(); }
    public void addEventsDetected(int n)   { eventsDetected.addAndGet(n); }
    public void incrementActionsExecuted() { actionsExecuted.incrementAndGet(); }
    public void incrementActionsFailed()   { actionsFailed.incrementAndGet(); }
    public void incrementProcessingErrors(){ processingErrors.incrementAndGet(); }

    // ── Reads ─────────────────────────────────────────────────────────────────

    public long getLogsEvaluated()    { return logsEvaluated.get(); }
    public long getLogsDiscarded()    { return logsDiscarded.get(); }
    public long getEventsDetected()   { return eventsDetected.get(); }
    public long getActionsExecuted()  { return actionsExecuted.get(); }
    public long getActionsFailed()    { return actionsFailed.get(); }
    public long getProcessingErrors() { return processingErrors.get(); }

    /**
     * Genera un snapshot legible del estado actual del agente.
     * No garantiza atomicidad entre contadores, pero es suficiente
     * para observabilidad operacional.
     */
    public String snapshot() {
        Duration uptime = Duration.between(startedAt, Instant.now());
        long evaluated  = logsEvaluated.get();
        long discarded  = logsDiscarded.get();
        long detected   = eventsDetected.get();
        long executed   = actionsExecuted.get();
        long failed     = actionsFailed.get();
        long errors     = processingErrors.get();

        double hitRate = evaluated > 0
                ? (double)(evaluated - discarded) / evaluated * 100
                : 0.0;

        return String.format("""
                ╔══════════════════════════════════════════════════╗
                ║           LoganAlyzr — MÉTRICAS                 ║
                ╠══════════════════════════════════════════════════╣
                ║  Uptime          : %s
                ║  Logs evaluados  : %d
                ║  Logs descartados: %d
                ║  Hit rate        : %.1f%%
                ║  Eventos emitidos: %d
                ║  Acciones ejec.  : %d
                ║  Acciones fallidas:%d
                ║  Errores pipeline: %d
                ╚══════════════════════════════════════════════════╝""",
                formatDuration(uptime),
                evaluated, discarded, hitRate,
                detected, executed, failed, errors);
    }

    private String formatDuration(Duration d) {
        long h = d.toHours();
        long m = d.toMinutesPart();
        long s = d.toSecondsPart();
        return String.format("%02dh %02dm %02ds", h, m, s);
    }
}
