package com.budgetpro.validator.engine.rule;

import com.budgetpro.validator.engine.ViolationBuilder;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.ValidationRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for validating that a state machine exists with required states.
 */
public class StateMachineExistsStrategy implements ValidationRuleStrategy {
    
    @Override
    public Violation execute(ModuleStatus moduleStatus, ValidationRule rule, ValidationContext context) {
        String target = rule.getTarget();
        List<String> requiredStates = rule.getRequiredStates();
        boolean required = rule.getRequired();
        
        List<String> actualStates = context.getAllStateMachines().get(target);
        
        if (actualStates == null && required) {
            return ViolationBuilder.validationRuleViolation(
                    moduleStatus.getModuleId(),
                    "state_machine_exists",
                    target,
                    true
            ).build();
        }
        
        if (actualStates != null && requiredStates != null && !requiredStates.isEmpty()) {
            List<String> missingStates = new ArrayList<>(requiredStates);
            missingStates.removeAll(actualStates);
            
            if (!missingStates.isEmpty()) {
                return ViolationBuilder.validationRuleViolation(
                        moduleStatus.getModuleId(),
                        "state_machine_exists",
                        target,
                        true
                ).message(String.format("Máquina de estado '%s' existe pero faltan estados: %s", target, String.join(", ", missingStates)))
                .suggestion(String.format("Agregar estados faltantes a '%s': %s", target, String.join(", ", missingStates)))
                .detectedVsExpected(actualStates, requiredStates)
                .build();
            }
        }
        
        return null;
    }
    
    @Override
    public String getRuleType() {
        return "state_machine_exists";
    }
}
