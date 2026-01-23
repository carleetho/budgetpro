package com.budgetpro.validator.output;

import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.RoadmapLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para MarkdownGenerator.
 */
class MarkdownGeneratorTest {

    private MarkdownGenerator generator;
    private CanonicalRoadmap roadmap;

    @BeforeEach
    void setUp() throws RoadmapLoader.RoadmapLoadException {
        generator = new MarkdownGenerator();
        RoadmapLoader loader = new RoadmapLoader();
        roadmap = loader.load();
    }

    @Test
    void deberiaGenerarDocumentoMarkdownValido() {
        String markdown = generator.generate(roadmap);
        
        assertNotNull(markdown);
        assertFalse(markdown.isEmpty());
        
        // Verificar estructura básica
        assertTrue(markdown.contains("# BudgetPro Canonical Development Roadmap"),
            "Debe contener título principal");
        assertTrue(markdown.contains("## Overview"),
            "Debe contener sección Overview");
        assertTrue(markdown.contains("## Baseline Principle"),
            "Debe contener sección Baseline Principle");
    }

    @Test
    void deberiaIncluirVersionYTimestamp() {
        String markdown = generator.generate(roadmap);
        
        assertTrue(markdown.contains("**Version**: " + roadmap.getVersion()),
            "Debe incluir versión del roadmap");
        assertTrue(markdown.contains("**Generated**: "),
            "Debe incluir timestamp de generación");
    }

    @Test
    void deberiaIncluirTodasLasFases() {
        String markdown = generator.generate(roadmap);
        
        assertTrue(markdown.contains("## Phase: Foundation"),
            "Debe incluir fase Foundation");
        assertTrue(markdown.contains("## Phase: Execution"),
            "Debe incluir fase Execution");
        assertTrue(markdown.contains("## Phase: Analysis"),
            "Debe incluir fase Analysis");
    }

    @Test
    void deberiaIncluirTodosLosModulos() {
        String markdown = generator.generate(roadmap);
        
        // Verificar que todos los módulos están documentados
        for (var module : roadmap.getModules()) {
            assertTrue(markdown.contains("### " + module.getName()),
                "Módulo '" + module.getName() + "' debe estar documentado");
        }
    }

    @Test
    void deberiaDocumentarPrincipioDeBaseline() {
        String markdown = generator.generate(roadmap);
        
        // Verificar que menciona el principio de baseline
        assertTrue(markdown.contains("Baseline Principle") ||
                   markdown.contains("freeze together") ||
                   markdown.contains("Presupuesto") && markdown.contains("Tiempo"),
            "Debe documentar el principio de baseline");
        
        // Verificar que menciona acoplamiento temporal
        assertTrue(markdown.contains("temporal") || markdown.contains("coupling"),
            "Debe mencionar acoplamiento temporal");
    }

    @Test
    void deberiaIncluirJustificaciones() {
        String markdown = generator.generate(roadmap);
        
        // Verificar que contiene justificaciones
        assertTrue(markdown.contains("**Justification**:") ||
                   markdown.contains("Principio de construcción"),
            "Debe incluir justificaciones para los módulos");
    }

    @Test
    void deberiaIncluirDependencias() {
        String markdown = generator.generate(roadmap);
        
        // Verificar que contiene sección de dependencias
        assertTrue(markdown.contains("**Dependencies**:") ||
                   markdown.contains("**Enables**:"),
            "Debe incluir secciones de dependencias");
    }

    @Test
    void deberiaIncluirConstraintsCriticos() {
        String markdown = generator.generate(roadmap);
        
        // Verificar que contiene constraints críticos
        assertTrue(markdown.contains("**Critical Constraints**:") ||
                   markdown.contains("CRITICAL"),
            "Debe incluir constraints críticos");
    }

    @Test
    void deberiaIncluirMustImplement() {
        String markdown = generator.generate(roadmap);
        
        // Verificar que contiene lista de Must Implement
        assertTrue(markdown.contains("**Must Implement**:") ||
                   markdown.contains("Entity") ||
                   markdown.contains("Service"),
            "Debe incluir lista de Must Implement");
    }

    @Test
    void deberiaIncluirPrincipiosDelDominio() {
        String markdown = generator.generate(roadmap);
        
        // Verificar que menciona principios del dominio de construcción
        assertTrue(markdown.contains("Principios del Dominio de Construcción") ||
                   markdown.contains("presupuesto aprobado") ||
                   markdown.contains("baseline"),
            "Debe incluir principios del dominio de construcción");
    }

    @Test
    void deberiaTenerFormatoMarkdownValido() {
        String markdown = generator.generate(roadmap);
        
        // Verificar estructura básica de Markdown
        assertTrue(markdown.startsWith("# "), "Debe empezar con H1");
        
        // Verificar que tiene múltiples niveles de encabezados
        long h2Count = markdown.lines()
                .filter(line -> line.startsWith("## "))
                .count();
        assertTrue(h2Count >= 3, "Debe tener al menos 3 secciones H2");
        
        // Verificar que tiene secciones H3 (módulos)
        long h3Count = markdown.lines()
                .filter(line -> line.startsWith("### "))
                .count();
        assertTrue(h3Count >= roadmap.getModules().size(),
            "Debe tener al menos un H3 por módulo");
    }

    @Test
    void deberiaIncluirAppendices() {
        String markdown = generator.generate(roadmap);
        
        assertTrue(markdown.contains("## Appendices") ||
                   markdown.contains("Validation") ||
                   markdown.contains("Exit Codes"),
            "Debe incluir sección de apéndices");
    }
}
