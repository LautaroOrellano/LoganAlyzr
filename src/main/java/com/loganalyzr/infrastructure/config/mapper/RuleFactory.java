package com.loganalyzr.infrastructure.config.mapper;

import com.loganalyzr.core.ports.LogRule;
import com.loganalyzr.core.rules.DateRangeRule;
import com.loganalyzr.core.rules.KeywordRule;
import com.loganalyzr.core.rules.LevelRule;
import com.loganalyzr.infrastructure.config.dto.DateRangeDTO;
import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RuleFactory {

    public List<LogRule> createRules(FilterRulesDTO dto) {
        List<LogRule> rules = new ArrayList<>();

        if (dto.hasLevels()) {
            LevelRule levelRule = new LevelRule(dto.getLevels());
            rules.add(levelRule);
        }

        if (dto.hasKeywords()) {
            dto.getKeywords().stream()
                    .map(k -> new KeywordRule(
                            k.getKeyword(),
                            k.isUseRegex(),
                            k.isUseLiteral(),
                            k.isCaseSensitive(),
                            k.isUseNegated()
                    ))
                    .forEach(rules::add);
        }

        if (dto.hasDateRange()) {
            try {
                String start = dto.getDateRange().getStart();
                String end = dto.getDateRange().getEnd();

                LocalDateTime newStart = (start != null && !start.isBlank()) ? LocalDateTime.parse(start) : null;
                LocalDateTime newEnd = (end != null && !end.isBlank()) ? LocalDateTime.parse(end): null;

                if (newStart != null || newEnd != null) {
                    rules.add(new DateRangeRule(newStart, newEnd));
                }

            } catch (DateTimeParseException e) {
                System.err.println("Error config fechas: " + e.getMessage());
                throw new IllegalArgumentException("Formato de fecha inválido. Use ISO-8601 (ej: 2025-12-01T10:00:00)");
            }
        }
        return rules;
    }


}
