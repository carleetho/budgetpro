package com.budgetpro.validator.engine;

import com.budgetpro.validator.engine.rule.*;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.ValidationRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ejecuta reglas de validación del roadmap contra el código detectado.
 * 
 * Refactorizado para usar Strategy Pattern, haciendo el código más mantenible
 * y fácil de entender para agentes de IA. Cada tipo de regla tiene su propia
 * estrategia de validación.
 */
public class ValidationRuleExecutor {
    
    private final Map<String, ValidationRuleStrategy> strategies;
    
    public ValidationRuleExecutor() {
        this.strategies = new HashMap<>();
        registerStrategies();
    }
    
    /**
     * Registra todas las estrategias de validación disponibles.
     */
    private void registerStrategies() {
        strategies.put("entity_exists", new EntityExistsStrategy());
        strategies.put("service_exists", new ServiceExistsStrategy());
        strategies.put("state_machine_exists", new StateMachineExistsStrategy());
        strategies.put("enum_exists", new EnumExistsStrategy());
        strategies.put("port_exists", new PortExistsStrategy());
        strategies.put("relationship_exists", new RelationshipExistsStrategy());
        strategies.put("reference_exists", new ReferenceExistsStrategy());
    }
    
    /**
     * Ejecuta todas las reglas de validación para un módulo.
     * 
     * @param moduleStatus Estado del módulo con elementos detectados
     * @param validationRules Lista de reglas a ejecutar
     * @param allStateMachines Map de todas las máquinas de estado detectadas
     * @param allRepositories Lista de todos los repositorios detectados
     * @param allModuleStatuses Map de todos los estados de módulos (para buscar entidades en otros módulos)
     * @return Lista de violaciones encontradas
     */
    public List<Violation> executeRules(
            ModuleStatus moduleStatus,
            List<ValidationRule> validationRules,
            Map<String, List<String>> allStateMachines,
            List<String> allRepositories,
            Map<String, ModuleStatus> allModuleStatuses) {
        
        List<Violation> violations = new ArrayList<>();
        
        for (ValidationRule rule : validationRules) {
            Violation violation = executeRule(moduleStatus, rule, allStateMachines, allRepositories, allModuleStatuses);
            if (violation != null) {
                violations.add(violation);
            }
        }
        
        return violations;
    }

    /**
     * Ejecuta una regla de validación individual usando Strategy Pattern.
     * 
     * @param moduleStatus Estado del módulo con elementos detectados
     * @param rule Regla de validación a ejecutar
     * @param allStateMachines Map de todas las máquinas de estado detectadas
     * @param allRepositories Lista de todos los repositorios detectados
     * @param allModuleStatuses Map de todos los estados de módulos
     * @return Violación si la regla no se cumple, null si se cumple
     */
    private Violation executeRule(
            ModuleStatus moduleStatus,
            ValidationRule rule,
            Map<String, List<String>> allStateMachines,
            List<String> allRepositories,
            Map<String, ModuleStatus> allModuleStatuses) {
        
        String ruleType = rule.getType();
        ValidationRuleStrategy strategy = strategies.get(ruleType);
        
        if (strategy == null) {
            // Unknown rule type - log warning but don't fail
            return null;
        }
        
        ValidationContext context = new ValidationContext(
                allStateMachines,
                allRepositories,
                allModuleStatuses
        );
        
        return strategy.execute(moduleStatus, rule, context);
    }
}
