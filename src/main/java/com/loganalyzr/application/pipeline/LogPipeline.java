package com.loganalyzr.application.pipeline;

import com.loganalyzr.application.ActionExecutor;
import com.loganalyzr.application.AgentMetrics;
import com.loganalyzr.application.EngineRegistry;
import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.Action;
import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.ports.ReportPublisher;
import com.loganalyzr.core.service.DecisionEngine;
import com.loganalyzr.core.service.RuleEngine;

import java.util.List;
import java.util.concurrent.*;

public class LogPipeline {
    private final BlockingQueue<LogEvent> queue;
    private final LogSource logSource;
    private final EngineRegistry engineRegistry;  // lee engines en cada processLog() para hot-reload
    private final ActionExecutor actionExecutor;
    private final ReportPublisher publisher;
    private final AgentMetrics metrics;
    private final ExecutorService workerPool;
    private volatile boolean isRunning = false;

    public LogPipeline(LogSource logSource,
                       EngineRegistry engineRegistry,
                       ActionExecutor actionExecutor,
                       ReportPublisher publisher,
                       AgentMetrics metrics) {
        this.logSource      = logSource;
        this.engineRegistry = engineRegistry;
        this.actionExecutor = actionExecutor;
        this.publisher      = publisher;
        this.metrics        = metrics;

        /*
            Si el procesamiento se atrasa, la ingestion se frena automaticamente.
            Cola con capacidad maxima de hasta 1000 logs.
        */
        this.queue = new LinkedBlockingDeque<>(1000);

        /*
            Devuelve la cantidad de nucleos logicos de la cpu.
            Crea los hilos una sola vez, los reutiliza evita crear y destruir hilos constantementes.
         */
        int workers = Runtime.getRuntime().availableProcessors();
        this.workerPool = Executors.newFixedThreadPool(workers);

    }

    public void start() {
        isRunning = true;

        Thread ingestorThread = new Thread(this::runIngestion, "log-ingestor");
        Thread dispatcherThread = new Thread(this::runDispatching, "log-dispatcher");

        ingestorThread.start();
        dispatcherThread.start();

        System.out.println("Pipeline Asíncrono INICIADO.");
    }

    private void runIngestion() {
        try {
            while (isRunning) {
                List<LogEvent> logs = logSource.fetchNewLogs();
                if (logs.isEmpty()) {
                    Thread.sleep(100);
                }

                for (LogEvent log : logs) {
                    queue.put(log);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Ingestor detenido.");
        } catch (Exception e) {
            System.err.println("Error en ingestor: " + e.getMessage());
        }
    }

    private void runDispatching() {
        try {
            while (isRunning || !queue.isEmpty()) {

                LogEvent log = queue.take();

                workerPool.submit(() -> processLog(log));

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Dispatcher detenido.");
        } catch (Exception e) {
            System.err.println("Error en Worker: " + e.getMessage());
        }
    }

    private void processLog(LogEvent log) {
        try {
            metrics.incrementLogsEvaluated();

            // Leer engines del registry en cada invocación.
            // Si hubo un hot-reload entre este log y el anterior,
            // aquí ya se usa la nueva configuración.
            RuleEngine    ruleEngine    = engineRegistry.getRuleEngine();
            DecisionEngine decisionEngine = engineRegistry.getDecisionEngine();

            // 1. Detectar eventos semánticos en el log.
            List<Event> events = ruleEngine.evaluate(log);

            if (events.isEmpty()) {
                metrics.incrementLogsDiscarded();
                return;
            }

            metrics.addEventsDetected(events.size());

            // 2. Publicar eventos detectados (auditoría / reporting).
            publisher.publish(events);

            // 3. Para cada evento, decidir y ejecutar acciones en orden.
            for (Event event : events) {
                List<Action> actions = decisionEngine.decide(event);
                actionExecutor.execute(actions, event);
            }
        } catch (Exception e) {
            metrics.incrementProcessingErrors();
            System.err.println("Error procesando log: " + e.getMessage());
        }
    }

    public void stop() {
        isRunning = false;

        workerPool.shutdown();

        try {
            if (!workerPool.awaitTermination(10, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Pipeline detenido.");
    }

}
