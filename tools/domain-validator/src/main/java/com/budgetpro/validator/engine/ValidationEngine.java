package com.budgetpro.validator.engine;

import com.budgetpro.validator.analyzer.CodebaseAnalyzer;
import com.budgetpro.validator.model.ValidationResult;
import com.budgetpro.validator.model.ValidationStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.ModuleDefinition;
import com.budgetpro.validator.roadmap.RoadmapLoader;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Motor de validación principal que orquesta todos los validadores.
 */
public class ValidationEngine {
    
    private final ValidationRuleExecutor ruleExecutor;
    private final DependencyValidator dependencyValidator;
    private final ConstraintValidator constraintValidator;
    
    public ValidationEngine() {
        this.ruleExecutor = new ValidationRuleExecutor();
        this.dependencyValidator = new DependencyValidator();
        this.constraintValidator = new ConstraintValidator();
    }

    /**
     * Ejecuta la validación completa del código contra el roadmap canónico.
     * 
     * @param repositoryPath Ruta al directorio del repositorio
     * @return Resultado de la validación con violaciones y estados de módulos
     */
    public ValidationResult validate(Path repositoryPath) {
        try {
            // Cargar roadmap canónico
            RoadmapLoader roadmapLoader = new RoadmapLoader();
            CanonicalRoadmap roadmap = roadmapLoader.load();
            
            // Analizar código fuente
            CodebaseAnalyzer analyzer = new CodebaseAnalyzer();
            List<com.budgetpro.validator.model.ModuleStatus> moduleStatuses = analyzer.analyze(repositoryPath, roadmap);
            
            // Convertir a mapa para acceso rápido
            Map<String, com.budgetpro.validator.model.ModuleStatus> moduleStatusMap = moduleStatuses.stream()
                    .collect(Collectors.toMap(
                            com.budgetpro.validator.model.ModuleStatus::getModuleId,
                            status -> status
                    ));
            
            // Obtener datos adicionales del analizador para validación de reglas
            Map<String, List<String>> allStateMachines = getStateMachines(analyzer, repositoryPath);
            List<String> allRepositories = getRepositories(analyzer, repositoryPath);
            
            // Crear resultado de validación
            ValidationResult result = new ValidationResult(
                    repositoryPath.toString(),
                    ValidationStatus.PASSED
            );
            
            // Agregar estados de módulos
            for (com.budgetpro.validator.model.ModuleStatus status : moduleStatuses) {
                result.addModuleStatus(status);
            }
            
            // Validar cada módulo
            List<Violation> allViolations = new ArrayList<>();
            
            for (ModuleDefinition moduleDef : roadmap.getModules()) {
                com.budgetpro.validator.model.ModuleStatus moduleStatus = moduleStatusMap.get(moduleDef.getId());
                
                if (moduleStatus == null) {
                    // Módulo no encontrado en análisis (no debería pasar)
                    continue;
                }
                
                // 1. Validar reglas de validación
                List<Violation> ruleViolations = ruleExecutor.executeRules(
                        moduleStatus,
                        moduleDef.getValidationRules(),
                        allStateMachines,
                        allRepositories,
                        moduleStatusMap
                );
                allViolations.addAll(ruleViolations);
                
                // 2. Validar dependencias
                List<Violation> dependencyViolations = dependencyValidator.validateDependencies(
                        moduleDef,
                        moduleStatusMap,
                        roadmap
                );
                allViolations.addAll(dependencyViolations);
                
                // 3. Validar desarrollo prematuro
                List<Violation> prematureViolations = dependencyValidator.validatePrematureDevelopment(
                        moduleDef,
                        moduleStatus,
                        moduleStatusMap,
                        roadmap
                );
                allViolations.addAll(prematureViolations);
                
                // 4. Validar constraints
                List<Violation> constraintViolations = constraintValidator.validateConstraints(
                        moduleDef,
                        moduleStatus,
                        moduleStatusMap,
                        roadmap
                );
                allViolations.addAll(constraintViolations);
            }
            
            // 5. Validar principio de baseline (especial)
            Violation baselineViolation = constraintValidator.validateBaselinePrinciple(roadmap, moduleStatusMap);
            if (baselineViolation != null) {
                allViolations.add(baselineViolation);
            }
            
            // Agregar todas las violaciones al resultado
            for (Violation violation : allViolations) {
                result.addViolation(violation);
            }
            
            // Determinar estado final
            determineValidationStatus(result);
            
            return result;
            
        } catch (RoadmapLoader.RoadmapLoadException e) {
            ValidationResult errorResult = new ValidationResult(
                    repositoryPath.toString(),
                    ValidationStatus.ERROR
            );
            errorResult.addViolation(
                    ViolationBuilder.validationRuleViolation("system", "roadmap_load", e.getMessage(), true)
                            .message("Error al cargar roadmap canónico: " + e.getMessage())
                            .build()
            );
            return errorResult;
        } catch (Exception e) {
            ValidationResult errorResult = new ValidationResult(
                    repositoryPath.toString(),
                    ValidationStatus.ERROR
            );
            errorResult.addViolation(
                    ViolationBuilder.validationRuleViolation("system", "validation_error", e.getMessage(), true)
                            .message("Error durante la validación: " + e.getMessage())
                            .build()
            );
            return errorResult;
        }
    }

    /**
     * Obtiene todas las máquinas de estado detectadas.
     */
    private Map<String, List<String>> getStateMachines(CodebaseAnalyzer analyzer, Path repositoryPath) {
        // Crear detector temporal para obtener state machines
        com.budgetpro.validator.analyzer.StateMachineDetector detector = 
                new com.budgetpro.validator.analyzer.StateMachineDetector();
        Path domainPath = repositoryPath.resolve("src/main/java/com/budgetpro/domain");
        return detector.detectStateMachines(domainPath);
    }

    /**
     * Obtiene todos los repositorios detectados.
     */
    private List<String> getRepositories(CodebaseAnalyzer analyzer, Path repositoryPath) {
        // Crear detector temporal para obtener repositorios
        com.budgetpro.validator.analyzer.IntegrationPointDetector detector = 
                new com.budgetpro.validator.analyzer.IntegrationPointDetector();
        Path domainPath = repositoryPath.resolve("src/main/java/com/budgetpro/domain");
        return detector.detectRepositories(domainPath);
    }

    /**
     * Determina el estado final de la validación basándose en las violaciones.
     */
    private void determineValidationStatus(ValidationResult result) {
        if (result.hasCriticalViolations()) {
            result.setStatus(ValidationStatus.CRITICAL_VIOLATIONS);
        } else if (result.hasWarnings()) {
            result.setStatus(ValidationStatus.WARNINGS);
        } else {
            result.setStatus(ValidationStatus.PASSED);
        }
    }
}
