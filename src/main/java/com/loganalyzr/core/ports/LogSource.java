package com.loganalyzr.core.ports;

import com.loganalyzr.core.model.LogEvent;

import java.util.List;

public interface LogSource {
    /**
     * Obtiene solo los logs que no se han leído anteriormente.
     * @return Una lista de nuevos eventos.
     */
    List<LogEvent> fetchNewLogs();

}
