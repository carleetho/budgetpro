package com.budgetpro.validator.engine.rule;

import com.budgetpro.validator.engine.ViolationBuilder;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.ValidationRule;

/**
 * Strategy for validating that a required entity exists.
 * 
 * Searches for the entity across all modules, not just the current module,
 * since some entities may be in related modules (e.g., APUSnapshot in catalogo).
 */
public class EntityExistsStrategy implements ValidationRuleStrategy {
    
    @Override
    public Violation execute(ModuleStatus moduleStatus, ValidationRule rule, ValidationContext context) {
        String target = rule.getTarget();
        boolean required = rule.getRequired();
        
        // Search across all modules
        boolean exists = context.getAllModuleStatuses().values().stream()
                .flatMap(ms -> ms.getDetectedEntities().stream())
                .anyMatch(e -> e.contains(target) || target.contains(e));
        
        if (!exists && required) {
            return ViolationBuilder.validationRuleViolation(
                    moduleStatus.getModuleId(),
                    "entity_exists",
                    target,
                    true
            ).build();
        }
        
        return null;
    }
    
    @Override
    public String getRuleType() {
        return "entity_exists";
    }
}
