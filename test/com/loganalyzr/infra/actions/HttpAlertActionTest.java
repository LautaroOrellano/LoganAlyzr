package com.loganalyzr.infra.actions;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.infrastructure.actions.HttpAlertAction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;

public class HttpAlertActionTest {

    private LogEvent anyLog() {
        return new LogEvent(LocalDateTime.now(), "ERROR", "test message");
    }

    private Event errorEvent() {
        return Event.builder(EventType.ERROR_SPIKE, anyLog())
                .source("test-source")
                .build();
    }

    @Test
    public void shouldThrow_WhenUrlIsMissingFromConfig() {
        // Given — config sin URL
        Map<String, Object> config = Map.of("timeoutSeconds", 3);

        // Then
        assertThrows(IllegalArgumentException.class,
                () -> new HttpAlertAction(config),
                "Debe lanzar excepción si 'url' no está en config");
    }

    @Test
    public void shouldConstruct_WhenValidConfigProvided() {
        // Given
        Map<String, Object> config = Map.of(
                "url", "http://localhost:9999/alerts",
                "timeoutSeconds", 2
        );

        // Then — debe construirse sin excepción
        assertDoesNotThrow(() -> new HttpAlertAction(config));
    }

    @Test
    public void shouldNotThrow_WhenServerIsUnavailable() {
        // Given — servidor que no existe
        Map<String, Object> config = Map.of(
                "url", "http://localhost:19999/does-not-exist",
                "timeoutSeconds", 1
        );
        HttpAlertAction action = new HttpAlertAction(config);
        Event event = errorEvent();

        // When/Then — execute() no debe propagar la excepción de conexión
        assertDoesNotThrow(() -> action.execute(event),
                "execute() debe absorber el error de conexión sin propagar");
    }

    @Test
    public void shouldReportCorrectName() {
        Map<String, Object> config = Map.of("url", "http://localhost/test");
        HttpAlertAction action = new HttpAlertAction(config);
        assertEquals("HTTP_ALERT", action.name());
    }
}
