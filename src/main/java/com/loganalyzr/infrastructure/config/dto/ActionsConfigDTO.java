package com.loganalyzr.infrastructure.config.dto;

import java.util.Collections;
import java.util.List;

/**
 * DTO raíz del archivo actions.json.
 */
public class ActionsConfigDTO {

    private List<ActionRuleDTO> rules;

    public ActionsConfigDTO() {}

    public List<ActionRuleDTO> getRules() {
        return rules != null ? rules : Collections.emptyList();
    }

    public void setRules(List<ActionRuleDTO> rules) {
        this.rules = rules;
    }
}
