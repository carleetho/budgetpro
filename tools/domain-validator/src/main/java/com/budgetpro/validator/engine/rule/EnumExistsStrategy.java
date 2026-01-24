package com.budgetpro.validator.engine.rule;

import com.budgetpro.validator.engine.ViolationBuilder;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.ValidationRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for validating that an enum exists with required values.
 */
public class EnumExistsStrategy implements ValidationRuleStrategy {
    
    @Override
    public Violation execute(ModuleStatus moduleStatus, ValidationRule rule, ValidationContext context) {
        String target = rule.getTarget();
        List<String> requiredValues = rule.getRequiredValues();
        boolean required = rule.getRequired();
        
        List<String> actualValues = context.getAllStateMachines().get(target);
        
        if (actualValues == null && required) {
            return ViolationBuilder.validationRuleViolation(
                    moduleStatus.getModuleId(),
                    "enum_exists",
                    target,
                    true
            ).build();
        }
        
        if (actualValues != null && requiredValues != null && !requiredValues.isEmpty()) {
            List<String> missingValues = new ArrayList<>(requiredValues);
            missingValues.removeAll(actualValues);
            
            if (!missingValues.isEmpty()) {
                return ViolationBuilder.validationRuleViolation(
                        moduleStatus.getModuleId(),
                        "enum_exists",
                        target,
                        required
                ).message(String.format("Enum '%s' existe pero faltan valores: %s", target, String.join(", ", missingValues)))
                .suggestion(String.format("Agregar valores faltantes a '%s': %s", target, String.join(", ", missingValues)))
                .detectedVsExpected(actualValues, requiredValues)
                .build();
            }
        }
        
        return null;
    }
    
    @Override
    public String getRuleType() {
        return "enum_exists";
    }
}
