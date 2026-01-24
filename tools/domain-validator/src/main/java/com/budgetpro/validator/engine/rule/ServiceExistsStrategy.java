package com.budgetpro.validator.engine.rule;

import com.budgetpro.validator.engine.ViolationBuilder;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.ValidationRule;

import java.util.List;

/**
 * Strategy for validating that a required service exists with required methods.
 */
public class ServiceExistsStrategy implements ValidationRuleStrategy {
    
    @Override
    public Violation execute(ModuleStatus moduleStatus, ValidationRule rule, ValidationContext context) {
        String target = rule.getTarget();
        List<String> requiredMethods = rule.getRequiredMethods();
        boolean required = rule.getRequired();
        
        boolean serviceExists = moduleStatus.getDetectedServices().stream()
                .anyMatch(s -> s.contains(target) || target.contains(s));
        
        if (!serviceExists && required) {
            return ViolationBuilder.validationRuleViolation(
                    moduleStatus.getModuleId(),
                    "service_exists",
                    target,
                    true
            ).suggestion(String.format("Implementar servicio '%s' con métodos: %s", target, 
                    requiredMethods != null ? String.join(", ", requiredMethods) : "N/A"))
            .build();
        }
        
        // TODO: Validate specific methods if deeper analysis is required
        
        return null;
    }
    
    @Override
    public String getRuleType() {
        return "service_exists";
    }
}
