package com.loganalyzr.infrastructure.config.mapper;

import com.loganalyzr.core.model.EventType;
import com.loganalyzr.core.ports.Action;
import com.loganalyzr.infrastructure.actions.HttpAlertAction;
import com.loganalyzr.infrastructure.actions.ScriptAction;
import com.loganalyzr.infrastructure.config.dto.ActionDeclarationDTO;
import com.loganalyzr.infrastructure.config.dto.ActionRuleDTO;
import com.loganalyzr.infrastructure.config.dto.ActionsConfigDTO;
import com.loganalyzr.infrastructure.ui.LogAction;

import java.util.*;

/**
 * Traduce ActionsConfigDTO en las estructuras que el DecisionEngine necesita.
 *
 * Responsabilidades:
 *   1. Resolver EventType desde string (con manejo de tipos desconocidos).
 *   2. Instanciar cada Action con su configuración específica.
 *   3. Ignorar reglas disabled o con tipos desconocidos sin romper el arranque.
 *
 * ── Para agregar una nueva Action ───────────────────────────────────────────
 *   1. Implementar Action en infrastructure/actions/.
 *   2. Registrarla en createAction() con su nombre como case.
 *   3. Agregar su declaración en actions.json.
 * ────────────────────────────────────────────────────────────────────────────
 */
public class ActionFactory {

    /**
     * Construye el mapa EventType → List<Action> (en orden de declaración).
     */
    public Map<EventType, List<Action>> buildActionMap(ActionsConfigDTO config) {
        Map<EventType, List<Action>> actionMap = new EnumMap<>(EventType.class);

        for (ActionRuleDTO rule : config.getRules()) {
            if (!rule.isEnabled()) continue;

            EventType type = resolveEventType(rule.getEventType());
            if (type == null) continue;

            List<Action> actions = new ArrayList<>();
            for (ActionDeclarationDTO declaration : rule.getActions()) {
                Action action = createAction(declaration);
                if (action != null) {
                    actions.add(action);
                }
            }

            if (!actions.isEmpty()) {
                actionMap.put(type, Collections.unmodifiableList(actions));
            }
        }

        return Collections.unmodifiableMap(actionMap);
    }

    /**
     * Construye el mapa EventType → cooldownSeconds.
     */
    public Map<EventType, Integer> buildCooldownMap(ActionsConfigDTO config) {
        Map<EventType, Integer> cooldownMap = new EnumMap<>(EventType.class);

        for (ActionRuleDTO rule : config.getRules()) {
            if (!rule.isEnabled()) continue;

            EventType type = resolveEventType(rule.getEventType());
            if (type == null) continue;

            cooldownMap.put(type, rule.getCooldownSeconds());
        }

        return Collections.unmodifiableMap(cooldownMap);
    }

    /**
     * Crea la instancia de Action correspondiente al tipo declarado.
     * Pasa el mapa de config específico para que cada acción se auto-configure.
     */
    private Action createAction(ActionDeclarationDTO declaration) {
        if (declaration.getType() == null) {
            System.err.println("[ActionFactory] Declaración sin 'type'. Ignorada.");
            return null;
        }

        String type = declaration.getType().toUpperCase();
        Map<String, Object> config = declaration.getConfig();

        try {
            return switch (type) {
                case "LOG"        -> new LogAction();
                case "HTTP_ALERT" -> new HttpAlertAction(config);
                case "SCRIPT"     -> new ScriptAction(config);
                // Fase 6+: case "SLACK" -> new SlackAction(config);
                // Fase 7+: case "KUBECTL" -> new KubectlAction(config);
                default -> {
                    System.err.println("[ActionFactory] Tipo de acción desconocido: '" + type + "'. Ignorado.");
                    yield null;
                }
            };
        } catch (IllegalArgumentException e) {
            System.err.println("[ActionFactory] Config inválida para acción '" + type + "': " + e.getMessage());
            return null;
        }
    }

    private EventType resolveEventType(String name) {
        if (name == null) return null;
        try {
            return EventType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("[ActionFactory] EventType desconocido: '" + name + "'. Regla ignorada.");
            return null;
        }
    }
}
