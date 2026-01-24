package com.budgetpro.validator.engine.rule;

import com.budgetpro.validator.model.ModuleStatus;

import java.util.List;
import java.util.Map;

/**
 * Context object containing all data needed for validation rule execution.
 * 
 * This centralizes context passing and makes it easier to add new context
 * without changing method signatures.
 */
public class ValidationContext {
    private final Map<String, List<String>> allStateMachines;
    private final List<String> allRepositories;
    private final Map<String, ModuleStatus> allModuleStatuses;
    
    public ValidationContext(
            Map<String, List<String>> allStateMachines,
            List<String> allRepositories,
            Map<String, ModuleStatus> allModuleStatuses) {
        this.allStateMachines = allStateMachines;
        this.allRepositories = allRepositories;
        this.allModuleStatuses = allModuleStatuses;
    }
    
    public Map<String, List<String>> getAllStateMachines() {
        return allStateMachines;
    }
    
    public List<String> getAllRepositories() {
        return allRepositories;
    }
    
    public Map<String, ModuleStatus> getAllModuleStatuses() {
        return allModuleStatuses;
    }
}
