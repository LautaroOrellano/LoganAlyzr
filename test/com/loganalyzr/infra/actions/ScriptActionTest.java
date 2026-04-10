package com.loganalyzr.infra.actions;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.infrastructure.actions.ScriptAction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;

public class ScriptActionTest {

    private LogEvent anyLog() {
        return new LogEvent(LocalDateTime.now(), "ERROR", "test message");
    }

    private Event errorEvent() {
        return Event.builder(EventType.ERROR_SPIKE, anyLog())
                .source("test-source")
                .build();
    }

    @Test
    public void shouldThrow_WhenPathIsMissingFromConfig() {
        Map<String, Object> config = Map.of("timeoutSeconds", 5);

        assertThrows(IllegalArgumentException.class,
                () -> new ScriptAction(config),
                "Debe lanzar excepción si 'path' no está en config");
    }

    @Test
    public void shouldConstruct_WhenValidConfigProvided() {
        Map<String, Object> config = Map.of(
                "path", "scripts/notify.sh",
                "timeoutSeconds", 10
        );
        assertDoesNotThrow(() -> new ScriptAction(config));
    }

    @Test
    public void shouldReportCorrectName() {
        ScriptAction action = new ScriptAction(Map.of("path", "scripts/test.sh"));
        assertEquals("SCRIPT", action.name());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void shouldExecuteSuccessfully_OnWindows() {
        // Ejecuta "echo" del sistema — garantizado en todos los Windows.
        Map<String, Object> config = Map.of(
                "path", "echo LoganAlyzr test",
                "timeoutSeconds", 5
        );
        ScriptAction action = new ScriptAction(config);

        // No debe propagar ninguna excepción.
        assertDoesNotThrow(() -> action.execute(errorEvent()));
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    public void shouldExecuteSuccessfully_OnUnix() {
        // echo es un builtin de bash — siempre disponible.
        Map<String, Object> config = Map.of(
                "path", "echo LoganAlyzr test",
                "timeoutSeconds", 5
        );
        ScriptAction action = new ScriptAction(config);

        assertDoesNotThrow(() -> action.execute(errorEvent()));
    }

    @Test
    public void shouldNotThrow_WhenScriptDoesNotExist() {
        // Un path que no existe no debe romper el ActionExecutor.
        Map<String, Object> config = Map.of(
                "path", "scripts/does-not-exist.sh",
                "timeoutSeconds", 2
        );
        ScriptAction action = new ScriptAction(config);

        assertDoesNotThrow(() -> action.execute(errorEvent()),
                "Script inexistente debe fallar silenciosamente");
    }
}
