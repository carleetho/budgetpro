package com.budgetpro.validator.engine.rule;

import com.budgetpro.validator.engine.ViolationBuilder;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.ValidationRule;

/**
 * Strategy for validating that a port (repository) exists.
 */
public class PortExistsStrategy implements ValidationRuleStrategy {
    
    @Override
    public Violation execute(ModuleStatus moduleStatus, ValidationRule rule, ValidationContext context) {
        String target = rule.getTarget();
        boolean required = rule.getRequired();
        
        boolean exists = context.getAllRepositories().stream()
                .anyMatch(r -> r.contains(target) || target.contains(r));
        
        if (!exists && required) {
            return ViolationBuilder.validationRuleViolation(
                    moduleStatus.getModuleId(),
                    "port_exists",
                    target,
                    true
            ).build();
        }
        
        return null;
    }
    
    @Override
    public String getRuleType() {
        return "port_exists";
    }
}
