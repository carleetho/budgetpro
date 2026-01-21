package com.budgetpro.validator.roadmap;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para RoadmapLoader.
 */
class RoadmapLoaderTest {

    @Test
    void deberiaCargarRoadmapDesdeRecursos() throws RoadmapLoader.RoadmapLoadException {
        RoadmapLoader loader = new RoadmapLoader();
        CanonicalRoadmap roadmap = loader.load();
        
        assertNotNull(roadmap);
        assertNotNull(roadmap.getVersion());
        assertFalse(roadmap.getModules().isEmpty());
    }

    @Test
    void deberiaCargarTodosLosModulos() throws RoadmapLoader.RoadmapLoadException {
        RoadmapLoader loader = new RoadmapLoader();
        CanonicalRoadmap roadmap = loader.load();
        
        // Verificar que se cargaron todos los módulos esperados
        assertTrue(roadmap.hasModule("proyecto"));
        assertTrue(roadmap.hasModule("presupuesto"));
        assertTrue(roadmap.hasModule("tiempo"));
        assertTrue(roadmap.hasModule("compras"));
        assertTrue(roadmap.hasModule("inventarios"));
        assertTrue(roadmap.hasModule("rrhh"));
        assertTrue(roadmap.hasModule("estimacion"));
        assertTrue(roadmap.hasModule("evm"));
        assertTrue(roadmap.hasModule("cambios"));
        assertTrue(roadmap.hasModule("alertas"));
        assertTrue(roadmap.hasModule("billetera"));
        assertTrue(roadmap.hasModule("catalogo"));
        
        // Verificar que hay al menos 10 módulos
        assertTrue(roadmap.getModules().size() >= 10);
    }

    @Test
    void deberiaTenerPrincipioBaselineCodificado() throws RoadmapLoader.RoadmapLoadException {
        RoadmapLoader loader = new RoadmapLoader();
        CanonicalRoadmap roadmap = loader.load();
        
        // Verificar que el principio de baseline está codificado
        assertTrue(roadmap.hasBaselinePrincipleEncoded(), 
            "Baseline principle (Presupuesto + Tiempo freeze together) debe estar codificado");
        
        // Verificar constraints específicos
        var presupuesto = roadmap.getPresupuestoModule();
        assertTrue(presupuesto.isPresent());
        assertTrue(presupuesto.get().hasTemporalCouplingWith("tiempo"));
        
        var tiempo = roadmap.getTiempoModule();
        assertTrue(tiempo.isPresent());
        assertTrue(tiempo.get().hasTemporalCouplingWith("presupuesto"));
    }

    @Test
    void deberiaValidarEstructuraDelRoadmap() throws RoadmapLoader.RoadmapLoadException {
        RoadmapLoader loader = new RoadmapLoader();
        CanonicalRoadmap roadmap = loader.load();
        
        // Validar que no hay errores de estructura
        List<String> errors = roadmap.validate();
        assertTrue(errors.isEmpty(), 
            "Roadmap debe ser válido. Errores: " + String.join(", ", errors));
    }

    @Test
    void deberiaObtenerModuloPorId() throws RoadmapLoader.RoadmapLoadException {
        RoadmapLoader loader = new RoadmapLoader();
        CanonicalRoadmap roadmap = loader.load();
        
        var proyecto = roadmap.getModuleById("proyecto");
        assertTrue(proyecto.isPresent());
        assertEquals("Proyecto", proyecto.get().getName());
        assertEquals("foundation", proyecto.get().getPhase());
        assertEquals("CRITICAL", proyecto.get().getPriority());
    }

    @Test
    void deberiaObtenerModulosPorFase() throws RoadmapLoader.RoadmapLoadException {
        RoadmapLoader loader = new RoadmapLoader();
        CanonicalRoadmap roadmap = loader.load();
        
        var foundationModules = roadmap.getModulesByPhase("foundation");
        assertFalse(foundationModules.isEmpty());
        
        var executionModules = roadmap.getModulesByPhase("execution");
        assertFalse(executionModules.isEmpty());
        
        var analysisModules = roadmap.getModulesByPhase("analysis");
        assertFalse(analysisModules.isEmpty());
    }

    @Test
    void deberiaTenerDependenciasCorrectas() throws RoadmapLoader.RoadmapLoadException {
        RoadmapLoader loader = new RoadmapLoader();
        CanonicalRoadmap roadmap = loader.load();
        
        var compras = roadmap.getModuleById("compras");
        assertTrue(compras.isPresent());
        
        // Compras debe depender de Presupuesto y Proyecto
        assertTrue(compras.get().getDependencies().contains("presupuesto"));
        assertTrue(compras.get().getDependencies().contains("proyecto"));
        
        // Compras debe habilitar Inventarios y EVM
        assertTrue(compras.get().getEnables().contains("inventarios"));
        assertTrue(compras.get().getEnables().contains("evm"));
    }

    @Test
    void deberiaTenerConstraintsYValidationRules() throws RoadmapLoader.RoadmapLoadException {
        RoadmapLoader loader = new RoadmapLoader();
        CanonicalRoadmap roadmap = loader.load();
        
        var compras = roadmap.getModuleById("compras");
        assertTrue(compras.isPresent());
        
        // Compras debe tener constraints
        assertFalse(compras.get().getConstraints().isEmpty());
        
        // Compras debe tener validation rules
        assertFalse(compras.get().getValidationRules().isEmpty());
    }
}
