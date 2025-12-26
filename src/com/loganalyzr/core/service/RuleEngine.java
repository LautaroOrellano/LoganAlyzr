package com.loganalyzr.core.service;

import com.loganalyzr.core.model.MatchMode;
import com.loganalyzr.core.ports.LogRule;
import com.loganalyzr.core.rules.KeywordRule;
import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.infrastructure.config.dto.KeywordCriteriaDTO;

import java.util.ArrayList;
import java.util.List;

import static com.loganalyzr.core.model.MatchMode.ALL;
import static com.loganalyzr.core.model.MatchMode.ANY;

public class RuleEngine {
    private final List<LogRule> rules;
    private final MatchMode matchMode;

    public RuleEngine(List<LogRule> rules, MatchMode matchMode) {
        this.rules = rules;
        this.matchMode = matchMode;
    }

    public boolean matches(LogEvent event) {

        if (matchMode == MatchMode.ALL) {
            for (LogRule rule : rules) {
                if (!rule.test(event)) {
                    return false;
                }
            }
            return true;
        } else if (matchMode == MatchMode.ANY) {
            for (LogRule rule : rules) {
                if (rule.test(event)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

}
