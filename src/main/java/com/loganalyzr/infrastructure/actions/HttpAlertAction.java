package com.loganalyzr.infrastructure.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzr.core.model.Event;
import com.loganalyzr.core.ports.Action;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Envía una alerta HTTP POST a un endpoint externo cuando se detecta un evento.
 *
 * El payload es un JSON con la información del evento:
 * <pre>
 * {
 *   "id":        "...",
 *   "type":      "ERROR_SPIKE",
 *   "source":    "...",
 *   "timestamp": "...",
 *   "origin":    "..."
 * }
 * </pre>
 *
 * Configuración requerida en actions.json:
 *   - "url"            (String)  : endpoint destino. Ej: "http://localhost:9000/alerts"
 *   - "timeoutSeconds" (int)     : timeout de conexión y lectura. Default: 5
 *
 * Comportamiento ante fallo:
 *   - Si la URL no responde, el execute() loguea el error y retorna.
 *   - No lanza excepciones — el ActionExecutor no debe romperse por esta acción.
 *   - Respuestas no-2xx se loguean como advertencia pero no se tratan como error fatal.
 */
public class HttpAlertAction implements Action {

    private static final String DEFAULT_TIMEOUT = "5";

    private final String url;
    private final int timeoutSeconds;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public HttpAlertAction(Map<String, Object> config) {
        this.url = (String) config.get("url");
        if (this.url == null || this.url.isBlank()) {
            throw new IllegalArgumentException("[HttpAlertAction] 'url' es requerida en config.");
        }

        this.timeoutSeconds = ((Number) config.getOrDefault("timeoutSeconds", 5)).intValue();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    @Override
    public String name() {
        return "HTTP_ALERT";
    }

    @Override
    public void execute(Event event) {
        try {
            String payload = buildPayload(event);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("X-Source", "LoganAlyzr")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.printf("[ACTION:HTTP_ALERT] ✓ Enviado a %s — HTTP %d%n",
                        url, response.statusCode());
            } else {
                System.err.printf("[ACTION:HTTP_ALERT] ⚠ Respuesta inesperada de %s — HTTP %d%n",
                        url, response.statusCode());
            }

        } catch (Exception e) {
            // No relanzamos — el ActionExecutor registra el fallo por encima.
            System.err.printf("[ACTION:HTTP_ALERT] ✗ Fallo al enviar a %s: %s%n",
                    url, e.getMessage());
        }
    }

    private String buildPayload(Event event) throws Exception {
        // LinkedHashMap para mantener orden consistente en el JSON.
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id",        event.getId());
        payload.put("type",      event.getType().name());
        payload.put("source",    event.getSource());
        payload.put("timestamp", event.getTimestamp().toString());
        payload.put("origin",    event.getOrigin().toString());
        return mapper.writeValueAsString(payload);
    }
}
