package com.loganalyzr.infrastructure.persistence;


import com.loganalyzr.core.model.LogEvent;
import com.loganalyzr.core.ports.LogRule;
import com.loganalyzr.core.ports.LogSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TextLogReader implements LogSource {

    private String filePath;
    private int currentOffset = 0;

    public TextLogReader(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public List<LogEvent> fetchNewLogs() {
        return loadNewLogs(1000);
    }

    private List<LogEvent> loadNewLogs(int limit) {
        List<LogEvent> logs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(this.filePath))) {
            String line;
            int linesSkipped = 0;

            // Saltar las líneas que YA leímos antes
            while (linesSkipped < currentOffset && reader.readLine() != null) {
                linesSkipped++;
            }

            // Leer solo las líneas nuevas
            int linesRead = 0;
            while (linesRead < limit && (line = reader.readLine()) != null) {

                // IMPORTANTE: Incrementamos el offset global por cada línea leída (útil o vacía)
                // para no volver a leerla nunca más.
                currentOffset++;

                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(" - ", 2);
                    if (parts.length == 2) {
                        try {
                            String left = parts[0].trim();
                            String message = parts[1].trim();
                            String level = left.substring(0, left.indexOf(" "));

                            int startTime = left.indexOf("[");
                            int endTime = left.indexOf("]", startTime);
                            String timestamp = left.substring(startTime + 1, endTime);

                            LocalDateTime time = LocalDateTime.parse(timestamp, formatter);
                            logs.add(new LogEvent(time, level, message));
                        } catch (Exception e) {

                        }
                    }
                }
                linesRead++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logs;
    }
}
