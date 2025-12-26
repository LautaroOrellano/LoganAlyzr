package com.loganalyzr.infrastructure.persistence;

import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogSource;

import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartFileReader implements LogSource {

    private final String filePath;
    private final Pattern pattern;
    private final DateTimeFormatter dateFormatter;
    private long lastBytePosition = 0;

    public SmartFileReader(String filePath, String regexPattern, String dateFormat) {
        this.filePath = filePath;
        this.pattern = Pattern.compile(regexPattern);
        this.dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    @Override
    public List<LogEvent> fetchNewLogs() {
        List<LogEvent> newLogs = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            raf.seek(lastBytePosition);
            String line;

            while ((line = raf.readLine()) != null) {
                if (!line.trim().isEmpty()) {

                    LogEvent event = parseGenericLine(line);
                    if (event != null) {
                        newLogs.add(event);
                    }
                }
            }
            this.lastBytePosition = raf.getFilePointer();
        } catch (Exception e) {
            System.err.println("Error leyendo archivo: " + e.getMessage());
        }
        return newLogs;
    }

    private LogEvent parseGenericLine(String line) {
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            try {

                String level = matcher.group("level");
                String dateStr = matcher.group("date");
                String msg = matcher.group("message");

                LocalDateTime timestamp = LocalDateTime.parse(dateStr, dateFormatter);
                return new LogEvent(timestamp, level, msg);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}