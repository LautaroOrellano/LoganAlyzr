package com.loganalyzr.core.service;

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
    public void shouldMatch_WhenMandatoryRulePasses() {
        // Given
        LogEvent errorLog = new LogEvent(LocalDateTime.now(), "ERROR", "Critical failure");

        // Mandatory Rule
        LogRule levelRule = new LevelRule(List.of("Error"));
        RuleEngine engine = new RuleEngine(List.of(levelRule), MatchMode.ANY);

        // When
        boolean result = engine.matches(errorLog);

        // Then
        assertTrue(result, "El log deberia pasar porque es ERROR");
    }

    @Test
    public void shouldFail_WhenMandatoryRuleFails() {
        // Given
        LogEvent infoLog = new LogEvent(LocalDateTime.now(), "INFO", "System ok");

        // Mandatory Rule
        LogRule levelRule = new LevelRule(List.of("Error"));
        RuleEngine engine = new RuleEngine(List.of(levelRule), MatchMode.ANY);

        // Then
        assertFalse(engine.matches(infoLog), "El log deberia fllar por que no es 'ERROR' ");
    }

    @Test
    public void shouldMatch_WhenKeywordIsFound_InAnyMode() {
        // Given
        LogEvent log1 = new LogEvent(
                LocalDateTime.now(),
                "ERROR",
                "Error de conexion en la base de datos"
        );

        // KeywordRule
        KeywordRule keyword = new KeywordRule(
                "conexion",
                false,
                false,
                false,
                false
        );

        // When
        RuleEngine engine = new RuleEngine(List.of(keyword), MatchMode.ANY);

        // Then
        assertTrue(engine.matches(log1), "La palabra esta en el mensaje, deberia pasar");
    }
}
