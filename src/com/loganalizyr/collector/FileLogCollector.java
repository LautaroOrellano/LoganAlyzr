package com.loganalizyr.collector;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileLogCollector {

    private String filePath;

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
    public List<String> readLogs(int offset, int limit)  {
        List<String> logs = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int currentLine = 0;

            while (currentLine < offset && (line = reader.readLine()) != null) {
                currentLine++;
            }

            int linesRead = 0;
            while (linesRead < limit && (line = reader.readLine()) != null) {
                logs.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logs;
    }
}
