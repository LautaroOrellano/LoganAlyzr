#!/usr/bin/env bash
# notify.sh — Notificación ante evento de alta latencia.
# Invocado por LoganAlyzr ScriptAction cuando se detecta HIGH_LATENCY.
#
# Variables de entorno disponibles (inyectadas por ScriptAction):
#   EVENT_ID        — UUID del evento
#   EVENT_TYPE      — Tipo de evento (ej: HIGH_LATENCY)
#   EVENT_SOURCE    — Fuente del evento
#   EVENT_TIMESTAMP — Timestamp ISO-8601
#   EVENT_MESSAGE   — Mensaje del log de origen
#   EVENT_LEVEL     — Nivel del log (ERROR, WARN, etc.)

set -euo pipefail

LOG_FILE="logs/agent-actions.log"
mkdir -p "$(dirname "$LOG_FILE")"

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

echo "[$TIMESTAMP] NOTIFY | type=$EVENT_TYPE | source=$EVENT_SOURCE | msg=$EVENT_MESSAGE" >> "$LOG_FILE"

echo "Notificación registrada: $EVENT_TYPE en $EVENT_SOURCE"
exit 0
