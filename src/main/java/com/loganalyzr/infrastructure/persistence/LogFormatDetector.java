package com.loganalyzr.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Detecta automáticamente el formato de un archivo de logs
 * inspeccionando sus primeras líneas no vacías.
 *
 * Formatos soportados:
 *   JSONL     — {"timestamp":"...","level":"...","message":"..."}
 *   LOGBACK   — yyyy-MM-dd HH:mm:ss.SSS  LEVEL ... mensaje
 *   SPRING    — yyyy-MM-dd HH:mm:ss.SSS  LEVEL 12345 --- [thread] class : mensaje
 *   LOG4J     — LEVEL [yyyy-MM-dd HH:mm:ss] - mensaje
 *   NGINX     — 127.0.0.1 - - [dd/MMM/yyyy:HH:mm:ss +zone] "METHOD ..." status bytes
 *   PLAIN     — fallback: trata cada línea como mensaje completo
 */
public class LogFormatDetector {

    public enum LogFormat {
        JSONL, LOGBACK, SPRING, LOG4J, NGINX, PLAIN
    }

    private static final int SAMPLE_LINES = 5;

    /**
     * Detecta el formato leyendo las primeras líneas del archivo.
     * Si no puede leer el archivo, retorna PLAIN como fallback seguro.
     */
    public LogFormat detect(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            int checked = 0;
            String line;
            while ((line = reader.readLine()) != null && checked < SAMPLE_LINES) {
                line = line.trim();
                if (line.isEmpty()) continue;
                checked++;

                LogFormat detected = matchLine(line);
                if (detected != LogFormat.PLAIN) {
                    System.out.printf("[LogFormatDetector] Formato detectado: %s (muestra: \"%s\")%n",
                            detected, truncate(line, 60));
                    return detected;
                }
            }
        } catch (IOException e) {
            System.err.println("[LogFormatDetector] No se pudo leer el archivo: " + e.getMessage());
        }

        System.out.println("[LogFormatDetector] Formato: PLAIN (fallback)");
        return LogFormat.PLAIN;
    }

    private LogFormat matchLine(String line) {
        // JSONL: empieza con '{'
        if (line.startsWith("{")) {
            return LogFormat.JSONL;
        }

        // Spring Boot: "2025-06-15 10:30:00.123  INFO 12345 --- [thread] ..."
        if (line.matches("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d+\\s+\\w+\\s+\\d+\\s+---.*")) {
            return LogFormat.SPRING;
        }

        // Logback estándar: "2025-06-15 10:30:00.123  INFO com.example.Foo - mensaje"
        if (line.matches("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d+\\s+(ERROR|WARN|INFO|DEBUG|TRACE).*")) {
            return LogFormat.LOGBACK;
        }

        // Log4j: "ERROR [2025-06-15 10:30:00] - mensaje"
        if (line.matches("(ERROR|WARN|INFO|DEBUG|TRACE|FATAL)\\s+\\[\\d{4}-\\d{2}-\\d{2}.*")) {
            return LogFormat.LOG4J;
        }

        // Nginx: "127.0.0.1 - - [15/Jun/2025:10:30:00 +0000] ..."
        if (line.matches("\\S+\\s+-\\s+-\\s+\\[\\d{2}/\\w+/\\d{4}.*")) {
            return LogFormat.NGINX;
        }

        return LogFormat.PLAIN;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
