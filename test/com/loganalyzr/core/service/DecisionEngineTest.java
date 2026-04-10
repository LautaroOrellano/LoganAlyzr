package com.loganalyzr.core.service;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.Action;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DecisionEngineTest {

    /** Acción stub para testing. */
    private static final Action STUB_ACTION = new Action() {
        @Override public String name()              { return "STUB"; }
        @Override public void execute(Event event)  { /* noop */ }
    };

    private LogEvent anyLog() {
        return new LogEvent(LocalDateTime.now(), "ERROR", "test message");
    }

    private Event eventOf(EventType type) {
        return Event.builder(type, anyLog()).build();
    }

    @Test
    public void shouldReturnActions_WhenEventTypeIsConfigured() {
        // Given
        DecisionEngine engine = new DecisionEngine(
                Map.of(EventType.ERROR_SPIKE, List.of(STUB_ACTION)),
                Map.of(EventType.ERROR_SPIKE, 0)
        );
        Event event = eventOf(EventType.ERROR_SPIKE);

        // When
        List<Action> actions = engine.decide(event);

        // Then
        assertEquals(1, actions.size());
        assertEquals("STUB", actions.get(0).name());
    }

    @Test
    public void shouldReturnEmpty_WhenEventTypeHasNoRule() {
        // Given
        DecisionEngine engine = new DecisionEngine(Map.of(), Map.of());
        Event event = eventOf(EventType.HIGH_LATENCY);

        // When
        List<Action> actions = engine.decide(event);

        // Then
        assertTrue(actions.isEmpty(), "Sin regla para HIGH_LATENCY, no debe retornar acciones");
    }

    @Test
    public void shouldRespectCooldown_WhenCalledTwiceInQuickSuccession() {
        // Given — cooldown de 60 segundos
        DecisionEngine engine = new DecisionEngine(
                Map.of(EventType.ERROR_SPIKE, List.of(STUB_ACTION)),
                Map.of(EventType.ERROR_SPIKE, 60)
        );
        Event event = eventOf(EventType.ERROR_SPIKE);

        // When — primera llamada pasa, segunda debe bloquearse por cooldown
        List<Action> first  = engine.decide(event);
        List<Action> second = engine.decide(event);

        // Then
        assertFalse(first.isEmpty(),  "Primera llamada debe retornar acciones");
        assertTrue(second.isEmpty(),  "Segunda llamada dentro del cooldown debe retornar vacío");
    }

    @Test
    public void shouldNotApplyCooldown_WhenCooldownIsZero() {
        // Given — cooldown = 0 (sin límite)
        DecisionEngine engine = new DecisionEngine(
                Map.of(EventType.ERROR_SPIKE, List.of(STUB_ACTION)),
                Map.of(EventType.ERROR_SPIKE, 0)
        );
        Event event = eventOf(EventType.ERROR_SPIKE);

        // When
        List<Action> first  = engine.decide(event);
        List<Action> second = engine.decide(event);

        // Then — ambas deben pasar
        assertFalse(first.isEmpty());
        assertFalse(second.isEmpty(), "Con cooldown=0, todas las llamadas deben pasar");
    }
}
