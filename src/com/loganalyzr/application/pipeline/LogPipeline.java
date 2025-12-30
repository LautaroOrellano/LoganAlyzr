package com.loganalyzr.application.pipeline;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.ports.ReportPublisher;
import com.loganalyzr.core.service.RuleEngine;

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

    public void runIngestion() {
        try {
            while (isRunning) {

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Ingestor detenido.");
        } catch (Exception e) {
            System.err.println("Error en ingestor: " + e.getMessage());
        }

    }

}
