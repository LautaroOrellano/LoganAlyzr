package com.loganalyzr.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzr.core.exception.LogReadException;
import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogSource;

import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JsonLogReader implements LogSource {

    private final String filePath;
    private long lastBytePosition = 0;

    // Esta es la clase mágica de la librería Jackson
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonLogReader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<LogEvent> fetchNewLogs() {
        List<LogEvent> newLogs = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long fileLength = raf.length();
            if (fileLength < lastBytePosition) {
                lastBytePosition = 0;
            }
            raf.seek(lastBytePosition);
            String line;

            while ((line = raf.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    LogEvent event = parseJsonLine(line);
                    if (event != null) {
                        newLogs.add(event);
                    }
                }
            }
            this.lastBytePosition = raf.getFilePointer();
        } catch (Exception e) {
            throw new LogReadException("Error leyendo JSON", e);
        }
        return newLogs;
    }

    private LogEvent parseJsonLine(String jsonLine) {
        try {
            // 1. Convertimos la linea de texto a un Nodo JSON
            JsonNode node = mapper.readTree(jsonLine);

            // 2. Extraemos los campos de forma segura
            // (Ajusta los nombres "level", "message", etc, según tu archivo real)
            String level = node.has("level") ? node.get("level").asText() : "UNKNOWN";
            String message = node.has("message") ? node.get("message").asText() : "";

            // 3. Manejo de fecha
            LocalDateTime timestamp;
            if (node.has("timestamp")) {
                String dateStr = node.get("timestamp").asText();
                // Jackson maneja ISO 8601 (con la T) por defecto
                timestamp = LocalDateTime.parse(dateStr);
            } else {
                timestamp = LocalDateTime.now(); // Fallback si no hay fecha
            }

            return new LogEvent(timestamp, level, message);

        } catch (Exception e) {
            // Si la línea no es JSON válido, la ignoramos pero imprimimos aviso
            // System.err.println("Linea ignorada (No JSON): " + jsonLine);
            return null;
        }
    }
}
