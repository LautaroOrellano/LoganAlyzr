package com.loganalizyr.collector;


import com.loganalizyr.model.LogEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileLogCollector {

    private String filePath;

    public FileLogCollector() {};

    public FileLogCollector(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Lee una cantidad limitada de líneas desde el archivo.
     * @param offset número de línea inicial (0-index)
     * @param limit cantidad máxima de líneas a leer
     * @return lista de líneas leídas
     */
    public List<LogEntry> loadLogs(String filePath, int offset, int limit)  {
        List<LogEntry> logs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int currentLine = 0;

            while (currentLine < offset && (line = reader.readLine()) != null) {
                currentLine++;
            }

            int linesRead = 0;
            while (linesRead < limit && (line = reader.readLine()) != null) {
                String[] parts = line.split(" - ", 2);
                if (parts.length == 2) {
                    String left = parts[0].trim();
                    String message = parts[1].trim();

                    String level = left.substring(0,left.indexOf(" "));

                    int startTime = left.indexOf("[");
                    int endTime = left.indexOf("]", startTime);
                    String timestamp = left.substring(startTime + 1, endTime);

                    LocalDateTime time = LocalDateTime.parse(timestamp, formatter);

                    logs.add(new LogEntry(time, level, message));
                }
                linesRead++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logs;
    }
}
