package com.loganalyzr.infrastructure.actions;

import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.ports.Action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Ejecuta un script externo cuando se detecta un evento.
 *
 * El contexto del evento se pasa al script como variables de entorno:
 *   EVENT_ID        — UUID del evento
 *   EVENT_TYPE      — Ej: "ERROR_SPIKE"
 *   EVENT_SOURCE    — Fuente del evento
 *   EVENT_TIMESTAMP — Timestamp ISO-8601
 *   EVENT_MESSAGE   — Mensaje del log de origen
 *   EVENT_LEVEL     — Nivel del log de origen
 *
 * Configuración requerida en actions.json:
 *   - "path"           (String) : ruta al script. Ej: "scripts/restart.sh"
 *   - "timeoutSeconds" (int)    : tiempo máximo de ejecución. Default: 30
 *
 * Cross-platform:
 *   - En Windows: se ejecuta como "cmd /c <path>"
 *   - En Unix/Linux/Mac: se ejecuta como "bash <path>"
 *
 * El script puede leer las variables de entorno y actuar en consecuencia.
 * Exit code != 0 se registra como advertencia, no como fallo del agente.
 */
public class ScriptAction implements Action {

    private final String scriptPath;
    private final int timeoutSeconds;
    private final boolean isWindows;

    public ScriptAction(Map<String, Object> config) {
        this.scriptPath = (String) config.get("path");
        if (this.scriptPath == null || this.scriptPath.isBlank()) {
            throw new IllegalArgumentException("[ScriptAction] 'path' es requerido en config.");
        }

        this.timeoutSeconds = ((Number) config.getOrDefault("timeoutSeconds", 30)).intValue();
        this.isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
    }

    @Override
    public String name() {
        return "SCRIPT";
    }

    @Override
    public void execute(Event event) {
        List<String> command = buildCommand();

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // stderr fusionado con stdout para capturarlo completo

        // Inyectar contexto del evento como variables de entorno.
        Map<String, String> env = pb.environment();
        env.put("EVENT_ID",        event.getId());
        env.put("EVENT_TYPE",      event.getType().name());
        env.put("EVENT_SOURCE",    event.getSource());
        env.put("EVENT_TIMESTAMP", event.getTimestamp().toString());
        env.put("EVENT_MESSAGE",   event.getOrigin().getMessage() != null
                ? event.getOrigin().getMessage() : "");
        env.put("EVENT_LEVEL",     event.getOrigin().getLevel() != null
                ? event.getOrigin().getLevel() : "");

        try {
            Process process = pb.start();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                System.err.printf("[ACTION:SCRIPT] ✗ Timeout (%ds) ejecutando: %s%n",
                        timeoutSeconds, scriptPath);
                return;
            }

            int exitCode = process.exitValue();
            if (exitCode == 0) {
                System.out.printf("[ACTION:SCRIPT] ✓ Ejecutado: %s (exit 0)%n", scriptPath);
            } else {
                System.err.printf("[ACTION:SCRIPT] ⚠ %s terminó con exit code %d%n",
                        scriptPath, exitCode);
            }

        } catch (IOException e) {
            System.err.printf("[ACTION:SCRIPT] ✗ No se pudo ejecutar '%s': %s%n",
                    scriptPath, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[ACTION:SCRIPT] ✗ Ejecución interrumpida: %s%n", scriptPath);
        }
    }

    private List<String> buildCommand() {
        List<String> command = new ArrayList<>();
        if (isWindows) {
            command.add("cmd");
            command.add("/c");
        } else {
            command.add("bash");
        }
        command.add(scriptPath);
        return command;
    }
}
