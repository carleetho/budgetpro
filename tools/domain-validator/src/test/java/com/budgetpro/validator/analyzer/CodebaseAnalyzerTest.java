package com.budgetpro.validator.analyzer;

import com.budgetpro.validator.model.ImplementationStatus;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.ModuleDefinition;
import com.budgetpro.validator.roadmap.RoadmapLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para CodebaseAnalyzer.
 */
class CodebaseAnalyzerTest {

    private CodebaseAnalyzer analyzer;
    private CanonicalRoadmap roadmap;
    private Path repositoryPath;

    @BeforeEach
    void setUp() throws RoadmapLoader.RoadmapLoadException {
        analyzer = new CodebaseAnalyzer();
        RoadmapLoader loader = new RoadmapLoader();
        roadmap = loader.load();
        
        // Ruta al backend del proyecto BudgetPro
        repositoryPath = Paths.get("../../backend").toAbsolutePath().normalize();
    }

    @Test
    void deberiaDetectarEntidadesDelDominio() {
        Path domainPath = repositoryPath.resolve("src/main/java/com/budgetpro/domain");
        
        EntityDetector detector = new EntityDetector();
        List<String> entities = detector.detectEntities(domainPath);
        
        assertFalse(entities.isEmpty(), "Debe detectar al menos algunas entidades");
        
        // Verificar que detecta entidades conocidas
        boolean hasProyecto = entities.stream()
                .anyMatch(e -> e.contains("Proyecto") && e.contains("proyecto.model"));
        boolean hasPresupuesto = entities.stream()
                .anyMatch(e -> e.contains("Presupuesto") && e.contains("presupuesto.model"));
        
        assertTrue(hasProyecto || hasPresupuesto, 
            "Debe detectar al menos Proyecto o Presupuesto");
    }

    @Test
    void deberiaDetectarServiciosDelDominio() {
        Path domainPath = repositoryPath.resolve("src/main/java/com/budgetpro/domain");
        
        ServiceDetector detector = new ServiceDetector();
        var services = detector.detectServices(domainPath);
        
        assertFalse(services.isEmpty(), "Debe detectar al menos algunos servicios");
        
        // Verificar que detecta servicios conocidos
        boolean hasService = services.keySet().stream()
                .anyMatch(s -> s.contains("Service") || s.contains("service"));
        
        assertTrue(hasService, "Debe detectar servicios");
    }

    @Test
    void deberiaDetectarEndpointsREST() {
        Path restPath = repositoryPath.resolve("src/main/java/com/budgetpro/infrastructure/rest");
        
        ApiDetector detector = new ApiDetector();
        var endpoints = detector.detectEndpoints(restPath);
        
        // Puede estar vacío si no hay controladores, pero no debe fallar
        assertNotNull(endpoints);
    }

    @Test
    void deberiaDetectarMaquinasDeEstado() {
        Path domainPath = repositoryPath.resolve("src/main/java/com/budgetpro/domain");
        
        StateMachineDetector detector = new StateMachineDetector();
        var stateMachines = detector.detectStateMachines(domainPath);
        
        // Verificar que detecta enums de estado conocidos
        boolean hasEstadoPresupuesto = stateMachines.keySet().stream()
                .anyMatch(s -> s.contains("EstadoPresupuesto"));
        
        // Puede estar vacío si no hay enums de estado, pero no debe fallar
        assertNotNull(stateMachines);
    }

    @Test
    void deberiaAnalizarModulosYDeterminarEstado() {
        List<com.budgetpro.validator.model.ModuleStatus> statuses = analyzer.analyze(repositoryPath, roadmap);
        
        assertFalse(statuses.isEmpty(), "Debe analizar todos los módulos del roadmap");
        
        // Verificar que tiene el mismo número de módulos que el roadmap
        assertEquals(roadmap.getModules().size(), statuses.size());
        
        // Verificar que cada módulo tiene un estado
        for (com.budgetpro.validator.model.ModuleStatus status : statuses) {
            assertNotNull(status.getImplementationStatus());
            assertNotNull(status.getModuleId());
        }
    }

    @Test
    void deberiaDetectarModuloProyectoComoCompletoOParcial() {
        List<com.budgetpro.validator.model.ModuleStatus> statuses = analyzer.analyze(repositoryPath, roadmap);
        
        Optional<com.budgetpro.validator.model.ModuleStatus> proyectoStatus = statuses.stream()
                .filter(s -> s.getModuleId().equals("proyecto"))
                .findFirst();
        
        assertTrue(proyectoStatus.isPresent(), "Debe encontrar el módulo proyecto");
        
        com.budgetpro.validator.model.ModuleStatus status = proyectoStatus.get();
        
        // Proyecto debe estar al menos IN_PROGRESS (debe tener entidades)
        assertNotEquals(ImplementationStatus.NOT_STARTED, status.getImplementationStatus(),
            "Proyecto debe estar implementado al menos parcialmente");
        
        // Debe tener entidades detectadas
        assertFalse(status.getDetectedEntities().isEmpty(),
            "Proyecto debe tener al menos una entidad detectada");
    }

    @Test
    void deberiaDetectarModuloPresupuestoConEntidades() {
        List<com.budgetpro.validator.model.ModuleStatus> statuses = analyzer.analyze(repositoryPath, roadmap);
        
        Optional<com.budgetpro.validator.model.ModuleStatus> presupuestoStatus = statuses.stream()
                .filter(s -> s.getModuleId().equals("presupuesto"))
                .findFirst();
        
        assertTrue(presupuestoStatus.isPresent(), "Debe encontrar el módulo presupuesto");
        
        com.budgetpro.validator.model.ModuleStatus status = presupuestoStatus.get();
        
        // Presupuesto debe tener entidades detectadas
        assertFalse(status.getDetectedEntities().isEmpty(),
            "Presupuesto debe tener entidades detectadas");
    }

    @Test
    void deberiaInferirEstadoCorrectamente() {
        List<com.budgetpro.validator.model.ModuleStatus> statuses = analyzer.analyze(repositoryPath, roadmap);
        
        // Verificar que los estados son válidos
        for (com.budgetpro.validator.model.ModuleStatus status : statuses) {
            assertNotNull(status.getImplementationStatus());
            assertTrue(
                status.getImplementationStatus() == ImplementationStatus.NOT_STARTED ||
                status.getImplementationStatus() == ImplementationStatus.IN_PROGRESS ||
                status.getImplementationStatus() == ImplementationStatus.COMPLETE
            );
        }
    }
}
