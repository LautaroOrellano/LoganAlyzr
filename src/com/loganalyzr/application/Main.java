package com.loganalyzr.application;

import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.ports.ReportPublisher;
import com.loganalyzr.infrastructure.persistence.JsonLogReader;
import com.loganalyzr.infrastructure.persistence.SmartFileReader;
import com.loganalyzr.infrastructure.ui.ConsoleReporter;

public class Main {
    public static void main(String[] args) {
        String filePath = "logs.jsonl";
        if (args.length > 0) {
            filePath = args[0];
        } else {
            System.out.println("No se especificó archivo. Usando por defecto 'logs.jsonl'");
        }

        try {
            LogSource source = new JsonLogReader(filePath);
            ReportPublisher reporter = new ConsoleReporter();

            System.out.println("Iniciando Log Analyzer con el archivo: " + filePath);
            Agent agent = new Agent(source, reporter);

            agent.run();

            System.out.println("Análisis completado.");

        } catch (Exception e) {
            System.err.println("Error fatal: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}