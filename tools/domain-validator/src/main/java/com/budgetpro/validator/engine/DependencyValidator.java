package com.budgetpro.validator.engine;

import com.budgetpro.validator.model.DependencyType;
import com.budgetpro.validator.model.ImplementationStatus;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.ModuleDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Valida el orden de desarrollo de módulos según sus dependencias.
 */
public class DependencyValidator {
    
    /**
     * Valida que todas las dependencias de un módulo estén implementadas.
     * 
     * @param moduleDef Definición del módulo a validar
     * @param moduleStatuses Estados de todos los módulos
     * @param roadmap Roadmap canónico completo
     * @return Lista de violaciones de dependencias
     */
    public List<Violation> validateDependencies(
            ModuleDefinition moduleDef,
            Map<String, ModuleStatus> moduleStatuses,
            CanonicalRoadmap roadmap) {
        
        List<Violation> violations = new ArrayList<>();
        
        // Obtener el estado del módulo actual
        Optional<ModuleStatus> currentModuleStatus = Optional.ofNullable(moduleStatuses.get(moduleDef.getId()));
        ImplementationStatus currentStatus = currentModuleStatus
                .map(ModuleStatus::getImplementationStatus)
                .orElse(ImplementationStatus.NOT_STARTED);
        
        for (String depModuleId : moduleDef.getDependencies()) {
            Optional<ModuleStatus> depStatus = Optional.ofNullable(moduleStatuses.get(depModuleId));
            
            if (depStatus.isEmpty()) {
                // Dependencia no encontrada en el análisis
                violations.add(createMissingDependencyViolation(moduleDef.getId(), depModuleId, DependencyType.DATA_DEPENDENCY).build());
                continue;
            }
            
            ModuleStatus dep = depStatus.get();
            
            // Verificar que la dependencia esté COMPLETE si el módulo está en desarrollo
            // Si el módulo está IN_PROGRESS o COMPLETE, requiere que sus dependencias estén COMPLETE
            if (currentStatus != ImplementationStatus.NOT_STARTED) {
                if (dep.getImplementationStatus() != ImplementationStatus.COMPLETE) {
                    String chain = buildDependencyChain(depModuleId, roadmap, moduleStatuses);
                    String suggestion = String.format("Implementar primero el módulo '%s' antes de continuar con '%s'", depModuleId, moduleDef.getId());
                    if (!chain.equals(depModuleId)) {
                        suggestion += String.format(". Cadena de dependencias: %s", chain);
                    }
                    violations.add(createMissingDependencyViolation(moduleDef.getId(), depModuleId, DependencyType.DATA_DEPENDENCY)
                            .dependencyChain(chain)
                            .suggestion(suggestion)
                            .build());
                }
            } else if (dep.getImplementationStatus() == ImplementationStatus.NOT_STARTED) {
                // Si el módulo no ha empezado, solo verificar que la dependencia no esté NOT_STARTED
                String chain = buildDependencyChain(depModuleId, roadmap, moduleStatuses);
                String suggestion = String.format("Implementar primero el módulo '%s' antes de continuar con '%s'", depModuleId, moduleDef.getId());
                if (!chain.equals(depModuleId)) {
                    suggestion += String.format(". Cadena de dependencias: %s", chain);
                }
                violations.add(createMissingDependencyViolation(moduleDef.getId(), depModuleId, DependencyType.DATA_DEPENDENCY)
                        .dependencyChain(chain)
                        .suggestion(suggestion)
                        .build());
            }
        }
        
        return violations;
    }

    /**
     * Construye una cadena de dependencias desde el módulo raíz hasta el módulo objetivo.
     */
    private String buildDependencyChain(String targetModuleId, CanonicalRoadmap roadmap, Map<String, ModuleStatus> moduleStatuses) {
        List<String> chain = new ArrayList<>();
        buildChainRecursive(targetModuleId, roadmap, moduleStatuses, chain, new ArrayList<>());
        
        if (chain.isEmpty()) {
            return targetModuleId;
        }
        
        return String.join(" → ", chain);
    }

    /**
     * Construye recursivamente la cadena de dependencias.
     */
    private void buildChainRecursive(
            String moduleId,
            CanonicalRoadmap roadmap,
            Map<String, ModuleStatus> moduleStatuses,
            List<String> chain,
            List<String> visited) {
        
        if (visited.contains(moduleId)) {
            return; // Evitar ciclos
        }
        
        visited.add(moduleId);
        Optional<ModuleDefinition> moduleDef = roadmap.getModuleById(moduleId);
        
        if (moduleDef.isEmpty()) {
            chain.add(moduleId);
            return;
        }
        
        List<String> dependencies = moduleDef.get().getDependencies();
        
        if (dependencies.isEmpty()) {
            // Módulo raíz
            chain.add(moduleId);
            return;
        }
        
        // Construir cadena desde la primera dependencia
        String firstDep = dependencies.get(0);
        buildChainRecursive(firstDep, roadmap, moduleStatuses, chain, visited);
        chain.add("→");
        chain.add(moduleId);
    }

    /**
     * Crea una violación de dependencia faltante.
     */
    private ViolationBuilder createMissingDependencyViolation(String moduleId, String missingDep, DependencyType depType) {
        return ViolationBuilder.missingDependency(moduleId, missingDep, depType);
    }

    /**
     * Valida que un módulo no se desarrolle prematuramente (antes de que sus dependencias estén completas).
     * 
     * @param moduleDef Definición del módulo
     * @param moduleStatus Estado del módulo
     * @param moduleStatuses Estados de todos los módulos
     * @param roadmap Roadmap canónico
     * @return Lista de violaciones de desarrollo prematuro
     */
    public List<Violation> validatePrematureDevelopment(
            ModuleDefinition moduleDef,
            ModuleStatus moduleStatus,
            Map<String, ModuleStatus> moduleStatuses,
            CanonicalRoadmap roadmap) {
        
        List<Violation> violations = new ArrayList<>();
        
        // Si el módulo está IN_PROGRESS o COMPLETE, verificar que sus dependencias estén COMPLETE
        if (moduleStatus.getImplementationStatus() != ImplementationStatus.NOT_STARTED) {
            for (String depModuleId : moduleDef.getDependencies()) {
                Optional<ModuleStatus> depStatus = Optional.ofNullable(moduleStatuses.get(depModuleId));
                
                if (depStatus.isPresent()) {
                    ModuleStatus dep = depStatus.get();
                    
                    // Si la dependencia no está completa, es una advertencia
                    if (dep.getImplementationStatus() != ImplementationStatus.COMPLETE) {
                        violations.add(
                                ViolationBuilder.missingDependency(moduleDef.getId(), depModuleId, DependencyType.BUSINESS_LOGIC)
                                        .severity(com.budgetpro.validator.model.ViolationSeverity.WARNING)
                                        .message(String.format("Módulo '%s' está en desarrollo pero su dependencia '%s' no está completa (%s). " +
                                                "Se recomienda completar '%s' primero para evitar problemas de integración.",
                                                moduleDef.getId(), depModuleId, dep.getImplementationStatus(), depModuleId))
                                        .suggestion(String.format("Completar implementación de '%s' antes de continuar con '%s'", depModuleId, moduleDef.getId()))
                                        .blocking(false)
                                        .build()
                        );
                    }
                }
            }
        }
        
        return violations;
    }
}
