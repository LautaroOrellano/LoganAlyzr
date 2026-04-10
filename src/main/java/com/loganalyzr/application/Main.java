package com.loganalyzr.application;

import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.ports.ReportPublisher;
import com.loganalyzr.infrastructure.persistence.LogSourceFactory;
import com.loganalyzr.infrastructure.ui.ConsoleReporter;

public class Main {

    public static void main(String[] args) {
        String filePath = "logs.jsonl";
        if (args.length > 0) {
            filePath = args[0];
        } else {
            System.out.println("Uso: java -jar loganalyzer.jar <archivo-de-logs>");
            System.out.println("Formatos soportados: JSONL, Logback, Spring Boot, Log4j, Nginx, texto plano");
            System.out.println("Usando por defecto: logs.jsonl");
        }

        try {
            // Detección automática de formato — no requiere configuración manual.
            LogSource source = new LogSourceFactory().create(filePath);
            ReportPublisher reporter = new ConsoleReporter();

            System.out.println("Iniciando Log Analyzer con el archivo: " + filePath);
            new Agent(source, reporter).run();

        } catch (Exception e) {
            System.err.println("Error fatal: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}