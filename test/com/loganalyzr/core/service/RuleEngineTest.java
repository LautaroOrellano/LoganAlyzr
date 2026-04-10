package com.loganalyzr.core.service;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.model.MatchMode;
import com.loganalyzr.core.ports.LogRule;
import com.loganalyzr.core.rules.KeywordRule;
import com.loganalyzr.core.rules.LevelRule;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

public class RuleEngineTest {

    @Test
    public void shouldEmitEvent_WhenMandatoryRulePasses() {
        // Given
        LogEvent errorLog = new LogEvent(LocalDateTime.now(), "ERROR", "Critical failure");
        LogRule levelRule = new LevelRule(List.of("ERROR"));
        // LevelRule es mandatory — si no hay flexibles, no emite eventos (solo filtra)
        // Para que emita, necesita al menos una regla flexible que pase.
        // Este test valida que UN mandatory que pasa no descarta el log.
        RuleEngine engine = new RuleEngine(List.of(levelRule), MatchMode.ANY);

        // When
        List<Event> events = engine.evaluate(errorLog);

        // Then — sin flexibles, evaluate retorna vacío (pasó el gate, pero no clasificó)
        assertTrue(events.isEmpty(), "Sin reglas flexibles, no se emiten eventos aunque el mandatory pase");
    }

    @Test
    public void shouldReturnEmpty_WhenMandatoryRuleFails() {
        // Given
        LogEvent infoLog = new LogEvent(LocalDateTime.now(), "INFO", "System ok");
        LogRule levelRule = new LevelRule(List.of("ERROR"));
        RuleEngine engine = new RuleEngine(List.of(levelRule), MatchMode.ANY);

        // When
        List<Event> events = engine.evaluate(infoLog);

        // Then
        assertTrue(events.isEmpty(), "Mandatory falló — el log debe descartarse");
    }

    @Test
    public void shouldEmitEvent_WhenKeywordMatches_InAnyMode() {
        // Given
        LogEvent log = new LogEvent(LocalDateTime.now(), "ERROR", "Error de conexion en la base de datos");
        KeywordRule keywordRule = new KeywordRule("conexion", false, false, false, false);
        RuleEngine engine = new RuleEngine(List.of(keywordRule), MatchMode.ANY);

        // When
        List<Event> events = engine.evaluate(log);

        // Then
        assertFalse(events.isEmpty(), "La keyword 'conexion' está en el mensaje — debe emitirse un evento");
        assertEquals(EventType.HIGH_LATENCY, events.get(0).getType());
    }

    @Test
    public void shouldEmitMultipleEvents_WhenMultipleKeywordsMatch_InAnyMode() {
        // Given
        LogEvent log = new LogEvent(LocalDateTime.now(), "ERROR", "conexion timeout 500ms");
        KeywordRule keyword1 = new KeywordRule("conexion", false, false, false, false);
        KeywordRule keyword2 = new KeywordRule("500ms",    false, false, false, false);
        RuleEngine engine = new RuleEngine(List.of(keyword1, keyword2), MatchMode.ANY);

        // When
        List<Event> events = engine.evaluate(log);

        // Then — 1 log → 2 eventos (uno por keyword que matcheó)
        assertEquals(2, events.size(), "Dos keywords matchearon: deben generarse 2 eventos");
    }

    @Test
    public void shouldEmitNoEvents_WhenAllModeAndOneKeywordFails() {
        // Given
        LogEvent log = new LogEvent(LocalDateTime.now(), "ERROR", "solo conexion aqui");
        KeywordRule keyword1 = new KeywordRule("conexion",    false, false, false, false);
        KeywordRule keyword2 = new KeywordRule("no_esta", false, false, false, false);
        RuleEngine engine = new RuleEngine(List.of(keyword1, keyword2), MatchMode.ALL);

        // When
        List<Event> events = engine.evaluate(log);

        // Then — ALL mode: keyword2 no pasa → no se emite ningún evento
        assertTrue(events.isEmpty(), "ALL mode: una flexible falla → lista vacía");
    }
}
