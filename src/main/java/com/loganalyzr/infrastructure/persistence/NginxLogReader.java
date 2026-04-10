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
 * Lector para logs de acceso de Nginx.
 *
 * Formato Combined Log (más común):
 *   IP - - [dd/Mon/yyyy:HH:mm:ss +zone] "METHOD /path HTTP/x.x" status bytes "referer" "ua"
 *
 * Mapeo a LogEvent:
 *   timestamp → campo fecha del log
 *   level     → derivado del HTTP status (2xx=INFO, 3xx=WARN, 4xx=WARN, 5xx=ERROR)
 *   message   → "METHOD /path → status"
 */
public class NginxLogReader implements LogSource {

    private static final Pattern PATTERN = Pattern.compile(
            "(?:\\S+)\\s+-\\s+-\\s+\\[(\\d{2}/\\w+/\\d{4}:\\d{2}:\\d{2}:\\d{2})\\s[^]]+]" +
            "\\s+\"(\\w+)\\s+(\\S+)[^\"]*\"\\s+(\\d{3}).*"
    );
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss");

    private final String filePath;
    private long lastPosition = 0;

    public NginxLogReader(String filePath) {
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
            System.err.println("[NginxLogReader] Error leyendo: " + e.getMessage());
        }
        return logs;
    }

    private LogEvent parse(String line) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        LocalDateTime timestamp = parseTimestamp(m.group(1));
        String method  = m.group(2);
        String path    = m.group(3);
        int    status  = Integer.parseInt(m.group(4));
        String level   = httpStatusToLevel(status);
        String message = String.format("HTTP %s %s → %d", method, path, status);

        return new LogEvent(timestamp, level, message);
    }

    private String httpStatusToLevel(int status) {
        if (status >= 500) return "ERROR";
        if (status >= 400) return "WARN";
        if (status >= 300) return "WARN";
        return "INFO";
    }

    private LocalDateTime parseTimestamp(String raw) {
        try {
            return LocalDateTime.parse(raw, FORMATTER);
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }
}
