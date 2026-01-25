package com.loganalyzr.core.service;

import com.loganalyzr.core.model.MatchMode;
import com.loganalyzr.core.ports.LogRule;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.rules.DateRangeRule;
import com.loganalyzr.core.rules.LevelRule;

import java.util.ArrayList;
import java.util.List;

public class RuleEngine {
    private final List<LogRule> mandatoryRules = new ArrayList<>();
    private final List<LogRule> flexibleRules = new ArrayList<>();
    private final MatchMode matchMode;

    public RuleEngine(List<LogRule> rules, MatchMode matchMode) {
        this.matchMode = matchMode;

        for (LogRule rule : rules) {
            if (rule.isMandatory()) {
                mandatoryRules.add(rule);
            } else {
                flexibleRules.add(rule);
            }
        }
    }

    public boolean matches(LogEvent event) {

        for (LogRule rule : mandatoryRules) {
            if (!rule.test(event)) {
                return false;
            }
        }

        if (flexibleRules.isEmpty()) {
            return true;
        }

        if (matchMode == MatchMode.ALL) {
            for (LogRule rule : flexibleRules) {
                if (!rule.test(event)) return false;
            }
            return true;
        } else {
            for (LogRule rule : flexibleRules) {
                if (rule.test(event)) return true;
            }
            return false;
        }
    }

}
