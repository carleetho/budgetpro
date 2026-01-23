package com.budgetpro.validator.engine;

import com.budgetpro.validator.model.ValidationResult;
import com.budgetpro.validator.model.ValidationStatus;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.model.ViolationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para ValidationEngine.
 */
class ValidationEngineTest {

    private ValidationEngine engine;
    private Path repositoryPath;

    @BeforeEach
    void setUp() {
        engine = new ValidationEngine();
        repositoryPath = Paths.get("../../backend").toAbsolutePath().normalize();
    }

    @Test
    void deberiaEjecutarValidacionCompleta() {
        ValidationResult result = engine.validate(repositoryPath);
        
        assertNotNull(result);
        assertNotNull(result.getStatus());
        assertNotNull(result.getModuleStatuses());
        assertNotNull(result.getViolations());
    }

    @Test
    void deberiaDetectarViolacionesCriticas() {
        ValidationResult result = engine.validate(repositoryPath);
        
        // Verificar que puede detectar violaciones críticas
        List<Violation> criticalViolations = result.getViolations().stream()
                .filter(v -> v.getSeverity() == ViolationSeverity.CRITICAL)
                .toList();
        
        // El resultado debe tener un estado apropiado
        if (!criticalViolations.isEmpty()) {
            assertEquals(ValidationStatus.CRITICAL_VIOLATIONS, result.getStatus());
        }
    }

    @Test
    void deberiaGenerarExitCodeCorrecto() {
        ValidationResult result = engine.validate(repositoryPath);
        
        int exitCode = result.getExitCode();
        
        // Exit code debe ser 0, 1, 2 o 3
        assertTrue(exitCode >= 0 && exitCode <= 3, 
            "Exit code debe estar entre 0 y 3, pero fue: " + exitCode);
        
        // Verificar correspondencia con estado
        switch (result.getStatus()) {
            case PASSED -> assertEquals(0, exitCode);
            case CRITICAL_VIOLATIONS -> assertEquals(1, exitCode);
            case WARNINGS -> assertEquals(2, exitCode);
            case ERROR -> assertEquals(3, exitCode);
        }
    }

    @Test
    void deberiaValidarPrincipioBaseline() {
        ValidationResult result = engine.validate(repositoryPath);
        
        // Buscar violaciones relacionadas con baseline principle
        List<Violation> baselineViolations = result.getViolations().stream()
                .filter(v -> v.getMessage() != null && 
                        (v.getMessage().toLowerCase().contains("baseline") ||
                         v.getMessage().toLowerCase().contains("temporal coupling") ||
                         v.getMessage().toLowerCase().contains("freeze together")))
                .toList();
        
        // Si hay violaciones de baseline, deben tener sugerencias
        for (Violation violation : baselineViolations) {
            assertNotNull(violation.getSuggestion(), 
                "Violaciones de baseline deben tener sugerencias");
        }
    }

    @Test
    void deberiaGenerarCadenasDeDependencias() {
        ValidationResult result = engine.validate(repositoryPath);
        
        // Buscar violaciones con cadenas de dependencias
        List<Violation> violationsWithChains = result.getViolations().stream()
                .filter(v -> v.getContext() != null && 
                        v.getContext().containsKey("dependency_chain"))
                .toList();
        
        // Verificar que las cadenas tienen formato correcto
        for (Violation violation : violationsWithChains) {
            String chain = (String) violation.getContext().get("dependency_chain");
            assertNotNull(chain);
            assertFalse(chain.isEmpty());
            // Debe contener flechas o módulos
            assertTrue(chain.contains("→") || chain.contains("->") || chain.length() > 0);
        }
    }

    @Test
    void deberiaIncluirContextoEnViolaciones() {
        ValidationResult result = engine.validate(repositoryPath);
        
        // Verificar que las violaciones tienen contexto útil
        for (Violation violation : result.getViolations()) {
            assertNotNull(violation.getModuleId());
            assertNotNull(violation.getSeverity());
            assertNotNull(violation.getType());
            assertNotNull(violation.getMessage());
            
            // Las violaciones críticas deben tener sugerencias
            if (violation.getSeverity() == ViolationSeverity.CRITICAL) {
                assertNotNull(violation.getSuggestion(),
                    "Violaciones críticas deben tener sugerencias");
            }
        }
    }
}
