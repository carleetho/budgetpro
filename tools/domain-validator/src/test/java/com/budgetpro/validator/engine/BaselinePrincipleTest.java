package com.budgetpro.validator.engine;

import com.budgetpro.validator.model.ValidationResult;
import com.budgetpro.validator.model.ValidationStatus;
import com.budgetpro.validator.model.ViolationSeverity;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.RoadmapLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests específicos para el principio de baseline (Presupuesto + Tiempo freeze together).
 */
class BaselinePrincipleTest {

    @TempDir
    Path tempDir;
    
    private ValidationEngine engine;
    private CanonicalRoadmap roadmap;

    @BeforeEach
    void setUp() throws RoadmapLoader.RoadmapLoadException {
        engine = new ValidationEngine();
        RoadmapLoader loader = new RoadmapLoader();
        roadmap = loader.load();
    }

    @Test
    void deberiaDetectarViolacionSiPresupuestoFreezeSinTiempoCoupling() throws Exception {
        // Crear código con Presupuesto que tiene freeze pero sin acoplamiento con Tiempo
        Path domainDir = tempDir.resolve("src/main/java/com/budgetpro/domain");
        Files.createDirectories(domainDir);
        
        // Crear Presupuesto con método congelar
        Path presupuestoDir = domainDir.resolve("finanzas/presupuesto/model");
        Files.createDirectories(presupuestoDir);
        Files.writeString(presupuestoDir.resolve("Presupuesto.java"),
            "package com.budgetpro.domain.finanzas.presupuesto.model;\n" +
            "public final class Presupuesto {\n" +
            "  public void congelar() { }\n" +
            "}\n");
        
        Files.writeString(presupuestoDir.resolve("EstadoPresupuesto.java"),
            "package com.budgetpro.domain.finanzas.presupuesto.model;\n" +
            "public enum EstadoPresupuesto {\n" +
            "  BORRADOR, CONGELADO, INVALIDADO\n" +
            "}\n");
        
        // NO crear Tiempo con freeze acoplado
        
        ValidationResult result = engine.validate(tempDir);
        
        // Debe detectar violación de acoplamiento temporal
        boolean hasTemporalViolation = result.getViolations().stream()
                .anyMatch(v -> v.getSeverity() == ViolationSeverity.CRITICAL &&
                              (v.getType().toString().contains("TEMPORAL") ||
                               v.getMessage().toLowerCase().contains("temporal") ||
                               v.getMessage().toLowerCase().contains("baseline") ||
                               v.getMessage().toLowerCase().contains("freeze together")));
        
        assertTrue(hasTemporalViolation || result.getStatus() == ValidationStatus.CRITICAL_VIOLATIONS,
            "Debe detectar violación de acoplamiento temporal del principio de baseline");
    }

    @Test
    void deberiaValidarQueRoadmapTieneBaselinePrinciple() throws RoadmapLoader.RoadmapLoadException {
        // Verificar que el roadmap tiene el principio de baseline codificado
        assertTrue(roadmap.hasBaselinePrincipleEncoded(),
            "El roadmap debe tener el principio de baseline codificado");
        
        // Verificar que Presupuesto y Tiempo tienen acoplamiento temporal
        var presupuestoModule = roadmap.getModuleById("presupuesto");
        var tiempoModule = roadmap.getModuleById("tiempo");
        
        assertTrue(presupuestoModule.isPresent(), "Módulo Presupuesto debe existir");
        assertTrue(tiempoModule.isPresent(), "Módulo Tiempo debe existir");
        
        boolean presupuestoHasCoupling = presupuestoModule.get()
                .hasTemporalCouplingWith("tiempo");
        boolean tiempoHasCoupling = tiempoModule.get()
                .hasTemporalCouplingWith("presupuesto");
        
        assertTrue(presupuestoHasCoupling || tiempoHasCoupling,
            "Presupuesto o Tiempo deben tener constraint de acoplamiento temporal");
    }

    @Test
    void deberiaBloquearDesarrolloSiBaselineNoEstaCompleto() throws Exception {
        // Crear código que intenta desarrollar módulo de ejecución sin baseline completo
        Path domainDir = tempDir.resolve("src/main/java/com/budgetpro/domain");
        Files.createDirectories(domainDir);
        
        // Crear módulo Compras (ejecución) sin baseline completo
        Path comprasDir = domainDir.resolve("logistica/compra/model");
        Files.createDirectories(comprasDir);
        Files.writeString(comprasDir.resolve("Compra.java"),
            "package com.budgetpro.domain.logistica.compra.model;\n" +
            "public final class Compra { }\n");
        
        ValidationResult result = engine.validate(tempDir);
        
        // Debe detectar que falta baseline (Presupuesto congelado)
        boolean hasBaselineViolation = result.getViolations().stream()
                .anyMatch(v -> v.getSeverity() == ViolationSeverity.CRITICAL &&
                              (v.getModuleId().equals("compras") ||
                               v.getMessage().toLowerCase().contains("baseline") ||
                               v.getMessage().toLowerCase().contains("presupuesto") ||
                               v.getMessage().toLowerCase().contains("congelado")));
        
        assertTrue(hasBaselineViolation || result.getStatus() == ValidationStatus.CRITICAL_VIOLATIONS,
            "Debe bloquear desarrollo de módulos de ejecución sin baseline completo");
    }
}
