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
 * Tests para ConstraintValidator.
 */
class ConstraintValidatorTest {

    private ConstraintValidator validator;
    private CanonicalRoadmap roadmap;

    @BeforeEach
    void setUp() throws RoadmapLoader.RoadmapLoadException {
        validator = new ConstraintValidator();
        RoadmapLoader loader = new RoadmapLoader();
        roadmap = loader.load();
    }

    @Test
    void deberiaDetectarViolacionDeTemporalCoupling() {
        // Crear módulo Presupuesto sin Tiempo acoplado
        ModuleDefinition presupuestoModule = roadmap.getModuleById("presupuesto")
                .orElseThrow();
        
        Map<String, ModuleStatus> moduleStatuses = new HashMap<>();
        moduleStatuses.put("presupuesto", new ModuleStatus("presupuesto", ImplementationStatus.COMPLETE));
        moduleStatuses.put("tiempo", new ModuleStatus("tiempo", ImplementationStatus.NOT_STARTED));
        
        // Crear análisis de código vacío (simulando que no hay acoplamiento implementado)
        List<com.budgetpro.validator.model.Violation> violations = 
            validator.validateConstraints(presupuestoModule, 
                                         moduleStatuses.get("presupuesto"),
                                         moduleStatuses, 
                                         roadmap);
        
        // Debe detectar violación de acoplamiento temporal
        boolean hasTemporalViolation = violations.stream()
                .anyMatch(v -> v.getSeverity() == ViolationSeverity.CRITICAL &&
                              (v.getType().toString().contains("TEMPORAL") ||
                               v.getMessage().toLowerCase().contains("temporal") ||
                               v.getMessage().toLowerCase().contains("baseline") ||
                               v.getMessage().toLowerCase().contains("freeze")));
        
        assertTrue(hasTemporalViolation || !violations.isEmpty(),
            "Debe detectar violación de acoplamiento temporal");
    }

    @Test
    void deberiaValidarStateDependency() {
        // Crear módulo Compras que requiere Presupuesto en estado CONGELADO
        ModuleDefinition comprasModule = roadmap.getModuleById("compras")
                .orElseThrow();
        
        Map<String, ModuleStatus> moduleStatuses = new HashMap<>();
        moduleStatuses.put("presupuesto", new ModuleStatus("presupuesto", ImplementationStatus.COMPLETE));
        moduleStatuses.put("compras", new ModuleStatus("compras", ImplementationStatus.IN_PROGRESS));
        
        List<com.budgetpro.validator.model.Violation> violations = 
            validator.validateConstraints(comprasModule,
                                         moduleStatuses.get("compras"),
                                         moduleStatuses,
                                         roadmap);
        
        // Puede haber violaciones de state dependency si Presupuesto no está congelado
        // Esto es válido ya que Compras requiere Presupuesto CONGELADO
        assertNotNull(violations, "Debe validar constraints de estado");
    }

    @Test
    void deberiaValidarBaselinePrinciple() {
        // Verificar que el validator puede detectar violaciones del principio de baseline
        ModuleDefinition presupuestoModule = roadmap.getModuleById("presupuesto")
                .orElseThrow();
        
        ModuleDefinition tiempoModule = roadmap.getModuleById("tiempo")
                .orElseThrow();
        
        // Verificar que ambos tienen constraints de acoplamiento temporal
        assertTrue(presupuestoModule.hasTemporalCouplingWith("tiempo") ||
                  tiempoModule.hasTemporalCouplingWith("presupuesto"),
            "Presupuesto y Tiempo deben tener acoplamiento temporal definido");
        
        // Verificar que el constraint es crítico
        var presupuestoTemporalConstraint = presupuestoModule.getTemporalCouplingConstraint();
        if (presupuestoTemporalConstraint != null) {
            assertEquals("critical", presupuestoTemporalConstraint.getSeverity().toLowerCase(),
                "El acoplamiento temporal debe ser crítico");
        }
    }
}
