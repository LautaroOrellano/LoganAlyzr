package com.loganalyzr.infrastructure.persistence;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogSource;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lector de fallback para logs de formato desconocido o texto plano.
 *
 * Comportamiento:
 *   - Trata cada línea no vacía como un LogEvent completo.
 *   - Timestamp: momento de lectura (no está en el log).
 *   - Level: intenta detectar palabras clave al inicio de la línea
 *     (ERROR, WARN, WARNING, INFO, DEBUG, FATAL). Si no encuentra, usa "INFO".
 *   - Message: la línea completa.
 *
 * Este reader garantiza que CUALQUIER archivo de texto es ingestado,
 * aunque la información semántica sea mínima.
 */
public class PlainTextLogReader implements LogSource {

    private final String filePath;
    private long lastPosition = 0;

    public PlainTextLogReader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<LogEvent> fetchNewLogs() {
        List<LogEvent> logs = new ArrayList<>();
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(lastPosition);
            String line;
            while ((line = file.readLine()) != null) {
                line = new String(line.getBytes("ISO-8859-1"), "UTF-8").trim();
                if (line.isEmpty()) continue;
                logs.add(new LogEvent(LocalDateTime.now(), detectLevel(line), line));
            }
            lastPosition = file.getFilePointer();
        } catch (IOException e) {
            System.err.println("[PlainTextLogReader] Error leyendo: " + e.getMessage());
        }
        return logs;
    }

    /**
     * Detecta el nivel buscando palabras clave conocidas al inicio de la línea
     * o como palabras completas. No es perfecto — es una heurística de fallback.
     */
    private String detectLevel(String line) {
        String upper = line.toUpperCase();
        if (upper.startsWith("ERROR")   || upper.contains("[ERROR]")  || upper.contains("ERROR:"))  return "ERROR";
        if (upper.startsWith("FATAL")   || upper.contains("[FATAL]")  || upper.contains("FATAL:"))  return "ERROR";
        if (upper.startsWith("WARN")    || upper.contains("[WARN]")   || upper.contains("WARN:"))   return "WARN";
        if (upper.startsWith("WARNING") || upper.contains("[WARNING]"))                              return "WARN";
        if (upper.startsWith("DEBUG")   || upper.contains("[DEBUG]")  || upper.contains("DEBUG:"))  return "DEBUG";
        return "INFO";
    }
}
