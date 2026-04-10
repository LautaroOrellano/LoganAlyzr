package com.loganalyzr.core.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa un evento semántico detectado a partir de uno o más LogEvents.
 *
 * Un Event NO es un log: es el resultado de aplicar reglas sobre un log.
 * Contiene el contexto suficiente para que el DecisionEngine actúe sin
 * necesitar acceder al log original directamente.
 *
 * Inmutable por diseño: una vez construido, su estado no cambia.
 * Esto garantiza seguridad en entornos concurrentes.
 */
public final class Event {

    /** Identificador único. Habilita idempotencia futura en el DecisionEngine. */
    private final String id;

    /** Tipo semántico del evento. Governa las decisiones del DecisionEngine. */
    private final EventType type;

    /** Origen del evento: archivo, pod, servicio, etc. */
    private final String source;

    /**
     * Log original que originó este evento.
     * Preservado para trazabilidad y auditoría.
     * Nunca debe usarse en lógica de decisión — para eso está 'data'.
     */
    private final LogEvent origin;

    /**
     * Datos contextuales adicionales del evento.
     * Uso: logging, auditoría y enriquecimiento.
     * Regla: el DecisionEngine NO debe leer de este map para tomar decisiones.
     * La lógica va en EventType, no en data.
     */
    private final Map<String, Object> data;

    /** Momento exacto en que se detectó el evento. */
    private final Instant timestamp;

    private Event(Builder builder) {
        this.id        = builder.id;
        this.type      = builder.type;
        this.source    = builder.source;
        this.origin    = builder.origin;
        this.data      = Collections.unmodifiableMap(new HashMap<>(builder.data));
        this.timestamp = builder.timestamp;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getId()        { return id; }
    public EventType getType()   { return type; }
    public String getSource()    { return source; }
    public LogEvent getOrigin()  { return origin; }
    public Map<String, Object> getData() { return data; }
    public Instant getTimestamp(){ return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s | source=%s | origin=[%s]",
                timestamp, type, source, origin);
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder(EventType type, LogEvent origin) {
        return new Builder(type, origin);
    }

    public static final class Builder {

        private final String    id        = UUID.randomUUID().toString();
        private final EventType type;
        private final LogEvent  origin;
        private String          source    = "unknown";
        private Map<String, Object> data  = new HashMap<>();
        private Instant         timestamp = Instant.now();

        private Builder(EventType type, LogEvent origin) {
            if (type == null)   throw new IllegalArgumentException("EventType no puede ser null");
            if (origin == null) throw new IllegalArgumentException("LogEvent de origen no puede ser null");
            this.type   = type;
            this.origin = origin;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder data(Map<String, Object> data) {
            this.data = data != null ? data : new HashMap<>();
            return this;
        }

        public Builder addData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Event build() {
            return new Event(this);
        }
    }
}
