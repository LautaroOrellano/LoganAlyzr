package com.loganalyzr.core.rules;

import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogRule;

import java.time.LocalDateTime;

public class DateRangeRule implements LogRule {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public DateRangeRule(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }


    @Override
    public boolean test(LogEvent event) {

        if (start != null) {
            if (event.getTimestamp().isBefore(start)) {
                return false;
            }
        }

        if (end != null) {
            if (event.getTimestamp().isAfter(end)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isMandatory() {
        return true;
    }

    /**
     * DateRangeRule es un filtro temporal (mandatory), no un clasificador de eventos.
     * Las reglas mandatory descartan logs; no emiten eventos propios.
     * UNKNOWN es el valor sentinel para reglas de este tipo.
     */
    @Override
    public EventType eventType() {
        return EventType.UNKNOWN;
    }
}
