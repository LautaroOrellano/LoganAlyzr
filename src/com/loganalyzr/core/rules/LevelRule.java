package com.loganalyzr.core.rules;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogRule;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LevelRule implements LogRule {

    private final Set<String> allowedLevels;

    public LevelRule(List<String> levels) {
        this.allowedLevels = levels.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean test(LogEvent event) {
        if (event.getLevel() == null) return false;

        return allowedLevels.contains(event.getLevel().toUpperCase());
    }
}
