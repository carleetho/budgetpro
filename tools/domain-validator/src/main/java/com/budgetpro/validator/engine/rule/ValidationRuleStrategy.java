package com.budgetpro.validator.engine.rule;

import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.ValidationRule;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for validation rule execution.
 * 
 * Each validation rule type (entity_exists, service_exists, etc.) has its own
 * strategy implementation, making the validation logic more maintainable and
 * easier for AI agents to understand.
 */
public interface ValidationRuleStrategy {
    
    /**
     * Executes the validation rule and returns a violation if the rule is not satisfied.
     * 
     * @param moduleStatus Current module status with detected elements
     * @param rule The validation rule to execute
     * @param context Additional context needed for validation (state machines, repositories, etc.)
     * @return Violation if rule is violated, null if rule is satisfied
     */
    Violation execute(
            ModuleStatus moduleStatus,
            ValidationRule rule,
            ValidationContext context
    );
    
    /**
     * Returns the rule type this strategy handles.
     * 
     * @return Rule type (e.g., "entity_exists", "service_exists")
     */
    String getRuleType();
}
