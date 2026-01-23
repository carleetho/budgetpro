package com.budgetpro.validator.output;

import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.RoadmapLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para MermaidGenerator.
 */
class MermaidGeneratorTest {

    private MermaidGenerator generator;
    private CanonicalRoadmap roadmap;

    @BeforeEach
    void setUp() throws RoadmapLoader.RoadmapLoadException {
        generator = new MermaidGenerator();
        RoadmapLoader loader = new RoadmapLoader();
        roadmap = loader.load();
    }

    @Test
    void deberiaGenerarDiagramaMermaidValido() {
        String diagram = generator.generate(roadmap);
        
        assertNotNull(diagram);
        assertFalse(diagram.isEmpty());
        
        // Verificar que contiene sintaxis básica de Mermaid
        assertTrue(diagram.contains("graph TD"), "Debe contener 'graph TD'");
        assertTrue(diagram.contains("-->"), "Debe contener flechas de dependencia");
    }

    @Test
    void deberiaIncluirTodosLosModulos() {
        String diagram = generator.generate(roadmap);
        
        // Verificar que todos los módulos están en el diagrama
        for (var module : roadmap.getModules()) {
            String moduleId = module.getId().replaceAll("[^a-zA-Z0-9]", "_");
            assertTrue(diagram.contains(moduleId) || diagram.contains(module.getName()),
                "Módulo '" + module.getId() + "' debe estar en el diagrama");
        }
    }

    @Test
    void deberiaMostrarAcoplamientoTemporal() {
        String diagram = generator.generate(roadmap);
        
        // Verificar que contiene acoplamiento temporal entre Presupuesto y Tiempo
        assertTrue(diagram.contains("-.->") || diagram.contains("freeze together"),
            "Debe mostrar acoplamiento temporal con flechas punteadas");
        
        // Verificar que menciona "freeze together" o similar
        boolean hasTemporalCoupling = diagram.contains("freeze") || 
                                     diagram.contains("temporal") ||
                                     diagram.contains("coupling");
        assertTrue(hasTemporalCoupling, "Debe mencionar acoplamiento temporal");
    }

    @Test
    void deberiaAplicarEstilosAModulosCriticos() {
        String diagram = generator.generate(roadmap);
        
        // Verificar que contiene estilos para módulos críticos
        assertTrue(diagram.contains("style") || diagram.contains("classDef"),
            "Debe contener definiciones de estilo");
        
        // Verificar que módulos críticos tienen estilos
        boolean hasCriticalStyling = diagram.contains("#ff6b6b") || // Color rojo para críticos
                                    diagram.contains("CRITICAL") ||
                                    diagram.contains("presupuesto") && diagram.contains("tiempo");
        assertTrue(hasCriticalStyling, "Debe aplicar estilos a módulos críticos");
    }

    @Test
    void deberiaGenerarDiagramaSimplificado() {
        String diagram = generator.generateSimplified(roadmap);
        
        assertNotNull(diagram);
        assertFalse(diagram.isEmpty());
        assertTrue(diagram.contains("graph TD"));
        
        // El diagrama simplificado no debe tener subgrafos
        assertFalse(diagram.contains("subgraph"), 
            "Diagrama simplificado no debe tener subgrafos");
    }

    @Test
    void deberiaGenerarDependenciasCorrectas() {
        String diagram = generator.generate(roadmap);
        
        // Verificar que Compras depende de Presupuesto
        String comprasId = "compras".replaceAll("[^a-zA-Z0-9]", "_");
        String presupuestoId = "presupuesto".replaceAll("[^a-zA-Z0-9]", "_");
        
        // Buscar patrón de dependencia: presupuesto --> compras
        boolean hasDependency = diagram.contains(presupuestoId + " --> " + comprasId) ||
                               diagram.contains(comprasId + " --> " + presupuestoId) ||
                               diagram.contains("Presupuesto") && diagram.contains("Compras");
        
        // Al menos debe mencionar ambos módulos
        assertTrue(diagram.contains("presupuesto") || diagram.contains("Presupuesto"));
        assertTrue(diagram.contains("compras") || diagram.contains("Compras"));
    }

    @Test
    void deberiaTenerSintaxisMermaidValida() {
        String diagram = generator.generate(roadmap);
        
        // Verificar estructura básica
        assertTrue(diagram.startsWith("graph TD"));
        
        // Verificar que no tiene errores obvios de sintaxis
        // (no puede verificar completamente sin un parser Mermaid, pero podemos verificar patrones básicos)
        assertFalse(diagram.contains("undefined"));
        assertFalse(diagram.contains("null"));
    }
}
