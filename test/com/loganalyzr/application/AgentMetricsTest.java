package com.loganalyzr.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgentMetricsTest {

    private AgentMetrics metrics;

    @BeforeEach
    public void setUp() {
        metrics = new AgentMetrics();
    }

    @Test
    public void shouldStartWithZeroCounters() {
        assertEquals(0, metrics.getLogsEvaluated());
        assertEquals(0, metrics.getLogsDiscarded());
        assertEquals(0, metrics.getEventsDetected());
        assertEquals(0, metrics.getActionsExecuted());
        assertEquals(0, metrics.getActionsFailed());
        assertEquals(0, metrics.getProcessingErrors());
    }

    @Test
    public void shouldIncrementLogsEvaluated() {
        metrics.incrementLogsEvaluated();
        metrics.incrementLogsEvaluated();
        assertEquals(2, metrics.getLogsEvaluated());
    }

    @Test
    public void shouldTrackDiscardedAndDetectedSeparately() {
        // 3 logs evaluados: 2 descartados, 1 genera 3 eventos
        metrics.incrementLogsEvaluated();
        metrics.incrementLogsDiscarded();

        metrics.incrementLogsEvaluated();
        metrics.incrementLogsDiscarded();

        metrics.incrementLogsEvaluated();
        metrics.addEventsDetected(3);

        assertEquals(3, metrics.getLogsEvaluated());
        assertEquals(2, metrics.getLogsDiscarded());
        assertEquals(3, metrics.getEventsDetected());
    }

    @Test
    public void shouldCountActionsAndFailuresSeparately() {
        metrics.incrementActionsExecuted();
        metrics.incrementActionsExecuted();
        metrics.incrementActionsExecuted();
        metrics.incrementActionsFailed();

        assertEquals(3, metrics.getActionsExecuted());
        assertEquals(1, metrics.getActionsFailed());
    }

    @Test
    public void shouldBeThreadSafe_UnderConcurrentIncrements() throws InterruptedException {
        int threads = 8;
        int incrementsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    metrics.incrementLogsEvaluated();
                }
                latch.countDown();
            });
        }

        latch.await();
        pool.shutdown();

        // Sin race condition: debe ser exactamente threads * incrementsPerThread
        assertEquals((long) threads * incrementsPerThread, metrics.getLogsEvaluated(),
                "AtomicLong debe garantizar exactitud bajo concurrencia");
    }

    @Test
    public void shouldGenerateNonEmptySnapshot() {
        metrics.incrementLogsEvaluated();
        metrics.addEventsDetected(1);
        metrics.incrementActionsExecuted();

        String snapshot = metrics.snapshot();

        assertNotNull(snapshot);
        assertTrue(snapshot.contains("LoganAlyzr"));
        assertTrue(snapshot.contains("Logs evaluados"));
        assertTrue(snapshot.contains("Acciones ejec."));
    }
}
