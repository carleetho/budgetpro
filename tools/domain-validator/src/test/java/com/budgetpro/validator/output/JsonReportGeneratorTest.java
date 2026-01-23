package com.budgetpro.validator.output;

import com.budgetpro.validator.model.*;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.RoadmapLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para JsonReportGenerator.
 */
class JsonReportGeneratorTest {

    private JsonReportGenerator generator;
    private CanonicalRoadmap roadmap;

    @BeforeEach
    void setUp() throws RoadmapLoader.RoadmapLoadException {
        generator = new JsonReportGenerator();
        RoadmapLoader loader = new RoadmapLoader();
        roadmap = loader.load();
    }

    @Test
    void deberiaGenerarJsonValido() throws IOException {
        ValidationResult result = createTestResult();
        String json = generator.generate(result, roadmap);
        
        assertNotNull(json);
        assertFalse(json.isEmpty());
        
        // Verificar que es JSON válido
        assertTrue(generator.isValidJson(json), "Debe generar JSON válido");
        
        // Verificar estructura básica
        assertTrue(json.contains("\"validation_id\""), "Debe contener validation_id");
        assertTrue(json.contains("\"timestamp\""), "Debe contener timestamp");
        assertTrue(json.contains("\"status\""), "Debe contener status");
        assertTrue(json.contains("\"violations\""), "Debe contener violations");
        assertTrue(json.contains("\"module_statuses\""), "Debe contener module_statuses");
    }

    @Test
    void deberiaIncluirViolacionCritica() throws IOException {
        ValidationResult result = createTestResult();
        
        Violation criticalViolation = new Violation(
            "compras",
            ViolationSeverity.CRITICAL,
            DependencyType.STATE_DEPENDENCY,
            "Presupuesto freeze mechanism missing",
            "Implement PresupuestoService.congelar() method",
            true
        );
        result.addViolation(criticalViolation);
        
        String json = generator.generate(result, roadmap);
        
        assertTrue(json.contains("\"severity\" : \"CRITICAL\""), 
            "Debe incluir violación crítica");
        assertTrue(json.contains("\"type\" : \"STATE_DEPENDENCY\""), 
            "Debe incluir tipo de dependencia");
        assertTrue(json.contains("\"blocking\" : true"), 
            "Debe marcar como blocking");
        assertTrue(json.contains("Presupuesto freeze mechanism missing"), 
            "Debe incluir mensaje");
        assertTrue(json.contains("Implement PresupuestoService.congelar()"), 
            "Debe incluir sugerencia");
    }

    @Test
    void deberiaIncluirModuleStatus() throws IOException {
        ValidationResult result = createTestResult();
        
        ModuleStatus proyectoStatus = new ModuleStatus("proyecto", ImplementationStatus.COMPLETE);
        proyectoStatus.getDetectedEntities().add("Proyecto");
        proyectoStatus.getDetectedServices().add("ProyectoService");
        
        ModuleStatus tiempoStatus = new ModuleStatus("tiempo", ImplementationStatus.IN_PROGRESS);
        tiempoStatus.getDetectedEntities().add("ProgramaObra");
        
        result.addModuleStatus(proyectoStatus);
        result.addModuleStatus(tiempoStatus);
        
        String json = generator.generate(result, roadmap);
        
        assertTrue(json.contains("\"module_id\" : \"proyecto\""), 
            "Debe incluir módulo proyecto");
        assertTrue(json.contains("\"implementation_status\" : \"COMPLETE\""), 
            "Debe incluir estado COMPLETE");
        assertTrue(json.contains("\"implementation_status\" : \"IN_PROGRESS\""), 
            "Debe incluir estado IN_PROGRESS");
        assertTrue(json.contains("\"detected_entities\""), 
            "Debe incluir detected_entities");
    }

    @Test
    void deberiaIncluirMetadataCompleta() throws IOException {
        ValidationResult result = createTestResult();
        String json = generator.generate(result, roadmap);
        
        // Verificar metadata
        assertTrue(json.contains("\"repository_path\""), 
            "Debe incluir repository_path");
        assertTrue(json.contains("\"canonical_version\""), 
            "Debe incluir canonical_version");
        assertTrue(json.contains(roadmap.getVersion()), 
            "Debe incluir versión del roadmap");
    }

    @Test
    void deberiaGenerarTimestampISO8601() throws IOException {
        ValidationResult result = createTestResult();
        String json = generator.generate(result, roadmap);
        
        // Verificar que contiene timestamp
        assertTrue(json.contains("\"timestamp\""), 
            "Debe incluir timestamp");
        
        // El timestamp debe estar en formato ISO 8601 (contiene 'T' o 'Z')
        assertTrue(json.contains("T") || json.contains("Z") || json.matches(".*\"timestamp\"\\s*:\\s*\"[0-9]{4}-[0-9]{2}-[0-9]{2}.*"), 
            "Timestamp debe estar en formato ISO 8601");
    }

    @Test
    void deberiaEscribirAArchivo() throws IOException {
        ValidationResult result = createTestResult();
        Path tempFile = Files.createTempFile("validation-report", ".json");
        
        try {
            generator.generateToFile(result, roadmap, tempFile);
            
            assertTrue(Files.exists(tempFile), "Archivo debe existir");
            assertTrue(Files.size(tempFile) > 0, "Archivo no debe estar vacío");
            
            String content = Files.readString(tempFile);
            assertTrue(generator.isValidJson(content), 
                "Contenido del archivo debe ser JSON válido");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void deberiaIncluirTodosLosTiposDeViolacion() throws IOException {
        ValidationResult result = createTestResult();
        
        // Agregar violaciones de todos los tipos
        result.addViolation(new Violation("mod1", ViolationSeverity.CRITICAL, 
            DependencyType.STATE_DEPENDENCY, "State violation", null, true));
        result.addViolation(new Violation("mod2", ViolationSeverity.WARNING, 
            DependencyType.DATA_DEPENDENCY, "Data violation", null, false));
        result.addViolation(new Violation("mod3", ViolationSeverity.INFO, 
            DependencyType.TEMPORAL_DEPENDENCY, "Temporal violation", null, false));
        result.addViolation(new Violation("mod4", ViolationSeverity.WARNING, 
            DependencyType.BUSINESS_LOGIC, "Business logic violation", null, false));
        
        String json = generator.generate(result, roadmap);
        
        assertTrue(json.contains("STATE_DEPENDENCY"), "Debe incluir STATE_DEPENDENCY");
        assertTrue(json.contains("DATA_DEPENDENCY"), "Debe incluir DATA_DEPENDENCY");
        assertTrue(json.contains("TEMPORAL_DEPENDENCY"), "Debe incluir TEMPORAL_DEPENDENCY");
        assertTrue(json.contains("BUSINESS_LOGIC"), "Debe incluir BUSINESS_LOGIC");
    }

    @Test
    void deberiaIncluirContextEnViolaciones() throws IOException {
        ValidationResult result = createTestResult();
        
        Violation violation = new Violation(
            "compras",
            ViolationSeverity.CRITICAL,
            DependencyType.STATE_DEPENDENCY,
            "Test violation",
            "Test suggestion",
            true
        );
        violation.setContext(java.util.Map.of(
            "expected_state", "CONGELADO",
            "actual_state", "BORRADOR",
            "file", "PresupuestoService.java"
        ));
        result.addViolation(violation);
        
        String json = generator.generate(result, roadmap);
        
        assertTrue(json.contains("\"context\""), "Debe incluir context");
        assertTrue(json.contains("expected_state") || json.contains("CONGELADO"), 
            "Debe incluir información del contexto");
    }

    /**
     * Crea un ValidationResult de prueba.
     */
    private ValidationResult createTestResult() {
        ValidationResult result = new ValidationResult(
            "/test/repo/path",
            ValidationStatus.PASSED
        );
        return result;
    }
}
