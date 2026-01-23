package com.budgetpro.validator.engine;

import com.budgetpro.validator.model.ImplementationStatus;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.DependencyConstraint;
import com.budgetpro.validator.roadmap.ModuleDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Valida constraints de dependencias, especialmente acoplamiento temporal.
 */
public class ConstraintValidator {
    
    /**
     * Valida todos los constraints de un módulo.
     * 
     * @param moduleDef Definición del módulo
     * @param moduleStatus Estado del módulo
     * @param moduleStatuses Estados de todos los módulos
     * @param roadmap Roadmap canónico
     * @return Lista de violaciones de constraints
     */
    public List<Violation> validateConstraints(
            ModuleDefinition moduleDef,
            ModuleStatus moduleStatus,
            Map<String, ModuleStatus> moduleStatuses,
            CanonicalRoadmap roadmap) {
        
        List<Violation> violations = new ArrayList<>();
        
        for (DependencyConstraint constraint : moduleDef.getConstraints()) {
            Violation violation = validateConstraint(constraint, moduleDef, moduleStatus, moduleStatuses, roadmap);
            if (violation != null) {
                violations.add(violation);
            }
        }
        
        return violations;
    }

    /**
     * Valida un constraint individual.
     */
    private Violation validateConstraint(
            DependencyConstraint constraint,
            ModuleDefinition moduleDef,
            ModuleStatus moduleStatus,
            Map<String, ModuleStatus> moduleStatuses,
            CanonicalRoadmap roadmap) {
        
        String constraintType = constraint.getType();
        
        return switch (constraintType) {
            case "temporal_coupling", "temporal" -> validateTemporalCoupling(
                    constraint, moduleDef, moduleStatus, moduleStatuses, roadmap);
            case "state", "state_dependency" -> validateStateDependency(
                    constraint, moduleDef, moduleStatus);
            case "state_transition" -> validateStateTransition(
                    constraint, moduleDef, moduleStatus);
            case "data_integrity" -> validateDataIntegrity(
                    constraint, moduleDef, moduleStatus);
            default -> null;
        };
    }

    /**
     * Valida acoplamiento temporal (ej: Budget + Schedule freeze together).
     */
    private Violation validateTemporalCoupling(
            DependencyConstraint constraint,
            ModuleDefinition moduleDef,
            ModuleStatus moduleStatus,
            Map<String, ModuleStatus> moduleStatuses,
            CanonicalRoadmap roadmap) {
        
        String coupledWith = constraint.getCoupledWith();
        if (coupledWith == null || coupledWith.isEmpty()) {
            return null;
        }
        
        // Verificar que el módulo acoplado existe en el roadmap
        Optional<ModuleDefinition> coupledModuleDef = roadmap.getModuleById(coupledWith);
        if (coupledModuleDef.isEmpty()) {
            return null; // Módulo acoplado no existe en roadmap
        }
        
        // Verificar que ambos módulos tienen el constraint de acoplamiento temporal
        boolean thisHasCoupling = moduleDef.hasTemporalCouplingWith(coupledWith);
        boolean coupledHasCoupling = coupledModuleDef.get().hasTemporalCouplingWith(moduleDef.getId());
        
        if (!thisHasCoupling || !coupledHasCoupling) {
            return ViolationBuilder.temporalCouplingViolation(
                    moduleDef.getId(),
                    coupledWith,
                    constraint.getRule()
            ).suggestion(String.format(
                    "Implementar acoplamiento temporal bidireccional: cuando '%s' se congela, '%s' debe congelarse automáticamente, y viceversa. " +
                    "Usar eventos de dominio o transacciones distribuidas para garantizar consistencia.",
                    coupledWith, moduleDef.getId()))
            .build();
        }
        
        // Verificar que ambos módulos estén implementados si uno está implementado
        Optional<ModuleStatus> thisModuleStatus = Optional.ofNullable(moduleStatuses.get(moduleDef.getId()));
        Optional<ModuleStatus> coupledModuleStatus = Optional.ofNullable(moduleStatuses.get(coupledWith));
        
        if (thisModuleStatus.isPresent() && coupledModuleStatus.isPresent()) {
            ModuleStatus thisStatus = thisModuleStatus.get();
            ModuleStatus coupledStatus = coupledModuleStatus.get();
            
            // Si uno está implementado (IN_PROGRESS o COMPLETE) y el otro está NOT_STARTED,
            // es una violación de acoplamiento temporal
            boolean thisImplemented = thisStatus.getImplementationStatus() != ImplementationStatus.NOT_STARTED;
            boolean coupledImplemented = coupledStatus.getImplementationStatus() != ImplementationStatus.NOT_STARTED;
            
            if (thisImplemented && !coupledImplemented) {
                return ViolationBuilder.temporalCouplingViolation(
                        moduleDef.getId(),
                        coupledWith,
                        constraint.getRule()
                ).message(String.format(
                        "Módulo '%s' está implementado pero su módulo acoplado '%s' no está implementado. " +
                        "El acoplamiento temporal requiere que ambos se implementen juntos.",
                        moduleDef.getId(), coupledWith))
                .suggestion(String.format(
                        "Implementar '%s' junto con '%s' para mantener el acoplamiento temporal. " +
                        "Cuando '%s' se congela, '%s' debe congelarse automáticamente.",
                        coupledWith, moduleDef.getId(), moduleDef.getId(), coupledWith))
                .build();
            } else if (!thisImplemented && coupledImplemented) {
                return ViolationBuilder.temporalCouplingViolation(
                        moduleDef.getId(),
                        coupledWith,
                        constraint.getRule()
                ).message(String.format(
                        "Módulo '%s' no está implementado pero su módulo acoplado '%s' está implementado. " +
                        "El acoplamiento temporal requiere que ambos se implementen juntos.",
                        moduleDef.getId(), coupledWith))
                .suggestion(String.format(
                        "Implementar '%s' junto con '%s' para mantener el acoplamiento temporal. " +
                        "Cuando '%s' se congela, '%s' debe congelarse automáticamente.",
                        moduleDef.getId(), coupledWith, coupledWith, moduleDef.getId()))
                .build();
            }
        }
        
        // Si ambos módulos están implementados, verificar que el acoplamiento esté implementado
        // Esto requiere análisis más profundo del código (buscar métodos de freeze acoplados)
        // Por ahora, solo verificamos que el constraint esté definido
        
        return null;
    }

    /**
     * Valida dependencia de estado (ej: Presupuesto.estado === CONGELADO).
     */
    private Violation validateStateDependency(
            DependencyConstraint constraint,
            ModuleDefinition moduleDef,
            ModuleStatus moduleStatus) {
        
        String rule = constraint.getRule();
        
        // Extraer estado requerido del rule (simplificado)
        // Ejemplo: "Presupuesto.estado === CONGELADO"
        if (rule.contains("===") || rule.contains("=")) {
            String[] parts = rule.split("===");
            if (parts.length == 2) {
                String requiredState = parts[1].trim();
                
                // Verificar que el módulo tiene una máquina de estado con ese estado
                // Por ahora, solo verificamos que el módulo esté implementado
                if (moduleStatus.getImplementationStatus() == ImplementationStatus.NOT_STARTED) {
                    return ViolationBuilder.stateDependencyViolation(
                            moduleDef.getId(),
                            requiredState,
                            extractEntityName(rule)
                    ).build();
                }
            }
        }
        
        return null;
    }

    /**
     * Valida transición de estado (ej: BORRADOR → CONGELADO genera snapshot).
     */
    private Violation validateStateTransition(
            DependencyConstraint constraint,
            ModuleDefinition moduleDef,
            ModuleStatus moduleStatus) {
        
        // Verificar que el módulo tiene servicios que implementan la transición
        // Por ahora, solo verificamos que el módulo esté implementado
        if (moduleStatus.getImplementationStatus() == ImplementationStatus.NOT_STARTED) {
            return ViolationBuilder.validationRuleViolation(
                    moduleDef.getId(),
                    "state_transition",
                    constraint.getRule(),
                    true
            ).message(String.format("Transición de estado requerida no implementada: %s", constraint.getRule()))
            .suggestion(String.format("Implementar transición de estado: %s", constraint.getRule()))
            .build();
        }
        
        return null;
    }

    /**
     * Valida integridad de datos (ej: No activities without Partidas).
     */
    private Violation validateDataIntegrity(
            DependencyConstraint constraint,
            ModuleDefinition moduleDef,
            ModuleStatus moduleStatus) {
        
        // Verificar que el módulo tiene las entidades necesarias para la integridad
        if (moduleStatus.getImplementationStatus() == ImplementationStatus.NOT_STARTED) {
            return ViolationBuilder.validationRuleViolation(
                    moduleDef.getId(),
                    "data_integrity",
                    constraint.getRule(),
                    true
            ).message(String.format("Regla de integridad de datos no implementada: %s", constraint.getRule()))
            .suggestion(String.format("Implementar validación de integridad: %s", constraint.getRule()))
            .build();
        }
        
        return null;
    }

    /**
     * Extrae el nombre de la entidad de una regla de constraint.
     */
    private String extractEntityName(String rule) {
        // Simplificado: buscar patrón "EntityName.estado" o "EntityName.estado"
        if (rule.contains(".")) {
            return rule.split("\\.")[0].trim();
        }
        return "Entidad";
    }

    /**
     * Valida el principio de baseline (Presupuesto + Tiempo freeze together).
     * 
     * @param roadmap Roadmap canónico
     * @param moduleStatuses Estados de todos los módulos
     * @return Violación si el principio no está implementado correctamente
     */
    public Violation validateBaselinePrinciple(CanonicalRoadmap roadmap, Map<String, ModuleStatus> moduleStatuses) {
        // Verificar que el principio está codificado en el roadmap
        if (!roadmap.hasBaselinePrincipleEncoded()) {
            return ViolationBuilder.temporalCouplingViolation(
                    "presupuesto",
                    "tiempo",
                    "Baseline principle: Budget + Schedule must freeze together"
            ).message("Principio de baseline no está codificado en el roadmap canónico")
            .suggestion("Agregar constraints de acoplamiento temporal entre Presupuesto y Tiempo en el roadmap")
            .build();
        }
        
        // Verificar que ambos módulos están implementados
        Optional<ModuleStatus> presupuestoStatus = Optional.ofNullable(moduleStatuses.get("presupuesto"));
        Optional<ModuleStatus> tiempoStatus = Optional.ofNullable(moduleStatuses.get("tiempo"));
        
        if (presupuestoStatus.isPresent() && tiempoStatus.isPresent()) {
            ModuleStatus presupuesto = presupuestoStatus.get();
            ModuleStatus tiempo = tiempoStatus.get();
            
            // Si ambos están implementados pero no hay evidencia de acoplamiento, generar advertencia
            // (Esto requeriría análisis más profundo del código para verificar métodos de freeze acoplados)
            if (presupuesto.getImplementationStatus() != ImplementationStatus.NOT_STARTED &&
                tiempo.getImplementationStatus() != ImplementationStatus.NOT_STARTED) {
                
                // Verificar que ambos tienen servicios de freeze
                boolean presupuestoHasFreeze = presupuesto.getDetectedServices().stream()
                        .anyMatch(s -> s.toLowerCase().contains("freeze") || s.toLowerCase().contains("congelar"));
                boolean tiempoHasFreeze = tiempo.getDetectedServices().stream()
                        .anyMatch(s -> s.toLowerCase().contains("freeze") || s.toLowerCase().contains("congelar"));
                
                if (!presupuestoHasFreeze || !tiempoHasFreeze) {
                    return ViolationBuilder.temporalCouplingViolation(
                            "presupuesto",
                            "tiempo",
                            "Baseline principle: Budget + Schedule must freeze together"
                    ).message("Módulos Presupuesto y Tiempo implementados pero falta mecanismo de freeze acoplado")
                    .suggestion("Implementar métodos de freeze acoplados: cuando Presupuesto.congelar() se ejecuta, " +
                            "debe disparar automáticamente ProgramaObra.congelar() usando eventos de dominio o transacciones")
                    .severity(com.budgetpro.validator.model.ViolationSeverity.WARNING)
                    .blocking(false)
                    .build();
                }
            }
        }
        
        return null;
    }
}
