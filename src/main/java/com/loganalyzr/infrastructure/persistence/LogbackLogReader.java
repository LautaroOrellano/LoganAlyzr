package com.loganalyzr.infrastructure.persistence;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogSource;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lector para logs en formato Logback estándar y Spring Boot.
 *
 * Logback:  2025-06-15 10:30:00.123  INFO  com.example.Foo - mensaje
 * Spring:   2025-06-15 10:30:00.123  INFO  12345 --- [thread] com.Foo : mensaje
 *
 * Ambos comparten el prefijo de fecha e nivel; el lector los unifica.
 */
public class LogbackLogReader implements LogSource {

    private static final Pattern PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?)\\s+" +
            "(ERROR|WARN|INFO|DEBUG|TRACE)\\s+" +
            "(.+)"
    );
    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
    };

    private final String filePath;
    private long lastPosition = 0;

    public LogbackLogReader(String filePath) {
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
                LogEvent event = parse(line);
                if (event != null) logs.add(event);
            }
            lastPosition = file.getFilePointer();
        } catch (IOException e) {
            System.err.println("[LogbackLogReader] Error leyendo: " + e.getMessage());
        }
        return logs;
    }

    private LogEvent parse(String line) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        LocalDateTime timestamp = parseTimestamp(m.group(1));
        String level   = m.group(2);
        String rest    = m.group(3);

        // Extraer solo el mensaje (después del " - " o " : " en Spring)
        String message = rest.replaceFirst(".*?(?:\\s-\\s|\\s:\\s)", "").trim();
        if (message.isEmpty()) message = rest;

        return new LogEvent(timestamp, level, message);
    }

    private LocalDateTime parseTimestamp(String raw) {
        for (DateTimeFormatter fmt : FORMATTERS) {
            try { return LocalDateTime.parse(raw, fmt); } catch (DateTimeParseException ignored) {}
        }
        return LocalDateTime.now();
    }
}
