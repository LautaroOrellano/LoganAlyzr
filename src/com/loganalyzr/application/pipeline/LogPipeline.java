package com.loganalyzr.application.pipeline;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.ports.ReportPublisher;
import com.loganalyzr.core.service.RuleEngine;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class LogPipeline {
    private final BlockingQueue<LogEvent> queue;
    private final LogSource logSource;
    private final RuleEngine ruleEngine;
    private final ReportPublisher publisher;
    private volatile boolean isRunning = false;

    public LogPipeline(LogSource logSource, RuleEngine ruleEngine, ReportPublisher publisher) {
        this.logSource = logSource;
        this.ruleEngine = ruleEngine;
        this.publisher = publisher;

        this.queue = new LinkedBlockingDeque<>(1000);
    }

    public void start() {
        isRunning = true;

        Thread ingestorThread = new Thread(this ::runIngestion);
        Thread workerThread = new Thread(this::runProcessing);

        ingestorThread.start();
        workerThread.start();

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

    private void runProcessing() {
        try {
            while (isRunning || !queue.isEmpty()) {

                LogEvent log = queue.take();

                if (ruleEngine.matches(log)){
                    publisher.publish(List.of(log));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Worker detenido.");
        } catch (Exception e) {
            System.err.println("Error en Worker: " + e.getMessage());
        }
    }

}
