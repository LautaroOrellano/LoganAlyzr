package com.loganalyzr.application;

import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.infrastructure.persistence.JsonLogReader;
import com.loganalyzr.infrastructure.persistence.SmartFileReader;

public class Main {
    public static void main(String[] args) {
        LogSource source;

        // --- OPCIÓN 1: Usar Logs de Texto (.txt) ---
        // String path = "logs.txt";
        // source = new TextLogReader(path);

        // --- OPCIÓN 2: Usar Logs JSON (.jsonl) ---
        // (Asegúrate de que logs.jsonl exista en la raíz)
        String path = "logs.jsonl";
        source = new JsonLogReader(path);

        // --- INYECCIÓN DE DEPENDENCIA ---
        // Aquí ocurre la magia: El Agente acepta cualquiera de los dos
        Agent agent = new Agent(source);

        System.out.println("Agente configurado con estrategia: " + source.getClass().getSimpleName());

        // --- EJECUCIÓN ---
        agent.run();
    }
}