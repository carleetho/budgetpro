package com.budgetpro.validator.engine.rule;

import com.budgetpro.validator.engine.ViolationBuilder;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.ValidationRule;

/**
 * Strategy for validating that a relationship exists between two entities.
 * 
 * Searches for entities across all modules, not just the current module.
 */
public class RelationshipExistsStrategy implements ValidationRuleStrategy {
    
    @Override
    public Violation execute(ModuleStatus moduleStatus, ValidationRule rule, ValidationContext context) {
        String source = rule.getSource();
        String target = rule.getTarget();
        boolean required = rule.getRequired();
        
        // Search for source across all modules
        boolean sourceExists = context.getAllModuleStatuses().values().stream()
                .flatMap(ms -> ms.getDetectedEntities().stream())
                .anyMatch(e -> e.contains(source));
        
        // Search for target across all modules
        boolean targetExists = context.getAllModuleStatuses().values().stream()
                .flatMap(ms -> ms.getDetectedEntities().stream())
                .anyMatch(e -> e.contains(target));
        
        if (required && (!sourceExists || !targetExists)) {
            String missing = !sourceExists ? source : target;
            return ViolationBuilder.validationRuleViolation(
                    moduleStatus.getModuleId(),
                    "relationship_exists",
                    String.format("%s -> %s", source, target),
                    true
            ).message(String.format("Relación requerida entre '%s' y '%s' no puede establecerse: '%s' no existe", source, target, missing))
            .build();
        }
        
        return null;
    }
    
    @Override
    public String getRuleType() {
        return "relationship_exists";
    }
}
