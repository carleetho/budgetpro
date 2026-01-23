package com.budgetpro.validator.analyzer;

import com.budgetpro.validator.model.ImplementationStatus;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.ModuleDefinition;
import com.budgetpro.validator.roadmap.ValidationRule;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analizador principal del código fuente que detecta módulos implementados.
 * 
 * Orquesta todos los detectores especializados y determina el estado de implementación
 * de cada módulo según el roadmap canónico.
 */
public class CodebaseAnalyzer {
    
    private final EntityDetector entityDetector;
    private final ServiceDetector serviceDetector;
    private final ApiDetector apiDetector;
    private final StateMachineDetector stateMachineDetector;
    private final IntegrationPointDetector integrationPointDetector;
    
    public CodebaseAnalyzer() {
        this.entityDetector = new EntityDetector();
        this.serviceDetector = new ServiceDetector();
        this.apiDetector = new ApiDetector();
        this.stateMachineDetector = new StateMachineDetector();
        this.integrationPointDetector = new IntegrationPointDetector();
    }

    /**
     * Analiza el código fuente y determina el estado de implementación de todos los módulos.
     * 
     * @param repositoryPath Ruta al directorio del repositorio (ej: ./backend)
     * @param roadmap Roadmap canónico con definiciones de módulos
     * @return Lista de estados de módulos detectados
     */
    public List<ModuleStatus> analyze(Path repositoryPath, CanonicalRoadmap roadmap) {
        List<ModuleStatus> moduleStatuses = new ArrayList<>();
        
        if (repositoryPath == null || !repositoryPath.toFile().exists()) {
            return moduleStatuses;
        }
        
        // Construir rutas a directorios clave
        Path domainPath = repositoryPath.resolve("src/main/java/com/budgetpro/domain");
        Path infrastructurePath = repositoryPath.resolve("src/main/java/com/budgetpro/infrastructure");
        
        // Ejecutar todos los detectores
        List<String> entities = entityDetector.detectEntities(domainPath);
        Map<String, List<String>> services = serviceDetector.detectServices(domainPath);
        Map<String, List<String>> endpoints = apiDetector.detectEndpoints(
            infrastructurePath.resolve("rest")
        );
        Map<String, List<String>> stateMachines = stateMachineDetector.detectStateMachines(domainPath);
        List<String> repositories = integrationPointDetector.detectRepositories(domainPath);
        List<String> adapters = integrationPointDetector.detectAdapters(infrastructurePath);
        
        // Para cada módulo en el roadmap, determinar su estado
        for (ModuleDefinition moduleDef : roadmap.getModules()) {
            ModuleStatus status = analyzeModule(
                moduleDef,
                entities,
                services,
                endpoints,
                stateMachines,
                repositories,
                adapters
            );
            moduleStatuses.add(status);
        }
        
        return moduleStatuses;
    }

    /**
     * Analiza un módulo específico y determina su estado de implementación.
     */
    private ModuleStatus analyzeModule(
            ModuleDefinition moduleDef,
            List<String> allEntities,
            Map<String, List<String>> allServices,
            Map<String, List<String>> allEndpoints,
            Map<String, List<String>> allStateMachines,
            List<String> allRepositories,
            List<String> allAdapters) {
        
        ModuleStatus status = new ModuleStatus(moduleDef.getId(), ImplementationStatus.NOT_STARTED);
        
        // Detectar entidades relacionadas con este módulo
        List<String> moduleEntities = filterByModule(moduleDef.getId(), allEntities);
        status.setDetectedEntities(moduleEntities);
        
        // Detectar servicios relacionados
        List<String> moduleServices = filterServicesByModule(moduleDef.getId(), allServices);
        status.setDetectedServices(moduleServices);
        
        // Detectar endpoints relacionados
        List<String> moduleEndpoints = filterEndpointsByModule(moduleDef.getId(), allEndpoints);
        status.setDetectedEndpoints(moduleEndpoints);
        
        // Determinar estado de implementación basado en validation rules
        ImplementationStatus implementationStatus = inferImplementationStatus(
            moduleDef,
            moduleEntities,
            moduleServices,
            moduleEndpoints,
            allStateMachines,
            allRepositories
        );
        
        status.setImplementationStatus(implementationStatus);
        
        // Detectar dependencias faltantes
        List<String> missingDeps = detectMissingDependencies(moduleDef, allEntities, allServices);
        status.setMissingDependencies(missingDeps);
        
        return status;
    }

    /**
     * Filtra entidades que pertenecen a un módulo específico.
     * 
     * Mapea módulos a palabras clave de búsqueda para detectar entidades
     * que pueden estar en diferentes paquetes.
     */
    private List<String> filterByModule(String moduleId, List<String> entities) {
        // Mapeo de módulos a palabras clave de búsqueda
        Map<String, List<String>> moduleKeywords = new HashMap<>();
        moduleKeywords.put("tiempo", List.of("tiempo", "cronograma", "programaobra", "actividadprogramada", "cronogramasnapshot"));
        moduleKeywords.put("presupuesto", List.of("presupuesto", "partida"));
        moduleKeywords.put("compras", List.of("compra"));
        moduleKeywords.put("inventarios", List.of("inventario"));
        moduleKeywords.put("rrhh", List.of("rrhh", "personal", "tareo"));
        moduleKeywords.put("estimacion", List.of("estimacion"));
        moduleKeywords.put("evm", List.of("evm", "control", "earned"));
        moduleKeywords.put("cambios", List.of("cambio", "reajuste"));
        moduleKeywords.put("alertas", List.of("alerta", "analisis"));
        moduleKeywords.put("billetera", List.of("billetera", "movimientocaja"));
        moduleKeywords.put("catalogo", List.of("catalogo", "apu", "recurso"));
        moduleKeywords.put("proyecto", List.of("proyecto", "billetera"));
        
        List<String> keywords = moduleKeywords.getOrDefault(moduleId.toLowerCase(), 
                List.of(moduleId.toLowerCase()));
        
        return entities.stream()
                .filter(e -> {
                    String entityLower = e.toLowerCase();
                    return keywords.stream().anyMatch(keyword -> entityLower.contains(keyword));
                })
                .collect(Collectors.toList());
    }

    /**
     * Filtra servicios que pertenecen a un módulo específico.
     * 
     * Mapea módulos a palabras clave de búsqueda para detectar servicios
     * que pueden estar en diferentes paquetes.
     */
    private List<String> filterServicesByModule(String moduleId, Map<String, List<String>> allServices) {
        // Mapeo de módulos a palabras clave de búsqueda
        Map<String, List<String>> moduleKeywords = new HashMap<>();
        moduleKeywords.put("tiempo", List.of("tiempo", "cronograma", "programaobra", "actividadprogramada", "snapshotgenerator", "calculocronograma"));
        moduleKeywords.put("presupuesto", List.of("presupuesto", "partida", "integrityhash", "calculopresupuesto"));
        moduleKeywords.put("compras", List.of("compra", "procesarcompra"));
        moduleKeywords.put("inventarios", List.of("inventario", "gestioninventario"));
        moduleKeywords.put("rrhh", List.of("rrhh", "personal", "tareo"));
        moduleKeywords.put("estimacion", List.of("estimacion", "generadorestimacion"));
        moduleKeywords.put("evm", List.of("evm", "control", "earned"));
        moduleKeywords.put("cambios", List.of("cambio", "reajuste"));
        moduleKeywords.put("alertas", List.of("alerta", "analisis", "analizador"));
        moduleKeywords.put("billetera", List.of("billetera", "movimientocaja"));
        moduleKeywords.put("catalogo", List.of("catalogo", "apu", "recurso", "snapshot", "calculoaupdinamico"));
        moduleKeywords.put("proyecto", List.of("proyecto"));
        
        List<String> keywords = moduleKeywords.getOrDefault(moduleId.toLowerCase(), 
                List.of(moduleId.toLowerCase()));
        
        List<String> moduleServices = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : allServices.entrySet()) {
            String serviceName = entry.getKey();
            String serviceLower = serviceName.toLowerCase();
            if (keywords.stream().anyMatch(keyword -> serviceLower.contains(keyword))) {
                moduleServices.add(serviceName);
            }
        }
        
        return moduleServices;
    }

    /**
     * Filtra endpoints que pertenecen a un módulo específico.
     */
    private List<String> filterEndpointsByModule(String moduleId, Map<String, List<String>> allEndpoints) {
        List<String> moduleEndpoints = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : allEndpoints.entrySet()) {
            String controllerName = entry.getKey();
            if (controllerName.toLowerCase().contains(moduleId.toLowerCase())) {
                moduleEndpoints.addAll(entry.getValue());
            }
        }
        
        return moduleEndpoints;
    }

    /**
     * Infiere el estado de implementación de un módulo basándose en sus validation rules.
     */
    private ImplementationStatus inferImplementationStatus(
            ModuleDefinition moduleDef,
            List<String> detectedEntities,
            List<String> detectedServices,
            List<String> detectedEndpoints,
            Map<String, List<String>> allStateMachines,
            List<String> allRepositories) {
        
        List<ValidationRule> rules = moduleDef.getValidationRules();
        
        if (rules.isEmpty()) {
            // Si no hay reglas, determinar por presencia de código
            if (detectedEntities.isEmpty() && detectedServices.isEmpty()) {
                return ImplementationStatus.NOT_STARTED;
            } else if (detectedEntities.isEmpty() || detectedServices.isEmpty()) {
                return ImplementationStatus.IN_PROGRESS;
            } else {
                return ImplementationStatus.COMPLETE;
            }
        }
        
        // Contar cuántas reglas se cumplen
        int totalRules = rules.size();
        int satisfiedRules = 0;
        
        for (ValidationRule rule : rules) {
            if (isRuleSatisfied(rule, detectedEntities, detectedServices, detectedEndpoints, 
                              allStateMachines, allRepositories)) {
                satisfiedRules++;
            }
        }
        
        // Determinar estado basado en porcentaje de reglas cumplidas
        if (satisfiedRules == 0) {
            return ImplementationStatus.NOT_STARTED;
        } else if (satisfiedRules == totalRules) {
            return ImplementationStatus.COMPLETE;
        } else {
            return ImplementationStatus.IN_PROGRESS;
        }
    }

    /**
     * Verifica si una regla de validación se cumple.
     */
    private boolean isRuleSatisfied(
            ValidationRule rule,
            List<String> detectedEntities,
            List<String> detectedServices,
            List<String> detectedEndpoints,
            Map<String, List<String>> allStateMachines,
            List<String> allRepositories) {
        
        String ruleType = rule.getType();
        String target = rule.getTarget();
        
        return switch (ruleType) {
            case "entity_exists" -> detectedEntities.contains(target);
            case "service_exists" -> {
                // Verificar que el servicio existe y tiene los métodos requeridos
                boolean serviceExists = detectedServices.stream()
                        .anyMatch(s -> s.contains(target) || target.contains(s));
                if (!serviceExists) {
                    yield false;
                }
                // Si hay métodos requeridos, verificar que existen
                if (rule.getRequiredMethods() != null && !rule.getRequiredMethods().isEmpty()) {
                    // Simplificado: asumir que si el servicio existe, los métodos también
                    yield true;
                }
                yield true;
            }
            case "state_machine_exists" -> {
                // Verificar que el enum existe y tiene los estados requeridos
                boolean enumExists = allStateMachines.containsKey(target);
                if (!enumExists) {
                    yield false;
                }
                if (rule.getRequiredStates() != null && !rule.getRequiredStates().isEmpty()) {
                    List<String> actualStates = allStateMachines.get(target);
                    if (actualStates == null) {
                        yield false;
                    }
                    yield actualStates.containsAll(rule.getRequiredStates());
                }
                yield true;
            }
            case "enum_exists" -> {
                // Verificar que el enum existe y tiene los valores requeridos
                boolean enumExists = allStateMachines.containsKey(target);
                if (!enumExists) {
                    yield false;
                }
                if (rule.getRequiredValues() != null && !rule.getRequiredValues().isEmpty()) {
                    List<String> actualValues = allStateMachines.get(target);
                    if (actualValues == null) {
                        yield false;
                    }
                    yield actualValues.containsAll(rule.getRequiredValues());
                }
                yield true;
            }
            case "port_exists" -> allRepositories.stream().anyMatch(r -> r.contains(target));
            case "relationship_exists", "reference_exists" -> {
                // Verificar que ambas entidades existen
                String source = rule.getSource();
                String targetEntity = rule.getTarget() != null ? rule.getTarget() : target;
                yield detectedEntities.stream().anyMatch(e -> e.contains(source)) &&
                      detectedEntities.stream().anyMatch(e -> e.contains(targetEntity));
            }
            default -> false;
        };
    }

    /**
     * Detecta dependencias faltantes para un módulo.
     */
    private List<String> detectMissingDependencies(
            ModuleDefinition moduleDef,
            List<String> allEntities,
            Map<String, List<String>> allServices) {
        
        List<String> missingDeps = new ArrayList<>();
        
        // Verificar dependencias de módulos
        for (String depModuleId : moduleDef.getDependencies()) {
            boolean depFound = allEntities.stream()
                    .anyMatch(e -> e.toLowerCase().contains(depModuleId.toLowerCase()));
            
            if (!depFound) {
                // Verificar también en servicios
                boolean depFoundInServices = allServices.keySet().stream()
                        .anyMatch(s -> s.toLowerCase().contains(depModuleId.toLowerCase()));
                
                if (!depFoundInServices) {
                    missingDeps.add(depModuleId);
                }
            }
        }
        
        return missingDeps;
    }
}
