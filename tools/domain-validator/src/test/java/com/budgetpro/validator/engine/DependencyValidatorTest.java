package com.budgetpro.validator.engine;

import com.budgetpro.validator.model.ImplementationStatus;
import com.budgetpro.validator.model.ModuleStatus;
import com.budgetpro.validator.model.ViolationSeverity;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.ModuleDefinition;
import com.budgetpro.validator.roadmap.RoadmapLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para DependencyValidator.
 */
class DependencyValidatorTest {

    private DependencyValidator validator;
    private CanonicalRoadmap roadmap;

    @BeforeEach
    void setUp() throws RoadmapLoader.RoadmapLoadException {
        validator = new DependencyValidator();
        RoadmapLoader loader = new RoadmapLoader();
        roadmap = loader.load();
    }

    @Test
    void deberiaDetectarDependenciaFaltante() {
        // Crear módulo Compras que requiere Presupuesto
        ModuleDefinition comprasModule = roadmap.getModuleById("compras")
                .orElseThrow();
        
        // Crear map de statuses donde Presupuesto NO está completo
        Map<String, ModuleStatus> moduleStatuses = new HashMap<>();
        moduleStatuses.put("proyecto", new ModuleStatus("proyecto", ImplementationStatus.COMPLETE));
        moduleStatuses.put("presupuesto", new ModuleStatus("presupuesto", ImplementationStatus.IN_PROGRESS));
        moduleStatuses.put("compras", new ModuleStatus("compras", ImplementationStatus.IN_PROGRESS));
        
        // Validar dependencias
        List<com.budgetpro.validator.model.Violation> violations = 
            validator.validateDependencies(comprasModule, moduleStatuses, roadmap);
        
        // Debe detectar que Presupuesto no está completo
        assertFalse(violations.isEmpty(),
            "Debe detectar violación por dependencia faltante");
        
        boolean hasCriticalViolation = violations.stream()
                .anyMatch(v -> v.getSeverity() == ViolationSeverity.CRITICAL &&
                              v.getModuleId().equals("compras"));
        
        assertTrue(hasCriticalViolation,
            "Debe ser violación crítica cuando falta dependencia requerida");
    }

    @Test
    void deberiaPermitirDesarrolloSiDependenciasEstanCompletas() {
        // Crear módulo Compras con todas sus dependencias completas
        ModuleDefinition comprasModule = roadmap.getModuleById("compras")
                .orElseThrow();
        
        Map<String, ModuleStatus> moduleStatuses = new HashMap<>();
        moduleStatuses.put("proyecto", new ModuleStatus("proyecto", ImplementationStatus.COMPLETE));
        moduleStatuses.put("presupuesto", new ModuleStatus("presupuesto", ImplementationStatus.COMPLETE));
        moduleStatuses.put("compras", new ModuleStatus("compras", ImplementationStatus.IN_PROGRESS));
        
        List<com.budgetpro.validator.model.Violation> violations = 
            validator.validateDependencies(comprasModule, moduleStatuses, roadmap);
        
        // No debe haber violaciones críticas por dependencias faltantes
        boolean hasCriticalDependencyViolation = violations.stream()
                .anyMatch(v -> v.getSeverity() == ViolationSeverity.CRITICAL &&
                              v.getType().toString().contains("DEPENDENCY"));
        
        assertFalse(hasCriticalDependencyViolation,
            "No debe haber violaciones críticas si las dependencias están completas");
    }

    @Test
    void deberiaGenerarCadenaDeDependencias() {
        // Crear escenario donde falta una dependencia transitiva
        ModuleDefinition comprasModule = roadmap.getModuleById("compras")
                .orElseThrow();
        
        Map<String, ModuleStatus> moduleStatuses = new HashMap<>();
        // Proyecto completo, pero Presupuesto no
        moduleStatuses.put("proyecto", new ModuleStatus("proyecto", ImplementationStatus.COMPLETE));
        moduleStatuses.put("presupuesto", new ModuleStatus("presupuesto", ImplementationStatus.NOT_STARTED));
        moduleStatuses.put("compras", new ModuleStatus("compras", ImplementationStatus.IN_PROGRESS));
        
        List<com.budgetpro.validator.model.Violation> violations = 
            validator.validateDependencies(comprasModule, moduleStatuses, roadmap);
        
        // Las violaciones deben incluir información sobre la cadena de dependencias
        assertFalse(violations.isEmpty(),
            "Debe generar violaciones con cadena de dependencias");
        
        // Verificar que las sugerencias mencionan las dependencias
        boolean hasDependencyChain = violations.stream()
                .anyMatch(v -> v.getSuggestion() != null &&
                              (v.getSuggestion().contains("Presupuesto") ||
                               v.getSuggestion().contains("Proyecto") ||
                               v.getSuggestion().contains("→")));
        
        assertTrue(hasDependencyChain,
            "Las violaciones deben incluir cadena de dependencias en las sugerencias");
    }
}
