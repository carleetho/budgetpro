package com.budgetpro.validator.engine;

import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.ValidationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ejecuta reglas de validación del roadmap contra el código detectado.
 */
public class ValidationRuleExecutor {
    
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
     * Ejecuta una regla de validación individual.
     */
    private Violation executeRule(
            ModuleStatus moduleStatus,
            ValidationRule rule,
            Map<String, List<String>> allStateMachines,
            List<String> allRepositories,
            Map<String, ModuleStatus> allModuleStatuses) {
        
        String ruleType = rule.getType();
        String target = rule.getTarget();
        boolean required = rule.getRequired();
        
        return switch (ruleType) {
            case "entity_exists" -> validateEntityExists(moduleStatus, target, required, allModuleStatuses);
            case "service_exists" -> validateServiceExists(moduleStatus, target, rule.getRequiredMethods(), required);
            case "state_machine_exists" -> validateStateMachineExists(moduleStatus, target, rule.getRequiredStates(), allStateMachines, required);
            case "enum_exists" -> validateEnumExists(moduleStatus, target, rule.getRequiredValues(), allStateMachines, required);
            case "port_exists" -> validatePortExists(moduleStatus, target, allRepositories, required);
            case "relationship_exists" -> validateRelationshipExists(moduleStatus, rule.getSource(), rule.getTarget(), required, allModuleStatuses);
            case "reference_exists" -> validateReferenceExists(moduleStatus, rule.getSource(), rule.getTarget(), rule.getField(), required, allModuleStatuses);
            default -> null;
        };
    }

    /**
     * Valida que una entidad existe.
     * Busca la entidad en todos los módulos, no solo en el módulo actual,
     * ya que algunas entidades pueden estar en módulos relacionados (ej: APUSnapshot en catalogo).
     */
    private Violation validateEntityExists(
            ModuleStatus moduleStatus, 
            String target, 
            boolean required,
            Map<String, ModuleStatus> allModuleStatuses) {
        
        // Buscar en todos los módulos
        boolean exists = allModuleStatuses.values().stream()
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

    /**
     * Valida que un servicio existe y tiene los métodos requeridos.
     */
    private Violation validateServiceExists(ModuleStatus moduleStatus, String target, List<String> requiredMethods, boolean required) {
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
        
        // TODO: Validar métodos específicos si se requiere análisis más profundo
        
        return null;
    }

    /**
     * Valida que una máquina de estado existe con los estados requeridos.
     */
    private Violation validateStateMachineExists(
            ModuleStatus moduleStatus,
            String target,
            List<String> requiredStates,
            Map<String, List<String>> allStateMachines,
            boolean required) {
        
        List<String> actualStates = allStateMachines.get(target);
        
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

    /**
     * Valida que un enum existe con los valores requeridos.
     */
    private Violation validateEnumExists(
            ModuleStatus moduleStatus,
            String target,
            List<String> requiredValues,
            Map<String, List<String>> allStateMachines,
            boolean required) {
        
        List<String> actualValues = allStateMachines.get(target);
        
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

    /**
     * Valida que un puerto (repositorio) existe.
     */
    private Violation validatePortExists(ModuleStatus moduleStatus, String target, List<String> allRepositories, boolean required) {
        boolean exists = allRepositories.stream()
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

    /**
     * Valida que existe una relación entre dos entidades.
     * Busca las entidades en todos los módulos, no solo en el módulo actual.
     */
    private Violation validateRelationshipExists(
            ModuleStatus moduleStatus, 
            String source, 
            String target, 
            boolean required,
            Map<String, ModuleStatus> allModuleStatuses) {
        
        // Buscar source en todos los módulos
        boolean sourceExists = allModuleStatuses.values().stream()
                .flatMap(ms -> ms.getDetectedEntities().stream())
                .anyMatch(e -> e.contains(source));
        
        // Buscar target en todos los módulos
        boolean targetExists = allModuleStatuses.values().stream()
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

    /**
     * Valida que existe una referencia entre entidades.
     * Busca las entidades en todos los módulos, no solo en el módulo actual.
     */
    private Violation validateReferenceExists(
            ModuleStatus moduleStatus, 
            String source, 
            String target, 
            String field, 
            boolean required,
            Map<String, ModuleStatus> allModuleStatuses) {
        
        // Buscar source en todos los módulos
        boolean sourceExists = allModuleStatuses.values().stream()
                .flatMap(ms -> ms.getDetectedEntities().stream())
                .anyMatch(e -> e.contains(source));
        
        // Buscar target en todos los módulos
        boolean targetExists = allModuleStatuses.values().stream()
                .flatMap(ms -> ms.getDetectedEntities().stream())
                .anyMatch(e -> e.contains(target));
        
        if (required && (!sourceExists || !targetExists)) {
            String missing = !sourceExists ? source : target;
            return ViolationBuilder.validationRuleViolation(
                    moduleStatus.getModuleId(),
                    "reference_exists",
                    String.format("%s.%s -> %s", source, field != null ? field : "?", target),
                    true
            ).message(String.format("Referencia requerida '%s.%s -> %s' no puede establecerse: '%s' no existe", 
                    source, field != null ? field : "?", target, missing))
            .build();
        }
        
        return null;
    }
}
