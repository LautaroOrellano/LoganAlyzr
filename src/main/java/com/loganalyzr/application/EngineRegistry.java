package com.loganalyzr.application;

import com.loganalyzr.core.service.DecisionEngine;
import com.loganalyzr.core.service.RuleEngine;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Registro central de engines con soporte de hot-reload.
 *
 * Problema que resuelve:
 *   El pipeline corre en múltiples workers concurrentes. Cuando el usuario
 *   modifica rules.json o actions.json, necesitamos reemplazar los engines
 *   sin detener el pipeline y sin que ningún worker vea un estado parcial.
 *
 * Solución:
 *   AtomicReference garantiza que el swap engine-viejo → engine-nuevo es
 *   atómico desde la perspectiva de los threads lectores. Un worker que
 *   empezó con el engine viejo termina con el viejo; el próximo log ya
 *   lee el nuevo. No hay ventana de estado inconsistente.
 *
 * Invariante:
 *   ruleEngine y decisionEngine nunca son null.
 *   update() solo se llama si AMBOS engines nuevos se construyeron exitosamente.
 */
public class EngineRegistry {

    private final AtomicReference<RuleEngine>    ruleEngine;
    private final AtomicReference<DecisionEngine> decisionEngine;
    private volatile Instant lastReloadedAt;

    public EngineRegistry(RuleEngine initialRuleEngine, DecisionEngine initialDecisionEngine) {
        this.ruleEngine     = new AtomicReference<>(initialRuleEngine);
        this.decisionEngine = new AtomicReference<>(initialDecisionEngine);
        this.lastReloadedAt = Instant.now();
    }

    /** Retorna el RuleEngine actual. Lectura lock-free. */
    public RuleEngine getRuleEngine() {
        return ruleEngine.get();
    }

    /** Retorna el DecisionEngine actual. Lectura lock-free. */
    public DecisionEngine getDecisionEngine() {
        return decisionEngine.get();
    }

    /**
     * Reemplaza ambos engines atómicamente.
     * Solo debe llamarse cuando ambos engines nuevos están completamente construidos.
     *
     * @param newRuleEngine     Nuevo engine de reglas. No puede ser null.
     * @param newDecisionEngine Nuevo engine de decisión. No puede ser null.
     */
    public void update(RuleEngine newRuleEngine, DecisionEngine newDecisionEngine) {
        if (newRuleEngine == null || newDecisionEngine == null) {
            throw new IllegalArgumentException("Los engines no pueden ser null.");
        }
        this.ruleEngine.set(newRuleEngine);
        this.decisionEngine.set(newDecisionEngine);
        this.lastReloadedAt = Instant.now();
        System.out.println("[EngineRegistry] ✓ Engines actualizados a las " + lastReloadedAt);
    }

    public Instant getLastReloadedAt() {
        return lastReloadedAt;
    }
}
