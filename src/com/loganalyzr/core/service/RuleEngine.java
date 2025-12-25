package com.loganalyzr.core.service;

import com.loganalyzr.infrastructure.config.dto.FilterRulesDTO;
import com.loganalyzr.core.model.LogEvent;

import java.util.List;

public class RuleEngine {

    public RuleEngine() {

    }

    /**
     * filtra los logs que contienen la palabra clave
     * @param logs lista de logs a filtrar
     * @return
     */
    public boolean filterLogs(List<LogEvent> logs) {
        if (logs.isEmpty()) {
            return false;
        }

        if

        return true;
    }

}
