package com.loganalyzr.application.pipeline;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.ports.ReportPublisher;
import com.loganalyzr.core.service.RuleEngine;

import java.util.List;
import java.util.concurrent.*;

public class LogPipeline {
    private final BlockingQueue<LogEvent> queue;
    private final LogSource logSource;
    private final RuleEngine ruleEngine;
    private final ReportPublisher publisher;
    private final ExecutorService workerPool;
    private volatile boolean isRunning = false;

    public LogPipeline(LogSource logSource, RuleEngine ruleEngine, ReportPublisher publisher) {
        this.logSource = logSource;
        this.ruleEngine = ruleEngine;
        this.publisher = publisher;

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
            if (ruleEngine.matches(log)){
                publisher.publish(List.of(log));
            }
        } catch (Exception e){
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
