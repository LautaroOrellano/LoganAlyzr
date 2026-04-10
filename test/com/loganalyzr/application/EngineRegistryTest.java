package com.loganalyzr.application;

import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.model.MatchMode;
import com.loganalyzr.core.service.DecisionEngine;
import com.loganalyzr.core.service.RuleEngine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EngineRegistryTest {

    private RuleEngine emptyRuleEngine() {
        return new RuleEngine(Collections.emptyList(), MatchMode.ANY);
    }

    private DecisionEngine emptyDecisionEngine() {
        return new DecisionEngine(Map.of(), Map.of());
    }

    @Test
    public void shouldReturnInitialEngines() {
        RuleEngine    re = emptyRuleEngine();
        DecisionEngine de = emptyDecisionEngine();

        EngineRegistry registry = new EngineRegistry(re, de);

        assertSame(re, registry.getRuleEngine(),    "Debe retornar el engine inicial");
        assertSame(de, registry.getDecisionEngine(),"Debe retornar el decision engine inicial");
    }

    @Test
    public void shouldSwapEngines_AfterUpdate() {
        EngineRegistry registry = new EngineRegistry(emptyRuleEngine(), emptyDecisionEngine());

        RuleEngine    newRe = emptyRuleEngine();
        DecisionEngine newDe = emptyDecisionEngine();

        registry.update(newRe, newDe);

        assertSame(newRe, registry.getRuleEngine(),    "Debe retornar el engine nuevo tras update");
        assertSame(newDe, registry.getDecisionEngine(),"Debe retornar el decision engine nuevo tras update");
    }

    @Test
    public void shouldRejectNullEngines() {
        EngineRegistry registry = new EngineRegistry(emptyRuleEngine(), emptyDecisionEngine());

        assertThrows(IllegalArgumentException.class,
                () -> registry.update(null, emptyDecisionEngine()),
                "RuleEngine null debe rechazarse");

        assertThrows(IllegalArgumentException.class,
                () -> registry.update(emptyRuleEngine(), null),
                "DecisionEngine null debe rechazarse");
    }

    @Test
    public void shouldUpdateLastReloadedAt_AfterEachUpdate() throws InterruptedException {
        EngineRegistry registry = new EngineRegistry(emptyRuleEngine(), emptyDecisionEngine());

        var before = registry.getLastReloadedAt();
        Thread.sleep(10); // garantizar diferencia de tiempo
        registry.update(emptyRuleEngine(), emptyDecisionEngine());

        assertTrue(registry.getLastReloadedAt().isAfter(before),
                "lastReloadedAt debe avanzar después de cada update");
    }

    @Test
    public void shouldBeThreadSafe_UnderConcurrentReads() throws InterruptedException {
        // Escenario: un hilo actualiza el registry mientras otros leen.
        // No debe haber NPE ni referencia null.
        EngineRegistry registry = new EngineRegistry(emptyRuleEngine(), emptyDecisionEngine());

        int readers = 6;
        Thread[] readerThreads = new Thread[readers];
        boolean[] sawNull = {false};

        for (int i = 0; i < readers; i++) {
            readerThreads[i] = new Thread(() -> {
                for (int j = 0; j < 500; j++) {
                    if (registry.getRuleEngine() == null || registry.getDecisionEngine() == null) {
                        sawNull[0] = true;
                    }
                }
            });
        }

        Thread writer = new Thread(() -> {
            for (int j = 0; j < 100; j++) {
                registry.update(emptyRuleEngine(), emptyDecisionEngine());
            }
        });

        for (Thread t : readerThreads) t.start();
        writer.start();
        for (Thread t : readerThreads) t.join();
        writer.join();

        assertFalse(sawNull[0], "Nunca debe haber un engine null durante la actualización concurrente");
    }
}
